package com.example.javatest.reserva;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void flujoReservasConValidacionConflictoSeguridadYListado() throws Exception {
        String nombreSala = "Sala-" + UUID.randomUUID();
        long salaId = crearSala(nombreSala);

        String reservaValida = """
                {
                  "salaId": %d,
                  "responsable": "Ana",
                  "inicio": "2030-01-10T10:00:00",
                  "fin": "2030-01-10T11:00:00"
                }
                """.formatted(salaId);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaValida))
                .andExpect(status().isUnauthorized());

        MvcResult creada = mockMvc.perform(post("/reservas")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaValida))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.salaId").value(salaId))
                .andExpect(jsonPath("$.responsable").value("Ana"))
                .andReturn();

        long reservaId = objectMapper.readTree(creada.getResponse().getContentAsString()).get("id").asLong();

        String reservaSolapada = """
                {
                  "salaId": %d,
                  "responsable": "Luis",
                  "inicio": "2030-01-10T10:30:00",
                  "fin": "2030-01-10T11:30:00"
                }
                """.formatted(salaId);

        mockMvc.perform(post("/reservas")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaSolapada))
                .andExpect(status().isConflict());

        String reservaInvalida = """
                {
                  "salaId": %d,
                  "responsable": "",
                  "inicio": "2030-01-10T12:00:00",
                  "fin": "2030-01-10T11:00:00"
                }
                """.formatted(salaId);

        mockMvc.perform(post("/reservas")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isArray());

        mockMvc.perform(get("/reservas")
                        .param("salaId", String.valueOf(salaId))
                        .param("fecha", "2030-01-10")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem((int) reservaId)));

        mockMvc.perform(delete("/reservas/{id}", reservaId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/reservas/{id}", reservaId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    private long crearSala(String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/salas")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}
