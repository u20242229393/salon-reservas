package co.edu.usco.reservas.api;

import co.edu.usco.reservas.entity.Reserva;
import co.edu.usco.reservas.entity.Usuario;
import co.edu.usco.reservas.repository.ReservaRepository;
import co.edu.usco.reservas.repository.UsuarioRepository;
import co.edu.usco.reservas.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Gestion de citas y reservas del salon")
public class ReservaApiController {

    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ReservaService reservaService;

    @Operation(summary = "Listar todas las reservas")
    @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<Reserva>> listarTodas() {
        return ResponseEntity.ok(reservaRepository.findAll());
    }

    @Operation(summary = "Obtener una reserva por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerPorId(
            @Parameter(description = "ID de la reserva") @PathVariable Long id) {
        Optional<Reserva> r = reservaRepository.findById(id);
        return r.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener reservas de una clienta")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Reserva>> porCliente(
            @Parameter(description = "ID de la clienta") @PathVariable Long clienteId) {
        return ResponseEntity.ok(reservaService.listarHistorialCliente(clienteId, null));
    }

    @Operation(summary = "Obtener agenda de una especialista")
    @GetMapping("/especialista/{profId}")
    public ResponseEntity<List<Reserva>> porProfesional(
            @Parameter(description = "ID de la especialista") @PathVariable Long profId) {
        return ResponseEntity.ok(reservaService.obtenerAgendaPorProfesional(profId));
    }

    @Operation(summary = "Cancelar una reserva")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva cancelada"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelar(
            @Parameter(description = "ID de la reserva") @PathVariable Long id) {
        boolean ok = reservaService.cambiarEstadoReserva(id, "CANCELADO");
        return ok ? ResponseEntity.ok("Reserva cancelada") : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Marcar reserva como realizada")
    @PatchMapping("/{id}/realizado")
    public ResponseEntity<String> marcarRealizado(
            @Parameter(description = "ID de la reserva") @PathVariable Long id) {
        boolean ok = reservaService.cambiarEstadoReserva(id, "REALIZADO");
        return ok ? ResponseEntity.ok("Reserva marcada como realizada") : ResponseEntity.notFound().build();
    }
}
