package com.elmandado.controlador;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.elmandado.dto.CrearPedidoDTO;
import com.elmandado.dto.PedidoDTO;
import com.elmandado.servicio.PedidoService;

@Controller
@RequestMapping("/pedidos")
public class PedidoViewController {

    private final PedidoService servicio;

    public PedidoViewController(PedidoService servicio) {
        this.servicio = servicio;
    }

    // 1. GET /pedidos -> Lista todos los pedidos en la tabla
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", servicio.listarTodos());
        return "pedidos/lista";
    }

    // 2. GET /pedidos/nuevo -> Muestra formulario vacío
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("nuevoPedido", new CrearPedidoDTO("", "", null));
        model.addAttribute("esEdicion", false);
        return "pedidos/formulario";
    }

    // 3. POST /pedidos -> Procesa la creación con validaciones
    @PostMapping
    public String guardar(@Valid @ModelAttribute("nuevoPedido") CrearPedidoDTO dto,
            BindingResult errores,
            Model model) {
        if (errores.hasErrors()) {
            model.addAttribute("esEdicion", false);
            return "pedidos/formulario";
        }
        servicio.crear(dto);
        return "redirect:/pedidos";
    }

    // 4. GET /pedidos/{id}/editar -> Muestra formulario cargado con los datos
    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        PedidoDTO pedido = servicio.obtenerPorId(id);
        CrearPedidoDTO dto = new CrearPedidoDTO(pedido.cliente(), pedido.plato(), pedido.precio());

        model.addAttribute("nuevoPedido", dto);
        model.addAttribute("idPedido", id);
        model.addAttribute("esEdicion", true);
        return "pedidos/formulario";
    }

    // 5. POST /pedidos/{id} -> Procesa la edición
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
            @Valid @ModelAttribute("nuevoPedido") CrearPedidoDTO dto,
            BindingResult errores,
            Model model) {
        if (errores.hasErrors()) {
            model.addAttribute("idPedido", id);
            model.addAttribute("esEdicion", true);
            return "pedidos/formulario";
        }
        servicio.actualizar(id, dto);
        return "redirect:/pedidos";
    }

    // 6. POST /pedidos/{id}/entregar -> Cambia el estado del pedido a entregado
    @PostMapping("/{id}/entregar")
    public String marcarEntregado(@PathVariable Long id) {
        servicio.marcarEntregado(id);
        return "redirect:/pedidos";
    }

    // 7. POST /pedidos/{id}/eliminar -> Elimina el registro
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return "redirect:/pedidos";
    }
}