package com.example.javatest.reserva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ReservaRequest {

    @NotNull(message = "El id de la sala es obligatorio")
    private Long salaId;

    @NotBlank(message = "El responsable es obligatorio")
    @Size(max = 100, message = "El responsable no debe superar 100 caracteres")
    private String responsable;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    private LocalDateTime inicio;

    @NotNull(message = "La fecha y hora de fin son obligatorias")
    private LocalDateTime fin;

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }
}
