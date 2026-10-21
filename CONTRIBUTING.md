# Contributing to SolarX

Thanks for your interest in improving SolarX! Bug reports, feature ideas, documentation fixes and code are all welcome.

## Ways to contribute

- **Report a bug** or **suggest a feature** using the [issue templates](https://github.com/AnnusShahzad/SolarXApp/issues/new/choose).
- **Add support for another inverter model.** If your SolaX model reports values at different positions in the `Data` array, open an issue with a sample response (remove your serial number first).
- **Improve the docs.** If something in the README was confusing, a pull request that clarifies it is very welcome.

## Local setup

Requirements: Android Studio (or the Android SDK with platform 37) and JDK 21 or newer.

```bash
git clone https://github.com/AnnusShahzad/SolarXApp.git
cd SolarXApp
./gradlew assembleDebug        # build the app
./gradlew testDebugUnitTest    # unit tests
./gradlew verifyPaparazziDebug # screenshot tests
./gradlew lintDebug            # Android lint
```

To try the app against a real inverter, install it on a phone that is on the same network as the Pocket WiFi dongle.

## Project layout

| Path | What lives there |
| --- | --- |
| `data/InverterClient.kt` | HTTP calls to the dongle. Every failure is reported as an `IOException`. |
| `data/InverterReading.kt` | Turns the raw `Data` registers into watts. Register positions and scaling live here. |
| `data/SettingsRepository.kt` | Saved connection and grid alert settings. |
| `ui/login/` | Connection screen and its ViewModel. |
| `ui/monitor/` | Dashboard, details and settings tabs, the energy-flow diagram and the polling ViewModel. |
| `ui/theme/` | Material 3 colours and typography. |
| `app/src/test/` | JVM unit tests, plus Paparazzi screenshot tests whose golden images are in `app/src/test/snapshots/`. |

## Making a change

1. Fork the repository and create a branch from `main`, e.g. `fix/offline-banner` or `feat/daily-yield`.
2. Keep the change focused on one thing, and match the style of the surrounding code.
3. Add or update tests for behaviour you change. If you change a screen on purpose, re-record its screenshots with `./gradlew recordPaparazziDebug` and commit the new images.
4. Run `./gradlew testDebugUnitTest verifyPaparazziDebug lintDebug assembleDebug` before pushing.
5. Write commit messages in the imperative mood ("Add daily yield card", not "Added...").
6. Open a pull request and fill in the template. CI must pass before merging.

## Coding guidelines

- **Screens are stateless.** A `*Screen` composable takes UI state and callbacks; only the `*Route` composables know about ViewModels. This keeps screens previewable and screenshot-testable.
- **Keep user-facing text in `strings.xml`.**
- **Never log or display the serial number** outside the login field.

## Releases

Maintainers release by adding the version to `CHANGELOG.md` and pushing a tag such as `v1.1.0`. The [release workflow](.github/workflows/release.yml) runs the checks, builds an APK signed with the release key from the repository secrets and attaches it to a GitHub release. The workflow refuses to publish without that key, because APKs signed with different keys cannot update each other.

By contributing you agree that your contributions are licensed under the [MIT License](LICENSE), and to follow our [Code of Conduct](CODE_OF_CONDUCT.md).
