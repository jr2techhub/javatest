package com.example.javatest.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    @Query("""
            SELECT COUNT(r) > 0 FROM Reserva r
            WHERE r.sala.id = :salaId
              AND r.estado = com.example.javatest.reserva.EstadoReserva.ACTIVA
              AND r.inicio < :fin
              AND r.fin > :inicio
            """)
    boolean existsOverlapping(@Param("salaId") Long salaId,
                              @Param("inicio") LocalDateTime inicio,
                              @Param("fin") LocalDateTime fin);
}
