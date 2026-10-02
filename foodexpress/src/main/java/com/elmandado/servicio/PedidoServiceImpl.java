package com.elmandado.servicio;

import com.elmandado.dto.CrearPedidoDTO;
import com.elmandado.dto.PedidoDTO;
import com.elmandado.exepciones.RecursoNoEncontradoException;
import com.elmandado.exepciones.ValidacionException;
import com.elmandado.modelo.Pedido;
import com.elmandado.repositorio.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repositorio;

    public PedidoServiceImpl(PedidoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<PedidoDTO> listarTodos() {
        return repositorio.buscarTodos().stream()
                .map(p -> new PedidoDTO(p.getId(), p.getCliente(), p.getPlato(), p.getPrecio(), p.isEntregado()))
                .toList();
    }

    @Override
    public PedidoDTO obtenerPorId(Long id) {
        Pedido p = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));
        return new PedidoDTO(p.getId(), p.getCliente(), p.getPlato(), p.getPrecio(), p.isEntregado());
    }

    @Override
    public PedidoDTO crear(CrearPedidoDTO dto) {
        // Regla opcional de negocio: validar que el DTO no llegue nulo
        if (dto == null) {
            throw new ValidacionException("Los datos del pedido son obligatorios");
        }

        Pedido pedido = new Pedido(dto.cliente(), dto.plato(), dto.precio());
        Pedido guardado = repositorio.guardar(pedido);
        return new PedidoDTO(
                guardado.getId(),
                guardado.getCliente(),
                guardado.getPlato(),
                guardado.getPrecio(),
                guardado.isEntregado());
    }

    @Override
    public PedidoDTO actualizar(Long id, CrearPedidoDTO dto) {
        Pedido pedido = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));

        pedido.setCliente(dto.cliente());
        pedido.setPlato(dto.plato());
        pedido.setPrecio(dto.precio());

        Pedido actualizado = repositorio.guardar(pedido);
        return new PedidoDTO(
                actualizado.getId(),
                actualizado.getCliente(),
                actualizado.getPlato(),
                actualizado.getPrecio(),
                actualizado.isEntregado());
    }

    @Override
    public void marcarEntregado(Long id) {
        Pedido pedido = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));

        // Ejemplo de regla de negocio con ValidacionException
        if (pedido.isEntregado()) {
            throw new ValidacionException("El pedido con ID " + id + " ya había sido marcado como entregado");
        }

        pedido.setEntregado(true);
        repositorio.guardar(pedido);
    }

    @Override
    public void eliminar(Long id) {
        if (!repositorio.existePorId(id)) {
            throw new RecursoNoEncontradoException("No existe el pedido con ID: " + id);
        }
        repositorio.eliminar(id);
    }
}