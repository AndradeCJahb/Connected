import '../css/SolvedConnectionPopup.css';

function SolvedConnectionPopup({ visible, onClose }) {
    if (!visible) return null;

    return (
        <div className="solved-popup">
                <h2 className="solved-title">Connected Solved!<span className="close-solved" onClick={onClose}>
                    &times;
                </span></h2>
                
                <p className="solved-text">
                    Congratulations! You've completed the connections.
                </p>
        </div>
    );
}

export default SolvedConnectionPopup;