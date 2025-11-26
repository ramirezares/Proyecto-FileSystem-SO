/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

import _02_DataStructures.SimpleList;
import _03_LowLevelAbstractions.DMA;
import _03_LowLevelAbstractions.Disk;

/**
 *
 * @author AresR
 */
public class FileSystem extends Thread {

    // ----- Atributos ----- 
    // Contador para la gestion de E/S
    private int remainingCycles;
    private volatile boolean isRunning;

    // Peticion E/S que se esta manejando
    private volatile IOPetition currentPetition;

    // Cola de peticiones al dispositivo E/S (al disco)
    private SimpleList<IOPetition> petitions;

    private volatile boolean busy; // Para saber si esta ocupado o no

    // Monitor para la sincronizacion para usar wait() y notify()
    private final Object syncMonitor = new Object();

    // Para sincronización de la cola de peticiones
    private final Object petitionsMonitor = new Object();

    // Para poder referenciar los atributos del DMA 
    private DMA DMAReference;

    // Politica de planificacion de disco actual
    private DiskPolicyType currentPolicy;
    private IOSupervisor diskSupervisor;

    // Indica si la cola de peticiones está ordenada segun la politica
    private boolean isOrdered;

    // Tiene la referencia a la tabla de asignación (lista de archivos para mostrarlos en la interfaz)
    private AllocationTable allocationTable;

    // Tiene el supervisor de E/S que se encarga de la planificación y estado de los ficheros
    // Tiene un disk handler que es el que se conecta directamente con el disco y realiza la E/S
    private DiskHandler diskHandler;

    // Tiene el disco 
    private Disk disk;

    // --------------- Metodos ---------------
    public FileSystem(DMA DMAReference) {
        this.remainingCycles = -1;
        this.currentPetition = null;
        this.petitions = new SimpleList<IOPetition>();
        this.busy = false;
        this.DMAReference = DMAReference;
        this.currentPolicy = DiskPolicyType.FIFO;
        this.diskSupervisor = new IOSupervisor();
        this.isOrdered = true;
        this.allocationTable = new AllocationTable();
        this.disk = new Disk(32);
        this.diskHandler = new DiskHandler(this.disk, this.allocationTable, 32);

    }

    /**
     * Hace que el DMA reanude su operacion
     */
    public void playFileSystem() {
        this.isRunning = true;
        this.executeIOOperation();
    }

    /**
     * Detiene la ejecucion del DMA
     */
    public void stopFileSystem() {
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
    public void executeIOOperation() {
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

                    // Sincronizamos el acceso a la lista de peticiones
                    synchronized (petitionsMonitor) {
                        // Planificacion 
                        if (this.petitions.GetpFirst() != null && this.currentPetition == null) {

                            // --- IOSUPERVISOR ---
                            if (!this.isOrdered || this.currentPolicy != DiskPolicyType.FIFO) {
                                System.out.println("[FileSystem] IOSupervisor reordenando cola usando: " + this.currentPolicy);

                                // Obtenemos la última posición del cabezal
                                int headPosition = this.diskHandler.getLastBlockReferenced();

                                this.diskSupervisor.planificatePetitions(this.petitions, this.currentPolicy, headPosition, this.allocationTable);
                                this.isOrdered = true; // Marcamos como ordenada
                            }

                            // Tomamos el primero (que será el mejor según el algoritmo)
                            this.currentPetition = (IOPetition) this.petitions.GetpFirst().GetData();

                            // Toma la duración para manejar la E/S
                            this.remainingCycles = this.currentPetition.getProcessReference().getCyclesToManageException();

                            this.petitions.delNodewithVal(currentPetition);
                        }
                    }
                    // Ejecucion
                    if (this.currentPetition != null) {
                        System.out.println("Sistema de archivos trabajando");
                        System.out.println(currentPetition.getAction());

                        this.remainingCycles--;

                        if (this.remainingCycles == 0) {

                            // Logica de IO en disco fisico
                            boolean success = this.diskHandler.executeOperation(this.currentPetition);

                            if (!success) {
                                // Opcional: Manejar el fallo (ej. loggear, notificar al proceso)
                                System.err.println("[FileSystem] DiskHandler falló al ejecutar la petición para el proceso: " + this.currentPetition.getProcessReference().getPID());
                            }

                            // Para darselo al Sistema operativo
                            Process1 terminatedProcess = this.currentPetition.getProcessReference();

                            // Hacer que el DMA invoque al sistema operativo 
                            this.DMAReference.setProcessOfPetitionSatisfied(terminatedProcess);
                            System.out.println("[FileSystem] Peticion E/S terminada por el disco");
                            this.currentPetition = null;

                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // Método usado por el DMA para insertar
    public void addPetition(Process1 process) {
        IOPetition newPetition = new IOPetition(process.getAction(), process.getCatalog(), process);

        synchronized (petitionsMonitor) {
            this.petitions.insertLast(newPetition);
            this.isOrdered = false; // Marcamos como desordenada
        }
    }

    public int getRemainingCycles() {
        return remainingCycles;
    }

    public void setRemainingCycles(int remainingCycles) {
        this.remainingCycles = remainingCycles;
    }

    public boolean isIsRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public IOPetition getCurrentPetition() {
        return currentPetition;
    }

    public void setCurrentPetition(IOPetition currentPetition) {
        this.currentPetition = currentPetition;
    }

    public SimpleList<IOPetition> getPetitions() {
        return petitions;
    }

    public void setPetitions(SimpleList<IOPetition> petitions) {
        this.petitions = petitions;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public DMA getDMAReference() {
        return DMAReference;
    }

    public void setDMAReference(DMA DMAReference) {
        this.DMAReference = DMAReference;
    }

    public DiskPolicyType getCurrentPolicy() {
        return currentPolicy;
    }

    public void setCurrentPolicy(DiskPolicyType currentPolicy) {
        this.currentPolicy = currentPolicy;
        this.isOrdered = false;
    }

    public boolean isIsOrdered() {
        return isOrdered;
    }

    public void setIsOrdered(boolean isOrdered) {
        this.isOrdered = isOrdered;
    }

    public AllocationTable getAllocationTable() {
        return allocationTable;
    }

    public void setAllocationTable(AllocationTable allocationTable) {
        this.allocationTable = allocationTable;
    }

    public DiskHandler getDiskHandler() {
        return diskHandler;
    }

    public void setDiskHandler(DiskHandler diskHandler) {
        this.diskHandler = diskHandler;
    }

    public Disk getDisk() {
        return disk;
    }

    public void setDisk(Disk disk) {
        this.disk = disk;
    }

}
