/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _01_ApplicationPackage;

import _04_OperatingSystem.OperatingSystem;
import _04_OperatingSystem.Catalog;
import _04_OperatingSystem.IOAction;
import _07_GUI.MainJFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author AresR
 */
public class Simulator {

    private OperatingSystem so;
    private MainJFrame view;

    // Estado del simulador
    private boolean started = false; // el SO ya arrancó al menos una vez
    private boolean paused = false;  // CPU/DMA/FS/Clock detenidos lógicamente

    // Usuario actual (para enlazar con el combo de la UI)
    private String currentUser = "User 1";

    public Simulator(MainJFrame view) {
        this.view = view;
        this.view.setSimulator(this);

        this.so = new OperatingSystem();
    }

    /**
     * Inicia la simulación por primera vez o reanuda si estaba pausada. Este
     * método es el que debe llamar el botón "Iniciar simulación".
     */
    public void startSimulation() {
        // Si nunca se ha arrancado el SO, lo arrancamos
        if (!started) {
            System.out.println(">>> Iniciando SO y componentes...");
            so.startOS();      // lanza hilos del SO, CPU, DMA, FileSystem, Clock
            started = true;
            paused = false;
        } else if (paused) {
            // Reanudar después de una pausa
            System.out.println(">>> Reanudando simulación...");
            resumeComponents();
            paused = false;
        } else {
            // Ya está corriendo
            System.out.println(">>> La simulación ya está en ejecución.");
        }

        refreshView();
    }

    /**
     * Pausa la simulación sin destruir el SO. Aquí no usamos stopOS() porque
     * mataría el hilo del SO y no podríamos reanudar.
     */
    public void pauseSimulation() {
        if (!started || paused) {
            return;
        }

        System.out.println(">>> Pausando simulación (CPU/DMA/FS/Clock)...");

        // Usamos directamente los métodos de los componentes
        try {
            so.getCpu().stopCPU();
        } catch (Exception ignored) {
        }

        try {
            so.getDma().stopDMA();
        } catch (Exception ignored) {
        }

        try {
            so.getFileSystem().stopFileSystem();
        } catch (Exception ignored) {
        }

        try {
            so.getClock().stopClock();
        } catch (Exception ignored) {
        }

        paused = true;
    }

    /**
     * Detiene definitivamente el SO actual (por ejemplo al cerrar la app). No
     * se recomienda usar esto para "pausar", porque el hilo del SO no se puede
     * reiniciar después.
     */
    public void stopSimulation() {
        if (!started) {
            return;
        }

        System.out.println(">>> Deteniendo simulación (shutdownOS)...");

        // Apaga todos los hilos del SO actual
        so.stopOS();

        started = false;
        paused = false;
    }

    /**
     * Reinicia completamente la simulación: - apaga el SO actual - crea un SO
     * nuevo - limpia la vista
     */
    public void resetSimulation() {
        System.out.println(">>> Reiniciando simulación completa...");

        // 1. Apagar el SO actual (si ya se inició)
        if (started) {
            try {
                so.shutdownOS();
            } catch (Exception ignored) {
            }
        }

        // 2. Crear una instancia nueva, limpia
        this.so = new OperatingSystem();
        this.started = false;
        this.paused = false;

        // 3. Limpiar la parte visual
        view.resetView();

        // 4. Refrescar vista con el nuevo SO vacío
        refreshView();
    }

    /*==============================================================
                     MÉTODOS AUXILIARES DE CONTROL
    ==============================================================*/
    /**
     * Reanuda CPU, DMA, FileSystem y Clock. Lo usamos cuando el usuario da
     * "Iniciar" estando en pausa.
     */
    private void resumeComponents() {
        try {
            so.getCpu().playCPU();
        } catch (Exception ignored) {
        }

        try {
            so.getDma().playDMA();
        } catch (Exception ignored) {
        }

        try {
            so.getFileSystem().playFileSystem();
        } catch (Exception ignored) {
        }

        try {
            so.getClock().playClock();
        } catch (Exception ignored) {
        }
    }

    /*==============================================================
              MÉTODO PARA GENERAR CARGA DE PRUEBA (GUI)
    ==============================================================*/
    /**
     * Crea algunos procesos de prueba relacionados con el File System. Se
     * ejecuta en otro hilo para no congelar la interfaz.
     */
    public void createTestLoad() {
        new Thread(() -> {
            try {
                int userId = resolveCurrentUserId();
                String userFolder = switch (currentUser) {
                    case "Admin" ->
                        "Admin";
                    case "User 2" ->
                        "User2";
                    default ->
                        "User1";
                };

                this.getSo().createRandomFiles(1);
                this.getSo().createRandomFiles(2);
                Thread.sleep(300);
                refreshView();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /*==============================================================
                      ACTUALIZAR LA INTERFAZ
    ==============================================================*/
    /**
     * Llama a los métodos de actualización de la vista en el hilo de Swing.
     */
    public void refreshView() {
        SwingUtilities.invokeLater(() -> {
            try {
                view.refreshUI(so);
            } catch (Exception e) {
                // Para evitar que una excepción gráfica tumbe la simulación
                e.printStackTrace();
            }
        });
    }

    /*==============================================================
                         USUARIO ACTUAL
    ==============================================================*/
    public void setCurrentUser(String user) {
        this.currentUser = user;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * Traduce el texto del combo ("Admin", "User 1", "User 2") a un ID entero
     * para el SO / FileSystem.
     */
    private int resolveCurrentUserId() {
        return switch (currentUser) {
            case "Admin" ->
                0;
            case "User 2" ->
                2;
            default ->
                1;  // "User 1"
        };
    }

    /*==============================================================
                         GETTERS Y SETTERS
    ==============================================================*/
    public OperatingSystem getSo() {
        return so;
    }

    public void setSo(OperatingSystem so) {
        this.so = so;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isPaused() {
        return paused;
    }

}
