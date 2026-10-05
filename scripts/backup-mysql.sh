#!/bin/bash
# Dumps the oltre MySQL database running in the oltre-mysql container,
# gzips it, and deletes backups older than $RETENTION_DAYS.
#
# Run from the repo root (reads .env for MYSQL_PASSWORD), e.g. via cron:
#   0 3 * * * cd /path/to/Oltre-backend && ./scripts/backup-mysql.sh >> /var/log/oltre-backup.log 2>&1
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "No .env file found - cannot read MYSQL_PASSWORD." >&2
  exit 1
fi
# shellcheck disable=SC1091
source .env

BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
OUT_FILE="$BACKUP_DIR/oltre-$TIMESTAMP.sql.gz"

mkdir -p "$BACKUP_DIR"

docker exec oltre-mysql mysqldump -uoltre -p"$MYSQL_PASSWORD" oltre | gzip > "$OUT_FILE"
echo "Backup written to $OUT_FILE ($(du -h "$OUT_FILE" | cut -f1))"

find "$BACKUP_DIR" -name 'oltre-*.sql.gz' -mtime "+$RETENTION_DAYS" -print -delete
