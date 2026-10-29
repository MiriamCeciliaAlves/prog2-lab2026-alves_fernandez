package modelo;

import java.util.Arrays;
import java.util.List;

/**
 * Equipo del laboratorio de informática que puede prestarse.
 * Adapta el Registro genérico del prototipo: conserva identificador, nombre,
 * categoría y activo (ahora significa "en servicio") y agrega los datos del préstamo.
 *
 * Las reglas del negocio se validan aquí, en el modelo, no en la vista.
 */
public class Equipo {

    public static final int DIAS_MINIMOS = 1;
    public static final int DIAS_MAXIMOS = 7;

    /** Categorías válidas; la vista las usa para cargar el JComboBox. */
    public static final List<String> CATEGORIAS =
            Arrays.asList("Notebook", "Proyector", "Kit Arduino", "Cable/Adaptador");

    // Atributos heredados del prototipo (mismos nombres para que Gson los lea).
    private int identificador;
    private String nombre;
    private String categoria;
    private boolean activo;                 // true = en servicio

    // Atributos agregados por el equipo.
    private EstadoPrestamo estadoPrestamo;  // JRadioButton
    private String responsable;             // JTextField
    private int diasPrestamo;               // JSpinner
    private String observaciones;           // JTextArea

    /**
     * Crea un equipo nuevo. El identificador lo asigna la fachada para que no se repita.
     * @throws IllegalArgumentException si algún dato no cumple las reglas.
     */
    public Equipo(int identificador, String nombre, String categoria, boolean activo,
                  EstadoPrestamo estadoPrestamo, String responsable, int diasPrestamo,
                  String observaciones) {
        if (identificador <= 0) {
            throw new IllegalArgumentException("El identificador debe ser un número positivo.");
        }
        this.identificador = identificador;
        actualizarDatos(nombre, categoria, activo, estadoPrestamo, responsable,
                diasPrestamo, observaciones);
    }

    /**
     * Modifica todos los datos editables de una vez, conservando el identificador.
     * Primero valida y recién después asigna: si hay un error, el equipo queda sin cambios.
     * @throws IllegalArgumentException con el motivo del error, para mostrarlo al usuario.
     */
    public void actualizarDatos(String nombre, String categoria, boolean activo,
                                EstadoPrestamo estadoPrestamo, String responsable,
                                int diasPrestamo, String observaciones) {
        validar(nombre, categoria, activo, estadoPrestamo, responsable, diasPrestamo);

        this.nombre = nombre.trim();
        this.categoria = categoria;
        this.activo = activo;
        this.estadoPrestamo = estadoPrestamo;
        this.observaciones = (observaciones == null) ? "" : observaciones.trim();

        // Los datos del préstamo solo tienen sentido si el equipo está prestado.
        if (estadoPrestamo == EstadoPrestamo.PRESTADO) {
            this.responsable = responsable.trim();
            this.diasPrestamo = diasPrestamo;
        } else {
            this.responsable = "";
            this.diasPrestamo = 0;
        }
    }

    /**
     * Comprueba que el estado actual cumpla las reglas.
     * Sirve para la persistencia: Gson crea los objetos sin pasar por el constructor,
     * así que un JSON editado a mano podría traer datos inválidos.
     */
    public void verificarIntegridad() {
        if (identificador <= 0) {
            throw new IllegalArgumentException("El identificador debe ser un número positivo.");
        }
        validar(nombre, categoria, activo, estadoPrestamo, responsable, diasPrestamo);
    }

    /** Reúne todas las validaciones del negocio en un solo lugar. */
    private static void validar(String nombre, String categoria, boolean activo,
                                EstadoPrestamo estadoPrestamo, String responsable,
                                int diasPrestamo) {
        // Validación 1: nombre obligatorio.
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del equipo es obligatoria.");
        }
        if (categoria == null || !CATEGORIAS.contains(categoria)) {
            throw new IllegalArgumentException("Debe seleccionar una categoría válida.");
        }
        if (estadoPrestamo == null) {
            throw new IllegalArgumentException("Debe seleccionar el estado del préstamo.");
        }

        if (estadoPrestamo == EstadoPrestamo.PRESTADO) {
            // Validación 3 (regla propia): no se presta un equipo fuera de servicio.
            if (!activo) {
                throw new IllegalArgumentException(
                        "No se puede prestar un equipo que está fuera de servicio.");
            }
            // Validación 2: datos del préstamo completos y en rango.
            if (responsable == null || responsable.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Debe indicar el responsable del préstamo.");
            }
            if (diasPrestamo < DIAS_MINIMOS || diasPrestamo > DIAS_MAXIMOS) {
                throw new IllegalArgumentException("Los días de préstamo deben estar entre "
                        + DIAS_MINIMOS + " y " + DIAS_MAXIMOS + ".");
            }
        }
    }

    /** Código para mostrar en la tabla, por ejemplo EQ-007. */
    public String getCodigo() {
        return String.format("EQ-%03d", identificador);
    }

    /** Útil para el filtro "solo disponibles": en servicio y sin prestar. */
    public boolean estaDisponible() {
        return activo && estadoPrestamo == EstadoPrestamo.DISPONIBLE;
    }

    public int getIdentificador() {
        return identificador;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public EstadoPrestamo getEstadoPrestamo() {
        return estadoPrestamo;
    }

    public String getResponsable() {
        return responsable;
    }

    public int getDiasPrestamo() {
        return diasPrestamo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    @Override
    public String toString() {
        return getCodigo() + " - " + nombre + " (" + estadoPrestamo + ")";
    }
}
