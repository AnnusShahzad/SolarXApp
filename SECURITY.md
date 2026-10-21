# Security Policy

## Supported versions

Security fixes are made on the latest release and on `master`.

## Reporting a vulnerability

Please **do not open a public issue** for security problems.

Report them privately through [GitHub Security Advisories](https://github.com/AnnusShahzad/SolarXApp/security/advisories/new). Include the steps to reproduce, the impact, and any suggested fix. You can expect an acknowledgement within a few days, and updates until a fix is released. Reporters are credited in the release notes unless they prefer otherwise.

## How the app handles your data

- **Local network only.** SolarX talks directly to the Pocket WiFi dongle over your Wi-Fi. It has no backend, analytics or third-party SDKs, and sends nothing to the internet.
- **Plain HTTP is required.** The dongle's local API only speaks unencrypted HTTP, so the app allows cleartext traffic. Anyone on the same network can read that traffic, including the serial number. Use SolarX on a network you trust.
- **The serial number is the dongle's password.** It is stored in the app's private storage and is excluded from cloud backups and device-to-device transfers.
