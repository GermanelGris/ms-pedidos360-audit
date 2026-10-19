package cl.duoc.pedidos360.audit.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Evento del exchange orders.events (solo los campos que usa la auditoría): quién, qué, cuándo y sobre qué pedido. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEventMessage(String eventId, String type, Instant occurredAt, String traceId,
                                String correlationId, String source, Actor actor, String previousStatus,
                                OrderSnapshot order) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Actor(String id, String name, List<String> roles) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderSnapshot(Long id, String customerName, String status, BigDecimal total) {
    }
}
