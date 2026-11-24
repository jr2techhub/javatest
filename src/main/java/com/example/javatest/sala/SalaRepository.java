package com.example.javatest.sala;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
