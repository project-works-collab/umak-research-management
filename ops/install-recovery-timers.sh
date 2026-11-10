#!/bin/sh
# Run on the intended Linux deployment after configuring recovery credentials.
set -eu
[ "$(id -u)" = 0 ] || { echo 'Run as root on the deployment host.' >&2; exit 1; }
[ -f /etc/crm/backup.env ] || { echo '/etc/crm/backup.env is required.' >&2; exit 1; }
id crm-backup >/dev/null 2>&1 || { echo 'Create the crm-backup service identity first.' >&2; exit 1; }
[ -f /opt/crm/scripts/backup-offsite.js ] || { echo 'Deploy the repository to /opt/crm first.' >&2; exit 1; }
command -v node >/dev/null
[ -x /usr/bin/node ] || { echo 'Units require Node at /usr/bin/node.' >&2; exit 1; }
command -v pg_dump >/dev/null
command -v pg_restore >/dev/null
unit_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
install -d -o crm-backup -g "$(id -gn crm-backup)" -m 0700 /var/lib/crm-backup
for name in crm-backup crm-restore-drill crm-privacy crm-backup-freshness; do
    install -m 0644 "$unit_dir/$name.service" "$unit_dir/$name.timer" /etc/systemd/system/
done
install -m 0644 "$unit_dir/crm-backup-alert.service" /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now crm-backup.timer crm-restore-drill.timer crm-privacy.timer crm-backup-freshness.timer
systemctl list-timers crm-backup.timer crm-restore-drill.timer crm-privacy.timer crm-backup-freshness.timer
