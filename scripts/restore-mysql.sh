#!/bin/bash
# Restores the oltre MySQL database from a backup produced by
# backup-mysql.sh. DESTRUCTIVE: overwrites all current data.
#
# Usage: ./scripts/restore-mysql.sh backups/oltre-20260101-030000.sql.gz
set -euo pipefail

cd "$(dirname "$0")/.."

BACKUP_FILE="${1:-}"
if [ -z "$BACKUP_FILE" ] || [ ! -f "$BACKUP_FILE" ]; then
  echo "Usage: $0 <path-to-backup.sql.gz>" >&2
  exit 1
fi

if [ ! -f .env ]; then
  echo "No .env file found - cannot read MYSQL_PASSWORD." >&2
  exit 1
fi
# shellcheck disable=SC1091
source .env

read -r -p "This will overwrite the current oltre database. Continue? [y/N] " confirm
case "$confirm" in
  [yY]*) ;;
  *) echo "Aborted."; exit 1 ;;
esac

gunzip -c "$BACKUP_FILE" | docker exec -i oltre-mysql mysql -uoltre -p"$MYSQL_PASSWORD" oltre
echo "Restored from $BACKUP_FILE"
