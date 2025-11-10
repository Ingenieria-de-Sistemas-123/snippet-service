# snippet-service

Servicio de snippets con creación/actualización vía JSON y multipart, validación contra language-service y verificación de permisos (placeholder) contra permission-service.

## Variables de entorno relevantes
- LANGUAGE_BASE_URL: Base URL del microservicio de lenguajes (default local http://localhost:8082)
- PERMISSION_BASE_URL: Base URL del microservicio de permisos (default local http://localhost:8083, docker: usar nombre de servicio)
- DB_USERNAME / DB_PASSWORD: Credenciales Postgres locales

## Endpoints principales
- POST /snippets (JSON)
- POST /snippets (multipart form-data: name, description?, language, version, file)
- PUT /snippets/{id} (JSON)
- PUT /snippets/{id} (multipart)
- GET /snippets

## Integración permission-service (placeholder)
Se espera que permission-service exponga endpoints como:
- GET /permissions/view?userId=<user>&snippetId=<id> -> { "allowed": true|false }
- GET /permissions/edit?userId=<user>&snippetId=<id> -> { "allowed": true|false }

`PermissionService` consume estos endpoints usando un RestClient configurado en `HttpConfig`.

## Docker Compose
Cada microservicio mantiene su propio docker-compose. Para que snippet-service conozca permission-service, definir PERMISSION_BASE_URL apuntando al hostname del contenedor del otro servicio (por ejemplo `http://permission-service:8080`). Asegurarse que corran en la misma red (puede necesitar `docker network create` y agregar ambos). Si se usan compose separados, unir redes con `--network` o declarar una red externa en ambos compose.

## Próximos pasos
- Sustituir endpoints placeholder de permission-service por los reales.
- Añadir autenticación (Auth0) para obtener userId.
- Implementar caché y manejo de timeouts/circuit breaker para permission y language services.
