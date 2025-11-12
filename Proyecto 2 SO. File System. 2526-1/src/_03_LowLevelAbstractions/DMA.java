/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _03_LowLevelAbstractions;

import _04_OperatingSystem.DiskHandler;
import _02_DataStructures.SimpleList;
import _04_OperatingSystem.FileSystem;
import _04_OperatingSystem.OperatingSystem;
import _04_OperatingSystem.Process1;

/**
 *
 * @author AresR
 */
public class DMA extends Thread {

    // ----- Atributos ----- 
    // Contador para la gestion de E/S
    private int remainingCycles;
    private volatile boolean isRunning;

    // Proceso al que se le esta manejando su E/S para redirigirla al disco
    // Si es null, no se esta gestionando la E/S de ningun proceso
    private volatile Process1 currentProcess;

    // Proceso al que se le manejo su E/S
    private volatile Process1 processOfPetitionSatisfied;

    // Cola de nuevos
    private SimpleList newProcesses;

    private volatile boolean busy; // Para saber si esta ocupado o no

    // Monitor para la sincronizacion para usar wait() y notify()
    private final Object syncMonitor = new Object();

    // Para poder referenciar los metodos 
    private final OperatingSystem osReference;
    private final FileSystem fileSystemReference;

    // Para la interfaz
    private String lastEventLog;

    // --------------- Metodos ---------------
    /**
     * Constructor
     */
    public DMA(OperatingSystem osReference, FileSystem fileSystemReference) {
        this.remainingCycles = -1;
        this.currentProcess = null;
        this.newProcesses = new SimpleList();
        this.busy = false;
        this.osReference = osReference;
        this.fileSystemReference = fileSystemReference;
        this.lastEventLog = "DMA Iniciado.";
    }

    /**
     * Hace que el DMA reanude su operacion
     */
    public void playDMA() {
        this.isRunning = true;
        this.receiveTick();
    }

    /**
     * Detiene la ejecucion del DMA
     */
    public void stopDMA() {
        this.isRunning = false;
        // Despertar a la CPU para que salga del wait() y termine el hilo.
        synchronized (syncMonitor) {
            syncMonitor.notify();
        }
    }

    // ----- Sincronización -----
    /**
     * Detiene el hilo de CPU y lo saca de la espera (wait).
     */
    public void receiveTick() {
        synchronized (syncMonitor) {
            syncMonitor.notify();
        }
    }

    /**
     * Metodo principal para el funcionamiento del DMA
     */
    @Override
    public void run() {

        this.isRunning = true;
        while (this.isRunning) {
            synchronized (syncMonitor) {
                try {
                    syncMonitor.wait();

                    if (processOfPetitionSatisfied != null) {
                        this.logEvent("Solicitud E/S del proceso "+ processOfPetitionSatisfied.getPID() + " completada, sacando de bloqueados");
                        // Ya se manejo la E/S del proceso

                        // Para darselo al Sistema operativo                            
                        Process1 p = this.processOfPetitionSatisfied;

                        // Invocar al sistema operativo para terminar E/S
                        this.osReference.manageIOInterruptionByDMA(p);

                        this.processOfPetitionSatisfied = null;
                        continue;
                    }

                    if (this.currentProcess != null) {
                        this.logEvent("Asignando peticion del proceso " + this.currentProcess.getPID() + " al FileSystem");

                        if (this.remainingCycles == 0) {
                            this.logEvent("Peticion del proceso a disco creada");
                            System.out.println("[DMA] Peticion del proceso a disco creada");

                            // Añadir el proceso a la cola de E/S del dispositivo (disco) que tiene el FileSystem
                            this.fileSystemReference.addPetition(currentProcess);

                            // Verificar que termine con un proceso
                            this.currentProcess = null;
                        }

                        this.remainingCycles--;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void addNewProcess(Process1 newProcess) {
        this.newProcesses.insertLast(newProcess);
    }

    // ------ Getters y Setters ------
    public int getRemainingCycles() {
        return remainingCycles;
    }

    public void setRemainingCycles(int remainingCycles) {
        this.remainingCycles = remainingCycles;
    }

    public Process1 getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(Process1 process) {
        this.currentProcess = process;
        if (process != null) {
            this.remainingCycles = 1;
            this.busy = true;
        }
    }

    public SimpleList getNewProcesses() {
        return newProcesses;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public void setProcessOfPetitionSatisfied(Process1 processOfPetitionSatisfied) {
        this.processOfPetitionSatisfied = processOfPetitionSatisfied;
    }

    public OperatingSystem getOsReference() {
        return osReference;
    }

    private void logEvent(String event) {
        int currentCycle = this.osReference.getCpu().getCycleCounter();
        // Sobrescribe el ultimo evento que guardo el planificador
        this.lastEventLog = String.format("%d: %s", currentCycle, event);
    }

}
