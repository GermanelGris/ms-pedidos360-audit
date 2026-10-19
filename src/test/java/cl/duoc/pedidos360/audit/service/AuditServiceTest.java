package cl.duoc.pedidos360.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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

import cl.duoc.pedidos360.audit.messaging.NonRecoverableMessageException;
import cl.duoc.pedidos360.audit.messaging.OrderEventMessage;
import cl.duoc.pedidos360.audit.messaging.OrderEventMessage.Actor;
import cl.duoc.pedidos360.audit.messaging.OrderEventMessage.OrderSnapshot;
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

    private static OrderEventMessage statusChanged(String eventId) {
        return new OrderEventMessage(eventId, "OrderStatusChanged", Instant.parse("2026-09-14T14:05:00Z"),
                "trace-1", "order-7", "ms-pedidos360-orders",
                new Actor("operador-1", "Operador Uno", List.of("Operador")), "CREADO",
                new OrderSnapshot(7L, "Cliente Uno", "ACEPTADO", new BigDecimal("17980")));
    }

    @Test
    void registraQuienQueCuandoYSobreQuePedido() {
        given(repository.existsByEventId("evt-1")).willReturn(false);

        assertThat(service.record(statusChanged("evt-1"))).isTrue();

        ArgumentCaptor<AuditEvent> saved = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getActorName()).isEqualTo("Operador Uno");
        assertThat(saved.getValue().getActorRoles()).isEqualTo("Operador");
        assertThat(saved.getValue().getAction()).isEqualTo("OrderStatusChanged");
        assertThat(saved.getValue().getEntityType()).isEqualTo("ORDER");
        assertThat(saved.getValue().getEntityId()).isEqualTo("7");
        assertThat(saved.getValue().getOccurredAt()).isEqualTo(Instant.parse("2026-09-14T14:05:00Z"));
        assertThat(saved.getValue().getSummary()).isEqualTo("Pedido #7: CREADO -> ACEPTADO por Operador Uno");
    }

    @Test
    void resumenDeCreacionIncluyeElTotal() {
        OrderEventMessage created = new OrderEventMessage("evt-2", "OrderCreated", Instant.now(), null, null, null,
                new Actor("cliente-1", "Cliente Uno", List.of("Cliente")), null,
                new OrderSnapshot(8L, "Cliente Uno", "CREADO", new BigDecimal("8990")));

        assertThat(AuditService.summary(created)).isEqualTo("Pedido #8 creado (total $8990) por Cliente Uno");
    }

    @Test
    void eventoDuplicadoNoSeGuardaDosVeces() {
        given(repository.existsByEventId("evt-1")).willReturn(true);

        assertThat(service.record(statusChanged("evt-1"))).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    void eventoIncompletoEsNoRecuperableYVaALaDlq() {
        OrderEventMessage broken = new OrderEventMessage("evt-3", "OrderCreated", null, null, null, null, null, null,
                null);

        assertThatThrownBy(() -> service.record(broken)).isInstanceOf(NonRecoverableMessageException.class);
    }

    @Test
    void filtroDeFechasIncluyeElDiaCompleto() {
        given(repository.search(any(), any(), any(), any(), any(), any(Pageable.class))).willReturn(List.of());

        service.search("operador", "OrderStatusChanged", " ", LocalDate.parse("2026-09-14"),
                LocalDate.parse("2026-09-14"), 50);

        verify(repository).search(eq("operador"), eq("OrderStatusChanged"), isNull(),
                eq(Instant.parse("2026-09-14T00:00:00Z")), eq(Instant.parse("2026-09-15T00:00:00Z")),
                any(Pageable.class));
    }
}
