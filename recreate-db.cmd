@echo off
echo Stopping containers...
docker compose down

echo Removing database volume...
docker volume rm springai_pgdata

echo Starting containers with fresh database...
docker compose up -d

echo Done! The database has been recreated with the chat memory table.
echo Wait a few seconds for the services to start, then check the logs with:
echo docker compose logs -f
