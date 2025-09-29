import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import webSocketManager from "./WebSocketManager.js";
import '../css/ConnectionsSelection.css';
 //process.env.REACT_APP_WEBSOCKET_URL

const ConnectionsSelection = () => {

    const [connections, setConnections] = useState([]);
    const navigate = useNavigate();

    const [loadedConnections, setLoadedConnections] = useState(false);

    useEffect(() => {
        const handleMessage = (data) => {
            if (data.type === "connections") {
                setConnections(data.connections);
                setLoadedConnections(true);
            }
        };

        webSocketManager.addListener(handleMessage);
        webSocketManager.send({ type: "fetchConnections" });

        return () => {
            webSocketManager.removeListener(handleMessage);
        };
    }, []);
    
    const handlePuzzleSelect = (connectionsId) => {
        navigate(`/connections/${connectionsId}`);
    };

    return (
        <div>
            <main className="main-content">
                <div className="content-container">
                    <section className="content-section">
                        {!loadedConnections ? (
                            <div className="loading-container">
                                <div className="loading-spinner"></div>
                                <p className="loading-message">Spinning Up Database...</p>
                            </div>
                        ) : (
                            <div className="puzzle-grid">
                                {connections.map((connection) => (
                                    <div
                                        key={connections.id}
                                        className="connections-card"
                                        onClick={() => handlePuzzleSelect(connections.id)}
                                    >
                                        <h3>{puzzle.title}</h3>
                                        <div className="puzzle-meta">
                                            <span className="status">
                                                {puzzle.status || "New"}
                                            </span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </section>
                </div>
            </main>
        </div>
    );
}

export default ConnectionsSelection;
