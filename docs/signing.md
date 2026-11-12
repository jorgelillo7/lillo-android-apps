# Signing — upload keys

Every app has its own **upload key**. It signs the bundles you upload to Google Play; with
**Play App Signing** (the default) Google then re-signs the app with an *app signing key* it keeps.
So the upload key never reaches users, and if you lose it Google can replace it — but it takes
days and a support request. Treat it like a password.

| Thing | Where | Secret? |
|---|---|---|
| `upload-<app>.jks` (the key) | `~/Projects/documentation/keys/<app>/` + Google Drive backup | **Yes** |
| Its password | Password manager + `credentials.txt` next to the Drive backup | **Yes** |
| `apps/<app>/keystore.properties` | Repo folder, git-ignored | **Yes** (contains the password) |
| Certificate fingerprints (SHA-1/SHA-256, serial) | `apps/<app>/OPERATIONS.md` | No |

Christmas Countdown's key keeps its 2021 name: `~/Projects/documentation/keys/christmas-countdown/christmas.jks`
(alias `key0`). If you move any key, update the app's `keystore.properties` (`storeFile`).

Keep folder names ASCII without spaces: `.properties` files are read as ISO-8859-1, so accents
or stray spaces in the path break signing.

## Create one (new app)

Run in **your own terminal** (it asks for the password without showing it; it must not go
through a chat):

```bash
cd ~/Projects/lillo-android-apps
scripts/new-upload-key.sh <app-folder>          # e.g. decision-wheel
```

It creates `~/Projects/documentation/keys/<app>/upload-<app>.jks` (RSA 4096, PKCS12, alias
`upload`, ~27 years) and `apps/<app>/keystore.properties` with the password (git-ignored,
readable only by you), prints the fingerprints, and backs the key up to Google Drive (below).

Then:

1. Save the password in your password manager.
2. Check the closing lines say the key was backed up; if not, fix what they say and run
   `scripts/backup-upload-key.sh <app>`.
3. Copy the printed serial and fingerprints into `apps/<app>/OPERATIONS.md`.
4. `git status`: neither `keystore.properties` nor the `.jks` may appear.
5. Build and check: `./gradlew :apps:<app>:app:bundleRelease` then
   `scripts/verify-bundle.sh apps/<app>/app/build/outputs/bundle/release/app-release.aab` —
   the owner must be `CN=Jorge Lillo`, not `Android Debug`.

Android Studio can do the same (Build → Generate Signed App Bundle → Create new); the script just
makes the location, alias and algorithm consistent.

## Backup to Google Drive

Losing the Mac must not mean losing the keys. `scripts/backup-upload-key.sh` copies an app's key
(the `.jks` named in its `keystore.properties`) and a `credentials.txt` with its alias and
passwords to Google Drive for Desktop, one folder per app:
`…/GoogleDrive-<account>/Mi unidad/Proyectos personales/backups/android-playstore-keys/<app>/`.

```bash
scripts/backup-upload-key.sh <app>          # or: all
scripts/backup-upload-key.sh --check all    # does Drive hold the same key for every app? writes nothing
```

- The folder is found at run time (the account is part of the path and is never written in this
  public repo); `LILLO_KEYS_BACKUP_DIR=<folder>` picks another one. If no folder or several
  match, it stops.
- A copy already there with the same checksum is "already backed up"; a **different** key there
  is never overwritten: it stops and you compare them by hand. Every copy is read back and checked.
- Passwords are never printed.

**Plain or encrypted.** By default the files go to Drive as they are, as they always have: anyone
who gets into that Google account gets the key and its password. With `--encrypt`
(`LILLO_KEYS_BACKUP_ENCRYPT=1` for `new-upload-key.sh`) they go as `upload-<app>.jks.enc` and
`credentials.txt.enc` (AES-256, PBKDF2 with 600 000 iterations, macOS's own `openssl`), with one
master passphrase kept in the Keychain item `lillo-android-keys-backup` and in your password
manager. Create it once:

```bash
security add-generic-password -s lillo-android-keys-backup -a "$USER" -w   # asks for the passphrase
```

Restore an encrypted copy (asks for the passphrase):

```bash
openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -md sha256 -in upload-<app>.jks.enc -out upload-<app>.jks
```

A lost passphrase means unreadable backups; a leaked plain backup means someone else holds the
upload key. Either way an upload key can be reset through Play (below), so the damage is a few
days without releases, not a lost app.

## First upload to Play

The first bundle you upload to a new app **registers** that upload key with Play App Signing
(Google generates the app signing key). From then on every upload must be signed with it.

For Android developer verification, register the package with the **app signing key**
fingerprint (Play Console → Test and release → App integrity → App signing), not the upload key.

## Check which key a bundle has

```bash
scripts/verify-bundle.sh <path/to/app-release.aab>
```

## Lost key or forgotten password

Play Console → the app → Test and release → App integrity → App signing →
**Request upload key reset**. Create a new key with the script (move the old
`keystore.properties` away first), export its certificate:

```bash
keytool -export -rfc -keystore upload-<app>.jks -alias upload -file upload-certificate.pem
```

and upload the `.pem` in the reset form. It becomes active after a couple of days.
