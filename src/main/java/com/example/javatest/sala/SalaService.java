package com.example.javatest.sala;

import com.example.javatest.common.ConflictException;
import com.example.javatest.sala.dto.SalaRequest;
import com.example.javatest.sala.dto.SalaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Transactional
    public SalaResponse crear(SalaRequest request) {
        if (salaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ConflictException("Ya existe una sala con ese nombre");
        }
        Sala sala = new Sala(request.getNombre().trim());
        Sala guardada = salaRepository.save(sala);
        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<SalaResponse> listar() {
        return salaRepository.findAll().stream().map(this::toResponse).toList();
    }

    private SalaResponse toResponse(Sala sala) {
        return new SalaResponse(sala.getId(), sala.getNombre());
    }
}
