# Guía para agentes

## Qué es el proyecto
UBUMonitor Analytics es una API Spring Boot que ofrece analítica Moodle por REST y persiste datos de cada sitio Moodle.
El reactor Maven tiene `ubumonitor-analytics` (API, lógica, persistencia jOOQ/H2) y `moodle-java-client` (cliente REST y modelos Moodle).
La API consume el cliente como módulo del mismo reactor; el POM raíz declara ambos módulos y la configuración de formato.

## Comandos
Ejecuta primero el test de un fixture concreto; deja el build limpio completo para el final. Los builds generan OpenAPI/jOOQ y arrancan el contexto Spring; tardan más que el test aislado. La compilación nativa tarda más y requiere GraalVM Native Image.

| Objetivo | PowerShell desde la raíz | Coste |
|---|---|---|
| Fixture concreto (primero) | `mvn -pl ubumonitor-analytics -Dtest=MoodleAnalyticsIntegrationTest '-Dintegration.fixture.pattern=classpath:/integration/public/sites/info/happy_path/test-case.json' test` | Un fixture; compila/genera fuentes si hace falta. |
| Todos los tests | `mvn test` | Largo; incluye tests de integración. |
| Empaquetar API y dependencias del reactor | `mvn -pl ubumonitor-analytics -am package` | Largo; compila módulos necesarios y ejecuta tests. |
| Formatear | `mvn spotless:apply` | Corto; puede modificar archivos Java/XML/JSON. |
| Arrancar con configuración local `dev` | `mvn -pl ubumonitor-analytics spring-boot:run '-Dspring-boot.run.profiles=dev'` | Compila y deja el servidor en primer plano. |
| Imagen nativa (perfil Maven) | `mvn -pl ubumonitor-analytics -am -Pnative -DskipTests package` | Muy largo; requiere GraalVM Native Image. |
| Build completo limpio (último) | `mvn clean install` | El más largo; limpia, genera, prueba e instala el reactor. |

En bash, los mismos comandos sirven; conserva las comillas simples alrededor de cada `-D...` como se muestra. El fixture aislado se selecciona con `integration.fixture.pattern`; cambia el patrón por el recurso `classpath:/integration/.../test-case.json` deseado. En un checkout limpio, instala/compila primero el reactor si el módulo `moodle-java-client` aún no está en el repositorio Maven local.

## Mapa del código
```text
ubumonitor-analytics/src/main/java/.../features/<feature>/
  domain/                         modelo y reglas del dominio
  application/{port,service,dto}/ casos de uso, servicios y contratos
  infrastructure/{in,out}/       entrada REST y adaptadores de salida
ubumonitor-analytics/src/main/java/.../shared/
  domain/, application/, infrastructure/ contratos y componentes transversales
ubumonitor-analytics/src/main/resources/
  static/openapi/{openapi.yaml,paths,components}/ contrato REST
  db/migration/                   migraciones Flyway para la BD bootstrap/tenant
  application*.yml                configuración común y perfiles
ubumonitor-analytics/src/test/
  java/.../integration/           runner de integración
  resources/integration/          test-case.json y auth fixtures compartidos
moodle-java-client/src/main/java/ cliente y adaptadores HTTP Moodle
moodle-java-client/src/main/resources/schema/ JSON Schema de request/response Moodle
```
No edites código generado: OpenAPI y jOOQ viven bajo `ubumonitor-analytics/target/generated-sources/`; JSON Schema genera modelos bajo `moodle-java-client/target/generated-sources/jsonschema2pojo/`. Edita su fuente (OpenAPI, migración o schema), nunca `target/`.

## Arquitectura
- Mantén el flujo `infrastructure/in` (REST/delegate) → `application/port/in` (caso de uso) → `application/service` → `application/port/out` → `infrastructure/out` (jOOQ, Moodle u otro IO).
- Haz que los adaptadores implementen puertos de salida. No hagas que un delegate REST acceda directamente a jOOQ o al cliente Moodle.
- Mantén `domain` independiente de Spring, DTOs OpenAPI generados, modelos Moodle y tipos jOOQ. Convierte los tipos en los mappers del adaptador correspondiente.
- Coloca algo en `shared` solo si su contrato o comportamiento se comparte entre features (por ejemplo, sesión, excepciones y configuración común). Mantén modelos y reglas específicos dentro de su feature.
- Referencia canónica para el endpoint pequeño `GET /api/public/sites/info`: `ubumonitor-analytics/src/main/resources/static/openapi/paths/sites/public-info.yaml` → `features/sites/infrastructure/in/rest/SitesApiDelegateImpl.java` → `features/sites/application/port/in/GetPublicSiteInfoUseCase.java` → `features/sites/application/service/PublicSiteInfoService.java` → `features/sites/application/port/out/PublicSiteInfoApiPort.java` → `features/sites/infrastructure/out/moodle/PublicSiteInfoApiAdapter.java` y `SiteInfoApiAdapterMapper.java` → `ubumonitor-analytics/src/test/resources/integration/public/sites/info/happy_path/test-case.json`.

## Recetas
### Añadir o modificar un endpoint
1. Edita `ubumonitor-analytics/src/main/resources/static/openapi/paths/<recurso>/<operacion>.yaml` y sus schemas en `components/schemas/`; registra el path en `static/openapi/openapi.yaml`.
2. Ejecuta `mvn -pl ubumonitor-analytics generate-sources`; inspecciona interfaces/DTOs bajo `target/generated-sources/openapi/`, no los edites.
3. Implementa el método generado en el delegate de la feature, delegando al caso de uso.
4. Define/actualiza `application/port/in/*UseCase.java` y su `application/service/*Service.java`.
5. Si hay IO, define `application/port/out/`, implementa el puerto en `infrastructure/out/` y adapta datos en un mapper.
6. Añade un fixture `test-case.json` bajo `ubumonitor-analytics/src/test/resources/integration/` y ejecuta solo ese fixture.

### Añadir un campo o tabla
1. Añade una migración versionada `ubumonitor-analytics/src/main/resources/db/migration/V<N>__<descripcion>.sql`; no reescribas una migración ya aplicada.
2. Ejecuta `mvn -pl ubumonitor-analytics generate-sources`: Flyway migra `target/moodle_dbs/bootstrap` y jOOQ genera el código a partir de esa base.
3. Actualiza el mapper de persistencia y después el adaptador jOOQ que lee/escribe el campo o tabla.
4. Actualiza el fixture/datos de integración necesario y valida la feature.

### Añadir o cambiar un mapeo Moodle
1. Edita el request/response correspondiente en `moodle-java-client/src/main/resources/schema/<componente>/...`; respeta `title`, `$ref` y tipos del schema vecino.
2. Implementa o actualiza el método de API en `moodle-java-client/src/main/java/es/ubu/lsi/moodle/api/<componente>/` y usa los modelos generados en `target/generated-sources/jsonschema2pojo/`.
3. Ejecuta `mvn -pl moodle-java-client generate-sources` para verificar la generación.
4. En `ubumonitor-analytics`, adapta la llamada en el adaptador Moodle de la feature y convierte el modelo del cliente a tipos de aplicación/dominio mediante el mapper.
5. Añade o ajusta el mock WireMock del fixture; no llames a un Moodle real.

### Añadir o actualizar un fixture de integración
1. Crea/actualiza `ubumonitor-analytics/src/test/resources/integration/<grupo>/<endpoint>/<caso>/test-case.json`.
2. Incluye `request` y `expectedResponse`; añade `moodleMocks` cuando haya llamadas Moodle. Para endpoints autenticados usa `authFixture` (por ejemplo `classpath:/integration/shared/teacher-token-auth.json`) o define `auth`; añade `databaseSetup` como lista de sentencias SQL cuando haga falta estado inicial.
3. Ejecuta el comando «Fixture concreto» cambiando `integration.fixture.pattern` por su ruta classpath. El runner descubre `test-case.json` y crea una BD aislada por caso bajo `target/test-moodle-dbs/`.

## Convenciones
- Marca las lecturas de servicio con `@Transactional(readOnly = true)` y las operaciones de escritura con `@Transactional`, siguiendo los servicios de `features/course/content` y `features/enrollment/course`.
- Lanza excepciones de aplicación existentes y conserva el contrato de `GlobalExceptionHandler`: 400 `BAD_REQUEST`, 401 `UNAUTHORIZED`, 404 `NOT_FOUND`, 409 `CONFLICT`, 500 `UNKNOWN_ERROR`; validación usa 400 `VALIDATION_ERROR` y ruta inexistente 404 `RESOURCE_NOT_FOUND`.
- Declara restricciones/validación en OpenAPI; el generador configura `useBeanValidation`. No parchees DTOs generados.
- No registres tokens, contraseñas, claves ni cabeceras sensibles. Conserva el enmascarado Logbook de `Authorization`, `X-Moodle-Token`, `Cookie`, `password` y `token`.
- Prefiere bucles `for` explícitos a lambdas/`forEach`; usa métodos estáticos con nombre o referencias a métodos en lugar de lambdas en línea y `BiConsumer` cuando sea razonable. Para mapas de clave única usa `Collectors.toMap`, no `groupingBy`; define un merger con nombre si hay colisiones.
- Comprueba `null` con cláusulas guarda y retorno temprano.
- Si ambas formas son igual de claras, prefiere la imperativa; usa streams cuando expresen mejor la transformación. Ejemplo equivalente: `for (User user : users) names.add(user.getName());` / `users.stream().map(User::getName).toList();`.

## Límites
### ✅ Hazlo sin preguntar
- Lee y conserva cambios previos del usuario; limita el diff a la tarea.
- Añade cobertura enfocada para el comportamiento nuevo y ejecuta los checks aplicables.
- Usa fixtures, WireMock y las bases H2 aisladas del runner para verificar integraciones.
### ⚠️ Pregunta antes
- Cambiar contratos REST, autenticación/autorización u otras decisiones de seguridad.
- Cambiar configuración o aislamiento de tenant, esquema de BD/migraciones o añadir dependencias.
### 🚫 Nunca
- Editar `target/` o introducir secretos en código, fixtures, logs o documentación.
- Registrar tokens/credenciales ni usar un Moodle real en pruebas.
- Ejecutar operaciones Git destructivas (por ejemplo `reset --hard`) o crear commits sin petición explícita.
- Modificar tests/fixtures existentes si basta con añadir cobertura nueva; no borres ni debilites asserts para hacer pasar una prueba.

## Gotchas
- Falla la compilación al faltar tipos jOOQ tras `clean` → `flyway:migrate` y `jooq:generate` corren en `generate-sources` sobre `target/moodle_dbs/bootstrap` → ejecuta el lifecycle Maven desde `generate-sources`, no compiles a mano.
- El código jOOQ no encuentra tabla/columna → las migraciones declaran identificadores como `USERS`, `COURSES` y `USERS_IMAGES` en mayúsculas → conserva los nombres SQL del DDL.
- No aparece una modificación del DTO → OpenAPI/JSON Schema se regenera en `target/` → cambia la fuente YAML/JSON Schema y vuelve a generar.
- La API local muestra Swagger/H2 Console solo con configuración de desarrollo → `application-dev.yml` no es el perfil por defecto → activa `dev` solo para uso local.
- `run.bat` tarda y cierra otro proceso → siempre ejecuta `mvn clean install` y mata el listener del puerto 9090 → para iterar, usa el comando dirigido al módulo; revisa el puerto antes de usar el `.bat`.
- `-Pnative` no activa configuración Spring → `native` es un perfil Maven declarado en `ubumonitor-analytics/pom.xml` → activa el perfil Maven y usa GraalVM Native Image.
- Falla una API Java reciente o la compilación con otro JDK → ambos módulos fijan Java 25 y la API usa Spring Boot 4.1.1 → compila con JDK 25 y no introduzcas APIs incompatibles.
- El test recorre todos los fixtures → el patrón por defecto es `classpath:/integration/**/test-case.json` → pasa `-Dintegration.fixture.pattern=classpath:/integration/.../test-case.json`.

## Hecho
- [ ] El fixture específico pasa y cubre el cambio.
- [ ] `mvn spotless:apply` termina; revisa y conserva solo el formato esperado.
- [ ] El build del módulo afectado pasa (`mvn -pl ubumonitor-analytics -am package` o `mvn -pl moodle-java-client package`).
- [ ] Revisa `git diff`; no hay cambios generados en `target/` ni cambios ajenos a la tarea.
- [ ] Si el cambio toca generación compartida o más de un módulo, completa `mvn clean install`.

## Git
- Usa los mensajes observados: `refactor: clean up imports and improve assertions in integration tests`, `chore: prepare next development version (0.0.4-SNAPSHOT)`, `chore(release): v0.0.3`.
- El workflow `.github/workflows/maven.yml` publica al hacer push a `main`, luego integra el commit de release en `develop` y prepara la siguiente versión `-SNAPSHOT`. No asumas otro flujo de ramas.