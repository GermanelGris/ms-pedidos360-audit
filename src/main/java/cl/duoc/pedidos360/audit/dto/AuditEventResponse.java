package cl.duoc.pedidos360.audit.dto;

import java.time.Instant;
import java.util.List;

/** Respuesta de la futura API de auditoría (solo lectura). */
public record AuditEventResponse(String eventId, Instant occurredAt, String actorId, String actorName,
                                 String actorEmail, List<String> actorRoles, String sourceIp, String service,
                                 String action, String entityType, String entityId, String summary,
                                 String traceId, String correlationId) {
}
