# Configuration

Application-wide Spring and authentication configuration:

- `SecurityConfig` defines stateless API security, route access, and password encoding.
- `JwtUtil` creates and validates JWTs.
- `JwtAuthFilter` reads bearer tokens and authenticates requests.
