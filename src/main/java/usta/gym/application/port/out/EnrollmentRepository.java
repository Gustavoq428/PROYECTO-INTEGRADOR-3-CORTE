package usta.gym.application.port.out;

import usta.gym.domain.model.Enrollment;

import java.util.List;
import java.util.Optional;

// Puerto de salida: guardar y consultar inscripciones
public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    List<Enrollment> findAll();

    Optional<Enrollment> findById(Long id);

    // Inscripciones (de cualquier estado) de un socio, buscadas por su documento
    List<Enrollment> findByMemberDocument(String document);
}
