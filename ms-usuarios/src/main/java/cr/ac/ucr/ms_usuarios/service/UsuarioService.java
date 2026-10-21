package cr.ac.ucr.ms_usuarios.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.ms_usuarios.excepcion.ApiException;
import cr.ac.ucr.ms_usuarios.model.Usuario;
import cr.ac.ucr.ms_usuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El usuario " + id + " no existe"));
    }

    @Transactional
    public Usuario crear(Usuario usuario) {
        if (repository.existsByCarnet(usuario.getCarnet())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con el carnet " + usuario.getCarnet());
        }
        usuario.setId(null); // el id lo genera la base
        return repository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario actual = obtener(id);
        if (repository.existsByCarnetAndIdNot(datos.getCarnet(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con el carnet " + datos.getCarnet());
        }
        actual.setNombre(datos.getNombre());
        actual.setCarnet(datos.getCarnet());
        actual.setCorreo(datos.getCorreo());
        actual.setCarrera(datos.getCarrera());
        return repository.save(actual);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }
}
