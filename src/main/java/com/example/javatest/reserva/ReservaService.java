package com.example.javatest.reserva;

import com.example.javatest.common.BadRequestException;
import com.example.javatest.common.ConflictException;
import com.example.javatest.common.NotFoundException;
import com.example.javatest.reserva.dto.ReservaRequest;
import com.example.javatest.reserva.dto.ReservaResponse;
import com.example.javatest.sala.Sala;
import com.example.javatest.sala.SalaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.javatest.reserva.ReservaSpecifications.fechaOverlaps;
import static com.example.javatest.reserva.ReservaSpecifications.salaIdEquals;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaRepository salaRepository;

    public ReservaService(ReservaRepository reservaRepository, SalaRepository salaRepository) {
        this.reservaRepository = reservaRepository;
        this.salaRepository = salaRepository;
    }

    @Transactional
    public ReservaResponse crear(ReservaRequest request) {
        if (!request.getFin().isAfter(request.getInicio())) {
            throw new BadRequestException("La fecha y hora de fin debe ser posterior al inicio");
        }

        Sala sala = salaRepository.findById(request.getSalaId())
                .orElseThrow(() -> new NotFoundException("La sala indicada no existe"));

        boolean ocupada = reservaRepository.existsOverlapping(sala.getId(), request.getInicio(), request.getFin());
        if (ocupada) {
            throw new ConflictException("La sala ya esta ocupada en ese horario");
        }

        Reserva reserva = new Reserva();
        reserva.setSala(sala);
        reserva.setResponsable(request.getResponsable().trim());
        reserva.setInicio(request.getInicio());
        reserva.setFin(request.getFin());
        reserva.setEstado(EstadoReserva.ACTIVA);

        return toResponse(reservaRepository.save(reserva));
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponse> listar(Long salaId, LocalDate fecha, Pageable pageable) {
        Specification<Reserva> spec = Specification.where(salaIdEquals(salaId)).and(fechaOverlaps(fecha));
        return reservaRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public void cancelar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La reserva indicada no existe"));
        if (reserva.getEstado() == EstadoReserva.ACTIVA) {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaRepository.save(reserva);
        }
    }

    private ReservaResponse toResponse(Reserva reserva) {
        Sala sala = reserva.getSala();
        return new ReservaResponse(
                reserva.getId(),
                sala.getId(),
                sala.getNombre(),
                reserva.getResponsable(),
                reserva.getInicio(),
                reserva.getFin(),
                reserva.getEstado()
        );
    }
}
