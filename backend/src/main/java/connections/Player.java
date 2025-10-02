package connections;

import jakarta.websocket.Session;

import java.util.Random;

public class Player {
    private Session session;
    private Integer currentConnectionsId;

    public Player(Session session) {
        this.session = session;
        this.currentConnectionsId = null;
    }

    public Session getSession() {
        return session;
    }

    public Integer getCurrentConnectionsId() {
        return currentConnectionsId;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setCurrentConnectionsId(Integer currentConnectionsId) {
        this.currentConnectionsId = currentConnectionsId;
    }
}