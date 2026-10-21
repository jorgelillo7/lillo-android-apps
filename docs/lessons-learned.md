# Lessons learned

Short, one line each. Add to it whenever something bites.

- **Test the release build before uploading.** v4's toolchain upgrade crashed on start only in
  release (R8). `tools/smoke_test.sh` exists for this.
- **Verify the bundle you upload** with `scripts/verify-bundle.sh`: version, signature, permissions.
  A stale `.aab` from an earlier build is easy to grab by mistake.
- **Inactive developer accounts get closed.** Log in and ship something at least once a year.
- **Keep the keystore and its password backed up outside the repo.** Without them you need an
  upload key reset (days).
- **Files with a leading space** (` keystore.properties`) dodge `.gitignore`. Check `git status`
  before committing anything near secrets.
- **Don't host the privacy policy in a repo you may rename**: GitHub Pages URLs don't redirect.
- **Stacked PRs**: merging the base PR with `--delete-branch` closes the child PR. Retarget the
  child to `master` first.
- **Don't publish a store listing ahead of the release** that adds the features it describes.
- **The emulator is heavy on this Mac.** Run capture/test scripts in the foreground with Chrome
  closed; background jobs get killed under memory pressure.
- **`.properties` files are read as ISO-8859-1.** A UTF-8 "ó" in a path broke
  `keystore.properties`. Keys now live in `~/Projects/documentation/keys/<app>/` (ASCII, no spaces),
  and `scripts/new-upload-key.sh` still escapes non-ASCII as `\uXXXX` just in case.
- **Compose dialogs are separate windows**: `testTagsAsResourceId` set on the activity content does
  not reach them; set it on the dialog modifier too, or UI scripts cannot find their buttons.
- **Keep `@Serializable` classes in a module with the serialization plugin.** Missing it compiles
  fine and crashes at runtime ("Serializer for class … is not found").

- **Screenshots in the middle of a drag:** `adb shell input swipe` releases at the end, so a
  "hold to reveal" screen is already closed when you capture it. Drive the finger by hand:
  `adb shell input motionevent DOWN x y`, a few `MOVE x y`, `screencap`, then `UP x y`.

- **Test short landscape screens too.** Handhelds like the AYN Thor run phone apps on a wide, low
  screen: a layout that only fits portrait cut the reveal text, shrank the drawing canvas to a
  stamp and hid the players on the vote screen. Rotate the emulator
  (`adb shell settings put system user_rotation 1`) and check every screen; `isShortScreen()`
  (height < 480 dp) switches to compact layouts. A UI battery can play every scenario sideways
  (whos-lying `battery_test.py --landscape`); make it check each screenshot really is landscape:
  something resets the rotation when a run starts, and a whole "landscape" scenario once ran upright.
- **A bottom bar eats short screens.** A big button plus a text button below the content left about
  130 dp for the result, ranking and podium in landscape. `LilloPage(sideActionsWhenShort = true)`
  moves the actions beside the content; opt-in per screen, not for screens whose bottom changes
  mid-gesture (the reveal curtain).
- **Compose only redraws what it can observe.** A screen that reads a plain `var` of a mutable domain
  object never updates when it changes (the "next vote" button never came back after a wrong Drifter
  guess). Keep UI state in `StateFlow` / `mutableStateOf`, or immutable objects replaced on change.
- **Stacked PRs and `--delete-branch`.** Squash-merging the lower PR with `--delete-branch` deletes
  the upper PR's base, and GitHub closes the upper PR for good. Retarget the upper PR to `master`
  first (or rebase it onto `master` once the lower one is in), then delete the branch.
- **One UI run on the emulator at a time.** Two scripts driving the same emulator (a smoke test
  moved to the background by a timeout plus a new one in the foreground) tap over each other and
  crash `uiautomator` ("already registered"): every result of that round is void. Wait for the
  background run to report before starting another, even if `ps` doesn't show it.
- **Smoke tests uninstall first.** Once an app has an upload key, its release build is signed
  differently from the debug build on the emulator and `adb install -r` fails with
  `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.
- **CI emulators: the ATD image, one job per app.** On GitHub's runners the `google_apis` image
  died on whos-lying right after its home screen in 9 of 11 runs (also with 4 GB of RAM, API 35
  and AOSP; no host OOM, nothing in logcat): a fresh boot indexes Google apps in the background.
  Google's Automated Test Device image (`google_atd`) passed every run. Six apps in a row on one
  emulator also took an hour, so each app runs in its own job. ATD shows no on-screen keyboard:
  close it with `hide_keyboard`, never a bare BACK, which leaves the screen.
- **Flow tests and DataStore.** Compose's test clock waits for recomposition and animations, not for
  DataStore writes on its IO thread. After a tap that saves, wait on the store (`waitUntil` on the
  saved state), not on the screen, or the next tap hits a button that is about to go. The same gap
  hid a real race twice: decision-wheel and tournaments opened a new item before its save landed.
