# ms-pedidos360-audit

Microservicio de **auditoría** de Pedidos360. Consume el tópico de Kafka `audit.timeline` y guarda una línea de tiempo inmutable con **quién / qué / cuándo / desde dónde**.

## Endpoints (solo lectura)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/audit/events?user=&type=&entityId=&from=yyyy-MM-dd&to=yyyy-MM-dd&limit=100` | Timeline con filtros por usuario, tipo de evento, entidad y rango de fechas |
| GET | `/api/audit/orders/{orderId}/timeline` | Trazabilidad completa de un pedido, en orden cronológico |

No existen endpoints para crear, modificar ni borrar eventos: la auditoría es de solo lectura.

Swagger: `http://localhost:8085/swagger-ui.html`

## Kafka

| Tópico | Particiones | Política | Retención |
|---|---|---|---|
| `audit.timeline` | 3 | compact,delete | 30 días |
| `audit.timeline.audit.DLT` | 3 | delete | 14 días |

- **Idempotencia:** `eventId` único.
- **Errores:** se reintenta 2 veces y luego se publica en la **DLT** con los metadatos del error.

## Datos registrados

| Campo | Contenido |
|---|---|
| Quién | `actorId`, `actorName`, `actorEmail`, `actorRoles` |
| Qué | `action` (OrderCreated, OrderAccepted...), `summary`, `entityType`, `entityId` |
| Cuándo | `occurredAt` |
| Desde dónde | `sourceIp` (IP del cliente vía API Gateway/BFF), `service` |
| Trazabilidad | `traceId`, `correlationId` |

| Variable | Por defecto |
|---|---|
| `DB_HOST` / `DB_USER` / `DB_PASSWORD` | `localhost` / `root` / `root` |
| `KAFKA_BOOTSTRAP` | `localhost:9092` |
| `AUDIT_ZONE` | `America/Santiago` |

## Ejecutar

```bash
./mvnw test
./mvnw spring-boot:run
```

## Autores

Germán Maraboli & Camila Vera

Proyecto Pedidos360 · DSY1107 Desarrollo Cloud Native I · Duoc UC
