package com.example.javatest.reserva;

import com.example.javatest.sala.Sala;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReservaSpecifications {

    private ReservaSpecifications() {
    }

    public static Specification<Reserva> salaIdEquals(Long salaId) {
        return (root, query, cb) -> salaId == null ? null : cb.equal(root.get("sala").get("id"), salaId);
    }

    public static Specification<Reserva> fechaOverlaps(LocalDate fecha) {
        return (root, query, cb) -> {
            if (fecha == null) {
                return null;
            }
            LocalDateTime startOfDay = fecha.atStartOfDay();
            LocalDateTime endOfDay = fecha.plusDays(1).atStartOfDay();
            return cb.and(
                    cb.lessThan(root.get("inicio"), endOfDay),
                    cb.greaterThan(root.get("fin"), startOfDay)
            );
        };
    }
}
