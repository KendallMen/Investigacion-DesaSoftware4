package cr.ac.ucr.ms_usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cr.ac.ucr.ms_usuarios.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCarnet(String carnet);

    boolean existsByCarnetAndIdNot(String carnet, Long id);
}
