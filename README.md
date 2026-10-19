# ms-pedidos360-audit

Microservicio de **auditoría** de Pedidos360: timeline de eventos de negocio, de solo lectura, con quién / qué / cuándo / sobre qué pedido.

Se alimenta de forma **asíncrona** con los eventos de dominio que publica `ms-pedidos360-orders` en RabbitMQ y los guarda como registros inmutables (schema `pedidos360_audit`). No existen endpoints para crear, modificar ni borrar eventos.

## Topología RabbitMQ

Todos los nombres viven en `application.yml` (`pedidos360.rabbitmq.*`) y se leen con `RabbitMessagingProperties`. Los beans `Queue` / `Exchange` / `Binding` están en `config/RabbitTopologyConfig`.

| Exchange | Tipo | Función |
|---|---|---|
| `orders.events` | topic | Eventos de dominio de pedidos. Lo comparte con `ms-pedidos360-report`: cada suscriptor tiene su propia cola |
| `audit.retry` | direct | Recibe los mensajes que se reintentan |
| `audit.dlx` | direct | Dead-letter exchange: los NACK de la cola principal llegan a la DLQ |

| Cola | Binding | Función |
|---|---|---|
| `q.audit.orders` | `orders.events` · `order.#` | Cola principal (todo evento de pedidos, incluidos los que se agreguen a futuro); la consume `OrderEventsListener` |
| `q.audit.orders.retry` | `audit.retry` · `q.audit.orders` | Espera 5 s (TTL) y devuelve el mensaje **solo** a `q.audit.orders` por el exchange por defecto |
| `q.audit.orders.dlq` | `audit.dlx` · `q.audit.orders` | Mensajes que no se pudieron registrar |

## Confirmación y manejo de errores

`acknowledge-mode: manual`, con la política en `messaging/EventAcknowledger`:

| Situación | Tipo | Acción |
|---|---|---|
| Evento registrado | — | `basicAck` |
| `eventId` ya registrado (columna única) | duplicado | `basicAck` sin duplicar el registro |
| JSON inválido, sin `eventId` o sin pedido | **no recuperable** | `basicNack(requeue=false)`: DLQ inmediata |
| Otra excepción (p. ej. la base de datos no responde) | **recuperable** | Se republica en `q.audit.orders.retry` con `x-retry-count` y `basicAck` del original |
| Falla recuperable en el último intento (`max-attempts: 3`) | agotado | `basicNack(requeue=false)`: DLQ |

Nunca se usa `requeue=true`. Cada envío a la DLQ queda en el log con nivel `ERROR`.

Cada evento se guarda con el actor (id, nombre y roles, propagados por el BFF en los headers `X-User-*`), el tipo (`OrderCreated`, `OrderStatusChanged`), el pedido y un resumen legible, por ejemplo `Pedido #7: CREADO -> ACEPTADO por Operador Uno`.

## API (solo lectura)

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/audit/events?user=&type=&entityId=&from=yyyy-MM-dd&to=yyyy-MM-dd&limit=100` | Eventos filtrados, del más reciente al más antiguo |
| GET | `/api/audit/orders/{orderId}/timeline` | Trazabilidad completa de un pedido, en orden cronológico |

Desde el front se consume a través del BFF (`/api/audit/**`, solo rol Admin). Swagger: `http://localhost:8085/swagger-ui.html`.

## Variables de entorno

| Variable | Por defecto |
|---|---|
| `DB_HOST`, `DB_USER`, `DB_PASSWORD` | `localhost`, `root`, `root` |
| `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | `localhost`, `pedidos360`, `pedidos360` |
| `AUDIT_ZONE` | `America/Santiago` |

Puerto: `8085`.

## Autores

Germán Maraboli & Camila Vera

Proyecto Pedidos360 · DSY1107 Desarrollo Cloud Native I · Duoc UC
