# Database Directory

This directory contains the persistent SQLite database for the Connections game.

## Files:
- `init.sql` - Database schema and initial data
- `connections.db` - SQLite database file (created automatically)

## Usage:
The database is automatically initialized when the Docker container starts.

## Database Schema:
```sql
CREATE TABLE connections_games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date DATE NOT NULL UNIQUE,
    words TEXT NOT NULL, 
    categories TEXT NOT NULL,
    status INTEGER DEFAULT 0
);
```

## Access:
The database file is mounted as a volume in Docker, ensuring data persistence across container restarts.