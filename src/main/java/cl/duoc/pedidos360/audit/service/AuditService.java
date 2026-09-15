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
import cl.duoc.pedidos360.audit.messaging.AuditTimelineMessage;
import cl.duoc.pedidos360.audit.model.AuditEvent;
import cl.duoc.pedidos360.audit.repository.AuditEventRepository;

@Service
public class AuditService {

    private final AuditEventRepository repository;
    private final ZoneId zone;

    public AuditService(AuditEventRepository repository, @Value("${audit.zone}") String zone) {
        this.repository = repository;
        this.zone = ZoneId.of(zone);
    }

    /** Persiste la entrada de auditoría. Idempotente por eventId. */
    @Transactional
    public boolean record(AuditTimelineMessage entry) {
        if (entry.eventId() == null || entry.action() == null || entry.occurredAt() == null) {
            throw new IllegalArgumentException("Entrada de auditoría incompleta: " + entry.eventId());
        }
        if (repository.existsByEventId(entry.eventId())) {
            return false;
        }
        AuditEvent event = new AuditEvent(entry.eventId(), entry.occurredAt(), entry.action(), Instant.now());
        if (entry.actor() != null) {
            event.setActorId(entry.actor().id());
            event.setActorName(entry.actor().name());
            event.setActorEmail(entry.actor().email());
            event.setActorRoles(entry.actor().roles() == null ? null : String.join(",", entry.actor().roles()));
            event.setSourceIp(entry.actor().ip());
        }
        event.setService(entry.source());
        event.setEntityType(entry.entityType());
        event.setEntityId(entry.entityId());
        event.setSummary(entry.summary());
        event.setTraceId(entry.traceId());
        event.setCorrelationId(entry.correlationId());
        repository.save(event);
        return true;
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
        return repository.findByEntityTypeAndEntityIdOrderByOccurredAtAsc("ORDER", String.valueOf(orderId))
                .stream()
                .map(AuditEventResponse::from)
                .toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
