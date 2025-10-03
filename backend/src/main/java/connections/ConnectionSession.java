package connections;

import java.sql.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
    
public class ConnectionSession {
    private static final String DB_URL = "jdbc:sqlite:../db/connections.db";
    private static final String[] monthNames = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    private final int connectionId;
    private String date;
    private List<String> words = new ArrayList<>();
    private Category[] categories = null;
    private int correctCategories = 0;

    private Set<String> selectedWords = new HashSet<>();

    private Set<Player> playerList = new HashSet<>();
    private Set<Player> requestCheckWordSelectionPlayers = new HashSet<>();
    private List<String> correctWords = new ArrayList<>();
    
    public ConnectionSession(int connectionId) {
        this.connectionId = connectionId;

        String query = "SELECT date, words, categories FROM connections_games WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, this.connectionId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String wordsJsonString = resultSet.getString("words");
                JSONArray wordsArray = new JSONArray(wordsJsonString);
                this.words = new ArrayList<>();

                for (int i = 0; i < wordsArray.length(); i++) {
                    this.words.add(wordsArray.getString(i));
                }

                Collections.shuffle(this.words, new Random(System.currentTimeMillis()));

                this.date = resultSet.getString("date");

                String categoriesJsonString = resultSet.getString("categories");

                JSONArray categoriesArray = new JSONArray(categoriesJsonString);
                this.categories = new Category[4];
                
                for (int i = 0; i < categories.length; i++) {
                    JSONObject categoryJson = categoriesArray.getJSONObject(i);
                    String description = categoryJson.getString("description");
                    int difficulty = categoryJson.getInt("difficulty") - 1; 
                    
                    JSONArray categoryWordsArray = categoryJson.getJSONArray("words");
                    String[] categoryWords = new String[categoryWordsArray.length()];
                    for (int j = 0; j < categoryWordsArray.length(); j++) {
                        categoryWords[j] = categoryWordsArray.getString(j);
                    }
                    
                    this.categories[i] = new Category(description, difficulty, categoryWords);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getVoteCount() {
        return requestCheckWordSelectionPlayers.size();
    }

    public boolean checkSelection() {
        if (this.selectedWords.size() != 4) {
            return false;
        }

        for (Category category : this.categories) {
            if (category.check(this.selectedWords)) {
                reorganizeWords();
                correctCategories++;
                this.correctWords.addAll(this.selectedWords);
                return true;
            }
        }

        clearSelectedWords();
        clearRequestCheckWordSelectionPlayers();
        return false;
    }

    public boolean areAllCategoriesCorrect() {
        return this.correctCategories ==4;
    }

    private void reorganizeWords() {
        for(String word : this.selectedWords) {
            this.words.remove(word);
            this.words.add(correctCategories * 4, word);
        }
    }

    public void toggleWord(String word) {
        if(this.selectedWords.size() > 3 && !this.selectedWords.contains(word)) {
            return;
        }

        if (this.selectedWords.contains(word)) {
            this.selectedWords.remove(word);
        } else {
            this.selectedWords.add(word);
        }
    }

    public boolean sufficientRequestCheckWordSelection (Player player) {
        if(requestCheckWordSelectionPlayers.contains(player)) {
            requestCheckWordSelectionPlayers.remove(player);
        } else  {
            requestCheckWordSelectionPlayers.add(player);
            System.out.println(Arrays.toString(requestCheckWordSelectionPlayers.toArray()));
            return requestCheckWordSelectionPlayers.size() > playerList.size() / 2;
        }
        System.out.println(Arrays.toString(requestCheckWordSelectionPlayers.toArray()));
        return false;
    }

    public Set<String> getSelectedWords() {
        return this.selectedWords;
    }

    public void clearSelectedWords() {
        this.selectedWords.clear();
    }

    public void clearRequestCheckWordSelectionPlayers() {
        this.requestCheckWordSelectionPlayers.clear();
    }

    public void resetSession() {
        this.selectedWords.clear();
        this.correctCategories = 0;
        Collections.shuffle(this.words, new Random(System.currentTimeMillis()));
    }

    public void removePlayer(Player player) {
        this.playerList.remove(player);
    }

    public void addPlayer(Player player) {
        this.playerList.add(player);
    }

    public Set<Player> getPlayerList() {
        return this.playerList;
    }

    public List<String> getWords() {
        return this.words;
    }

    public String getDateString() {
        if (this.date == null) {
            return null;
        }

        String[] dateParts = this.date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        return monthNames[month - 1] + " " + day + ", " + year;
    }

    public List<String> getCorrectWords() {
        return this.correctWords;
    }
}
