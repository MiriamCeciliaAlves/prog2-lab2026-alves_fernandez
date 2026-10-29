package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import modelo.Equipo;
import modelo.EstadoPrestamo;

/**
 * Ventana principal del gestor de préstamos.
 * Solo muestra datos y entrega lo que el usuario escribió: no lee archivos
 * ni decide reglas del negocio (eso lo hacen el modelo y la fachada).
 */
public class VentanaPrincipal extends JFrame {

    // ---- Formulario: componentes de la base (adaptados)
    private final JTextField campoIdentificador = new JTextField(8);
    private final JTextField campoNombre = new JTextField(22);
    private final JComboBox<String> comboCategoria = new JComboBox<>();
    private final JCheckBox casillaActivo = new JCheckBox("En servicio", true);

    // ---- Formulario: componentes nuevos
    private final JRadioButton opcionDisponible =
            new JRadioButton(EstadoPrestamo.DISPONIBLE.getTexto(), true);
    private final JRadioButton opcionPrestado =
            new JRadioButton(EstadoPrestamo.PRESTADO.getTexto());
    private final JRadioButton opcionReparacion =
            new JRadioButton(EstadoPrestamo.EN_REPARACION.getTexto());
    private final ButtonGroup grupoEstado = new ButtonGroup();
    private final JTextField campoResponsable = new JTextField(22);
    private final JSpinner selectorDias = new JSpinner(
            new SpinnerNumberModel(Equipo.DIAS_MINIMOS, Equipo.DIAS_MINIMOS,
                    Equipo.DIAS_MAXIMOS, 1));
    private final JTextArea areaObservaciones = new JTextArea(3, 22);

    // ---- Botones de gestión
    private final JButton botonRegistrar = new JButton("Registrar");
    private final JButton botonModificar = new JButton("Modificar");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final JButton botonGuardar = new JButton("Guardar");

    // ---- Búsqueda y filtro
    private final JTextField campoBusqueda = new JTextField(16);
    private final JComboBox<String> comboFiltroCategoria = new JComboBox<>();
    private final JCheckBox casillaSoloDisponibles = new JCheckBox("Solo disponibles");
    private final JButton botonBuscar = new JButton("Buscar");

    // ---- Listado
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[] {"Código", "Equipo", "Categoría", "En servicio", "Estado",
                "Responsable", "Días"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaRegistros = new JTable(modeloTabla);

    public VentanaPrincipal() {
        super("Préstamos de equipos - Laboratorio de Informática");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        campoIdentificador.setEditable(false);  // lo asigna el sistema
        grupoEstado.add(opcionDisponible);
        grupoEstado.add(opcionPrestado);
        grupoEstado.add(opcionReparacion);
        areaObservaciones.setLineWrap(true);
        areaObservaciones.setWrapStyleWord(true);
        tablaRegistros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Comportamiento solo visual: los datos del préstamo se habilitan si está prestado.
        opcionDisponible.addActionListener(e -> actualizarCamposPrestamo());
        opcionPrestado.addActionListener(e -> actualizarCamposPrestamo());
        opcionReparacion.addActionListener(e -> actualizarCamposPrestamo());
        actualizarCamposPrestamo();

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(new JScrollPane(tablaRegistros), BorderLayout.CENTER);
        add(crearPanelBusqueda(), BorderLayout.SOUTH);
        setSize(900, 620);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ armado

    private JPanel crearPanelSuperior() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del equipo"));

        JPanel estados = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        estados.add(opcionDisponible);
        estados.add(opcionPrestado);
        estados.add(opcionReparacion);

        agregarFila(formulario, 0, "Código:", campoIdentificador, "Estado:", estados);
        agregarFila(formulario, 1, "Equipo:", campoNombre, "Responsable:", campoResponsable);
        agregarFila(formulario, 2, "Categoría:", comboCategoria, "Días de préstamo:", selectorDias);
        agregarFila(formulario, 3, "", casillaActivo, "Observaciones:",
                new JScrollPane(areaObservaciones));

        JPanel acciones = new JPanel(new FlowLayout());
        acciones.add(botonRegistrar);
        acciones.add(botonModificar);
        acciones.add(botonEliminar);
        acciones.add(botonLimpiar);
        acciones.add(botonGuardar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(formulario, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta1, JComponent campo1,
                             String etiqueta2, JComponent campo2) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = fila;

        c.gridx = 0;
        panel.add(new JLabel(etiqueta1), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo1, c);

        c.gridx = 2;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        panel.add(new JLabel(etiqueta2), c);
        c.gridx = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo2, c);
    }

    private JPanel crearPanelBusqueda() {
        JPanel busqueda = new JPanel(new FlowLayout());
        busqueda.add(new JLabel("Buscar (equipo o responsable):"));
        busqueda.add(campoBusqueda);
        busqueda.add(new JLabel("Categoría:"));
        busqueda.add(comboFiltroCategoria);
        busqueda.add(casillaSoloDisponibles);
        busqueda.add(botonBuscar);
        return busqueda;
    }

    private void actualizarCamposPrestamo() {
        boolean prestado = opcionPrestado.isSelected();
        campoResponsable.setEnabled(prestado);
        selectorDias.setEnabled(prestado);
    }

    // ------------------------------------------------------------------ mostrar datos

    /** Carga las categorías del formulario y del filtro (vienen de la fachada). */
    public void cargarCategorias(List<String> categorias, String opcionTodas) {
        comboCategoria.removeAllItems();
        comboFiltroCategoria.removeAllItems();
        comboFiltroCategoria.addItem(opcionTodas);
        for (String categoria : categorias) {
            comboCategoria.addItem(categoria);
            comboFiltroCategoria.addItem(categoria);
        }
    }

    public void mostrarRegistros(List<Equipo> equipos) {
        modeloTabla.setRowCount(0);
        for (Equipo equipo : equipos) {
            boolean prestado = equipo.getEstadoPrestamo() == EstadoPrestamo.PRESTADO;
            modeloTabla.addRow(new Object[] {
                equipo.getCodigo(),
                equipo.getNombre(),
                equipo.getCategoria(),
                equipo.isActivo() ? "Sí" : "No",
                equipo.getEstadoPrestamo().getTexto(),
                equipo.getResponsable(),
                prestado ? equipo.getDiasPrestamo() : ""
            });
        }
    }

    /** Muestra en el formulario los datos del equipo seleccionado. */
    public void mostrarEnFormulario(Equipo equipo) {
        campoIdentificador.setText(equipo.getCodigo());
        campoNombre.setText(equipo.getNombre());
        comboCategoria.setSelectedItem(equipo.getCategoria());
        casillaActivo.setSelected(equipo.isActivo());
        switch (equipo.getEstadoPrestamo()) {
            case PRESTADO -> opcionPrestado.setSelected(true);
            case EN_REPARACION -> opcionReparacion.setSelected(true);
            default -> opcionDisponible.setSelected(true);
        }
        campoResponsable.setText(equipo.getResponsable());
        selectorDias.setValue(Math.max(Equipo.DIAS_MINIMOS, equipo.getDiasPrestamo()));
        areaObservaciones.setText(equipo.getObservaciones());
        actualizarCamposPrestamo();
    }

    public void limpiarFormulario() {
        tablaRegistros.clearSelection();
        campoIdentificador.setText("");
        campoNombre.setText("");
        if (comboCategoria.getItemCount() > 0) {
            comboCategoria.setSelectedIndex(0);
        }
        casillaActivo.setSelected(true);
        opcionDisponible.setSelected(true);
        campoResponsable.setText("");
        selectorDias.setValue(Equipo.DIAS_MINIMOS);
        areaObservaciones.setText("");
        actualizarCamposPrestamo();
        campoNombre.requestFocusInWindow();
    }

    public void limpiarFiltros() {
        campoBusqueda.setText("");
        if (comboFiltroCategoria.getItemCount() > 0) {
            comboFiltroCategoria.setSelectedIndex(0);
        }
        casillaSoloDisponibles.setSelected(false);
    }

    // ------------------------------------------------------------------ mensajes

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Préstamos",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Pregunta Sí/No; devuelve true solo si el usuario elige Sí. */
    public boolean confirmar(String pregunta) {
        int respuesta = JOptionPane.showConfirmDialog(this, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }

    // ------------------------------------------------------------------ datos ingresados

    public String getNombreIngresado() {
        return campoNombre.getText();
    }

    public String getCategoriaSeleccionada() {
        return (String) comboCategoria.getSelectedItem();
    }

    public boolean isEnServicio() {
        return casillaActivo.isSelected();
    }

    public EstadoPrestamo getEstadoSeleccionado() {
        if (opcionPrestado.isSelected()) {
            return EstadoPrestamo.PRESTADO;
        }
        if (opcionReparacion.isSelected()) {
            return EstadoPrestamo.EN_REPARACION;
        }
        return EstadoPrestamo.DISPONIBLE;
    }

    public String getResponsableIngresado() {
        return campoResponsable.getText();
    }

    public int getDiasIngresados() {
        return (Integer) selectorDias.getValue();
    }

    public String getObservacionesIngresadas() {
        return areaObservaciones.getText();
    }

    public String getTextoBusqueda() {
        return campoBusqueda.getText();
    }

    public String getCategoriaFiltro() {
        return (String) comboFiltroCategoria.getSelectedItem();
    }

    public boolean isSoloDisponibles() {
        return casillaSoloDisponibles.isSelected();
    }

    // ------------------------------------------------------------------ componentes

    public JTable getTablaRegistros() {
        return tablaRegistros;
    }

    public JButton getBotonRegistrar() {
        return botonRegistrar;
    }

    public JButton getBotonModificar() {
        return botonModificar;
    }

    public JButton getBotonEliminar() {
        return botonEliminar;
    }

    public JButton getBotonBuscar() {
        return botonBuscar;
    }

    public JButton getBotonLimpiar() {
        return botonLimpiar;
    }

    public JButton getBotonGuardar() {
        return botonGuardar;
    }
}
