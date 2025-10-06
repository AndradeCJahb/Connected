package connections;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class WebSocketServer {
    private static final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private static final Map<Integer, ConnectionSession> connectionSessions = new ConcurrentHashMap<>();
    private static final String DB_URL = "jdbc:sqlite:../db/connections.db";

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connection opened: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println(message);
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String requestType = jsonMessage.getString("type");

            switch (requestType) {
                case "fetchAllConnections":
                    handleFetchAllConnections(session);
                    break;
                case "fetchIdentity":
                    handleIdentity(session, jsonMessage);
                    break;
                case "fetchConnectionSession":
                    handleFetchConnectionSession(session, jsonMessage);
                    break;
                case "fetchReorganizedWords":
                    handleFetchReorganizedWords(session, jsonMessage);
                    break;
                case "fetchCorrectWords":
                    handleFetchCorrectWords(session, jsonMessage);
                    break;
                case "sendLeaveRoom":
                    handleSendLeaveRoom(jsonMessage);
                    break;
                case "sendWordToggleSelection":
                    handleSendWordToggleSelection(jsonMessage);
                    break;
                case "sendClearWordSelection":
                    handleSendClearWordSelection(jsonMessage);
                    break;
                case "sendResetConnectionGame":
                    handleSendResetConnectionGame(session, jsonMessage);
                    break;
                case "sendSubmitSelectionRequest":
                    handleSendSubmitWordSelectionRequest(jsonMessage);
                    break;
                case "fetchSelectedWords":
                    handleFetchSelectedWords(session, jsonMessage);
                    break;
                default:
                    System.out.println("Unknown request type: " + requestType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        System.out.println("Connection closed: " + sessionId);

        for (Player currPlayer : players.values()) {
            if (currPlayer.getSession() == null) {
                continue;
            }

            if (currPlayer.getSession().getId().equals(sessionId)) {
                connectionSessions.get(currPlayer.getCurrentConnectionsId()).removePlayer(currPlayer);
                updatePlayerCount(currPlayer.getCurrentConnectionsId());

                currPlayer.setCurrentConnectionsId(null);
                currPlayer.setSession(null);
                break;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error on session " + session.getId() + ": " + throwable.getMessage());
    }

    private void updatePlayerCount(int connectionId) {
        int playersInConnectionSession = connectionSessions.get(connectionId).getPlayerList().size();

        JSONObject response = new JSONObject();
        response.put("type", "updatePlayerCount");
        response.put("numberPlayersConnectionSession", playersInConnectionSession);

        broadCastConnectionSessionJson(connectionId, response);
    }

    private void handleFetchAllConnections(Session session) {
        String query = "SELECT id, date, status FROM connections_games ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            JSONArray allConnections = new JSONArray();

            while (resultSet.next()) {
                JSONObject connection = new JSONObject();
                connection.put("id", resultSet.getInt("id"));
                connection.put("date", resultSet.getString("date"));
                connection.put("status", resultSet.getInt("status"));

                allConnections.put(connection);
            }

            JSONObject response = new JSONObject();
            response.put("type", "updateAllConnections");
            response.put("connections", allConnections);
            session.getBasicRemote().sendText(response.toString());
        } catch (SQLException e) {
            System.err.println("Error fetching puzzles: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error sending puzzles response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleIdentity(Session session, JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        players.putIfAbsent(clientId, new Player(session));
    }

    private void handleFetchConnectionSession(Session session, JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        int connectionId = jsonMessage.getInt("connectionId");

        Player currPlayer = players.get(clientId);
        currPlayer.setCurrentConnectionsId(connectionId);

        connectionSessions.putIfAbsent(connectionId, new ConnectionSession(connectionId));
        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);

        currConnectionSession.addPlayer(currPlayer);
        currPlayer.setSession(session);

        updatePlayerCount(currPlayer.getCurrentConnectionsId());
        updateCorrectWords(connectionId, currConnectionSession.getCorrectWords());

        JSONObject response = new JSONObject();
        response.put("type", "updateWords");
        response.put("words", currConnectionSession.getWords());
        response.put("date", currConnectionSession.getDateString());

        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException e) {
            System.err.println("Error sending identity response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSendWordToggleSelection(JSONObject jsonMessage) {
        int connectionId = jsonMessage.getInt("connectionId");
        String word = jsonMessage.getString("word");

        connectionSessions.putIfAbsent(connectionId, new ConnectionSession(connectionId));
        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);

        currConnectionSession.toggleWord(word);
        updateSelectedWords(connectionId);
    }

    private void updateSelectedWords(int connectionId) {
        Set<String> selectedWords = connectionSessions.get(connectionId).getSelectedWords();

        JSONObject response = new JSONObject();
        response.put("type", "updateSelectedWords");
        response.put("selectedWords", selectedWords);

        broadCastConnectionSessionJson(connectionId, response);
    }

    private void broadCastConnectionSessionJson(int connectionId, JSONObject response) {
        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);
        for (Player currentPlayer : currConnectionSession.getPlayerList()) {
            Session currentSession = currentPlayer.getSession();
            if (currentSession == null) {
                continue;
            }
            try {
                currentSession.getBasicRemote().sendText(response.toString());
            } catch (IOException e) {
                System.err.println("Error broadcasting players: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleSendLeaveRoom(JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        int connectionId = jsonMessage.getInt("connectionId");

        Player player = players.get(clientId);

        connectionSessions.get(connectionId).removePlayer(player);
        connectionSessions.get(connectionId).clearRequestCheckWordSelectionPlayers();

        player.setCurrentConnectionsId(null);
        player.setSession(null);

        updatePlayerCount(connectionId);
        updateVoteCount(connectionId);
    }

    private void handleSendClearWordSelection(JSONObject jsonMessage) {
        int connectionId = jsonMessage.getInt("connectionId");

        connectionSessions.putIfAbsent(connectionId, new ConnectionSession(connectionId));

        connectionSessions.get(connectionId).clearSelectedWords();
        updateSelectedWords(connectionId);
    }

    private void handleSendResetConnectionGame(Session session, JSONObject jsonMessage) {
        int connectionId = jsonMessage.getInt("connectionId");
        connectionSessions.get(connectionId).resetSession();

        updateSelectedWords(connectionId);
        updateCorrectWords(connectionId, new ArrayList<>());

        JSONObject response = new JSONObject();
        response.put("type", "updateClearCorrectWords");

        broadCastConnectionSessionJson(connectionId, response);
    }

    private void updateCorrectWords(int connectionId, List<String> correctWords) {
        JSONObject response = new JSONObject();
        response.put("type", "updateWordSelectionResult");
        response.put("correctWords", correctWords);
        response.put("allWordsCorrect", connectionSessions.get(connectionId).areAllCategoriesCorrect());

        broadCastConnectionSessionJson(connectionId, response);
    }

    private void handleFetchReorganizedWords(Session session, JSONObject jsonMessage) {
        int connectionId = jsonMessage.getInt("connectionId");

        connectionSessions.putIfAbsent(connectionId, new ConnectionSession(connectionId));
        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);

        JSONObject response = new JSONObject();
        response.put("type", "updateWords");
        response.put("words", currConnectionSession.getWords());
        response.put("date", currConnectionSession.getDateString());

        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException e) {
            System.err.println("Error sending identity response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSendSubmitWordSelectionRequest(JSONObject jsonMessage) {
        int connectionId = jsonMessage.getInt("connectionId");
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));

        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);
        if (currConnectionSession.sufficientRequestCheckWordSelection(players.get(clientId))) {
            if (currConnectionSession.checkSelection()) {
                List<String> correctWords = connectionSessions.get(connectionId).getCorrectWords();
                updateCorrectWords(connectionId, correctWords);
            }
            currConnectionSession.clearSelectedWords();
            currConnectionSession.clearRequestCheckWordSelectionPlayers();
        }

        updateSelectedWords(connectionId);
        updateVoteCount(connectionId);
    }

    private void updateVoteCount(int connectionId) {
        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);

        JSONObject response = new JSONObject();
        response.put("type", "updateVoteCount");
        response.put("voteCount", currConnectionSession.getVoteCount());

        broadCastConnectionSessionJson(connectionId, response);
    }

    private void handleFetchCorrectWords(Session session, JSONObject jsonMessage) {
        int connectionId = jsonMessage.getInt("connectionId");

        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);

        JSONObject response = new JSONObject();
        response.put("type", "updateCorrectWords");
        response.put("correctWords", currConnectionSession.getCorrectWords());

        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException e) {
            System.err.println("Error sending correct words response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleFetchSelectedWords(Session session, JSONObject jsonMessage) {
        int connectionId = jsonMessage.getInt("connectionId");

        ConnectionSession currConnectionSession = connectionSessions.get(connectionId);

        JSONObject response = new JSONObject();
        response.put("type", "updateSelectedWords");
        response.put("selectedWords", currConnectionSession.getSelectedWords());

        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException e) {
            System.err.println("Error sending selected words response: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
    // private void broadcastSolvedBoard(int puzzleId) {
    //     JSONObject response = new JSONObject();
    //     response.put("type", "updatePuzzleSolved");
    //     for (Player currentPlayer : players.values()) {
    //         Session currentSession = currentPlayer.getSession();
    //         if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
    //             try {
    //                 currentSession.getBasicRemote().sendText(response.toString());
    //             } catch (IOException e) {
    //                 System.err.println("Error sending updatePuzzleSolved" + e.getMessage());
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    // }



