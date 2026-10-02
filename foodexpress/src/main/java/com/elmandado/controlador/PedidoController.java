package com.elmandado.controlador;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.elmandado.dto.CrearPedidoDTO;
import com.elmandado.dto.PedidoDTO;
import com.elmandado.servicio.PedidoService;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PedidoDTO> listar() {
        return servicio.listarTodos();
    }

    @GetMapping("/{id}")
    public PedidoDTO obtenerPorId(@PathVariable Long id) {
        return servicio.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDTO crear(@Valid @RequestBody CrearPedidoDTO dto) {
        return servicio.crear(dto);
    }

    @PutMapping("/{id}")
    public PedidoDTO actualizar(@PathVariable Long id, @Valid @RequestBody CrearPedidoDTO dto) {
        return servicio.actualizar(id, dto);
    }

    @PutMapping("/{id}/entregar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarEntregado(@PathVariable Long id) {
        servicio.marcarEntregado(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}

// Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos" -Method Post
// -ContentType "application/json" -Body '{"cliente":"Carlos", "plato":"Pizza
// Pepperoni", "precio":12.50}'
// Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos" -Method Post
// -ContentType "application/json" -Body '{"cliente":"Ana", "plato":"Hamburguesa
// Doble", "precio":9.80}'
// Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos" -Method Post
// -ContentType "application/json" -Body '{"cliente":"Luis", "plato":"Tacos al
// Pastor", "precio":8.50}'