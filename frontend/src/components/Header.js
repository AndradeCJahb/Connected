import '../css/Header.css';
import Footer from './Footer.js';
function Header() {
	return (
		<header className="header">
			<div className="header-content">
				<a className="app-title" href="/">Connected</a>
				<p className="app-description">Collaborative NYT Connections</p>
				<Footer />
			</div>
		</header>
	);
}

export default Header;
