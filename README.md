# ms-pedidos360-audit

> 🚧 **En construcción.** Fuera del alcance de la EP1: por ahora solo contiene los DTO y el controller. Los endpoints responden `501 Not Implemented`.

Microservicio de **auditoría** de Pedidos360: timeline de eventos de negocio, de solo lectura, con quién / qué / cuándo / desde dónde.

## Contenido actual

| Tipo | Clase | Descripción |
|---|---|---|
| Controller | `controller/AuditController` | Define la API de solo lectura; aún sin implementación (501) |
| DTO | `dto/AuditEventResponse` | Evento de auditoría devuelto por la API |
| DTO | `dto/AuditTimelineMessage` | Entrada de auditoría: actor (id, nombre, email, roles, IP), acción, entidad y trazabilidad |

## Endpoints definidos

| Método | Ruta | Respuesta futura |
|---|---|---|
| GET | `/api/audit/events?user=&type=&entityId=&from=yyyy-MM-dd&to=yyyy-MM-dd&limit=100` | `List<AuditEventResponse>` |
| GET | `/api/audit/orders/{orderId}/timeline` | `List<AuditEventResponse>` |

Puerto reservado: `8085`.

## Autores

Germán Maraboli & Camila Vera

Proyecto Pedidos360 · DSY1107 Desarrollo Cloud Native I · Duoc UC
