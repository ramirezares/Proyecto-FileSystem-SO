package _07_GUI;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;

/**
 * Panel visual que representa de forma simplificada el PCB (Process Control Block)
 * de un proceso dentro del simulador del sistema operativo.
 * 
 * Muestra información básica: nombre, tipo e instrucciones del proceso.
 * 
 * @author Danaz
 */
public class SimplePCBPanel extends javax.swing.JPanel {


    /**
     * Crea un panel visual compacto que muestra los datos principales de un proceso.
     *
     * @param name Nombre del proceso.
     * @param type Tipo de proceso (CPU Bound / IO Bound).
     * @param instructions Cantidad total de instrucciones.
     */
    public SimplePCBPanel(String name, String type, int instructions) {
        // Inicializar etiquetas
        nameLabel = new JLabel("Nombre: " + name);
        typeLabel = new JLabel("Tipo: " + type);
        instructionsLabel = new JLabel("Instrucciones: " + instructions);

        initUI();
    }

    /**
     * Configura la apariencia visual y disposición de los elementos del panel.
     */
    private void initUI() {
        // Configuración general del panel
        setBackground(new Color(153,204,255)); // Gris oscuro
        setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2, true));
        setPreferredSize(new Dimension(160, 100));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Configuración de fuente y color de texto
        Font font = new Font("Segoe UI", Font.PLAIN, 12);
        Color textColor = Color.BLACK;

        // Aplicar formato a las etiquetas
        for (JLabel label : new JLabel[]{nameLabel, typeLabel, instructionsLabel}) {
            label.setFont(font);
            label.setForeground(textColor);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(label);
            add(Box.createVerticalStrut(3)); // Espacio entre líneas
        }
    }


   

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        instructionsLabel = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(153, 204, 255));

        typeLabel.setForeground(new java.awt.Color(0, 0, 0));
        typeLabel.setText("Type:");

        nameLabel.setForeground(new java.awt.Color(0, 0, 0));
        nameLabel.setText("Nombre:");

        instructionsLabel.setForeground(new java.awt.Color(0, 0, 0));
        instructionsLabel.setText("PC:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(instructionsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(typeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(instructionsLabel)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel instructionsLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
