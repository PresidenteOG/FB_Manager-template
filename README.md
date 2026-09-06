![FB_Manager](./docs/banner.png)

# FB_Manager [TEMPLATE]

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)

An Android console for a Firebase-backed device fleet: sign in, see connected devices update in
real time, get notified when a new one appears.

## Showcase

Real screenshots from the app running on a physical device, not the Firebase console.

![Sign-in screen: email and password fields under "Iniciar sesión / Administrador Firebase"](./docs/sign-in.jpg) | ![Live validation: "The email address is badly formatted" shown under the password field](./docs/validation-error.jpg) | ![Empty device list: 0 total / 0 active / 0 blocked / 0 expired, "Sin dispositivos registrados"](./docs/empty-state.jpg)
:---:|:---:|:---:
Sign-in screen | Real-time field validation | Empty device list

These three screens run without a live backend behind them, which is why they're the ones shown:
sign-in, its own input validation, and the list's empty state. The populated device list only
renders once real devices are talking to a real Firebase project — that's the one screen this
template can't show without also showing someone's live production data, so it's left out on
purpose rather than faked or blurred.

## Setup

See [SETUP.md](./SETUP.md) — you'll need your own free Firebase project.

## Architecture

![FB_Manager architecture: Compose UI calls domain interfaces, implemented by AuthRepository and DeviceRepositoryImpl, backed by your own Firebase project](./docs/architecture.png)

See [ARCHITECTURE.md](./ARCHITECTURE.md) for the full breakdown.

## License

MIT — see [LICENSE](./LICENSE).
