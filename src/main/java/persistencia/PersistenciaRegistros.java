package persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import modelo.Equipo;

public class PersistenciaRegistros {
    private final Path archivo;
    private final Gson gson;

    public PersistenciaRegistros(Path archivo) {
        this.archivo = archivo;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setStrictness(Strictness.STRICT)
                .create();
    }

    public List<Equipo> leer() throws IOException {
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            Equipo[] equipos = gson.fromJson(lector, Equipo[].class);
            if (equipos == null) {
                throw new IOException("El archivo debe contener un arreglo JSON, aunque este vacio.");
            }
            Set<Integer> identificadores = new HashSet<>();
            for (Equipo equipo : equipos) {
                if (equipo == null) {
                    throw new IOException("El archivo contiene un equipo vacio.");
                }
                // Gson no pasa por el constructor: se revisan las reglas del modelo.
                try {
                    equipo.verificarIntegridad();
                } catch (IllegalArgumentException e) {
                    throw new IOException("El archivo contiene un equipo invalido: " + e.getMessage());
                }
                if (!identificadores.add(equipo.getIdentificador())) {
                    throw new IOException("El archivo repite el identificador "
                            + equipo.getIdentificador() + ".");
                }
            }
            return new ArrayList<>(Arrays.asList(equipos));
        } catch (NoSuchFileException e) {
            return new ArrayList<>();
        } catch (JsonParseException e) {
            throw new IOException("El archivo no contiene registros JSON validos.", e);
        }
    }

    public void escribir(List<Equipo> equipos) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        Files.createDirectories(carpeta);
        try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            gson.toJson(equipos, escritor);
        }
    }
}
