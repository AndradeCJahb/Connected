import '../css/Header.css';
import Footer from './Footer.js';
function Header({ activeSection, setActiveSection }) {
	return (
		<header className="header">
			<div className="header-content">
				<h1 className="header-title">Connected</h1>
				<p className="job-title">Collaborative NYT Connections</p>
				<Footer />
			</div>
		</header>
	);
}

export default Header;
