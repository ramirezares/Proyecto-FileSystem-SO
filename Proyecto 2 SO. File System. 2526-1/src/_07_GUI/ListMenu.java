package _07_GUI;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

/**
 * Lista personalizada utilizada para representar el menú lateral de la aplicación.
 * 
 * Cada elemento de la lista se renderiza como un {@link MenuItem}, utilizando la
 * información contenida en un objeto {@link Model_Menu}. Esta clase permite manejar
 * la selección visual de los elementos del menú y generar una interfaz moderna y dinámica.
 *
 * @param <E> tipo de elementos del menú, generalmente {@link Model_Menu}
 * 
 * @author Danaz
 */
public class ListMenu<E extends Object> extends JList<E> {

    /** Modelo de datos de la lista. */
    private final DefaultListModel<E> model;

    /** Índice del elemento actualmente seleccionado. */
    private int selectedIndex = -1;

    /**
     * Crea una nueva lista de menú con soporte para elementos personalizados.
     */
    public ListMenu() {
        model = new DefaultListModel<>();
        setModel(model);
        setOpaque(false);

        // Listener para manejar la selección con clic izquierdo
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    int index = locationToIndex(me.getPoint());
                    if (index >= 0 && index < model.size()) {
                        Object o = model.getElementAt(index);
                        if (o instanceof Model_Menu menu && menu.getType() == Model_Menu.MenuType.MENU) {
                            selectedIndex = index;
                        } else {
                            selectedIndex = index;
                        }
                        repaint();
                    }
                }
            }
        });
    }

    /**
     * Devuelve el renderizador personalizado que convierte cada elemento de la lista
     * en un componente visual {@link MenuItem}.
     *
     * @return un renderizador de celdas personalizado
     */
    @Override
    public ListCellRenderer<? super E> getCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean selected, boolean hasFocus) {

                Model_Menu data;

                if (value instanceof Model_Menu) {
                    data = (Model_Menu) value;
                } else {
                    data = new Model_Menu("", String.valueOf(value), Model_Menu.MenuType.EMPTY);
                }

                MenuItem item = new MenuItem(data);
                item.setSelected(selectedIndex == index);
                return item;
            }
        };
    }

    /**
     * Agrega un nuevo elemento al menú.
     *
     * @param data objeto {@link Model_Menu} a añadir
     */
    public void addItem(Model_Menu data) {
        model.addElement((E) data);
    }

    /**
     * Limpia todos los elementos del menú.
     */
    public void clearItems() {
        model.clear();
        selectedIndex = -1;
        repaint();
    }

    /**
     * Devuelve el elemento actualmente seleccionado.
     *
     * @return el objeto {@link Model_Menu} seleccionado, o {@code null} si no hay selección
     */
    public Model_Menu getSelectedMenuItem() {
        if (selectedIndex >= 0 && selectedIndex < model.size()) {
            Object o = model.get(selectedIndex);
            if (o instanceof Model_Menu menu) {
                return menu;
            }
        }
        return null;
    }
}