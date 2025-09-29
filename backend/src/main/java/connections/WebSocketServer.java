package connections;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
                case "fetchConnections":
                    handleFetchConnections(session);
                    break;
                case "fetchIdentity":
                    handleIdentity(session, jsonMessage);
                    break;
                case "fetchPuzzle":
                    //handleFetchPuzzle(jsonMessage);
                    break;
                case "sendPlayerPosition":
                    //handleSendPlayerPosition(session, jsonMessage);
                    break;
                case "sendLeaveRoom":
                    //handleSendLeaveRoom(jsonMessage);
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
        System.out.println("Connection closed: " + session.getId());
        for(Player currPlayer : players.values()) {
            if (currPlayer.getSession().equals(session)) {
                Integer puzzleId = currPlayer.getCurrentConnectionsId();
                currPlayer.setCurrentConnectionsId(null);

                if (puzzleId != null) {
                    //broadcastPlayerPosition(puzzleId);
                    //broadcastPlayers(puzzleId);
                }

                break;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error on session " + session.getId() + ": " + throwable.getMessage());
    }

    private void handleFetchConnections(Session session) {
        String query = "SELECT id, date, words, categories, status FROM connections_games ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery()) {

            JSONArray connections = new JSONArray();

            while (resultSet.next()) {
                JSONObject connection = new JSONObject();
                connection.put("id", resultSet.getInt("id"));
                connection.put("date", resultSet.getString("date"));
                connection.put("words", resultSet.getString("words"));
                connection.put("categories", resultSet.getString("categories"));

                connections.put(connection);
            }

            JSONObject response = new JSONObject();
            response.put("type", "connections");
            response.put("connections", connections);
            session.getBasicRemote().sendText(response.toString());
        } catch (SQLException e) {
            System.err.println("Error fetching puzzles: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error sending puzzles response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleIdentity (Session session, JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));

        if (!players.containsKey(clientId)) {
            players.put(clientId, new Player(session));
        } else {
            players.get(clientId).setSession(session);
        }

        Player clientPlayer = players.get(clientId);
        String clientName = clientPlayer.getName();
        String clientColor = clientPlayer.getColor();

        JSONObject clientInfo = new JSONObject();
        clientInfo.put("name", clientName);
        clientInfo.put("color", clientColor);
    
        JSONObject response = new JSONObject();
        response.put("type", "updateIdentity");
        response.put("client", clientInfo);

        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException e) {
            System.err.println("Error sending identity response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // private void handleFetchPuzzle(JSONObject jsonMessage) {
    //     UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
    //     int puzzleId = jsonMessage.getInt("puzzleId");

    //     players.get(clientId).setCurrentConnectionsId(puzzleId);
        
    //     if (!boards.containsKey(puzzleId)) {
    //         boards.put(puzzleId, new Board(puzzleId));
    //     }

    //     broadcastPlayerPosition(puzzleId);
    //     broadcastPlayers(puzzleId);
    // }

    // private void broadcastPlayers(int puzzleId) {
    //     List<Player> playersInPuzzle = getPlayersInPuzzle(puzzleId);
    //     JSONArray playersArray = new JSONArray();
    //     for (Player player : playersInPuzzle) {
    //         JSONObject playerJson = new JSONObject();
    //         playerJson.put("name", player.getName());
    //         playerJson.put("color", player.getColor());
    //         playersArray.put(playerJson);
    //     }

    //     JSONObject response = new JSONObject();
    //     response.put("type", "updatePlayers");
    //     response.put("players", playersArray);

    //     for (Player currentPlayer : players.values()) {
    //         Session currentSession = currentPlayer.getSession();

    //         if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
    //             try {
    //                 currentSession.getBasicRemote().sendText(response.toString());
    //             } catch (IOException e) {
    //                 System.err.println("Error broadcasting players: " + e.getMessage());
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    // }

    // private List<Player> getPlayersInPuzzle(int puzzleId) {
    //     List<Player> playersInPuzzle = new ArrayList<>();
    //     for(Player currentPlayer : players.values()) {
    //         if (currentPlayer.getCurrentPuzzleId() == puzzleId) {
    //             playersInPuzzle.add(currentPlayer);
    //         }
    //     }
    //     return playersInPuzzle;
    // }

    // private void handleSendPlayerPosition(Session session, JSONObject jsonMessage) {
    //     UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
    //     Player player = players.get(clientId);

    //     if (player == null) {
    //         handleIdentity(session, jsonMessage);
    //         player = players.get(clientId);
    //     }
        
    //     JSONObject position = jsonMessage.getJSONObject("position");
    //     int row = position.getInt("row");
    //     int col = position.getInt("col");
        
    //     player.setSelectedRow(row);
    //     player.setSelectedCol(col);

    //     int puzzleId = player.getCurrentPuzzleId();
    //     broadcastPlayerPosition(puzzleId);
    // }

    // private void broadcastPlayerPosition (int puzzleId) {
    //     List<Player> playersInPuzzle = getPlayersInPuzzle(puzzleId);
    //     JSONArray playerPositions = new JSONArray();

    //     for (Player currPlayer : playersInPuzzle) {
    //         JSONObject playerJson = new JSONObject();
    //         playerJson.put("name", currPlayer.getName());
    //         playerJson.put("color", currPlayer.getColor());
    //         playerJson.put("position", new JSONObject()
    //             .put("row", currPlayer.getSelectedRow())
    //             .put("col", currPlayer.getSelectedCol())
    //         );
    //         playerPositions.put(playerJson);
    //     }

    //     JSONObject response = new JSONObject();
    //     response.put("type", "updatePlayerPositions");
    //     response.put("positions", playerPositions);

    //     for (Player currentPlayer : players.values()) {
    //         Session currentSession = currentPlayer.getSession();

    //         if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
    //             try {
    //                 currentSession.getBasicRemote().sendText(response.toString());
    //             } catch (IOException e) {
    //                 System.err.println("Error broadcasting players: " + e.getMessage());
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
        
    // }

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

    // private void handleSendLeaveRoom(JSONObject jsonMessage) {
    //     Player player = players.get(UUID.fromString(jsonMessage.getString("clientId")));
    //     if (player == null) {
    //         return;
    //     }
    //     int puzzleId = jsonMessage.getInt("puzzleId");

    //     player.setCurrentConnectionsId(null);

    //     if (puzzleId != -1) {
    //         broadcastPlayerPosition(puzzleId);
    //         broadcastPlayers(puzzleId);
    //     }
    // }
}
