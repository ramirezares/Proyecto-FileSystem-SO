package _07_GUI;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JFrame;

/**
 * Panel lateral que contiene el menú principal de navegación del programa.
 *
 * Este panel gestiona la interfaz visual del menú, incluyendo:
 * 
 * Los botones de navegación: Simulación, Configuración y Gráficos.
 * Un encabezado con el título del proyecto.
 * El fondo con efecto de degradado vertical.
 * El movimiento del marco principal (ventana sin bordes).
 * 
 *
 *
 * El {@code MenuPanel} interactúa directamente con {@link MainJframe1} para
 * cambiar entre las distintas vistas del programa según el botón
 * seleccionado.
 *
 * @author Danaz
 */
public class MenuPanel extends javax.swing.JPanel {

    /**
     * Coordenada X del ratón al presionar sobre el panel (para movimiento).
     */
    private int x;

    /**
     * Coordenada Y del ratón al presionar sobre el panel (para movimiento).
     */
    private int y;

    /**
     * Referencia al JFrame principal del programa.
     */
    private MainJFrame mainFrame;

    /**
     * Constructor del panel de menú lateral. Inicializa componentes visuales y
     * la lista de opciones.
     */
    public MenuPanel() {
        initComponents();
        setOpaque(false);
        listMenu1.setOpaque(false);
        init();
    }

    /**
     * Asigna el marco principal para permitir el cambio de paneles.
     *
     * @param mainFrame instancia del JFrame principal.
     */
    public void setMainFrame(MainJFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Inicializa los elementos del menú y sus acciones asociadas.
     */
    private void init() {
        // Botones principales del programa
        //listMenu1.addItem(new Model_Menu("CpuIcon1-white", "Simulación", Model_Menu.MenuType.MENU));
        //listMenu1.addItem(new Model_Menu("CpuIcon1-white", "Configuración", Model_Menu.MenuType.MENU));

        // Listener para detectar cambios de selección y cambiar de panel
        //listMenu1.addListSelectionListener(e -> {
        //    if (!e.getValueIsAdjusting() && mainFrame != null) {
        //        int selectedIndex = listMenu1.getSelectedIndex();
        //        switch (selectedIndex) {
        //            case 0 ->
        //                mainFrame.switchToPanel("simulation");
        //            case 1 ->
        //                mainFrame.switchToPanel("simulation2");
        //            case 2 ->
        //                mainFrame.switchToPanel("graphics");
        //        }
        //    }
        //});
    }

    // =======================================================================
    //                           MÉTODOS VISUALES
    // =======================================================================
    /**
     * Pinta el fondo del panel con un degradado vertical azul oscuro.
     *
     * @param grphcs el contexto gráfico sobre el cual se dibuja el panel.
     */
    @Override
    protected void paintChildren(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(0, 0, Color.decode("#000046"),
                0, getHeight(), Color.decode("#1CB5E0"));
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
        g2.fillRect(getWidth() - 20, 0, getWidth(), getHeight());
        super.paintChildren(grphcs);
    }

    /**
     * Permite mover la ventana principal al arrastrar con el mouse sobre el
     * panel superior.
     *
     * @param frame Ventana principal a mover.
     */
    public void initMoving(JFrame frame) {
        panelMoving.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                x = me.getX();
                y = me.getY();
            }
        });

        panelMoving.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                frame.setLocation(me.getXOnScreen() - x, me.getYOnScreen() - y);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMoving = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        listMenu1 = new _07_GUI.ListMenu<>();
        jLabel2 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

        panelMoving.setOpaque(false);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("<html>Simulación Sistema<br>de Archivo</html>");

        javax.swing.GroupLayout panelMovingLayout = new javax.swing.GroupLayout(panelMoving);
        panelMoving.setLayout(panelMovingLayout);
        panelMovingLayout.setHorizontalGroup(
            panelMovingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMovingLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelMovingLayout.setVerticalGroup(
            panelMovingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMovingLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        listMenu1.setForeground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("<html>Hecho por:<br>Ares Ramírez<br>Daniela Zambrano</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(listMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMoving, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMoving, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private _07_GUI.ListMenu<String> listMenu1;
    private javax.swing.JPanel panelMoving;
    // End of variables declaration//GEN-END:variables
}
