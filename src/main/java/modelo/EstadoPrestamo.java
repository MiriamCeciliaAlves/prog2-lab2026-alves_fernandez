package modelo;

/**
 * Estados posibles de un equipo respecto al préstamo.
 * Son excluyentes: en la vista se representan con JRadioButton.
 * En el JSON se guarda el nombre de la constante (por ejemplo "PRESTADO").
 */
public enum EstadoPrestamo {
    DISPONIBLE("Disponible"),
    PRESTADO("Prestado"),
    EN_REPARACION("En reparación");

    private final String texto;

    EstadoPrestamo(String texto) {
        this.texto = texto;
    }

    /** Texto legible para mostrar en la tabla y en los botones de opción. */
    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
