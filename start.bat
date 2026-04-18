@echo off

echo 1. Building Docker images
docker-compose build

echo 2. Starting services
docker-compose up -d

timeout /t 5

echo 3. Checking services status
docker-compose ps

echo 4. Printing logs
docker-compose logs -f