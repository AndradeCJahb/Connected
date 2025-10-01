import React, { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";

import webSocketManager from "./WebSocketManager.js";
import SolvedConnectionPopup from "./SolvedConnectionPopup.js";

import "../css/index.css";
import "../css/ConnectionGame.css";

let clientId = localStorage.getItem("clientId");
if (!clientId) {
    clientId = generateUUID();
    localStorage.setItem("clientId", clientId);
}

function generateUUID() {
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(
        /[xy]/g,
        function (c) {
            var r = (Math.random() * 16) | 0,
                v = c === "x" ? r : (r & 0x3) | 0x8;
            return v.toString(16);
        },
    );
}

function ConnectionGame() {
    const navigate = useNavigate();
    const { connectionsId: urlConnectionsId } = useParams();
    const [connectionId] = useState(parseInt(urlConnectionsId) || null);
    const [words, setWords] = useState([]);
    const [date, setDate] = useState("");
    const [selectedWords, setSelectedWords] = useState([]);
    const [correctWords, setCorrectWords] = useState([]);
    const [allWordsCorrect, setAllWordsCorrect] = useState(false);

    const handleWordSelection = (word) => {
        webSocketManager.send({
            type: "sendWordToggleSelection",
            clientId: clientId,
            connectionId: connectionId,
            word: word,
        });
    };

    const handleClearWordSelection = () => {
        webSocketManager.send({
            type: "sendClearWordSelection",
            connectionId: connectionId
        });
    };

    const handleSubmitWordSelection = () => {
        webSocketManager.send({
            type: "sendSubmitWordSelection",
            connectionId: connectionId,
            selectedWords: selectedWords
        });
        handleClearWordSelection();
    };

    console.log(connectionId);
    
    useEffect(() => {
        webSocketManager.send({ type: "fetchIdentity", clientId });
        webSocketManager.send({ type: "fetchConnectionSession", clientId, connectionId });
    }, [connectionId, navigate]);

    useEffect(() => {
        const handleMessage = (data) => {
            console.log("Received message:", data);
            if (data.type === "getWords") {
                setWords(data.words);
                setDate(data.date);
            } else if (data.type === "updatePlayers") {
                setPlayers(data.players);
            } else if (data.type === "updateSelectedWords") {
                setSelectedWords(data.selectedWords);
            } else if (data.type === "updateWordSelectionResult") {
                setCorrectWords((prevCorrectWords) => [...prevCorrectWords, ...data.correctWords]);
                setAllWordsCorrect(data.allWordsCorrect);
                webSocketManager.send({ type: "fetchConnectionSession", clientId, connectionId });
            }
    };

    webSocketManager.addListener(handleMessage);

    return () => {
        webSocketManager.removeListener(handleMessage, connectionId);
        webSocketManager.send({
            type: "sendLeaveRoom",
            clientId: clientId,
            connectionId: connectionId,
        });
    };

    }, [connectionId, navigate]);

    return (
        <main className="main-content">
            <div className="content-container">
                <h1 className="connection-title">Connected <span className="title-date">{date}</span></h1>
                <SolvedConnectionPopup
                    visible={allWordsCorrect}
                    onClose={() => setAllWordsCorrect(false)}
                />
                <div className="words-grid">
                    {words.map((word, index) => (
                        <div 
                            key={index} 
                            onClick={() => handleWordSelection(word)} 
                            className={`word-card ${correctWords.includes(word) ? 'correct' : selectedWords.includes(word) ? 'selected' : ''}`}
                        >
                            {word}
                        </div>
                    ))}
                </div>
                <div className="button-container">
                    <div className="clear-selection-button" onClick={handleClearWordSelection}>Clear Selection</div>
                    <div className="submit-selection-button" onClick={handleSubmitWordSelection}>Submit Selection</div>
                </div>
            </div>
        </main>
    );
}

export default ConnectionGame;