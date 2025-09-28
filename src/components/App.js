import { useState } from 'react';

import Header from './Header.js';
import '../css/App.css';
import Footer from './Footer.js';

function App() {
  const [activeSection, setActiveSection] = useState('about');

  const renderContent = () => {
    switch (activeSection) {

    }
  };

  return (
    <div className="App">
      <Header />
    </div>
  );
}

export default App;
