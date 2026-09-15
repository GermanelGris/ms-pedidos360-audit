package cl.duoc.pedidos360.audit.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.pedidos360.audit.dto.AuditEventResponse;

/**
 * API de auditoría (solo lectura). Contrato definido; aún sin implementación:
 * todos los endpoints responden 501 Not Implemented (fuera del alcance de la EP1).
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @GetMapping("/events")
    public ResponseEntity<List<AuditEventResponse>> events(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "100") int limit) {
        return notImplemented();
    }

    @GetMapping("/orders/{orderId}/timeline")
    public ResponseEntity<List<AuditEventResponse>> orderTimeline(@PathVariable Long orderId) {
        return notImplemented();
    }

    private static <T> ResponseEntity<T> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
