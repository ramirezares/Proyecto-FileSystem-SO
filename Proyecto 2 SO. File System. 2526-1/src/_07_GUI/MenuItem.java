package _07_GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Representa un elemento individual dentro del menú lateral.
 *
 * 
 * Un {@code MenuItem} puede representar distintos tipos de ítems definidos en
 * {@link Model_Menu.MenuType}, como:
 * 
 * MENU: Opción seleccionable con ícono y nombre.
 * TITLE: Título de sección dentro del menú.
 * EMPTY: Espaciador visual sin texto ni ícono.
 * 
 *
 * 
 * El componente adapta su estilo según el tipo de ítem recibido en el
 * constructor y permite resaltar la selección mediante el atributo
 * {@code selected}.
 *
 * @author Danaz
 */
public class MenuItem extends javax.swing.JPanel {

    /**
     * Indica si el elemento del menú se encuentra seleccionado.
     */
    private boolean selected;

    /**
     * Crea un nuevo elemento del menú basado en la información proporcionada.
     *
     * @param data modelo que contiene el ícono, nombre y tipo del elemento.
     */
    public MenuItem(Model_Menu data) {
        initComponents();
        setOpaque(false);

        if (data.getType() == null) {
            lbName.setText(" ");
        } else {
            switch (data.getType()) {
                case MENU -> {
                    lbIcon.setIcon(data.toIcon());
                    lbName.setText(data.getName());
                    lbName.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    lbName.setForeground(Color.WHITE);
                }
                case TITLE -> {
                    lbIcon.setText(data.getName());
                    lbIcon.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    lbIcon.setForeground(Color.WHITE);
                    lbName.setVisible(false);
                }
                default ->
                    lbName.setText(" ");
            }
        }
    }

    /**
     * Define si el elemento se encuentra seleccionado. Cambia el estado visual
     * del ítem.
     *
     * @param selected {@code true} si el elemento está seleccionado.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    /**
     * Pinta un efecto visual de selección si el ítem está seleccionado.
     *
     * @param g el contexto gráfico utilizado para dibujar el componente.
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (selected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 40)); // fondo semitransparente
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        }
        super.paintComponent(g);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbIcon = new javax.swing.JLabel();
        lbName = new javax.swing.JLabel();

        lbIcon.setBackground(new java.awt.Color(255, 255, 255));
        lbIcon.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbIcon.setForeground(new java.awt.Color(255, 255, 255));

        lbName.setBackground(new java.awt.Color(255, 255, 255));
        lbName.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbName.setForeground(new java.awt.Color(255, 255, 255));
        lbName.setText("Menu Name");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lbIcon)
                .addGap(18, 18, 18)
                .addComponent(lbName)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lbName, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lbIcon;
    private javax.swing.JLabel lbName;
    // End of variables declaration//GEN-END:variables
}
