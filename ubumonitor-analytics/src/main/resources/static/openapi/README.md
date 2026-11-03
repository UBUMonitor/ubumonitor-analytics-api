# UBU Monitor Analytics API (OpenAPI)

## Estructura

```
openapi.yaml                  # índice: info, servers, security, tags y paths
paths/<recurso>/<accion>.yaml # un archivo por path (la carpeta refleja la URL)
components/
  schemas/<recurso>/<accion>/{request,response}.yaml
  parameters/                 # courseId, userId
  responses/                  # errores comunes 400, 401, 404, 409
  securitySchemes/
```

## Reglas

- Los paths solo se declaran en `openapi.yaml`; cada uno apunta con `$ref` a su archivo.
- Un schema por archivo, con el nombre del schema como clave raíz (`#/NombreSchema`).
- Parámetros y errores repetidos viven en `components/` y se referencian; no se copian.
- Los endpoints bajo `/public` declaran `security: [ ]`.

## Comandos (Redocly CLI)

```bash
npx @redocly/cli lint openapi.yaml
npx @redocly/cli bundle openapi.yaml -o dist/openapi.bundle.yaml
npx @redocly/cli preview-docs openapi.yaml
```
