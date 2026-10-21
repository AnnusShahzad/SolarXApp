# Changelog

All notable changes to this project are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.1.0] - 2026-10-21

Polish from testing on a real phone and inverter.

### Changed
- Dashboard tiles now show their name and reading inside the icon, and the flow dots stay on the wires between tiles instead of sliding underneath them.
- Complete warm colour scheme for light and dark themes; no default purple left on buttons, switches or the navigation bar.
- Details tab shows each reading large and bold, and Settings has the same header as Details.
- The connection screen is centred and keeps a comfortable width in landscape.

### Fixed
- Diagram tiles overlapped at large system font sizes; they now scale with the font but never exceed the available space.
- The dashboard, details and settings could be cut off in landscape; they now scroll.
- The keyboard stayed open after tapping Connect and hid error messages.
- The keyboard's Done key didn't save the alert threshold.
- The keyboard learned and suggested the Pocket WiFi serial; the field now uses a password keyboard.
- Long navigation labels wrapped onto two lines at the largest font size.
- The window flashed white when the app opened in dark mode.

## [1.0.0] - 2026-10-21

First public release.

### Added
- Live dashboard that polls the inverter every second, with an animated energy-flow diagram showing solar, grid and home load.
- Details tab listing grid exchange, solar output and home load.
- Grid power alert: a warning banner when the house draws more than a configurable number of watts from the grid. Settings are saved across launches.
- "Change inverter" option to connect to a different dongle without reinstalling.
- Clear error messages on the connection screen for missing fields, invalid addresses, a wrong serial, and an unreachable inverter.
- Offline banner that keeps the last reading on screen while the inverter is unreachable.
- Dark theme and accessibility labels for the diagram and status banners.
- Unit tests for the register decoding, HTTP client and UI logic, and Paparazzi screenshot tests for every screen.
- CI (unit tests, screenshot tests, lint, build), CodeQL scanning, Dependabot, and a tag-driven release workflow that publishes the APK.

### Fixed
- Home load on the details screen was solar plus grid instead of solar minus grid export.
- A new ViewModel and HTTP client were created on every recomposition, leaking connections; polling now runs in one ViewModel and pauses when the app is in the background.
- The diagram's paths grew on every recomposition.
- The connection screen only reported timeouts; other network failures failed silently.
- Pressing back on the dashboard returned to the connection screen.

### Changed
- Application ID changed from `com.example.solarx` to `io.github.annusshahzad.solarx`.
- Replaced Retrofit, Ktor and Gson with a single OkHttp client with LAN-friendly timeouts.
- Release builds are minified with R8, reducing the APK from 19.8 MB to about 2.3 MB.
- The saved serial number is excluded from backups and device transfers.

[Unreleased]: https://github.com/AnnusShahzad/SolarXApp/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/AnnusShahzad/SolarXApp/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/AnnusShahzad/SolarXApp/releases/tag/v1.0.0
