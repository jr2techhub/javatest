package com.example.javatest.sala.dto;

public class SalaResponse {

    private Long id;
    private String nombre;

    public SalaResponse(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
