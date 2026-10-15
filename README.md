# Vision-Based AI Gym Assistant

An Android-first personal gym coach that stores training context, analyzes recorded exercise sets, and presents post-set video feedback with evidence and confidence.

## Project records

- [Decision Log](AI%20Decision/Decision%20Log.md) records dated decisions, reasons, and impacts.
- [Project Design](AI%20Decision/Project%20Design.md) describes the product goal, workflow, architecture, data boundaries, and implementation sequence.

## Backend starting point

The initial Python/FastAPI foundation is in [backend](backend/README.md). Current scope is local development authentication and a health endpoint. Video analysis, training plans, workouts, storage, and the Android client are planned follow-on work.

## Android client

The native Kotlin / Jetpack Compose starter is in [android](android/README.md), with registration and login connected to the backend API.
