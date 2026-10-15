# Backend

Initial FastAPI backend foundation for the Android gym coach. This increment includes local database initialization and email/password registration, login, and authenticated current-user lookup.

## Run locally

From this directory, create a virtual environment, install `requirements.txt`, then run:

```powershell
$env:GYM_COACH_JWT_SECRET = "replace-with-a-long-random-local-secret"
uvicorn app.main:app --reload --host 0.0.0.0
```

The default database is `gym_coach.db` in the current directory. It is created automatically, including its tables, when the API starts; no database server setup is needed for local development. Since the URL is relative, launching Uvicorn from `backend/` creates `backend/gym_coach.db`. Set `GYM_COACH_DATABASE_URL` to use another SQLAlchemy-supported database URL. The API docs are available at `/docs` while running locally. Binding to `0.0.0.0` allows the Android emulator to connect; the emulator uses `10.0.2.2` to reach the development machine.

## Initial endpoints

- `GET /health`
- `POST /api/v1/auth/register` — JSON body: `email`, `password` (at least 10 characters); returns bearer token and user.
- `POST /api/v1/auth/login` — same JSON fields; returns bearer token and user.
- `GET /api/v1/auth/me` — requires `Authorization: Bearer <access_token>`.

## Current limits

This is a development foundation, not a production-ready authentication service. It uses schema creation at startup rather than migrations, has no refresh/revocation flow or rate limiting, and defaults to a development signing secret if the environment variable is absent. Configure a strong secret and production database, then add migrations and operational controls before deployment. Profile, plan, workout, media, worker, and analysis endpoints are subsequent milestones.
