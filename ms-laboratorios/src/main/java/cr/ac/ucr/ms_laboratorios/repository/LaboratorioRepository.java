package cr.ac.ucr.ms_laboratorios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cr.ac.ucr.ms_laboratorios.model.Laboratorio;

public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);
}
