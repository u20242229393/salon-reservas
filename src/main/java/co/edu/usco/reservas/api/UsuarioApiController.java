package co.edu.usco.reservas.api;

import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestion de clientas, especialistas y administradoras")
public class UsuarioApiController {

    @Autowired private UsuarioRepository usuarioRepository;

    @Operation(summary = "Listar todas las especialistas")
    @ApiResponse(responseCode = "200", description = "Lista de especialistas")
    @GetMapping("/especialistas")
    public ResponseEntity<List<Usuario>> listarEspecialistas() {
        return ResponseEntity.ok(usuarioRepository.findByRol("ROLE_PROFESIONAL"));
    }

    @Operation(summary = "Listar todas las clientas")
    @GetMapping("/clientas")
    public ResponseEntity<List<Usuario>> listarClientas() {
        return ResponseEntity.ok(usuarioRepository.findByRol("ROLE_CLIENTE"));
    }

    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        Optional<Usuario> u = usuarioRepository.findById(id);
        return u.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
