# Controllers

REST controllers expose the HTTP API and connect requests to application services:

- `AuthController` handles login.
- `UserController` handles registration, profile, and PIN routes.
- `TrustedContactController` handles trusted-contact routes.
- `SosEventController` handles SOS event routes.
- `GuardianTimerController` handles guardian-timer routes.

Request and response payloads are defined in `dto`.
