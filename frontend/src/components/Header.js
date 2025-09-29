import '../css/Header.css';
import Footer from './Footer.js';
function Header() {
	return (
		<header className="header">
			<div className="header-content">
				<h1 className="app-title">Connected</h1>
				<p className="app-description">Collaborative NYT Connections</p>
				<Footer />
			</div>
		</header>
	);
}

export default Header;
