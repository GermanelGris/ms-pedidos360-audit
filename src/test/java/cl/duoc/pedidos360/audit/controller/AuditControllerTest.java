package cl.duoc.pedidos360.audit.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void eventosConFiltrosAunNoImplementado() throws Exception {
        mvc.perform(get("/api/audit/events")
                        .param("user", "operador")
                        .param("type", "OrderAccepted")
                        .param("from", "2026-09-14")
                        .param("to", "2026-09-15"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void fechaInvalidaRespondeBadRequest() throws Exception {
        mvc.perform(get("/api/audit/events").param("from", "14-09-2026"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void timelineDePedidoAunNoImplementado() throws Exception {
        mvc.perform(get("/api/audit/orders/7/timeline")).andExpect(status().isNotImplemented());
    }
}
