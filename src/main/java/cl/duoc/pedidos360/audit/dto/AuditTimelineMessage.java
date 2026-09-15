package cl.duoc.pedidos360.audit.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entrada de auditoría: quién / qué / cuándo / desde dónde.
 * Contrato definido; el consumidor aún no está implementado (fuera del alcance de la EP1).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuditTimelineMessage(String eventId, Instant occurredAt, Actor actor, String action, String summary,
                                   String entityType, String entityId, String source, String traceId,
                                   String correlationId) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Actor(String id, String name, String email, List<String> roles, String ip) {
    }
}
