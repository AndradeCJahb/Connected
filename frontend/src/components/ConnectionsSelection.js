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
            if (data.type === "updateAllConnections") {
                setConnections(data.connections);
                setLoadedConnections(true);
                console.log("Connections data received:", data.connections);
            }
        };

        webSocketManager.addListener(handleMessage);
        webSocketManager.send({ type: "fetchAllConnections" });

        return () => {
            webSocketManager.removeListener(handleMessage);
        };
    }, []);
    
    const handleConnectionSelect = (connectionsId) => {
        navigate(`/connections/${connectionsId}`);
    };

    const statuses = ({
        0: "New",
        1: "In Progress",
        2: "Completed"
    });

    return (
        <div>
            <main className="main-content">
                <div className="content-container">
                    <section className="content-section">
                        {!loadedConnections ? (
                            <div className="loading-container">
                                <div className="loading-spinner"></div>
                                <p className="loading-message">Loading Puzzles...</p>
                            </div>
                        ) : (
                            <div className="connections-grid">
                                {connections.map((connection) => (
                                    <div
                                        key={connection.id}
                                        className="connections-card"
                                        onClick={() => handleConnectionSelect(connection.id)}
                                    >

                                        <h3>{connection.date}</h3>
                                        <div className="connections-meta">
                                            <span className="status">
                                                {statuses[connection.status]}
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
