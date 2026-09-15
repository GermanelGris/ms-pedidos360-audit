package cl.duoc.pedidos360.audit.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import cl.duoc.pedidos360.audit.config.KafkaConfig;
import cl.duoc.pedidos360.audit.service.AuditService;

@Component
public class AuditTimelineListener {

    private static final Logger log = LoggerFactory.getLogger(AuditTimelineListener.class);

    private final ObjectMapper mapper;
    private final AuditService auditService;

    public AuditTimelineListener(ObjectMapper mapper, AuditService auditService) {
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @KafkaListener(topics = KafkaConfig.AUDIT_TIMELINE)
    public void onAuditEntry(String payload) {
        AuditTimelineMessage entry = mapper.readValue(payload, AuditTimelineMessage.class);
        boolean stored = auditService.record(entry);
        log.info("Auditoría {}: {}", stored ? "registrada" : "duplicada (ignorada)", entry.summary());
    }
}
