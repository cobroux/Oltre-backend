#!/bin/bash
# Dumps the oltre MySQL database running in the oltre-mysql container,
# gzips it, and deletes backups older than $RETENTION_DAYS.
#
# Copie hors machine (optionnelle) : si .env définit BACKUP_AGE_RECIPIENT (clé
# publique age) et BACKUP_RCLONE_REMOTE (ex. oracle:oltre-backups), le dump est
# chiffré puis copié et vérifié sur le stockage distant ($REMOTE_DAYS jours gardés).
# Seule la version chiffrée quitte la machine.
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

# MYSQL_PWD est passé à docker exec par nom (sans valeur) : le mot de passe
# n'apparaît ni dans les arguments de mysqldump ni dans ps.
export MYSQL_PWD="$MYSQL_PASSWORD"
docker exec -e MYSQL_PWD oltre-mysql mysqldump -uoltre --single-transaction oltre | gzip > "$OUT_FILE"
echo "Backup written to $OUT_FILE ($(du -h "$OUT_FILE" | cut -f1))"

find "$BACKUP_DIR" -name 'oltre-*.sql.gz' -mtime "+$RETENTION_DAYS" -print -delete

if [ -n "${BACKUP_AGE_RECIPIENT:-}" ] && [ -n "${BACKUP_RCLONE_REMOTE:-}" ]; then
  REMOTE_DAYS="${REMOTE_DAYS:-90}"
  ENC_FILE="$OUT_FILE.age"
  age -r "$BACKUP_AGE_RECIPIENT" -o "$ENC_FILE" "$OUT_FILE"
  rclone copy "$ENC_FILE" "$BACKUP_RCLONE_REMOTE"
  rclone check --size-only --one-way "$BACKUP_DIR" "$BACKUP_RCLONE_REMOTE" --include "$(basename "$ENC_FILE")"
  rm -f "$ENC_FILE"
  rclone delete --min-age "${REMOTE_DAYS}d" "$BACKUP_RCLONE_REMOTE" --include 'oltre-*.sql.gz.age'
  echo "Copie chiffrée hors machine : $BACKUP_RCLONE_REMOTE/$(basename "$ENC_FILE")"
fi
