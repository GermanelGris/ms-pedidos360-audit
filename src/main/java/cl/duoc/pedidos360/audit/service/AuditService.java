package cl.duoc.pedidos360.audit.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.pedidos360.audit.dto.AuditEventResponse;
import cl.duoc.pedidos360.audit.messaging.NonRecoverableMessageException;
import cl.duoc.pedidos360.audit.messaging.OrderEventMessage;
import cl.duoc.pedidos360.audit.model.AuditEvent;
import cl.duoc.pedidos360.audit.repository.AuditEventRepository;

@Service
public class AuditService {

    static final String ORDER = "ORDER";

    private final AuditEventRepository repository;
    private final ZoneId zone;

    public AuditService(AuditEventRepository repository, @Value("${audit.zone}") String zone) {
        this.repository = repository;
        this.zone = ZoneId.of(zone);
    }

    /** Registra un evento de pedido en el timeline. Idempotente por eventId: devuelve false si ya existía. */
    @Transactional
    public boolean record(OrderEventMessage entry) {
        if (entry.eventId() == null || entry.type() == null || entry.order() == null || entry.order().id() == null) {
            // Datos que nunca podrán registrarse: el consumidor lo envía a la DLQ sin reintentar
            throw new NonRecoverableMessageException("Evento de pedido incompleto: " + entry.eventId());
        }
        if (repository.existsByEventId(entry.eventId())) {
            return false;
        }
        Instant occurredAt = entry.occurredAt() != null ? entry.occurredAt() : Instant.now();
        AuditEvent event = new AuditEvent(entry.eventId(), occurredAt, entry.type(), Instant.now());
        if (entry.actor() != null) {
            event.setActorId(entry.actor().id());
            event.setActorName(entry.actor().name());
            event.setActorRoles(entry.actor().roles() == null ? null : String.join(",", entry.actor().roles()));
        }
        event.setService(entry.source());
        event.setEntityType(ORDER);
        event.setEntityId(String.valueOf(entry.order().id()));
        event.setSummary(summary(entry));
        event.setTraceId(entry.traceId());
        event.setCorrelationId(entry.correlationId());
        repository.save(event);
        return true;
    }

    /** Texto legible del evento, p. ej. "Pedido #7: CREADO -> ACEPTADO por Operador Uno". */
    static String summary(OrderEventMessage entry) {
        String who = entry.actor() != null && entry.actor().name() != null ? " por " + entry.actor().name() : "";
        OrderEventMessage.OrderSnapshot order = entry.order();
        if (entry.previousStatus() == null) {
            String total = order.total() != null ? " (total $" + order.total().toPlainString() + ")" : "";
            return "Pedido #%d creado%s%s".formatted(order.id(), total, who);
        }
        return "Pedido #%d: %s -> %s%s".formatted(order.id(), entry.previousStatus(), order.status(), who);
    }

    /** Búsqueda con filtros opcionales: usuario, tipo de evento, entidad y rango de fechas (inclusive). */
    @Transactional(readOnly = true)
    public List<AuditEventResponse> search(String user, String type, String entityId, LocalDate from, LocalDate to,
                                           int limit) {
        Instant fromInstant = from == null ? null : from.atStartOfDay(zone).toInstant();
        Instant toInstant = to == null ? null : to.plusDays(1).atStartOfDay(zone).toInstant();
        return repository.search(blankToNull(user), blankToNull(type), blankToNull(entityId), fromInstant, toInstant,
                        PageRequest.of(0, Math.max(1, Math.min(limit, 500))))
                .stream()
                .map(AuditEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> orderTimeline(Long orderId) {
        return repository.findByEntityTypeAndEntityIdOrderByOccurredAtAsc(ORDER, String.valueOf(orderId))
                .stream()
                .map(AuditEventResponse::from)
                .toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
