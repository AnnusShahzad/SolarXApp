# Changelog

All notable changes to this project are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

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

[Unreleased]: https://github.com/AnnusShahzad/SolarXApp/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/AnnusShahzad/SolarXApp/releases/tag/v1.0.0
