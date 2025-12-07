# Gym Desktop App

## Quick setup (MySQL)
1) Ensure MySQL is running on port 3306 and `mysql` is on PATH.
2) Initialize the database (creates `gymapp` DB and user `gymapp`/`changeme`):
   - If root has no password: `mysql -u root < db-init.sql`
   - If root has a password: `mysql -u root -p < db-init.sql`
3) Configure the app credentials:
   - Default in `src/main/resources/db.properties` is `root` with empty password (common local setup). If your root has a password, set `db.password` accordingly.
   - If you want to use the `gymapp` user created by db-init, set `db.user=gymapp` and `db.password=changeme`.
4) Run the app:
   - `mvn -q javafx:run` (or double-click `run.bat`)

If `mysql` is not on PATH, use the full path to `mysql.exe` (e.g., `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe -u root < db-init.sql`).

## Default app logins
- owner / owner123
- trainer / train123
- member / member123
