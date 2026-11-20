# Gimnasio Hexagonal — planes, inscripciones y pagos

Proyecto final de **Desarrollo Orientado a Servicios**: aplicación web con **Quarkus**, **arquitectura hexagonal**,
vistas **Qute + Bootstrap 5**, base de datos **MySQL con JPA/Hibernate**, reportes **PDF (OpenPDF)** y documentación **Swagger**.

## Propuesta (3 frases)

1. **Problema:** un gimnasio pequeño lleva en cuadernos los planes, las inscripciones y los abonos de sus socios, y no sabe quién debe plata, a quién se le venció el plan ni cuánto recaudó en un periodo.
2. **A quién le sirve:** al administrador y a la recepción del gimnasio, que inscriben socios, cobran en abonos y consultan el estado de cuenta.
3. **Datos que guarda y consulta:** planes (precio y duración), inscripciones de cada socio con su vigencia y estado, y los pagos realizados con medio y fecha.

## Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21 |
| IntelliJ IDEA | Community o Ultimate (no necesita plugins ni Lombok) |
| XAMPP | MySQL/MariaDB encendido en el puerto 3306 (usuario `root`, sin contraseña) |
| Maven | Incluido (`mvnw`) |

## Cómo ejecutarlo en IntelliJ IDEA

1. **Enciende MySQL** en el panel de XAMPP (botón *Start* de MySQL). No hay que crear la base: se crea sola.
2. En IntelliJ: **File → Open…** y elige la carpeta del proyecto (o el archivo `pom.xml` → *Open as Project*).
3. Espera a que Maven descargue las dependencias. Si no arranca solo: pestaña **Maven** → botón *Reload All Maven Projects*.
4. Verifica el JDK: **File → Project Structure → Project → SDK = 21**.
5. Abre la pestaña **Maven** (derecha) → *gimnasio-hexagonal → Plugins → quarkus → `quarkus:dev`* (doble clic).
   También sirve la terminal de IntelliJ: `./mvnw quarkus:dev` (en Windows: `mvnw.cmd quarkus:dev`).
6. Abre el navegador en **http://localhost:8080**.

Al arrancar por primera vez se crean la base `gimnasio`, sus tablas y los **datos de prueba** (5 planes, 8 inscripciones, 8 pagos).
Si la tabla de planes ya tiene datos, no se vuelven a cargar. Para empezar de cero: borra la base `gimnasio` en phpMyAdmin y reinicia.

## Direcciones

| Pantalla | URL |
|---|---|
| Listado de inscripciones (principal) | http://localhost:8080/ |
| Formulario de nueva inscripción | http://localhost:8080/enrollments/new |
| Detalle, estado de cuenta y pago | http://localhost:8080/enrollments/1 |
| Planes (listar, crear, activar/desactivar) | http://localhost:8080/plans |
| Reporte de ingresos por fechas | http://localhost:8080/reports |
| PDF: listado de inscripciones | http://localhost:8080/reports/enrollments.pdf |
| PDF: reporte de ingresos | http://localhost:8080/reports/income.pdf?from=2026-01-01&to=2026-12-31 |
| PDF: comprobante de un pago | http://localhost:8080/reports/receipt/1.pdf |
| **Swagger UI** | http://localhost:8080/q/swagger-ui |
| **Documento OpenAPI** | http://localhost:8080/q/openapi |

## Flujo para la demostración

1. Entrar a **/** y ver el listado, los contadores y el saldo por cobrar. Filtrar por estado.
2. **Nueva inscripción** → elegir plan, escribir socio, documento y fecha → queda *Pendiente de pago*.
3. En el detalle, **registrar un pago** (ej. 30% del plan) → sigue *Pendiente*; pagar el resto → pasa a *Activa*.
4. Descargar el **comprobante PDF** de un pago.
5. Probar errores de reglas de negocio (salen como alertas rojas): documento inválido, segunda inscripción vigente
   del mismo socio, pago mayor al saldo, primer pago menor al 30%, pagar una inscripción vencida o cancelada.
6. **Cancelar** una inscripción y ver cómo cambia el estado.
7. **Reportes** → elegir rango de fechas → ver totales por medio de pago → descargar el PDF.
8. Abrir **Swagger** y mostrar los recursos documentados con `@Tag` y `@Operation`.

## Reglas de negocio (dominio)

| Regla | Dónde está |
|---|---|
| Plan: nombre obligatorio y no repetido, precio > 0, duración 1–730 días | `domain/service/PlanRules.java` |
| Inscripción: documento 5–12 dígitos, teléfono 7–10 dígitos (opcional), fecha de inicio no anterior a hoy, plan activo | `domain/service/EnrollmentRules.java` → `create` |
| Un socio no puede tener dos inscripciones vigentes (pendiente/activa) | `EnrollmentRules.create` |
| Fecha de fin = inicio + duración del plan | `EnrollmentRules.create` |
| Se cancelan solo inscripciones pendientes o activas | `EnrollmentRules.cancel` |
| Inscripción pendiente/activa con fecha de fin pasada → **Vencida** | `EnrollmentRules.expireIfOverdue` |
| Pago: valor > 0, no puede superar el saldo, no en inscripciones vencidas/canceladas | `domain/service/PaymentRules.java` → `register` |
| El primer pago debe ser al menos el **30%** del plan | `PaymentRules.register` |
| Al quedar el saldo en 0 la inscripción pasa a **Activa** | `PaymentRules.updateStatusAfterPayment` |
| Reporte: rango de fechas válido, total y total por medio de pago | `domain/service/IncomeRules.java` |

## Arquitectura hexagonal — ¿dónde está cada cosa?

```
                        ┌────────────────────────────────────────────┐
  Navegador  ──HTML──▶  │ ADAPTADOR WEB (infrastructure/adapter/in)   │
                        │   usa ▼ puertos de ENTRADA                  │
                        │ ┌────────────────────────────────────────┐ │
                        │ │ APLICACIÓN (application)               │ │
                        │ │  port/in  : casos de uso (interfaces)  │ │
                        │ │  usecase  : implementan los casos      │ │
                        │ │  port/out : lo que el sistema necesita │ │
                        │ │ ┌────────────────────────────────────┐ │ │
                        │ │ │ DOMINIO (domain)  ← el centro      │ │ │
                        │ │ │  model, service (reglas), exception│ │ │
                        │ │ └────────────────────────────────────┘ │ │
                        │ └────────────────────────────────────────┘ │
                        │   implementa ▲ puertos de SALIDA            │
  MySQL  ◀──JPA──────── │ ADAPTADOR JPA (infrastructure/adapter/out)  │
                        └────────────────────────────────────────────┘
        Todas las flechas de dependencia apuntan hacia el dominio.
```

| Elemento | Archivo(s) |
|---|---|
| **Entidades del dominio** (sin `@Entity`) | `domain/model/Plan.java`, `Enrollment.java`, `Payment.java` |
| **Servicios del dominio** (reglas) | `domain/service/PlanRules`, `EnrollmentRules`, `PaymentRules`, `IncomeRules` |
| **Puertos de entrada** (casos de uso) | `application/port/in/*UseCase.java` |
| **Implementación de casos de uso** | `application/usecase/*Service.java` |
| **Puertos de salida** (guardar/consultar) | `application/port/out/PlanRepository`, `EnrollmentRepository`, `PaymentRepository` |
| **Adaptador JPA** (implementa puertos de salida) | `infrastructure/adapter/out/persistence/adapter/Jpa*Repository.java` |
| **Entidades JPA** (`@Entity`, `@ManyToOne`) | `infrastructure/adapter/out/persistence/entity/*Entity.java` |
| **Mappers** dominio ↔ entidad | `infrastructure/adapter/out/persistence/mapper/*EntityMapper.java` |
| **Adaptador web** (única entrada) | `infrastructure/adapter/in/web/*WebResource.java` |
| **Vistas HTML** (Qute + Bootstrap) | `src/main/resources/templates/*.html` |
| **Reportes PDF** (OpenPDF) | `infrastructure/adapter/in/web/PdfReports.java` |
| **Datos de prueba** | `infrastructure/config/DataSeeder.java` |
| **Configuración** (MySQL, Swagger) | `src/main/resources/application.properties` |

Regla de dependencias: `domain` no importa nada de `application` ni de `infrastructure` (ni Jakarta, ni Quarkus).
`application` solo conoce el dominio. `infrastructure` conoce a los dos. No hay adaptador REST ni de consola: la única entrada es la vista HTML.

## Base de datos (JPA + MySQL)

Base `gimnasio`, 3 tablas relacionadas:

```
plans (id PK, name, description, price, duration_days, active)
   ▲
   │ plan_id (FK, @ManyToOne)
enrollments (id PK, member_name, member_document, member_phone, plan_id FK, start_date, end_date, status)
   ▲
   │ enrollment_id (FK, @ManyToOne)
payments (id PK, enrollment_id FK, amount, payment_date, method)
```

**Evidencia en phpMyAdmin** (http://localhost/phpmyadmin):
1. Base `gimnasio` → ver las 3 tablas con sus datos (pestaña *Examinar*).
2. Tabla `enrollments` → pestaña **Estructura → Vista de relaciones** para las llaves foráneas, o
   base `gimnasio` → pestaña **Diseñador** para ver el diagrama con las relaciones.
3. Consultas listas para pegar en la pestaña *SQL*: `docs/consultas-phpmyadmin.sql`.

## Documentación Swagger

Con la aplicación corriendo: **/q/swagger-ui** (interfaz) y **/q/openapi** (documento). Los recursos que sirven las páginas
están agrupados con `@Tag` (Inscripciones, Planes, Reportes) y cada operación tiene `@Operation`.
Swagger no crea una API REST aparte: documenta las rutas que sirven las páginas y los PDF.

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Communications link failure` / no conecta a la base | MySQL apagado en XAMPP. Enciéndelo y reinicia `quarkus:dev`. |
| `Access denied for user 'root'` | Tu MySQL tiene contraseña: cámbiala en `quarkus.datasource.password`. |
| Puerto 8080 ocupado | Cambia `quarkus.http.port` en `application.properties`. |
| IntelliJ marca errores en rojo | *Reload All Maven Projects* y revisa que el SDK del proyecto sea 21. |
| Quiero los datos de prueba otra vez | Borra la base `gimnasio` en phpMyAdmin y reinicia. |
