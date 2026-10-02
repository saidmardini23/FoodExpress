package com.elmandado.repositorio;

import org.springframework.stereotype.Repository;

import com.elmandado.modelo.Pedido;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PedidoRepositoryMemoria implements PedidoRepository {

    private final Map<Long, Pedido> mapa = new ConcurrentHashMap<>();
    private final AtomicLong secuenciador = new AtomicLong(1);

    @Override
    public List<Pedido> buscarTodos() {
        return List.copyOf(mapa.values());
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return Optional.ofNullable(mapa.get(id));
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(secuenciador.getAndIncrement());
        }
        // Se o ID já existir, substitui no mapa (Update); caso contrário, insere
        // (Create)
        mapa.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public void eliminar(Long id) {
        mapa.remove(id);
    }

    @Override
    public boolean existePorId(Long id) {
        return mapa.containsKey(id);
    }
}