-- SQLite database initialization script for Connections game
-- Create the connections_games table

CREATE TABLE IF NOT EXISTS connections_games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date DATE NOT NULL UNIQUE,
    words TEXT NOT NULL, 
    categories TEXT NOT NULL,
    status INTEGER DEFAULT 0
);

-- Insert sample data for testing
INSERT OR IGNORE INTO connections_games (date, words, categories, status) VALUES (
    '2025-09-28',
    '["Bass", "Sole", "Pike", "Carp", "Airplane", "Butterfly", "Angel", "Eagle", "Latte", "Mocha", "Americano", "Cappuccino", "Dash", "Key", "Mother", "Surf"]',
    '[{"id":1,"name":"Fish","difficulty":1,"color":"#f9df74","words":["Bass","Sole","Pike","Carp"],"description":"Types of fish"},{"id":2,"name":"Things with Wings","difficulty":2,"color":"#a9c9dd","words":["Airplane","Butterfly","Angel","Eagle"],"description":"Items that have wings"},{"id":3,"name":"Coffee Drinks","difficulty":3,"color":"#b49bc8","words":["Latte","Mocha","Americano","Cappuccino"],"description":"Coffee beverages"},{"id":4,"name":"Words Before Board","difficulty":4,"color":"#ba5365","words":["Dash","Key","Mother","Surf"],"description":"Words that come before board"}]',
    0
);

INSERT OR IGNORE INTO connections_games (date, words, categories, status) VALUES (
    '2025-09-29',
    '["Apple", "Orange", "Banana", "Grape", "Red", "Blue", "Green", "Yellow", "Chair", "Table", "Sofa", "Desk", "Run", "Walk", "Jump", "Swim"]',
    '[{"id":1,"name":"Fruits","difficulty":1,"color":"#f9df74","words":["Apple","Orange","Banana","Grape"],"description":"Types of fruit"},{"id":2,"name":"Colors","difficulty":2,"color":"#a9c9dd","words":["Red","Blue","Green","Yellow"],"description":"Basic colors"},{"id":3,"name":"Furniture","difficulty":3,"color":"#b49bc8","words":["Chair","Table","Sofa","Desk"],"description":"Household furniture"},{"id":4,"name":"Physical Activities","difficulty":4,"color":"#ba5365","words":["Run","Walk","Jump","Swim"],"description":"Types of movement"}]',
    0
);