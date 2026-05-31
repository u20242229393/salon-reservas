package co.edu.usco.reservas.service;

import co.edu.usco.reservas.entity.Servicio;
import co.edu.usco.reservas.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByActivoTrue();
    }

    public void desactivarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con el ID: " + id));

        servicio.setActivo(false); // Borrado lógico
        servicioRepository.save(servicio);
    }
}