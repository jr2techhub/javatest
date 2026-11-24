package com.example.javatest.reserva;

import com.example.javatest.common.BadRequestException;
import com.example.javatest.common.ConflictException;
import com.example.javatest.reserva.dto.ReservaRequest;
import com.example.javatest.sala.Sala;
import com.example.javatest.sala.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SalaRepository salaRepository;

    private ReservaService reservaService;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(reservaRepository, salaRepository);
    }

    @Test
    void crearDebeFallarCuandoFinNoEsPosteriorAlInicio() {
        ReservaRequest request = new ReservaRequest();
        request.setSalaId(1L);
        request.setResponsable("Ana");
        request.setInicio(LocalDateTime.of(2026, 11, 24, 10, 0));
        request.setFin(LocalDateTime.of(2026, 11, 24, 10, 0));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reservaService.crear(request));
        assertEquals("La fecha y hora de fin debe ser posterior al inicio", ex.getMessage());
    }

    @Test
    void crearDebeFallarConConflictoCuandoSalaEstaOcupada() {
        ReservaRequest request = new ReservaRequest();
        request.setSalaId(1L);
        request.setResponsable("Ana");
        request.setInicio(LocalDateTime.of(2026, 11, 24, 10, 0));
        request.setFin(LocalDateTime.of(2026, 11, 24, 11, 0));

        Sala sala = new Sala("Sala 1");
        sala.setId(1L);
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(reservaRepository.existsOverlapping(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> reservaService.crear(request));
        assertEquals("La sala ya esta ocupada en ese horario", ex.getMessage());
    }
}
