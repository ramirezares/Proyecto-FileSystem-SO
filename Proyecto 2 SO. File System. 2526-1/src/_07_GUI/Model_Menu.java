package _07_GUI;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Representa un elemento del menú principal de la aplicación, incluyendo su
 * icono, nombre visible y tipo de comportamiento dentro de la interfaz.
 *
 * Esta clase se utiliza para generar dinámicamente los ítems del menú lateral o
 * superior en función de su categoría (título, opción o espacio vacío).
 *
 * <p>
 * Los iconos se cargan desde la ruta <b>/_08_SourcesGUI/</b> y deben tener
 * formato PNG.</p>
 *
 * Ejemplo:
 *
 * Model_Menu item = new Model_Menu("cpu_icon", "Simulación", MenuType.MENU);
 *
 *
 * @author Danaz
 */
public class Model_Menu {

    /**
     * Nombre del archivo del icono (sin extensión).
     */
    private String icon;

    /**
     * Nombre visible del elemento de menú.
     */
    private String name;

    /**
     * Tipo de elemento del menú (Título, Menú o Espacio vacío).
     */
    private MenuType type;

    /**
     * Crea un nuevo elemento de menú con los parámetros especificados.
     *
     * @param icon Nombre del icono (sin extensión ni ruta).
     * @param name Texto visible en el menú.
     * @param type Tipo de elemento dentro del menú.
     */
    public Model_Menu(String icon, String name, MenuType type) {
        this.icon = icon;
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor vacío utilizado para inicializaciones por defecto.
     */
    public Model_Menu() {
    }

    /**
     * Enumeración que define los tipos de elementos del menú.
     */
    public static enum MenuType {
        /**
         * Elemento de tipo título, usado para encabezados.
         */
        TITLE,
        /**
         * Elemento seleccionable del menú principal.
         */
        MENU,
        /**
         * Espacio vacío, usado para separación visual.
         */
        EMPTY
    }

    // ===== Getters y Setters =====
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MenuType getType() {
        return type;
    }

    public void setType(MenuType type) {
        this.type = type;
    }

    /**
     * Devuelve un objeto {@link Icon} basado en el nombre del icono definido.
     * Busca la imagen dentro del paquete <b>/_08_SourcesGUI/</b> y la carga
     * como PNG.
     *
     * @return Icono cargado correspondiente al elemento del menú.
     */
    public Icon toIcon() {
        return new ImageIcon(getClass().getResource("/_08_SourcesGUI/" + icon + ".png"));
    }
}
