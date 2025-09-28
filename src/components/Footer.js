import '../css/Footer.css';

const Footer = () => (
  <footer className="footer">
    <div className="footer-content">
        <span>Built with React</span>
        <br />
        <span>© {new Date().getFullYear()} Christopher Andrade</span>
        <br />
        <span> <a className="footer-link" href="https://github.com/AndradeCJahb/andradecjahb.github.io" target="_blank" rel="noopener noreferrer">Source Code</a></span>
    </div>
  </footer>
);

export default Footer;
