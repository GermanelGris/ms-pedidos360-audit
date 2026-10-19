package cl.duoc.pedidos360.audit.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.pedidos360.audit.model.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    boolean existsByEventId(String eventId);

    List<AuditEvent> findByEntityTypeAndEntityIdOrderByOccurredAtAsc(String entityType, String entityId);

    @Query("""
            select a from AuditEvent a
            where (:user is null or a.actorId = :user
                   or lower(a.actorName) like lower(concat('%', :user, '%'))
                   or lower(a.actorEmail) like lower(concat('%', :user, '%')))
              and (:action is null or a.action = :action)
              and (:entityId is null or a.entityId = :entityId)
              and (:from is null or a.occurredAt >= :from)
              and (:to is null or a.occurredAt < :to)
            order by a.occurredAt desc
            """)
    List<AuditEvent> search(@Param("user") String user, @Param("action") String action,
                            @Param("entityId") String entityId, @Param("from") Instant from,
                            @Param("to") Instant to, Pageable pageable);
}
