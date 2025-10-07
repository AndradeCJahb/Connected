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
    const [playerCount, setPlayerCount] = useState(0);
    const [voteCount, setVoteCount] = useState(0);

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
            type: "sendSubmitSelectionRequest",
            connectionId: connectionId,
            clientId: clientId
        });
    };

    const handleResetConnection = () => {
        webSocketManager.send({
            type: "sendResetConnectionGame",
            connectionId: connectionId,
            clientId: clientId
        });
    };
    
    useEffect(() => {
        webSocketManager.send({ type: "fetchIdentity", clientId });
        webSocketManager.send({ type: "fetchBaseConnectionSession", clientId, connectionId });
        webSocketManager.send({ type: "fetchCorrectWords", connectionId });
        webSocketManager.send({ type: "fetchSelectedWords", connectionId });
        webSocketManager.send({ type: "fetchVoteCount", connectionId });
    }, [connectionId, navigate]);

    useEffect(() => {
        const handleMessage = (data) => {
            console.log("Received message:", data);
            if (data.type === "updateBaseConnectionSession") {
                setWords(data.words);
                setDate(data.date);
            } else if(data.type ==="updateVoteCount") {
                setVoteCount(data.voteCount);
            } else if (data.type === "updateCorrectWords") {
                setCorrectWords(data.correctWords);
            } else if (data.type === "updateSelectedWords") {
                setSelectedWords(data.selectedWords);
            } else if (data.type === "updateResetConnectionGame") {
                setCorrectWords([]);
                setAllWordsCorrect(false);
                setSelectedWords([]);
            } else if (data.type === "updatePlayerCount") {
                setPlayerCount(data.numberPlayersConnectionSession);
            } else if(data.type === "updateClearCorrectWords") {
                setCorrectWords([]); 
                setAllWordsCorrect(false);
            } else if (data.type === "updateReorganizedWords") {
                setWords(data.words);
                setCorrectWords(data.correctWords);
                setAllWordsCorrect(data.allWordsCorrect);
            }
        };

    webSocketManager.addListener(handleMessage);

    return () => {
        webSocketManager.removeListener(handleMessage);
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

                {allWordsCorrect && (
                    <div className="reset-button-container">
                        <div className="reset-connection-button" onClick={handleResetConnection}>Play Again</div>
                    </div>
                )}

                {!allWordsCorrect && (
                    <div className="button-container">
                        <div className="clear-selection-button" onClick={handleClearWordSelection}>Clear Selection</div>
                        <div className="submit-selection-button" onClick={handleSubmitWordSelection}>Submit Selection ({voteCount}/{playerCount})</div>
                    </div>
                )}

                <div className="player-count-container">
                    <div>
                        <div className="head"></div>
                        <div className="body"></div>
                    </div>
                    <div className="player-count-number">{playerCount}</div>
                </div>
            </div>
        </main>
    );
}

export default ConnectionGame;