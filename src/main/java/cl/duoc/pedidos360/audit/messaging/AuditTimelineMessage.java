package cl.duoc.pedidos360.audit.messaging;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Registro del tópico audit.timeline: quién / qué / cuándo / desde dónde. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuditTimelineMessage(String eventId, Instant occurredAt, Actor actor, String action, String summary,
                                   String entityType, String entityId, String source, String traceId,
                                   String correlationId) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Actor(String id, String name, String email, List<String> roles, String ip) {
    }
}
