package com.example.javatest.reserva.dto;

import com.example.javatest.reserva.EstadoReserva;

import java.time.LocalDateTime;

public class ReservaResponse {

    private Long id;
    private Long salaId;
    private String salaNombre;
    private String responsable;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private EstadoReserva estado;

    public ReservaResponse(Long id, Long salaId, String salaNombre, String responsable,
                           LocalDateTime inicio, LocalDateTime fin, EstadoReserva estado) {
        this.id = id;
        this.salaId = salaId;
        this.salaNombre = salaNombre;
        this.responsable = responsable;
        this.inicio = inicio;
        this.fin = fin;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Long getSalaId() {
        return salaId;
    }

    public String getSalaNombre() {
        return salaNombre;
    }

    public String getResponsable() {
        return responsable;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}
