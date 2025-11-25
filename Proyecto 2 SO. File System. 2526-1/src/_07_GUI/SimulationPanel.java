/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package _07_GUI;

import _01_ApplicationPackage.Simulator;
import _02_DataStructures.SimpleList;
import _02_DataStructures.SimpleNode;
import _04_OperatingSystem.Directory;
import _04_OperatingSystem.File;
import _04_OperatingSystem.IOPetition;
import _04_OperatingSystem.OperatingSystem;
import _04_OperatingSystem.Process1;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author AresR
 */
public class SimulationPanel extends javax.swing.JPanel {

    private Simulator simulator;

    // Componentes UI
    private JPanel processesPanel; // Panel con scroll para los PCB
    private JPanel petitionsQueuePanel; 
    
    private DefaultTableModel petitionsTableModel; // Modelo para la tabla de peticiones
    private DefaultTableModel diskTableModel; // Modelo para visualizar bloques del disco
    private JTree dirTree; // Árbol de directorios
    private DefaultTreeModel treeModel;
    private JLabel currentOperationLabel; // Mostrar qué hace el FileSystem ahora

    /**
     * Vincula el simulador con este panel.
     *
     * @param simulator instancia del simulador.
     */
    
    /**
     * Creates new form SimulationPanel
     */
    public SimulationPanel() {
        initComponents();
        
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP: Controles ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnStart = new JButton("Iniciar Simulación");
        JButton btnLoadTest = new JButton("Cargar Datos Prueba");
        
        btnStart.addActionListener(e -> {
            if (simulator != null) simulator.startSimulation();
        });
        
        btnLoadTest.addActionListener(e -> {
            if (simulator != null) simulator.createTestLoad();
        });

        topPanel.add(btnStart);
        topPanel.add(btnLoadTest);
        this.add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Dividido en Izquierda (SO) y Derecha (FileSystem) ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // 1. PANEL IZQUIERDO: SISTEMA OPERATIVO
        JPanel osPanel = new JPanel(new BorderLayout());
        osPanel.setBorder(BorderFactory.createTitledBorder("Sistema Operativo - Cola de Procesos"));
        
        processesPanel = new JPanel();
        processesPanel.setLayout(new BoxLayout(processesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollProc = new JScrollPane(processesPanel);
        osPanel.add(scrollProc, BorderLayout.CENTER);
        
        centerPanel.add(osPanel);

        // 2. PANEL DERECHO: FILESYSTEM
        JPanel fsPanel = new JPanel(new BorderLayout());
        fsPanel.setBorder(BorderFactory.createTitledBorder("Sistema de Archivos"));
        
        // Sub-panel superior: Estado actual y Cola de Peticiones
        JPanel fsTop = new JPanel(new BorderLayout());
        currentOperationLabel = new JLabel("Estado: Inactivo");
        currentOperationLabel.setFont(currentOperationLabel.getFont().deriveFont(java.awt.Font.BOLD));
        currentOperationLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fsTop.add(currentOperationLabel, BorderLayout.NORTH);

        petitionsQueuePanel = new JPanel();
        petitionsQueuePanel.setLayout(new BoxLayout(petitionsQueuePanel, BoxLayout.Y_AXIS)); // Vertical
        JScrollPane scrollPet = new JScrollPane(petitionsQueuePanel);
        scrollPet.setBorder(BorderFactory.createTitledBorder("Cola de Peticiones E/S"));
        scrollPet.setPreferredSize(new java.awt.Dimension(300, 200)); // Darle altura
        fsTop.add(scrollPet, BorderLayout.CENTER);
                
        fsPanel.add(fsTop, BorderLayout.NORTH);

        // Sub-panel central: Árbol de Directorios y Disco
        JPanel fsCenter = new JPanel(new GridLayout(1, 2, 5, 0));
        
        // Árbol
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        treeModel = new DefaultTreeModel(rootNode);
        dirTree = new JTree(treeModel);
        JScrollPane scrollTree = new JScrollPane(dirTree);
        scrollTree.setBorder(BorderFactory.createTitledBorder("Directorios"));
        fsCenter.add(scrollTree);

        // Disco (Tabla simple de bloques)
        String[] colDisk = {"Bloque", "Estado", "Archivo"};
        diskTableModel = new DefaultTableModel(colDisk, 0);
        JTable tblDisk = new JTable(diskTableModel);
        JScrollPane scrollDisk = new JScrollPane(tblDisk);
        scrollDisk.setBorder(BorderFactory.createTitledBorder("Bloques de Disco"));
        fsCenter.add(scrollDisk);

        fsPanel.add(fsCenter, BorderLayout.CENTER);
        
        centerPanel.add(fsPanel);

        this.add(centerPanel, BorderLayout.CENTER);
    }
    
    public void setSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(1400, 600));
        setRequestFocusEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1111, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 591, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    
    // Metodos
    
    /**
     * Actualiza toda la interfaz gráfica con el estado actual del SO.
     * Se debe llamar dentro del hilo de Swing (invokeLater).
     */
    public void updateView(OperatingSystem so) {
        updateProcessQueue(so);
        updateFileSystemInfo(so);
    }

    private void updateProcessQueue(OperatingSystem so) {
        processesPanel.removeAll();
        SimpleList<Process1> allProcs = so.getAllProcessesQueue();
        
        if (allProcs != null && !allProcs.isEmpty()) {
            SimpleNode<Process1> node = allProcs.GetpFirst();
            while (node != null) {
                // Usamos tu clase PCBPanel existente
                PCBPanel pcb = new PCBPanel(node.GetData());
                // Añadimos un pequeño espacio
                pcb.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 2, 2, 2),
                        pcb.getBorder()));
                processesPanel.add(pcb);
                node = node.GetNxt();
            }
        }
        processesPanel.revalidate();
        processesPanel.repaint();
    }

    private void updateFileSystemInfo(OperatingSystem so) {
        // 1. Petición Actual
        IOPetition current = so.getFileSystem().getCurrentPetition();
        if (current != null) {
            currentOperationLabel.setText("Procesando: " + current.getAction() + " sobre " + current.getCatalog().getName());
            currentOperationLabel.setForeground(new Color(0, 100, 0));
        } else {
            currentOperationLabel.setText("Estado: Esperando peticiones...");
            currentOperationLabel.setForeground(Color.GRAY);
        }

        // 2. Tabla de Peticiones
        // 2. Cola de Peticiones (VISUAL con PetitionPanel)
        petitionsQueuePanel.removeAll(); // Limpiar paneles anteriores
        
        SimpleList<IOPetition> queue = so.getFileSystem().getPetitions();
        if (queue != null && !queue.isEmpty()) {
            SimpleNode<IOPetition> node = queue.GetpFirst();
            while (node != null) {
                IOPetition p = node.GetData();
                
                // Crear la tarjeta visual para esta petición
                PetitionPanel pPanel = new PetitionPanel(p);
                
                // Añadir margen visual entre tarjetas
                pPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(3, 3, 3, 3), // Margen exterior
                        pPanel.getBorder())); // Borde original
                
                petitionsQueuePanel.add(pPanel);
                
                node = node.GetNxt();
            }
        }
        petitionsQueuePanel.revalidate();
        petitionsQueuePanel.repaint();

        // 3. Disco (Bloques)
        // Asumimos que getFileSystem().getDisk().getListOfBlocks() devuelve SimpleList<Block>
        // Esto es una visualización simplificada
        /* Nota: Implementar esto requiere recorrer la lista de bloques del disco
           y llenar diskTableModel. Similar a petitionsTableModel.
        */
        
        // 4. Árbol de Directorios (JTree)
        // Reconstruir el árbol desde el rootDirectory del DiskHandler
        Directory rootDir = so.getFileSystem().getDiskHandler().getRootDirectory();
        if (rootDir != null) {
            DefaultMutableTreeNode rootVisual = new DefaultMutableTreeNode(rootDir.getName());
            buildTreeRecursive(rootVisual, rootDir);
            treeModel.setRoot(rootVisual);
            treeModel.reload();
        }
    }

    // Helper recursivo para el JTree
    private void buildTreeRecursive(DefaultMutableTreeNode visualNode, Directory logicalDir) {
        // Añadir archivos
        SimpleNode<File> fileNode = logicalDir.getFiles().GetpFirst();
        while(fileNode != null) {
            File f = fileNode.GetData();
            visualNode.add(new DefaultMutableTreeNode(f.getName() + " [F]"));
            fileNode = fileNode.GetNxt();
        }
        
        // Añadir subdirectorios (y llamar recursivamente)
        SimpleNode<Directory> dirNode = logicalDir.getSubDirectories().GetpFirst();
        while(dirNode != null) {
            Directory d = dirNode.GetData();
            DefaultMutableTreeNode subDirVisual = new DefaultMutableTreeNode(d.getName());
            visualNode.add(subDirVisual);
            buildTreeRecursive(subDirVisual, d); // Recursión
            dirNode = dirNode.GetNxt();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
