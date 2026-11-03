#!/usr/bin/env bash
# Regenerates the three database dumps from the running containers (run after `docker compose up`).
# Usage: ./docs/export-db-dumps.sh   (reads the DB names/users from .env)
set -e
cd "$(dirname "$0")/.."
set -a; source .env; set +a
docker exec ems-auth-db   pg_dump -U "$AUTH_DB_USER"   "$AUTH_DB_NAME"   > docs/auth_db_dump.sql
docker exec ems-user-db   pg_dump -U "$USER_DB_USER"   "$USER_DB_NAME"   > docs/user_db_dump.sql
docker exec ems-device-db pg_dump -U "$DEVICE_DB_USER" "$DEVICE_DB_NAME" > docs/device_db_dump.sql
echo "Dumps written to docs/"
