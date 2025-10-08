import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import webSocketManager from "./components/WebSocketManager.js";
import ConnectionsSelection from './components/ConnectionsSelection.js';
import Header from './components/Header.js';
import ConnectionGame from './components/ConnectionGame.js';

import './css/index.css';

// Make WebSocket URL configurable via environment variables
const getWebSocketUrl = () => {
  // Check for environment variable (REACT_APP_BACKEND_HOST)
  if (process.env.REACT_APP_BACKEND_HOST) {
    const host = process.env.REACT_APP_BACKEND_HOST;
    const port = process.env.REACT_APP_BACKEND_PORT || '8080';
    const protocol = process.env.REACT_APP_BACKEND_SECURE === 'true' ? 'wss' : 'ws';
    return `${protocol}://${host}:${port}/ws`;
  }

  // For production on Vercel, use the WSL server IP
  if (process.env.NODE_ENV === 'production') {
    const host = '192.168.4.25';
    const port = '8080';  // Direct backend port, or use '80' if going through Caddy
    return `ws://${host}:${port}/ws`;
  }

  // Default fallback for local development
  return 'ws://localhost:8080/ws';
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
