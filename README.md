# Connected - React Dev Container

This directory contains a minimal React app scaffolded to run inside a Docker development container.

Quick start:

```bash
# build and run with docker-compose (maps container port 3000 to host 3001)
docker-compose up --build

# or run directly
docker build -f Dockerfile.dev -t connected-dev .
docker run -p 3001:3000 -v $(pwd):/app -v /app/node_modules connected-dev
```

Open http://localhost:3001 to view the app.
