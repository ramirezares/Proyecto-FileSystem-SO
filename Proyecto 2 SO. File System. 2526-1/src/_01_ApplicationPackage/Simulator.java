/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _01_ApplicationPackage;

import _04_OperatingSystem.OperatingSystem;
import _07_GUI.SimulationPanel;
import _04_OperatingSystem.Catalog;
import _04_OperatingSystem.IOAction;
import javax.swing.Timer;

/**
 *
 * @author AresR
 */
public class Simulator {

    private OperatingSystem so;
    private SimulationPanel view;
    private Timer guiTimer; // Temporizador para refrescar la pantalla

    public Simulator(SimulationPanel view) {
        this.view = view;
        this.so = new OperatingSystem();
        this.view.setSimulator(this);
        
        // Configurar timer para actualizar la GUI cada 100ms (10 FPS)
        this.guiTimer = new Timer(100, e -> {
            if (so != null) {
                this.view.updateView(so);
            }
        });
    }

    public void startSimulation() {
        System.out.println(">>> Iniciando Simulador...");
        so.startOS();
        guiTimer.start(); // Empezar a refrescar la pantalla
    }

    public void stopSimulation() {
        System.out.println(">>> Deteniendo Simulador...");
        so.shutdownOS();
        guiTimer.stop();
    }

    // Método para generar datos de prueba desde la GUI
    public void createTestLoad() {
        new Thread(() -> { // Hacerlo en otro hilo para no congelar la GUI
            try {
                int userId = 1;
                
                // 1. Crear carpeta
                Catalog catDir = so.createCatalogForProcess(IOAction.CREATE_DIR, "root", "User1", "", 0, userId, "Directory");
                so.newProcess(IOAction.CREATE_DIR, catDir);
                Thread.sleep(500); // Pausa dramática para ver el efecto

                // 2. Crear archivo
                Catalog catFile1 = so.createCatalogForProcess(IOAction.CREATE_FILE, "root/User1", "Tesis.docx", "", 4, userId, "File");
                so.newProcess(IOAction.CREATE_FILE, catFile1);
                Thread.sleep(500);

                // 3. Leer archivo
                Catalog catRead = so.createCatalogForProcess(IOAction.READ_FILE, "root/User1", "Tesis.docx", "", 0, userId, "File");
                so.newProcess(IOAction.READ_FILE, catRead);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
}
