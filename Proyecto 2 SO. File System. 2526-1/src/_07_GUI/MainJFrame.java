/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package _07_GUI;

import _01_ApplicationPackage.Simulator;
import _02_DataStructures.SimpleList;
import _02_DataStructures.SimpleNode;
import _03_LowLevelAbstractions.Disk;
import _04_OperatingSystem.AllocationTable;
import _04_OperatingSystem.Block;
import _04_OperatingSystem.Catalog;
import _04_OperatingSystem.Directory;
import _04_OperatingSystem.DiskHandler;
import _04_OperatingSystem.DiskPolicyType;
import _04_OperatingSystem.File_Proyect;
import _04_OperatingSystem.FileSystem;
import _04_OperatingSystem.IOAction;
import _04_OperatingSystem.IOPetition;
import _04_OperatingSystem.OperatingSystem;
import _04_OperatingSystem.Process1;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Danaz
 */
public class MainJFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainJFrame.class.getName());
    private Simulator simulator;
    private OperatingSystem so;
    private DefaultTreeModel treeModel;
    private Timer uiTimer;
    private List<FSItem> fileSystemItems = new ArrayList<>();

    private String selectedPath = null;
    private boolean selectedIsFile = false;

    public void setSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    public Simulator getSimulator() {
        return simulator;
    }

    public OperatingSystem getSo() {
        return so;
    }

    public MainJFrame() {
        initComponents();

        setTitle("Simulación Sistema de Archivos");
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        menuPanel1.initMoving(MainJFrame.this);

        // Crear el OS y el simulador:
        this.simulator = new Simulator(this);
        this.so = simulator.getSo();

        //Imagen de la aplicacion
        String rutaIcono = "/_08_SourcesGUI/CpuIcon-blue.png";

        try {
            // Carga la imagen desde la ruta relativa del proyecto
            Image icono = new ImageIcon(getClass().getResource(rutaIcono)).getImage();

            // Establece la imagen como el icono de la ventana
            this.setIconImage(icono);

        } catch (NullPointerException e) {
            System.err.println("No se encontró el archivo de imagen en la ruta: " + rutaIcono);

        }

        blocksSpinner.setModel(new SpinnerNumberModel(1, 1, 32, 1));

        processPanel = new JPanel();
        processPanel.setLayout(new BoxLayout(processPanel, BoxLayout.Y_AXIS));
        processScroll.setViewportView(processPanel);

        petitionPanel = new JPanel();
        petitionPanel.setLayout(new BoxLayout(petitionPanel, BoxLayout.Y_AXIS)); // Vertical
        petitionScroll.setViewportView(petitionPanel);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        treeModel = new DefaultTreeModel(root);
        jTree1.setModel(treeModel);
        // Listener para selección en el JTree
        jTree1.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node
                    = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();

            if (node == null) {
                selectedPath = null;
                selectedIsFile = false;
                nodeSeclectedLabel.setText("...");
                return;
            }

            String label = node.toString();
            nodeSeclectedLabel.setText(label); // Aquí se muestra el nombre seleccionado

            // Construir ruta "root/User1/Tesis.docx [F]"
            TreeNode[] pathNodes = node.getPath();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pathNodes.length; i++) {
                if (i > 0) {
                    sb.append("/");
                }
                sb.append(pathNodes[i].toString());
            }
            selectedPath = sb.toString();

            // Si termina en " [F]" lo consideramos archivo
            selectedIsFile = label.endsWith(" [F]");
        });

        updateDiskBlocks(so.getFileSystem().getDisk());

        uiTimer = new Timer(300, e -> {
            if (simulator != null && simulator.getSo() != null) {
                OperatingSystem soRef = simulator.getSo();
                updateFileSystemInfo(soRef);
                updateFileSystemTree(soRef);
                updateQueues(soRef);
                updateAllocationTable();
                updateDiskBlocks(soRef.getFileSystem().getDisk());
            }
        });

    }

    private String buildPathFromNode(DefaultMutableTreeNode node) {
        TreeNode[] nodes = node.getPath();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < nodes.length; i++) {
            sb.append(nodes[i].toString());
            if (i < nodes.length - 1) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    public void updateFileSystemInfo(OperatingSystem so) {

        if (so == null || so.getFileSystem() == null) {
            return;
        }

        FileSystem fs = so.getFileSystem();

        // PETICIÓN ACTUAL
        IOPetition current = fs.getCurrentPetition();
        if (current != null) {
            currentPetitionLabel.setText("Procesando: " + current.getAction() + " sobre " + current.getCatalog().getName());
            currentPetitionLabel.setForeground(new Color(0, 100, 0));
        } else {
            currentPetitionLabel.setText("Estado: Esperando peticiones...");
            currentPetitionLabel.setForeground(Color.GRAY);
        }
    }

    public void refreshUI(OperatingSystem so) {

        if (so == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {

            // 1. Actualizar estado del FileSystem
            updateFileSystemInfo(so);

            // 2. Actualizar el árbol de directorios
            updateFileSystemTree(so);

            // 3. Colas del sistema operativo (ready, blocked, terminated)
            updateQueues(so);

            // 4. Tabla de asignación
            updateAllocationTable();

            // 5. Bloques del disco
            updateDiskBlocks(simulator.getSo().getFileSystem().getDisk());
        });
    }

    public void updateDiskBlocks(Disk disk) {

        blocksPanel.removeAll();

        if (disk == null || disk.getListOfBlocks() == null) {
            blocksPanel.revalidate();
            blocksPanel.repaint();
            return;
        }

        blocksPanel.setLayout(new GridLayout(4, 8, 4, 4));

        SimpleNode<Block> node = disk.getListOfBlocks().GetpFirst();

        while (node != null) {

            Block b = node.GetData();

            // === PANEL DEL BLOQUE ===
            JPanel blockCard = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    // Color garantizado
                    if (b.isState()) {
                        g.setColor(new Color(0xFF, 0xD2, 0x8A)); // naranja claro
                    } else {
                        g.setColor(new Color(0xAD, 0xD8, 0xE6)); // azul claro
                    }
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };

            blockCard.setLayout(new BoxLayout(blockCard, BoxLayout.Y_AXIS));
            blockCard.setOpaque(true);
            blockCard.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            blockCard.setPreferredSize(new Dimension(110, 70));

            // Texto
            JLabel id = new JLabel("B" + b.getBlockID());
            JLabel state = new JLabel((b.isState() ? "Ocupado" : "Libre"));
            JLabel file = new JLabel((b.getFileReference() != null ? b.getFileReference().getName() : "—"));

            id.setOpaque(false);
            state.setOpaque(false);
            file.setOpaque(false);

            blockCard.add(id);
            blockCard.add(state);
            blockCard.add(file);

            blocksPanel.add(blockCard);

            node = node.GetNxt();
        }

        blocksPanel.revalidate();
        blocksPanel.repaint();
    }

    public void updateAllocationTable() {

        AllocationTable at = simulator.getSo().getFileSystem().getAllocationTable();
        SimpleList<File_Proyect> files = at.getFiles();
        DefaultTableModel model = (DefaultTableModel) tablaAsignacion.getModel();
        model.setRowCount(0);

        SimpleNode node = files.GetpFirst();

        while (node != null) {
            File_Proyect f = (File_Proyect) node.GetData();

            String name = f.getName();
            int blocks = f.getNumberOfBlocks();
            int firstBlock = (f.getFirstBlock() != null)
                    ? f.getFirstBlock().getBlockID() : -1;

            model.addRow(new Object[]{
                name,
                blocks,
                firstBlock
            });

            node = node.GetNxt();
        }
    }

    private void updateFileSystemTree(OperatingSystem so) {
        if (so == null || so.getFileSystem() == null) {
            return;
        }

        DiskHandler dh = so.getFileSystem().getDiskHandler();
        if (dh == null || dh.getRootDirectory() == null) {
            return;
        }

        // Guardar selección actual
        TreePath selectedTreePath = jTree1.getSelectionPath();
        String previouslySelected = null;
        if (selectedTreePath != null) {
            DefaultMutableTreeNode selectedNode
                    = (DefaultMutableTreeNode) selectedTreePath.getLastPathComponent();
            previouslySelected = buildPathFromNode(selectedNode);
        }

        // Reconstruir árbol
        Directory root = dh.getRootDirectory();
        DefaultMutableTreeNode rootVisual = new DefaultMutableTreeNode(root.getName());
        buildTreeRecursive(rootVisual, root);

        treeModel.setRoot(rootVisual);
        treeModel.reload();
        jTree1.setModel(treeModel);

        // Restaurar selección si existía
        if (previouslySelected != null) {
            DefaultMutableTreeNode nodeToSelect = findNodeByPath(rootVisual, previouslySelected);
            if (nodeToSelect != null) {
                TreePath pathToSelect = new TreePath(nodeToSelect.getPath());
                jTree1.setSelectionPath(pathToSelect);
                jTree1.scrollPathToVisible(pathToSelect);
            }
        }
        // Restaurar selección si existía
        if (previouslySelected != null) {
            DefaultMutableTreeNode nodeToSelect = findNodeByPath(rootVisual, previouslySelected);
            if (nodeToSelect != null) {
                TreePath pathToSelect = new TreePath(nodeToSelect.getPath());
                jTree1.setSelectionPath(pathToSelect);
                jTree1.scrollPathToVisible(pathToSelect);

                // Actualizar JLabel con la selección restaurada
                updateSelectedLabel(nodeToSelect);
            } else {
                nodeSeclectedLabel1.setText("Nada seleccionado");
                nodeSeclectedLabel2.setText("Nada seleccionado");
            }
        } else {
            nodeSeclectedLabel1.setText("Nada seleccionado");
            nodeSeclectedLabel2.setText("Nada seleccionado");
        }

    }

    private void updateSelectedLabel(DefaultMutableTreeNode node) {
        if (node == null) {
            nodeSeclectedLabel1.setText("Nada seleccionado");
            nodeSeclectedLabel2.setText("Nada seleccionado");
            return;
        }

        Object userObject = node.getUserObject(); // o node.toString() si no usas userObject
        boolean isFile = false;

        if (node.isLeaf()) {
            isFile = true; // o según tu lógica File vs Folder
        }
        nodeSeclectedLabel1.setText(userObject + (isFile ? " [Archivo]" : " [Carpeta]"));
        nodeSeclectedLabel2.setText(userObject + (isFile ? " [Archivo]" : " [Carpeta]"));
    }

    // Helper recursivo para el JTree
    private void buildTreeRecursive(DefaultMutableTreeNode visualNode, Directory logicalDir) {
        // Añadir archivos
        SimpleNode<File_Proyect> fileNode = logicalDir.getFiles().GetpFirst();
        while (fileNode != null) {
            File_Proyect f = fileNode.GetData();
            visualNode.add(new DefaultMutableTreeNode(f.getName() + " [F]"));
            fileNode = fileNode.GetNxt();
        }

        // Añadir subdirectorios (y llamar recursivamente)
        SimpleNode<Directory> dirNode = logicalDir.getSubDirectories().GetpFirst();
        while (dirNode != null) {
            Directory d = dirNode.GetData();
            DefaultMutableTreeNode subDirVisual = new DefaultMutableTreeNode(d.getName());
            visualNode.add(subDirVisual);
            buildTreeRecursive(subDirVisual, d); // Recursión
            dirNode = dirNode.GetNxt();
        }
    }

    private DefaultMutableTreeNode findNodeByPath(DefaultMutableTreeNode root, String path) {
        if (root == null || path == null) {
            return null;
        }

        String[] parts = path.split("/");
        return findNodeRecursive(root, parts, 0);
    }

    private DefaultMutableTreeNode findNodeRecursive(DefaultMutableTreeNode current, String[] parts, int index) {
        if (index >= parts.length) {
            return current;
        }
        if (!current.toString().equals(parts[index])) {
            return null;
        }

        if (index == parts.length - 1) {
            return current; // nodo final encontrado
        }
        // Recorrer hijos
        for (int i = 0; i < current.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) current.getChildAt(i);
            DefaultMutableTreeNode found = findNodeRecursive(child, parts, index + 1);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Actualiza las colas visuales a partir de los datos del OperatingSystem.
     *
     * @param so instancia del sistema operativo que contiene las colas.
     */
    public void updateQueues(OperatingSystem so) {
        if (so == null) {
            return;
        }

        processPanel.removeAll();
        petitionPanel.removeAll();

        updateProcessPanel(processPanel, so.getAllProcessesQueue());
        updatePetitionPanel(petitionPanel, so.getFileSystem().getPetitions());

        processPanel.revalidate();
        processPanel.repaint();
        petitionPanel.revalidate();
        petitionPanel.repaint();
    }

    private void updatePetitionPanel(JPanel targetPanel, SimpleList<IOPetition> queue) {

        if (queue == null || queue.isEmpty()) {
            return;
        }

        SimpleNode<IOPetition> node = queue.GetpFirst();
        while (node != null) {
            IOPetition p = node.GetData();
            PetitionPanel pPanel = new PetitionPanel(p);

            pPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(3, 3, 3, 3),
                    pPanel.getBorder()
            ));

            targetPanel.add(pPanel);
            node = node.GetNxt();
        }
    }

    /**
     * Agrega PCBPanels a un panel destino a partir de una SimpleList de
     * procesos.
     *
     * @param targetPanel panel contenedor donde se añadirán los PCB.
     * @param list lista simple de procesos (puede ser null).
     */
    private void updateProcessPanel(JPanel targetPanel, SimpleList<Process1> list) {
        if (list == null) {
            return;
        }
        SimpleNode<Process1> node = list.GetpFirst();
        while (node != null) {
            PCBPanel pcb = new PCBPanel(node.GetData());
            targetPanel.add(pcb);
            node = node.GetNxt();
        }
    }

    /**
     * Reinicia la vista gráfica sin tocar los JScrollPane (solo limpia los
     * paneles internos).
     */
    public void resetView() {
        SwingUtilities.invokeLater(() -> {
            currentPetitionLabel.setText("...");
            nodeSeclectedLabel.setText("...");

            processPanel.removeAll();
            petitionPanel.removeAll();

            processPanel.revalidate();
            processPanel.repaint();
            petitionPanel.revalidate();
            petitionPanel.repaint();
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        petitionScroll = new javax.swing.JScrollPane();
        petitionPanel = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        processScroll = new javax.swing.JScrollPane();
        processPanel = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jtableScroll = new javax.swing.JScrollPane();
        tablaAsignacion = new javax.swing.JTable();
        jLabel26 = new javax.swing.JLabel();
        treeScroll = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jLabel22 = new javax.swing.JLabel();
        blocksPanel = new javax.swing.JPanel();
        currentPetitionLabel = new javax.swing.JLabel();
        politicsComboBox = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        startSimulation = new javax.swing.JToggleButton();
        resetSimulation = new javax.swing.JButton();
        generate20Process = new javax.swing.JButton();
        saveSimulation = new javax.swing.JButton();
        uploadSimulation = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        userComboBox = new javax.swing.JComboBox<>();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        startSimulation2 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        nameNewRecurso = new javax.swing.JTextField();
        blocksSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        editResource = new javax.swing.JToggleButton();
        nodeSeclectedLabel = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        nameNewRecurso1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        deleteResource = new javax.swing.JToggleButton();
        nodeSeclectedLabel1 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        readResource = new javax.swing.JToggleButton();
        nodeSeclectedLabel2 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        menuPanel1 = new _07_GUI.MenuPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 0, 70));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel24.setBackground(new java.awt.Color(255, 255, 255));
        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("Peticiones al Disco");
        jPanel1.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 20, -1, -1));

        javax.swing.GroupLayout petitionPanelLayout = new javax.swing.GroupLayout(petitionPanel);
        petitionPanel.setLayout(petitionPanelLayout);
        petitionPanelLayout.setHorizontalGroup(
            petitionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 188, Short.MAX_VALUE)
        );
        petitionPanelLayout.setVerticalGroup(
            petitionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 298, Short.MAX_VALUE)
        );

        petitionScroll.setViewportView(petitionPanel);

        jPanel1.add(petitionScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 50, 190, 300));

        jLabel23.setBackground(new java.awt.Color(255, 255, 255));
        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("Procesos del Sistema");
        jPanel1.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 20, -1, -1));

        javax.swing.GroupLayout processPanelLayout = new javax.swing.GroupLayout(processPanel);
        processPanel.setLayout(processPanelLayout);
        processPanelLayout.setHorizontalGroup(
            processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
        );
        processPanelLayout.setVerticalGroup(
            processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 298, Short.MAX_VALUE)
        );

        processScroll.setViewportView(processPanel);

        jPanel1.add(processScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 50, 180, 300));

        jLabel25.setBackground(new java.awt.Color(255, 255, 255));
        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("Tabla de Asignación");
        jPanel1.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 10, -1, -1));

        tablaAsignacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nombre", "Cantidad Bloques", "Dir. 1er Bloque"
            }
        ));
        jtableScroll.setViewportView(tablaAsignacion);

        jPanel1.add(jtableScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 40, 370, 310));

        jLabel26.setBackground(new java.awt.Color(255, 255, 255));
        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("Estructura de carpetas");
        jPanel1.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 20, -1, -1));

        treeScroll.setViewportView(jTree1);

        jPanel1.add(treeScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 50, 170, 300));

        jLabel22.setBackground(new java.awt.Color(255, 255, 255));
        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Bloques del SD");
        jPanel1.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 370, -1, -1));

        javax.swing.GroupLayout blocksPanelLayout = new javax.swing.GroupLayout(blocksPanel);
        blocksPanel.setLayout(blocksPanelLayout);
        blocksPanelLayout.setHorizontalGroup(
            blocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 590, Short.MAX_VALUE)
        );
        blocksPanelLayout.setVerticalGroup(
            blocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );

        jPanel1.add(blocksPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 400, 590, 260));

        currentPetitionLabel.setBackground(new java.awt.Color(255, 255, 255));
        currentPetitionLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        currentPetitionLabel.setForeground(new java.awt.Color(255, 255, 255));
        currentPetitionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        currentPetitionLabel.setText("...");
        jPanel1.add(currentPetitionLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 370, 420, -1));

        politicsComboBox.setBackground(new java.awt.Color(0, 0, 70));
        politicsComboBox.setForeground(new java.awt.Color(255, 255, 255));
        politicsComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FIFO", "SSTF", "SCAN", "LIFO" }));
        politicsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                politicsComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(politicsComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 460, 190, 30));

        jLabel27.setBackground(new java.awt.Color(255, 255, 255));
        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("<html>Política de planificación<br> de Disco</html>");
        jPanel1.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 410, 200, -1));

        startSimulation.setBackground(new java.awt.Color(0, 0, 70));
        startSimulation.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        startSimulation.setForeground(new java.awt.Color(255, 255, 255));
        startSimulation.setText("Iniciar Simulación");
        startSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startSimulationActionPerformed(evt);
            }
        });
        jPanel1.add(startSimulation, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 210, 40));

        resetSimulation.setBackground(new java.awt.Color(0, 0, 70));
        resetSimulation.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        resetSimulation.setForeground(new java.awt.Color(255, 255, 255));
        resetSimulation.setText("Reiniciar Simulación");
        resetSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetSimulationActionPerformed(evt);
            }
        });
        jPanel1.add(resetSimulation, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 210, 40));

        generate20Process.setBackground(new java.awt.Color(0, 0, 70));
        generate20Process.setFont(new java.awt.Font("Segoe UI", 3, 10)); // NOI18N
        generate20Process.setForeground(new java.awt.Color(255, 255, 255));
        generate20Process.setText("Generación Aleatoria Simulación");
        generate20Process.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generate20ProcessActionPerformed(evt);
            }
        });
        jPanel1.add(generate20Process, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 210, 40));

        saveSimulation.setBackground(new java.awt.Color(0, 0, 70));
        saveSimulation.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        saveSimulation.setForeground(new java.awt.Color(255, 255, 255));
        saveSimulation.setText("Guardar la simulación");
        saveSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSimulationActionPerformed(evt);
            }
        });
        jPanel1.add(saveSimulation, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 210, 40));

        uploadSimulation.setBackground(new java.awt.Color(0, 0, 70));
        uploadSimulation.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        uploadSimulation.setForeground(new java.awt.Color(255, 255, 255));
        uploadSimulation.setText("Precargar una simulación");
        uploadSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadSimulationActionPerformed(evt);
            }
        });
        jPanel1.add(uploadSimulation, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, 210, 40));

        jLabel36.setBackground(new java.awt.Color(255, 255, 255));
        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel36.setText("<html>Tipo de Usuario</html>");
        jPanel1.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 510, 200, -1));

        userComboBox.setBackground(new java.awt.Color(0, 0, 70));
        userComboBox.setForeground(new java.awt.Color(255, 255, 255));
        userComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "User 1", "User 2" }));
        userComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(userComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 540, 190, 30));

        startSimulation2.setBackground(new java.awt.Color(0, 0, 70));
        startSimulation2.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        startSimulation2.setForeground(new java.awt.Color(255, 255, 255));
        startSimulation2.setText("Crear Recurso");
        startSimulation2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startSimulation2ActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Nombre del Nuevo Recurso");

        nameNewRecurso.setText("Nombre del Recurso");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("N° Bloques del Nuevo Recurso");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("Tipo de Recurso");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Archivo", "Directorio", " " }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(startSimulation2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(95, 95, 95))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blocksSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameNewRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                .addGap(19, 19, 19))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameNewRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blocksSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(startSimulation2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("Crear Recurso", jPanel3);

        editResource.setBackground(new java.awt.Color(0, 0, 70));
        editResource.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        editResource.setForeground(new java.awt.Color(255, 255, 255));
        editResource.setText("Modificar Recurso");
        editResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editResourceActionPerformed(evt);
            }
        });

        nodeSeclectedLabel.setBackground(new java.awt.Color(255, 255, 255));
        nodeSeclectedLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        nodeSeclectedLabel.setForeground(new java.awt.Color(0, 0, 0));
        nodeSeclectedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nodeSeclectedLabel.setText("...");

        jLabel28.setBackground(new java.awt.Color(255, 255, 255));
        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(0, 0, 0));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Recurso Seleccionado:");

        nameNewRecurso1.setText("Nuevo Nombre");
        nameNewRecurso1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameNewRecurso1ActionPerformed(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Nuevo nombre del Recurso");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel28)
                    .addComponent(nodeSeclectedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameNewRecurso1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editResource, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeSeclectedLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameNewRecurso1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(editResource, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Modificar Recurso", jPanel4);

        deleteResource.setBackground(new java.awt.Color(0, 0, 70));
        deleteResource.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        deleteResource.setForeground(new java.awt.Color(255, 255, 255));
        deleteResource.setText("Eliminar Recurso");
        deleteResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteResourceActionPerformed(evt);
            }
        });

        nodeSeclectedLabel1.setBackground(new java.awt.Color(255, 255, 255));
        nodeSeclectedLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        nodeSeclectedLabel1.setForeground(new java.awt.Color(0, 0, 0));
        nodeSeclectedLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nodeSeclectedLabel1.setText("...");

        jLabel30.setBackground(new java.awt.Color(255, 255, 255));
        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(0, 0, 0));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setText("Recurso Seleccionado:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel30)
                    .addComponent(nodeSeclectedLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteResource, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(76, 76, 76))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeSeclectedLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(deleteResource, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Eliminar Recurso", jPanel5);

        readResource.setBackground(new java.awt.Color(0, 0, 70));
        readResource.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        readResource.setForeground(new java.awt.Color(255, 255, 255));
        readResource.setText("Leer Recurso");
        readResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readResourceActionPerformed(evt);
            }
        });

        nodeSeclectedLabel2.setBackground(new java.awt.Color(255, 255, 255));
        nodeSeclectedLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        nodeSeclectedLabel2.setForeground(new java.awt.Color(0, 0, 0));
        nodeSeclectedLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nodeSeclectedLabel2.setText("...");

        jLabel31.setBackground(new java.awt.Color(255, 255, 255));
        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(0, 0, 0));
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setText("Recurso Seleccionado:");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(readResource, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodeSeclectedLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeSeclectedLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(readResource, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Leer Recurso", jPanel6);

        jPanel1.add(jTabbedPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 400, 360, 260));

        jLabel29.setBackground(new java.awt.Color(255, 255, 255));
        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Petición Actual:");
        jPanel1.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 370, -1, -1));
        jPanel1.add(menuPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 680));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1250, 675));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSimulationActionPerformed
        if (startSimulation.isSelected()) {
            startSimulation.setText("Pausar simulación");
            simulator.startSimulation();
            if (uiTimer != null) {
                uiTimer.start();
            }
            uploadSimulation.setEnabled(false);
            resetSimulation.setEnabled(true);
        } else {
            startSimulation.setText("Iniciar simulación");
            simulator.stopSimulation();
            if (uiTimer != null) {
                uiTimer.stop();
            }
            uploadSimulation.setEnabled(true);
        }

    }//GEN-LAST:event_startSimulationActionPerformed


    private void uploadSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadSimulationActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Cargar Configuración de Archivos");

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line.trim());
                }
                reader.close();

                String json = jsonBuilder.toString();

                // 1. Extraer array "files"
                int start = json.indexOf("[");
                int end = json.lastIndexOf("]");

                if (start == -1 || end == -1) {
                    // Caso borde: JSON válido pero array vacío
                    if (json.contains("\"files\": []")) {
                        javax.swing.JOptionPane.showMessageDialog(this, "El archivo JSON no contiene archivos.");
                        return;
                    }
                    throw new RuntimeException("Formato JSON inválido o array no encontrado");
                }

                String itemsText = json.substring(start + 1, end).trim();

                if (itemsText.isEmpty()) {
                    javax.swing.JOptionPane.showMessageDialog(this, "El archivo JSON no contiene archivos.");
                    return;
                }

                // 2. Separar objetos
                // Regex mejorado para separar por "},"
                String[] objects = itemsText.split("\\},\\s*\\{");

                int loadedCount = 0;

                for (String obj : objects) {
                    // Limpieza básica
                    obj = obj.replace("{", "").replace("}", "");

                    // Valores por defecto
                    String name = "";
                    int user = 0;
                    int blocks = 0;
                    String path = "root";

                    // Parsear campos manualmente (simple pero efectivo para este formato)
                    String[] fields = obj.split(",");
                    for (String f : fields) {
                        String[] pair = f.split(":", 2);
                        if (pair.length < 2) {
                            continue;
                        }

                        String key = pair[0].trim().replace("\"", "");
                        String value = pair[1].trim().replace("\"", "");

                        switch (key) {
                            case "name":
                                name = value;
                                break;
                            case "user":
                                user = Integer.parseInt(value);
                                break;
                            case "blocks":
                                blocks = Integer.parseInt(value);
                                break;
                            case "path":
                                path = value;
                                break;
                        }
                    }

                    // 3. CREAR EL ARCHIVO EN EL SIMULADOR
                    // Usamos el OS del simulador para generar la petición real
                    if (!name.isEmpty() && blocks > 0) {
                        System.out.println("Cargando archivo: " + name + " en " + path);

                        // Crear el catálogo (IOAction.CREATE_FILE)
                        // Nota: 'path' es el directorio padre (nameOfDirectory en Catalog)
                        _04_OperatingSystem.Catalog cat = simulator.getSo().createCatalogForProcess(
                                _04_OperatingSystem.IOAction.CREATE_FILE,
                                path,
                                name,
                                "",
                                blocks,
                                user,
                                "File"
                        );

                        // Enviar el proceso al SO
                        simulator.getSo().newProcess(_04_OperatingSystem.IOAction.CREATE_FILE, cat);
                        loadedCount++;
                    }
                }

                javax.swing.JOptionPane.showMessageDialog(this, "Se han cargado " + loadedCount + " archivos exitosamente.\n(Los procesos de creación se han añadido a la cola)");

            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al cargar JSON: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

    }//GEN-LAST:event_uploadSimulationActionPerformed

    private void resetSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetSimulationActionPerformed
        if (simulator == null) {
            return;
        }

        // 1. Detener simulación actual
        if (simulator != null) {
            simulator.stopSimulation();
        }

        // 2. Cerrar ventana actual
        this.dispose();

        // 3. Volver a abrir desde cero
        java.awt.EventQueue.invokeLater(() -> {
            new MainJFrame().setVisible(true);
        });
    }//GEN-LAST:event_resetSimulationActionPerformed

    private void generate20ProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generate20ProcessActionPerformed
        if (simulator == null) {
            return;
        }

        simulator.createTestLoad(); // tu función de generación aleatoria

        // Después de crear procesos, refrescamos TODO lo visual
        OperatingSystem so = simulator.getSo();
        updateQueues(so);
        updateFileSystemInfo(so);
        updateAllocationTable();
        updateDiskBlocks(so.getFileSystem().getDisk());

    }//GEN-LAST:event_generate20ProcessActionPerformed

    private void readResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readResourceActionPerformed
        readResource.setSelected(false);
        if (simulator == null) {
            return;
        }

        if (selectedPath == null || selectedPath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un archivo o directorio en el árbol.",
                    "Leer recurso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        OperatingSystem so = simulator.getSo();
        FileSystem fs = so.getFileSystem();
        DiskHandler dh = fs.getDiskHandler();

        String[] parts = selectedPath.split("/");
        if (parts.length == 0) {
            return;
        }

        String last = parts[parts.length - 1];
        boolean isFile = selectedIsFile;

        String resourceName;
        String parentPath;

        if (isFile) {
            if (last.endsWith(" [F]")) {
                resourceName = last.substring(0, last.length() - 4);
            } else {
                resourceName = last;
            }
            if (parts.length > 1) {
                parentPath = String.join("/", java.util.Arrays.copyOf(parts, parts.length - 1));
            } else {
                parentPath = "root";
            }
        } else {
            resourceName = last;
            if (parts.length > 1) {
                parentPath = String.join("/", java.util.Arrays.copyOf(parts, parts.length - 1));
            } else {
                parentPath = "";
            }
        }

// Actualizar JLabel con la selección
        nodeSeclectedLabel2.setText("Seleccionado: " + resourceName);

        String user = (String) userComboBox.getSelectedItem();
        int userId = userComboBox.getSelectedIndex();

// --- CASO ADMIN: mostrar JOptionPane ---
        if ("Admin".equals(user)) {
            JOptionPane.showMessageDialog(this,
                    "Se abrió el recurso: " + resourceName,
                    "Lectura",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            // --- CASO USUARIO NORMAL: crear proceso en el SO ---
            if (isFile) {
                Catalog cat = so.createCatalogForProcess(
                        IOAction.READ_FILE,
                        parentPath,
                        resourceName,
                        "",
                        0,
                        userId,
                        "File"
                );
                so.newProcess(IOAction.READ_FILE, cat);
            } else {
                Catalog cat = so.createCatalogForProcess(
                        IOAction.READ_FILE,
                        parentPath,
                        resourceName,
                        "",
                        0,
                        userId,
                        "Directory"
                );
                so.newProcess(IOAction.READ_FILE, cat);
            }
        }

// Refrescar UI
        refreshUI(so);

    }//GEN-LAST:event_readResourceActionPerformed

    private void startSimulation2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSimulation2ActionPerformed
        startSimulation2.setSelected(false);
        if (simulator == null) {
            return;
        }

        if (selectedPath == null || selectedPath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un directorio en el árbol donde crear el recurso.",
                    "Crear recurso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newName = nameNewRecurso.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese el nombre del nuevo recurso.",
                    "Crear recurso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int blocks = (int) blocksSpinner.getValue();
        if (blocks <= 0) {
            JOptionPane.showMessageDialog(this,
                    "La cantidad de bloques debe ser mayor que cero.",
                    "Crear recurso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tipo de recurso (File o Directory)
        String type = (String) jComboBox1.getSelectedItem();
        boolean createFile = "File".equalsIgnoreCase(type);

        // Obtener SO y DiskHandler
        OperatingSystem so = simulator.getSo();
        FileSystem fs = so.getFileSystem();
        DiskHandler dh = fs.getDiskHandler();

        // selectedPath representa el directorio donde se creará el recurso
        String parentPath = selectedPath;

        // Evitar crear cosas dentro de archivos
        if (selectedIsFile) {
            JOptionPane.showMessageDialog(this,
                    "No se puede crear un recurso dentro de un archivo.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String user = (String) userComboBox.getSelectedItem();
        int userId = userComboBox.getSelectedIndex();

        // -----------------------------
        //        CASO ADMIN
        // -----------------------------
        if ("Admin".equals(user)) {

            Directory parentDir = dh.getDirectoryByPath(parentPath);
            if (parentDir == null) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró el directorio destino: " + parentPath,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Catalog cat = new Catalog(parentPath, newName, "", blocks, userId, type);

            if (createFile) {

                dh.createFile(cat, parentDir); //Aca archivo
            } else {
                dh.createDirectory(cat, parentDir); // Directorio
            }

        } // -----------------------------
        //        CASO USUARIO
        // -----------------------------
        else {

            IOAction action = createFile ? IOAction.CREATE_FILE : IOAction.CREATE_DIR;

            Catalog cat = so.createCatalogForProcess(
                    action, // acción a ejecutar
                    parentPath, // directorio padre donde crear
                    newName, // nombre del recurso
                    "", // newName no aplica para crear
                    blocks, // número de bloques asignados
                    userId, // user ID
                    type // tipo (File/Directory)
            );

            so.newProcess(action, cat);
        }
        nameNewRecurso.setText("");
        blocksSpinner.setValue(1);    // valor por defecto
        jComboBox1.setSelectedIndex(0); // File es el primer ítem normalmente
        refreshUI(so);

    }//GEN-LAST:event_startSimulation2ActionPerformed

    private void editResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editResourceActionPerformed
        editResource.setSelected(false);

        if (simulator == null) {
            return;
        }

        if (selectedPath == null || selectedPath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un archivo o directorio en el árbol.",
                    "Modificar recurso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newName = nameNewRecurso1.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un nuevo nombre para el recurso.",
                    "Modificar recurso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        OperatingSystem so = simulator.getSo();
        FileSystem fs = so.getFileSystem();
        DiskHandler dh = fs.getDiskHandler();

        String[] parts = selectedPath.split("/");
        if (parts.length == 0) {
            return;
        }

        String last = parts[parts.length - 1];
        boolean isFile = selectedIsFile;

        String resourceName;
        String parentPath;

        if (isFile) {
            resourceName = last.endsWith(" [F]") ? last.substring(0, last.length() - 4) : last;
            parentPath = (parts.length > 1) ? String.join("/", java.util.Arrays.copyOf(parts, parts.length - 1)) : "root";
        } else {
            resourceName = last;
            parentPath = (parts.length > 1) ? String.join("/", java.util.Arrays.copyOf(parts, parts.length - 1)) : "";
        }

        String user = (String) userComboBox.getSelectedItem();
        int userId = userComboBox.getSelectedIndex(); // 0=Admin,1=User1,2=User2

        if ("Admin".equals(user)) {
            // --- Admin: renombrar directo ---
            Directory parentDir = dh.getDirectoryByPath(parentPath);
            if (parentDir == null) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró el directorio padre: " + parentPath,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isFile) {
                File_Proyect file = parentDir.findFileByName(resourceName);
                if (file == null) {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró el archivo: " + resourceName,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Creamos un catalogo temporal para llamar updateFile
                Catalog cat = new Catalog(parentPath, resourceName, newName, 0, userId, "File");
                dh.executeOperation(new IOPetition(IOAction.UPDATE_FILE, cat, null));
            } else {
                Directory dir = parentDir.findSubDirectoryByName(resourceName);
                if (dir == null) {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró el directorio: " + resourceName,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Catalog cat = new Catalog(parentPath, resourceName, newName, 0, userId, "Directory");
                // Para directorios, podrías necesitar un método renameDirectoryDirect similar al de archivos
                // Si no existe, renombrar directamente
                dir.setName(newName);
                System.out.println("Admin: Directorio renombrado de " + resourceName + " a " + newName);
            }

        } else {
            // --- Usuario normal: crear proceso ---
            IOAction action = isFile ? IOAction.UPDATE_FILE : IOAction.UPDATE_FILE; // MODIFY_DIR no existe
            String type = isFile ? "File" : "Directory";

            Catalog cat = so.createCatalogForProcess(
                    action,
                    parentPath,
                    resourceName,
                    newName,
                    0,
                    userId,
                    type
            );
            so.newProcess(action, cat);
        }

        nameNewRecurso1.setText("Nuevo Nombre");
        refreshUI(so);
    }//GEN-LAST:event_editResourceActionPerformed

    private void deleteResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteResourceActionPerformed
        deleteResource.setSelected(false);
        if (simulator == null) {
            return;
        }

        if (selectedPath == null || selectedPath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un archivo o directorio en el árbol.",
                    "Eliminar recurso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        OperatingSystem so = simulator.getSo();
        FileSystem fs = so.getFileSystem();
        DiskHandler dh = fs.getDiskHandler();

        // Descomponer la ruta "root/User1/Tesis.docx [F]" en partes
        String[] parts = selectedPath.split("/");
        if (parts.length == 0) {
            return;
        }

        String last = parts[parts.length - 1]; // nombre del nodo seleccionado
        boolean isFile = selectedIsFile;      // ya lo detectamos en el listener

        // Directorio padre y nombre del recurso
        String resourceName;
        String parentPath;

        if (isFile) {
            // Quitar el " [F]" del nombre
            if (last.endsWith(" [F]")) {
                resourceName = last.substring(0, last.length() - 4);
            } else {
                resourceName = last;
            }
            // El directorio padre son todas las partes menos la última
            if (parts.length > 1) {
                parentPath = String.join("/", java.util.Arrays.copyOf(parts, parts.length - 1));
            } else {
                parentPath = "root";
            }
        } else {
            // Es un directorio
            resourceName = last;
            // El parent es todo antes de este directorio
            if (parts.length > 1) {
                parentPath = String.join("/", java.util.Arrays.copyOf(parts, parts.length - 1));
            } else {
                parentPath = ""; // root no tiene padre real
            }
        }

        String user = (String) userComboBox.getSelectedItem();
        int userId = userComboBox.getSelectedIndex(); // 0=Admin,1=User 1,2=User 2

        // --- CASO ADMIN: eliminar directo en DiskHandler ---
        if ("Admin".equals(user)) {

            if (isFile) {
                Directory parentDir = dh.getDirectoryByPath(parentPath);
                if (parentDir == null) {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró el directorio padre: " + parentPath,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                File_Proyect file = parentDir.findFileByName(resourceName);
                if (file == null) {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró el archivo: " + resourceName,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                dh.deleteFileDirect(file);

            } else { // directorio
                // No permitir borrar root
                if ("root".equals(resourceName) && (parentPath == null || parentPath.isEmpty())) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede eliminar el directorio root.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Directory parentDir = dh.getDirectoryByPath(parentPath);
                if (parentDir == null) {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró el directorio padre: " + parentPath,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Directory dirToDelete = parentDir.findSubDirectoryByName(resourceName);
                if (dirToDelete == null) {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró el directorio: " + resourceName,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                dh.deleteDirectoryDirect(dirToDelete);
            }

        } else {
            // --- CASO USUARIO NORMAL: crear proceso en el SO ---
            if (isFile) {
                Catalog cat = so.createCatalogForProcess(
                        IOAction.DELETE_FILE,
                        parentPath, // nameOfDirectory
                        resourceName, // name
                        "", // newName (no aplica)
                        0, // blocksQuantity (no aplica)
                        userId, // user (1 o 2)
                        "File" // resourceType
                );
                so.newProcess(IOAction.DELETE_FILE, cat);

            } else {
                // Igual que arriba, pero usando DELETE_DIR
                if ("root".equals(resourceName) && (parentPath == null || parentPath.isEmpty())) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede eliminar el directorio root.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Catalog cat = so.createCatalogForProcess(
                        IOAction.DELETE_DIR,
                        parentPath, // nameOfDirectory (padre)
                        resourceName, // name (directorio a borrar)
                        "", // newName
                        0, // blocksQuantity
                        userId, // user
                        "Directory" // resourceType
                );
                so.newProcess(IOAction.DELETE_DIR, cat);
            }
        }

        // Refrescar toda la interfaz con el SO actual
        refreshUI(so);
    }//GEN-LAST:event_deleteResourceActionPerformed

    private void saveSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSimulationActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Configuración de Archivos (JSON)");

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getAbsolutePath() + ".json");
            }

            try {
                // 1. Obtener la lista REAL de archivos del sistema
                // Asumimos que tienes acceso a 'simulator' desde este panel
                _02_DataStructures.SimpleList<_04_OperatingSystem.File_Proyect> fileList
                        = simulator.getSo().getFileSystem().getAllocationTable().getFiles();

                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"files\": [\n");

                if (fileList != null && !fileList.isEmpty()) {
                    _02_DataStructures.SimpleNode<_04_OperatingSystem.File_Proyect> node = fileList.GetpFirst();

                    while (node != null) {
                        _04_OperatingSystem.File_Proyect f = node.GetData();

                        // Construir la ruta completa para poder restaurarlo luego
                        // (Asumimos que getFullPath() existe en Directory, como agregamos antes)
                        String parentPath = (f.getParentDirectory() != null) ? f.getParentDirectory().getFullPath() : "root";

                        json.append("    {\n");
                        json.append("      \"name\": \"").append(f.getName()).append("\",\n");
                        json.append("      \"user\": ").append(f.getUser()).append(",\n"); // Int sin comillas
                        json.append("      \"blocks\": ").append(f.getNumberOfBlocks()).append(",\n");
                        json.append("      \"path\": \"").append(parentPath).append("\"\n"); // Guardamos la ruta padre
                        json.append("    }");

                        if (node.GetNxt() != null) {
                            json.append(",");
                        }
                        json.append("\n");

                        node = node.GetNxt();
                    }
                }

                json.append("  ]\n");
                json.append("}\n");

                // Escribir al disco
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(json.toString());
                writer.close();

                javax.swing.JOptionPane.showMessageDialog(this, "Configuración guardada exitosamente.");

            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error guardando JSON: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

    }//GEN-LAST:event_saveSimulationActionPerformed

    private void politicsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_politicsComboBoxActionPerformed
        if (simulator == null) {
            return;
        }

        String selected = (String) politicsComboBox.getSelectedItem();
        OperatingSystem so = simulator.getSo();

        // Esto depende completamente de tu API de OperatingSystem
        // pero sería algo de este estilo:
        switch (selected) {
            case "FIFO" ->
                so.getFileSystem().setCurrentPolicy(DiskPolicyType.FIFO);
            case "SSTF" ->
                so.getFileSystem().setCurrentPolicy(DiskPolicyType.SSTF);
            case "SCAN" ->
                so.getFileSystem().setCurrentPolicy(DiskPolicyType.SCAN);
            case "LIFO" ->
                so.getFileSystem().setCurrentPolicy(DiskPolicyType.LIFO);
        }
    }//GEN-LAST:event_politicsComboBoxActionPerformed

    private void userComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userComboBoxActionPerformed

    private void nameNewRecurso1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameNewRecurso1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameNewRecurso1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainJFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel blocksPanel;
    private javax.swing.JSpinner blocksSpinner;
    private javax.swing.JLabel currentPetitionLabel;
    private javax.swing.JToggleButton deleteResource;
    private javax.swing.JToggleButton editResource;
    private javax.swing.JButton generate20Process;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTree jTree1;
    private javax.swing.JScrollPane jtableScroll;
    private _07_GUI.MenuPanel menuPanel1;
    private javax.swing.JTextField nameNewRecurso;
    private javax.swing.JTextField nameNewRecurso1;
    private javax.swing.JLabel nodeSeclectedLabel;
    private javax.swing.JLabel nodeSeclectedLabel1;
    private javax.swing.JLabel nodeSeclectedLabel2;
    private javax.swing.JPanel petitionPanel;
    private javax.swing.JScrollPane petitionScroll;
    private javax.swing.JComboBox<String> politicsComboBox;
    private javax.swing.JPanel processPanel;
    private javax.swing.JScrollPane processScroll;
    private javax.swing.JToggleButton readResource;
    private javax.swing.JButton resetSimulation;
    private javax.swing.JButton saveSimulation;
    private javax.swing.JToggleButton startSimulation;
    private javax.swing.JToggleButton startSimulation2;
    private javax.swing.JTable tablaAsignacion;
    private javax.swing.JScrollPane treeScroll;
    private javax.swing.JButton uploadSimulation;
    private javax.swing.JComboBox<String> userComboBox;
    // End of variables declaration//GEN-END:variables
}
