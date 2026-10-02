package com.elmandado.repositorio;

import java.util.List;
import java.util.Optional;

import com.elmandado.modelo.Pedido;

public interface PedidoRepository {

    List<Pedido> buscarTodos();

    Optional<Pedido> buscarPorId(Long id);

    Pedido guardar(Pedido pedido);

    void eliminar(Long id);

    boolean existePorId(Long id);
}