# Android app

Native Kotlin / Jetpack Compose client. The first screen supports registration and login against the FastAPI backend, validates a saved access token on launch, and shows a signed-in placeholder screen.

## Open and run

Open this `android/` directory in Android Studio and let Gradle sync. Run the `app` configuration on an Android emulator. The default debug API base URL is `http://10.0.2.2:8000`, which routes from the emulator to the development computer.

Start the backend from `backend/` with a development signing key and a host binding reachable by the emulator:

```powershell
$env:GYM_COACH_JWT_SECRET = "replace-with-a-long-random-local-secret"
uvicorn app.main:app --reload --host 0.0.0.0
```

For a physical phone on the same Wi-Fi, override the API URL with the development computer's LAN address in Android Studio's Gradle properties, for example `GYM_COACH_API_BASE_URL=http://192.168.1.20:8000`. The phone and computer must be able to reach each other, and the computer firewall may need to allow the development port.

The app currently stores the bearer token in app-private preferences for development convenience. The debug manifest permits local HTTP; the main/release manifest does not. Before release, use HTTPS and move token persistence to a suitable protected storage implementation.

## Current scope

- Login and registration screens with basic input checks and loading/error states.
- Calls `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, and `GET /api/v1/auth/me`.
- No profile, workout, camera, or video review screens yet.
