package cl.duoc.pedidos360.audit.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Evento de auditoría inmutable (el servicio solo inserta y consulta). */
@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(length = 100)
    private String actorId;

    @Column(length = 150)
    private String actorName;

    @Column(length = 150)
    private String actorEmail;

    @Column(length = 200)
    private String actorRoles;

    @Column(length = 64)
    private String sourceIp;

    @Column(length = 80)
    private String service;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(length = 30)
    private String entityType;

    @Column(length = 64)
    private String entityId;

    @Column(length = 500)
    private String summary;

    @Column(length = 64)
    private String traceId;

    @Column(length = 80)
    private String correlationId;

    @Column(nullable = false)
    private Instant receivedAt;

    protected AuditEvent() {
    }

    public AuditEvent(String eventId, Instant occurredAt, String action, Instant receivedAt) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.action = action;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getActorRoles() {
        return actorRoles;
    }

    public void setActorRoles(String actorRoles) {
        this.actorRoles = actorRoles;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
