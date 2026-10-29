package app;

import controlador.ControladorRegistros;
import fachada.FachadaRegistros;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import persistencia.PersistenciaRegistros;
import vista.VentanaPrincipal;

public class Aplicacion {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PersistenciaRegistros persistencia =
                    new PersistenciaRegistros(Path.of("datos", "equipos.json"));
            FachadaRegistros fachada = new FachadaRegistros(persistencia);
            VentanaPrincipal vista = new VentanaPrincipal();
            ControladorRegistros controlador = new ControladorRegistros(vista, fachada);
            controlador.iniciar();
        });
    }
}
