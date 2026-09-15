package cl.duoc.pedidos360.audit.dto;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import cl.duoc.pedidos360.audit.model.AuditEvent;

public record AuditEventResponse(String eventId, Instant occurredAt, String actorId, String actorName,
                                 String actorEmail, List<String> actorRoles, String sourceIp, String service,
                                 String action, String entityType, String entityId, String summary,
                                 String traceId, String correlationId) {

    public static AuditEventResponse from(AuditEvent event) {
        List<String> roles = event.getActorRoles() == null || event.getActorRoles().isBlank()
                ? List.of()
                : Arrays.asList(event.getActorRoles().split(","));
        return new AuditEventResponse(event.getEventId(), event.getOccurredAt(), event.getActorId(),
                event.getActorName(), event.getActorEmail(), roles, event.getSourceIp(), event.getService(),
                event.getAction(), event.getEntityType(), event.getEntityId(), event.getSummary(),
                event.getTraceId(), event.getCorrelationId());
    }
}
