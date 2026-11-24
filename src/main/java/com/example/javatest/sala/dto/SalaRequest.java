package com.example.javatest.sala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SalaRequest {

    @NotBlank(message = "El nombre de la sala es obligatorio")
    @Size(max = 100, message = "El nombre de la sala no debe superar 100 caracteres")
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
