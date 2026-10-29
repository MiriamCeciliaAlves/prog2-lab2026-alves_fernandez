# Gestor de préstamos de equipos del laboratorio de informática

Trabajo de Programación 2 del CeRP del Suroeste, modalidad semipresencial.
Docente: Domingo Pérez. Año: 2026.

## Integrantes

- Miriam Alves.
- Javier Fernández.

## Estado del proyecto

Aplicación de escritorio en Java y Swing, organizada con MVC, fachada y
persistencia en JSON, desarrollada a partir del prototipo docente de un gestor
de registros genéricos.

La versión actual permite registrar, modificar y eliminar equipos, buscar y
filtrar el listado, limpiar el formulario y conservar los datos en
`datos/equipos.json` entre ejecuciones.

## Problema y destinatarios

En el laboratorio de informática del centro se prestan notebooks, proyectores,
kits de Arduino, cables y adaptadores a estudiantes y docentes. El control suele
hacerse de memoria o en papel. Con frecuencia no queda claro qué equipo está
disponible, quién lo tiene y por cuántos días, ni cuáles están fuera de servicio.

La aplicación está pensada para el **docente o la persona encargada del
laboratorio**, que es quien registra los equipos y los préstamos.

## Utilidad de la solución

- Muestra en un solo listado todos los equipos, su estado y quién los tiene.
- Evita prestar equipos fuera de servicio, porque el sistema lo impide.
- Permite encontrar rápido un equipo por su nombre o por el responsable, y ver
  solo los disponibles o los de una categoría.
- Registra observaciones de cada equipo, como accesorios incluidos o fallas.
- Conserva la información al cerrar el programa, sin base de datos ni conexión
  a Internet.

## Adaptación del modelo y reglas

El `Registro` genérico del prototipo se reemplazó por la clase **`Equipo`**
(paquete `modelo`). Cada equipo representa un recurso físico del laboratorio
que puede prestarse.

| Atributo | Tipo | Origen | Significado en el proyecto |
| --- | --- | --- | --- |
| `identificador` | `int` | Prototipo | Número único asignado por el sistema; se muestra como código `EQ-001`. |
| `nombre` | `String` | Prototipo | Descripción del equipo, por ejemplo "Notebook Lenovo 14". |
| `categoria` | `String` | Prototipo, adaptado | Notebook, Proyector, Kit Arduino o Cable/Adaptador. |
| `activo` | `boolean` | Prototipo, adaptado | Indica si el equipo está **en servicio**. |
| `estadoPrestamo` | `EstadoPrestamo` | Agregado | Disponible, Prestado o En reparación (enumerado nuevo). |
| `responsable` | `String` | Agregado | Persona que tiene el equipo prestado. |
| `diasPrestamo` | `int` | Agregado | Días de préstamo, de 1 a 7. |
| `observaciones` | `String` | Agregado | Notas sobre el equipo. |

Las reglas se validan en el modelo (`Equipo`), nunca en la vista. Si un dato es
inválido, el modelo lanza una excepción con el motivo, el controlador lo muestra
y el formulario conserva lo escrito para que el usuario pueda corregirlo.

| Validación requerida | Regla de nuestro proyecto | Estado |
| --- | --- | --- |
| Primera validación | La descripción del equipo es obligatoria. | Implementada. |
| Segunda validación | Si el equipo está prestado, el responsable es obligatorio y los días deben estar entre 1 y 7. | Implementada. |
| Tercera validación, propia del problema | **No se puede prestar un equipo que está fuera de servicio.** | Implementada. |

Otras reglas implementadas:

- La categoría debe ser una de las definidas.
- Cuando un equipo deja de estar prestado, se borran el responsable y los días.
- El identificador lo asigna la fachada y no se repite. Al modificar se conserva
  el identificador, y un código eliminado no se reutiliza durante la ejecución.
- Al leer el JSON se verifica que cada equipo cumpla las reglas y que no haya
  identificadores repetidos.

## Componentes gráficos

La base incluye JTextField, JComboBox, JCheckBox, JButton y JTable; los cinco se
utilizan en el proyecto (nombre, responsable y búsqueda; categoría y filtro;
"En servicio" y "Solo disponibles"; botones de acciones; listado de equipos).

Agregamos tres tipos nuevos, que modifican datos guardados en el JSON:

| Componente nuevo | Dato o acción real que permite gestionar | Estado |
| --- | --- | --- |
| `JRadioButton` (en un `ButtonGroup`) | Elegir el estado del préstamo: Disponible, Prestado o En reparación. Son opciones excluyentes. Al elegir "Prestado" se habilitan el responsable y los días. | Implementado. |
| `JSpinner` | Indicar los días de préstamo. Su modelo limita los valores de 1 a 7. | Implementado. |
| `JTextArea` | Escribir observaciones de varias líneas sobre el equipo. | Implementado. |

## Organización de paquetes y recorrido de una operación

| Paquete o carpeta | Responsabilidad | Clases |
| --- | --- | --- |
| `app` | Iniciar la aplicación y conectar los objetos. | `Aplicacion` |
| `modelo` | Representar los equipos y sus reglas. | `Equipo`, `EstadoPrestamo` |
| `vista` | Mostrar la interfaz Swing, los mensajes y las confirmaciones. | `VentanaPrincipal` |
| `controlador` | Recibir eventos y coordinar las acciones mediante la fachada. | `ControladorRegistros` |
| `fachada` | Ofrecer operaciones y coordinar el modelo con la persistencia. | `FachadaRegistros` |
| `persistencia` | Leer y escribir JSON, sin depender de Swing. | `PersistenciaRegistros` |
| `datos` | Contener el archivo JSON; no es un paquete Java. | `equipos.json` |

### Recorrido de la operación «Registrar»

1. **Vista:** el usuario completa el formulario y pulsa *Registrar*. La vista
   solo entrega los valores escritos; no valida ni guarda.
2. **Controlador:** `ControladorRegistros.registrar()` lee los datos de la vista
   y llama a `fachada.registrar(...)`.
3. **Fachada:** `FachadaRegistros` calcula el siguiente identificador libre y
   crea el `Equipo`.
4. **Modelo:** el constructor de `Equipo` valida las reglas. Si alguna no se
   cumple, lanza `IllegalArgumentException` con el motivo y no se agrega nada.
5. **Persistencia:** la fachada agrega el equipo a la lista y pide a
   `PersistenciaRegistros` que escriba el JSON. Si la escritura falla, la
   fachada quita el equipo de la lista, para que la memoria y el archivo no
   queden distintos.
6. **Vuelta a la vista:** el controlador actualiza el listado, limpia el
   formulario y muestra «Equipo EQ-00X registrado correctamente», o el mensaje
   de error si hubo un problema.

Modificar y eliminar siguen el mismo camino. Eliminar pide antes una
confirmación en la vista y, si el usuario cancela, no llama a la fachada.

**Guardado:** cada operación de gestión guarda automáticamente. El botón
*Guardar* de la base se conservó para forzar un guardado manual.

## Instrucciones de ejecución

### Requisitos

- **JDK 25 LTS o superior**, con `JAVA_HOME` apuntando al JDK y su carpeta `bin`
  en el `PATH`. La compilación genera clases para Java 25; no se pueden ejecutar
  con Java 17 ni Java 21.
- Windows, macOS o Linux con entorno gráfico.
- Git, si se descarga el proyecto mediante clonación.
- Conexión a Internet en la primera ejecución para que Maven Wrapper descargue
  Maven 3.9.11 y las dependencias. Después, la aplicación funciona sin conexión.

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.
Comprobar la versión con `java -version` y `javac -version`.

### Descargar el proyecto

```
git clone https://github.com/MiriamCeciliaAlves/prog2-lab2026-alves_fernandez.git
cd prog2-lab2026-alves_fernandez
```

Ejecutar los siguientes comandos desde la raíz del proyecto, donde está `pom.xml`.

### Windows: PowerShell

```
.\mvnw.cmd compile exec:java
```

Si aparece «no se reconoce como nombre de un cmdlet», comprobar con `dir` que la
carpeta actual contenga `mvnw.cmd` y `pom.xml`.

### macOS y Linux: terminal

```
sh mvnw compile exec:java
```

### Dependencias

- **Gson 2.13.2** para leer y escribir JSON, declarada en `pom.xml`.
- **Swing**, incluido en Java, para la interfaz.

### Archivo de datos

Los datos se guardan en `datos/equipos.json`. La ruta es relativa al directorio
desde el cual se inicia la aplicación. El archivo incluye todos los atributos de
cada equipo, también los agregados por el equipo de desarrollo. Ejemplo:

```json
{
  "identificador": 2,
  "nombre": "Proyector Epson X41",
  "categoria": "Proyector",
  "activo": true,
  "estadoPrestamo": "PRESTADO",
  "responsable": "Docente de prueba",
  "diasPrestamo": 3,
  "observaciones": "Con cable HDMI."
}
```

Si el archivo no existe, la aplicación inicia con una lista vacía y lo crea al
registrar el primer equipo. Si el archivo es inválido o contiene un equipo que
no cumple las reglas, muestra un error, mantiene la ventana abierta y no
reemplaza el archivo.

## Pruebas realizadas y limitaciones conocidas

Pruebas manuales realizadas en Windows con Java 27.

| Acción | Resultado esperado | Resultado obtenido |
| --- | --- | --- |
| Crear un equipo válido. | Aparece en el listado con un código nuevo. | Correcto. |
| Registrar sin descripción. | Informa «La descripción del equipo es obligatoria» y permite corregir. | [COMPLETAR] |
| Registrar como prestado sin responsable, o con días fuera de 1 a 7. | Informa el motivo y permite corregir. | [COMPLETAR] |
| Prestar un equipo sin «En servicio» marcado. | Rechaza la operación: «No se puede prestar un equipo que está fuera de servicio». | [COMPLETAR] |
| Seleccionar un equipo de la tabla. | Sus datos se cargan en el formulario. | [COMPLETAR] |
| Modificar un equipo seleccionado. | Se actualiza conservando su código, sin duplicarlo. | [COMPLETAR] |
| Modificar o eliminar sin seleccionar un equipo. | Pide seleccionar un equipo de la lista. | [COMPLETAR] |
| Cancelar una eliminación y confirmar otra con un equipo de prueba. | Se conserva el primero y se elimina solo el confirmado. | [COMPLETAR] |
| Buscar por texto (nombre o responsable). | Muestra solo los equipos que contienen el texto. | [COMPLETAR] |
| Filtrar por categoría y por «Solo disponibles». | Muestra solo los equipos que cumplen los criterios. | [COMPLETAR] |
| Limpiar el formulario. | Queda vacío, con estado «Disponible», listo para un nuevo registro. | [COMPLETAR] |
| Crear y modificar datos, cerrar y volver a abrir. | Los cambios se recuperan desde el JSON. | Correcto. |
| Iniciar sin `equipos.json` (renombrando el archivo). | La tabla aparece vacía y se puede registrar. | [COMPLETAR] |
| Iniciar con un JSON inválido, usando una copia de prueba. | Informa el error sin cerrar la ventana ni modificar el archivo. | [COMPLETAR] |

### Limitaciones conocidas

- No se registra el historial de préstamos: solo el estado actual de cada equipo.
- No se calcula la fecha de devolución ni se avisa de préstamos vencidos.
- Las categorías están definidas en el código; no se pueden agregar desde la
  interfaz.
- Hay un único usuario y no se controla el acceso.
- [COMPLETAR: otras limitaciones que encuentren al probar].

## Procedencia del código y recursos utilizados

- **Base docente:** estructura MVC con fachada, ventana inicial, carga de
  ejemplo, infraestructura JSON (`PersistenciaRegistros`), bloqueo del guardado
  tras una carga fallida y configuración Maven con Wrapper.
- **Trabajo de nuestro equipo:**
  - `modelo`: clase `Equipo`, que reemplaza a `Registro`, y enumerado
    `EstadoPrestamo`, con las validaciones.
  - `fachada`: identificador único, registrar, modificar, eliminar, buscar y
    filtrar, guardado automático y reversión del cambio si falla la escritura.
  - `persistencia`: adaptación a `Equipo` y verificación de reglas e
    identificadores repetidos al leer.
  - `vista`: nuevo formulario con JRadioButton, JSpinner y JTextArea, columnas
    nuevas en la tabla, filtro por categoría y disponibilidad, confirmaciones.
  - `controlador`: conexión de todos los botones y de la selección de la tabla
    con la fachada, y manejo de mensajes de error.
  - `datos/equipos.json`: datos de ejemplo del problema.
- **Bibliotecas:** Gson 2.13.2 y Swing.
- **Herramientas de IA:** utilizamos Claude (Anthropic) como asistente para
  [COMPLETAR/REVISAR: por ejemplo, proponer el diseño del modelo, generar una
  primera versión del código de las clases `Equipo`, `EstadoPrestamo`,
  `FachadaRegistros`, `ControladorRegistros` y `VentanaPrincipal`, y redactar
  este README]. Revisamos, ejecutamos y probamos el código, y podemos explicar
  cada parte.

## Aportes de cada integrante

| Integrante | Aportes realizados |
| --- | --- |
| Miriam Alves | [COMPLETAR: aportes concretos] |
| Javier Fernández | [COMPLETAR: aportes concretos] |

Ambos integrantes conocemos la solución completa y participamos en el video.

## Video explicativo

Enlace público: 
