import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import webSocketManager from "./components/WebSocketManager.js";
import ConnectionsSelection from './components/ConnectionsSelection.js';
import Header from './components/Header.js';
import ConnectionGame from './components/ConnectionGame.js';

import './css/index.css';

// WebSocket URL for cloudflared tunnel
const getWebSocketUrl = () => {
  const host = 'metro-laser-harbor-dir.trycloudflare.com';
  return `wss://${host}/ws`;
};

const wsUrl = getWebSocketUrl();
console.log(`Connecting to WebSocket server at ${wsUrl}`);
webSocketManager.connect(wsUrl);

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  	<React.StrictMode>
    	<div className="App">
            <Header />
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<ConnectionsSelection />} />
                    <Route path="/connections/:connectionsId" element={<ConnectionGame />} />
                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            </BrowserRouter>
        </div>
  	</React.StrictMode>
);
