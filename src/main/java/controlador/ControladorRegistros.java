package controlador;

import fachada.FachadaRegistros;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Equipo;
import vista.VentanaPrincipal;

/**
 * Recibe los eventos de la ventana y los traduce en operaciones de la fachada.
 * No valida reglas del negocio: si la fachada o el modelo rechazan los datos,
 * muestra el motivo y deja el formulario como estaba para que el usuario corrija.
 */
public class ControladorRegistros {
    private final VentanaPrincipal vista;
    private final FachadaRegistros fachada;

    /** Equipos tal como se ven en la tabla (fila i = equipo i). */
    private List<Equipo> listadoActual = new ArrayList<>();
    /** Identificador del equipo seleccionado; 0 si no hay ninguno. */
    private int identificadorSeleccionado = 0;

    public ControladorRegistros(VentanaPrincipal vista, FachadaRegistros fachada) {
        this.vista = vista;
        this.fachada = fachada;
        vista.getBotonRegistrar().addActionListener(evento -> registrar());
        vista.getBotonModificar().addActionListener(evento -> modificar());
        vista.getBotonEliminar().addActionListener(evento -> eliminar());
        vista.getBotonBuscar().addActionListener(evento -> buscar());
        vista.getBotonLimpiar().addActionListener(evento -> limpiar());
        vista.getBotonGuardar().addActionListener(evento -> guardar());
        vista.getTablaRegistros().getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                seleccionarFila(vista.getTablaRegistros().getSelectedRow());
            }
        });
    }

    public void iniciar() {
        vista.cargarCategorias(fachada.listarCategorias(), FachadaRegistros.TODAS_LAS_CATEGORIAS);
        vista.limpiarFormulario();
        vista.setVisible(true);
        try {
            fachada.cargarDatos();
            mostrar(fachada.listarRegistros());
        } catch (IOException e) {
            vista.mostrarError("No se pudieron cargar los datos: " + e.getMessage()
                    + "\nEl archivo no fue modificado y no se guardarán cambios.");
        }
    }

    // ------------------------------------------------------------------ operaciones

    private void registrar() {
        try {
            Equipo nuevo = fachada.registrar(
                    vista.getNombreIngresado(),
                    vista.getCategoriaSeleccionada(),
                    vista.isEnServicio(),
                    vista.getEstadoSeleccionado(),
                    vista.getResponsableIngresado(),
                    vista.getDiasIngresados(),
                    vista.getObservacionesIngresadas());
            actualizarDespuesDeOperacion();
            vista.mostrarMensaje("Equipo " + nuevo.getCodigo() + " registrado correctamente.");
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());          // datos inválidos: el usuario corrige
        } catch (IOException e) {
            vista.mostrarError("No se pudo guardar: " + e.getMessage()
                    + "\nEl equipo no fue registrado.");
        }
    }

    private void modificar() {
        if (!hayEquipoSeleccionado("modificar")) {
            return;
        }
        try {
            Equipo modificado = fachada.modificar(
                    identificadorSeleccionado,
                    vista.getNombreIngresado(),
                    vista.getCategoriaSeleccionada(),
                    vista.isEnServicio(),
                    vista.getEstadoSeleccionado(),
                    vista.getResponsableIngresado(),
                    vista.getDiasIngresados(),
                    vista.getObservacionesIngresadas());
            actualizarDespuesDeOperacion();
            vista.mostrarMensaje("Equipo " + modificado.getCodigo() + " modificado correctamente.");
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        } catch (IOException e) {
            vista.mostrarError("No se pudo guardar: " + e.getMessage()
                    + "\nLos cambios no fueron aplicados.");
        }
    }

    private void eliminar() {
        if (!hayEquipoSeleccionado("eliminar")) {
            return;
        }
        Equipo equipo = fachada.buscarPorIdentificador(identificadorSeleccionado);
        if (!vista.confirmar("¿Desea eliminar el equipo " + equipo + "?")) {
            vista.mostrarMensaje("Eliminación cancelada.");
            return;
        }
        try {
            fachada.eliminar(identificadorSeleccionado);
            actualizarDespuesDeOperacion();
            vista.mostrarMensaje("Equipo " + equipo.getCodigo() + " eliminado.");
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        } catch (IOException e) {
            vista.mostrarError("No se pudo guardar: " + e.getMessage()
                    + "\nEl equipo no fue eliminado.");
        }
    }

    private void buscar() {
        List<Equipo> resultado = fachada.filtrar(
                vista.getTextoBusqueda(),
                vista.getCategoriaFiltro(),
                vista.isSoloDisponibles());
        vista.limpiarFormulario();
        mostrar(resultado);
        if (resultado.isEmpty()) {
            vista.mostrarMensaje("No se encontraron equipos con esos criterios.");
        }
    }

    private void limpiar() {
        vista.limpiarFormulario();
        identificadorSeleccionado = 0;
    }

    private void guardar() {
        try {
            fachada.guardarDatos();
            vista.mostrarMensaje("Datos guardados.");
        } catch (IOException e) {
            vista.mostrarError("No se pudo guardar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ auxiliares

    private void seleccionarFila(int fila) {
        if (fila < 0 || fila >= listadoActual.size()) {
            identificadorSeleccionado = 0;
            return;
        }
        Equipo equipo = listadoActual.get(fila);
        identificadorSeleccionado = equipo.getIdentificador();
        vista.mostrarEnFormulario(equipo);
    }

    private boolean hayEquipoSeleccionado(String accion) {
        if (identificadorSeleccionado == 0) {
            vista.mostrarError("Seleccione un equipo de la lista para " + accion + ".");
            return false;
        }
        return true;
    }

    /** Tras registrar, modificar o eliminar: listado completo y formulario vacío. */
    private void actualizarDespuesDeOperacion() {
        vista.limpiarFiltros();
        vista.limpiarFormulario();
        identificadorSeleccionado = 0;
        mostrar(fachada.listarRegistros());
    }

    private void mostrar(List<Equipo> equipos) {
        listadoActual = equipos;
        vista.mostrarRegistros(equipos);
    }
}
