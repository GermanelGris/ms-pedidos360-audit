package cl.duoc.pedidos360.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import cl.duoc.pedidos360.audit.messaging.AuditTimelineMessage;
import cl.duoc.pedidos360.audit.messaging.AuditTimelineMessage.Actor;
import cl.duoc.pedidos360.audit.model.AuditEvent;
import cl.duoc.pedidos360.audit.repository.AuditEventRepository;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository repository;

    private AuditService service;

    @BeforeEach
    void setUp() {
        service = new AuditService(repository, "UTC");
    }

    private static AuditTimelineMessage entry(String eventId) {
        return new AuditTimelineMessage(eventId, Instant.parse("2026-09-14T14:05:00Z"),
                new Actor("operador-1", "Operador Uno", "operador1@pedidos360.onmicrosoft.com", List.of("Operador"),
                        "190.1.2.3"),
                "OrderAccepted", "Pedido #7: CREADO -> ACEPTADO por Operador Uno", "ORDER", "7",
                "ms-pedidos360-orders", "trace-1", "order-7");
    }

    @Test
    void registraQuienQueCuandoYDesdeDonde() {
        given(repository.existsByEventId("evt-1")).willReturn(false);

        assertThat(service.record(entry("evt-1"))).isTrue();

        ArgumentCaptor<AuditEvent> saved = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getActorName()).isEqualTo("Operador Uno");
        assertThat(saved.getValue().getAction()).isEqualTo("OrderAccepted");
        assertThat(saved.getValue().getOccurredAt()).isEqualTo(Instant.parse("2026-09-14T14:05:00Z"));
        assertThat(saved.getValue().getSourceIp()).isEqualTo("190.1.2.3");
        assertThat(saved.getValue().getActorRoles()).isEqualTo("Operador");
    }

    @Test
    void entradaDuplicadaNoSeGuardaDosVeces() {
        given(repository.existsByEventId("evt-1")).willReturn(true);

        assertThat(service.record(entry("evt-1"))).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void entradaIncompletaLanzaErrorParaIrALaDlt() {
        AuditTimelineMessage broken = new AuditTimelineMessage(null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.record(broken)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void filtroDeFechasIncluyeElDiaCompleto() {
        given(repository.search(any(), any(), any(), any(), any(), any(Pageable.class))).willReturn(List.of());

        service.search("operador", "OrderAccepted", " ", LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-14"), 50);

        verify(repository).search(eq("operador"), eq("OrderAccepted"), isNull(),
                eq(Instant.parse("2026-09-14T00:00:00Z")), eq(Instant.parse("2026-09-15T00:00:00Z")),
                any(Pageable.class));
    }
}
