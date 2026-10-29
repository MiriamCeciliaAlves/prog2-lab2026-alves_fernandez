package fachada;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Equipo;
import modelo.EstadoPrestamo;
import persistencia.PersistenciaRegistros;

/**
 * Punto único de acceso a las operaciones del sistema de préstamos.
 * El controlador solo habla con esta clase: no conoce la lista ni el archivo.
 *
 * Cada operación de gestión guarda automáticamente en JSON. Si la escritura falla,
 * se deshace el cambio en memoria para que la lista y el archivo no queden distintos.
 *
 * Errores que lanza:
 *  - IllegalArgumentException: datos que no cumplen las reglas (el usuario puede corregir).
 *  - IOException: problema al leer o escribir el archivo.
 */
public class FachadaRegistros {

    /** Valor del JComboBox de filtro que significa "no filtrar por categoría". */
    public static final String TODAS_LAS_CATEGORIAS = "Todas";

    private final PersistenciaRegistros persistencia;
    private List<Equipo> equipos;
    private boolean cargaCorrecta;
    private int ultimoIdentificador;

    public FachadaRegistros(PersistenciaRegistros persistencia) {
        this.persistencia = persistencia;
        equipos = new ArrayList<>();
    }

    // ------------------------------------------------------------------ carga y guardado

    public void cargarDatos() throws IOException {
        cargaCorrecta = false;
        equipos = persistencia.leer();
        ultimoIdentificador = 0;
        for (Equipo equipo : equipos) {
            if (equipo.getIdentificador() > ultimoIdentificador) {
                ultimoIdentificador = equipo.getIdentificador();
            }
        }
        cargaCorrecta = true;
    }

    public void guardarDatos() throws IOException {
        // No reemplazar un archivo que no se pudo recuperar.
        if (!cargaCorrecta) {
            throw new IOException("No se puede guardar sin haber cargado los datos correctamente.");
        }
        persistencia.escribir(equipos);
    }

    // ------------------------------------------------------------------ consultas

    /** Devuelve una copia para que nadie modifique la lista desde afuera. */
    public List<Equipo> listarRegistros() {
        return new ArrayList<>(equipos);
    }

    /** Busca un equipo por su identificador; devuelve null si no existe. */
    public Equipo buscarPorIdentificador(int identificador) {
        int posicion = posicionDe(identificador);
        return (posicion == -1) ? null : equipos.get(posicion);
    }

    /** Categorías para cargar el JComboBox del formulario. */
    public List<String> listarCategorias() {
        return new ArrayList<>(Equipo.CATEGORIAS);
    }

    /**
     * Filtra los equipos combinando tres criterios (los vacíos no se aplican):
     * @param texto parte del nombre o del responsable, sin distinguir mayúsculas.
     * @param categoria una categoría, o TODAS_LAS_CATEGORIAS / null para no filtrar.
     * @param soloDisponibles true para ver solo los equipos en servicio y sin prestar.
     */
    public List<Equipo> filtrar(String texto, String categoria, boolean soloDisponibles) {
        String buscado = (texto == null) ? "" : texto.trim().toLowerCase();
        boolean todas = categoria == null || categoria.equals(TODAS_LAS_CATEGORIAS);

        List<Equipo> resultado = new ArrayList<>();
        for (Equipo equipo : equipos) {
            boolean coincideTexto = buscado.isEmpty()
                    || equipo.getNombre().toLowerCase().contains(buscado)
                    || equipo.getResponsable().toLowerCase().contains(buscado);
            boolean coincideCategoria = todas || equipo.getCategoria().equals(categoria);
            boolean coincideDisponible = !soloDisponibles || equipo.estaDisponible();

            if (coincideTexto && coincideCategoria && coincideDisponible) {
                resultado.add(equipo);
            }
        }
        return resultado;
    }

    // ------------------------------------------------------------------ gestión

    /**
     * Registra un equipo nuevo con un identificador que no se repite.
     * @return el equipo creado, para que la vista pueda seleccionarlo o informar su código.
     */
    public Equipo registrar(String nombre, String categoria, boolean activo,
                            EstadoPrestamo estado, String responsable, int diasPrestamo,
                            String observaciones) throws IOException {
        int nuevoIdentificador = ultimoIdentificador + 1;
        // El constructor valida; si algo está mal, se lanza la excepción y no se agrega nada.
        Equipo nuevo = new Equipo(nuevoIdentificador, nombre, categoria, activo, estado,
                responsable, diasPrestamo, observaciones);

        equipos.add(nuevo);
        try {
            guardarDatos();
        } catch (IOException e) {
            equipos.remove(nuevo);  // deshacer
            throw e;
        }
        ultimoIdentificador = nuevoIdentificador;
        return nuevo;
    }

    /**
     * Modifica el equipo indicado conservando su identificador (no crea duplicados).
     * Se arma un objeto nuevo con el mismo identificador y se reemplaza en su posición;
     * así, si la escritura falla, se vuelve a poner el original.
     */
    public Equipo modificar(int identificador, String nombre, String categoria, boolean activo,
                            EstadoPrestamo estado, String responsable, int diasPrestamo,
                            String observaciones) throws IOException {
        int posicion = posicionObligatoria(identificador);
        Equipo original = equipos.get(posicion);

        Equipo modificado = new Equipo(identificador, nombre, categoria, activo, estado,
                responsable, diasPrestamo, observaciones);

        equipos.set(posicion, modificado);
        try {
            guardarDatos();
        } catch (IOException e) {
            equipos.set(posicion, original);  // deshacer
            throw e;
        }
        return modificado;
    }

    /**
     * Elimina el equipo indicado. La confirmación ("¿Está seguro?") la pide la vista
     * antes de llamar a este método.
     */
    public void eliminar(int identificador) throws IOException {
        int posicion = posicionObligatoria(identificador);
        Equipo eliminado = equipos.remove(posicion);
        try {
            guardarDatos();
        } catch (IOException e) {
            equipos.add(posicion, eliminado);  // deshacer
            throw e;
        }
        // ultimoIdentificador no baja: un código eliminado no se reutiliza en esta ejecución.
    }

    // ------------------------------------------------------------------ auxiliares

    private int posicionDe(int identificador) {
        for (int i = 0; i < equipos.size(); i++) {
            if (equipos.get(i).getIdentificador() == identificador) {
                return i;
            }
        }
        return -1;
    }

    private int posicionObligatoria(int identificador) {
        int posicion = posicionDe(identificador);
        if (posicion == -1) {
            throw new IllegalArgumentException(
                    "No existe un equipo con el identificador " + identificador + ".");
        }
        return posicion;
    }
}
