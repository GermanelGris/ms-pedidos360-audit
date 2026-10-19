package cl.duoc.pedidos360.audit.controller;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.pedidos360.audit.dto.AuditEventResponse;
import cl.duoc.pedidos360.audit.service.AuditService;

/** API de solo lectura: no existen endpoints para crear, modificar ni borrar eventos de auditoría. */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Auditoría (solo lectura)")
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping("/events")
    @Operation(summary = "Timeline de eventos con filtros: usuario, tipo de evento, entidad y rango de fechas (yyyy-MM-dd)")
    public List<AuditEventResponse> events(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "100") int limit) {
        return service.search(user, type, entityId, from, to, limit);
    }

    @GetMapping("/orders/{orderId}/timeline")
    @Operation(summary = "Trazabilidad completa de un pedido, en orden cronológico")
    public List<AuditEventResponse> orderTimeline(@PathVariable Long orderId) {
        return service.orderTimeline(orderId);
    }
}
