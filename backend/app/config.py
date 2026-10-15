import os


DATABASE_URL = os.getenv("GYM_COACH_DATABASE_URL", "sqlite:///./gym_coach.db")
JWT_SECRET = os.getenv(
    "GYM_COACH_JWT_SECRET", "local-development-only-change-this-secret"
)
ACCESS_TOKEN_MINUTES = int(os.getenv("GYM_COACH_ACCESS_TOKEN_MINUTES", "30"))
JWT_ALGORITHM = "HS256"
