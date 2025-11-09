/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

import _02_DataStructures.SimpleList;
import _03_LowLevelAbstractions.CPU;
import _03_LowLevelAbstractions.DMA;
import _03_LowLevelAbstractions.MainMemory;
import _03_LowLevelAbstractions.RealTimeClock;
import java.io.File;
import java.util.Random;

/**
 *
 * @author AresR
 */
public class OperatingSystem extends Thread {

    //          ----- Atributos -----
    // Componentes del sistema
    private CPU cpu;
    private DMA dma;
    private MainMemory mp;
    private Scheduler scheduler;
    private RealTimeClock clock;

    // Colas del sistema
    private SimpleList<Process1> readyQueue;
    private SimpleList<Process1> blockedQueue;
    private SimpleList<Process1> terminatedQueue;

    // Para sincronización
    private final Object osMonitor = new Object();
    private volatile boolean isRunning = true;

    // Para crear procesos random
    private static final Random RANDOM = new Random();


    //          --------------- Metodos ---------------
    public OperatingSystem() {
        this.cpu = new CPU(this);
        this.dma = new DMA(this);
        this.mp = new MainMemory(this);
        this.scheduler = new Scheduler(this, PolicyType.FIFO);
        this.readyQueue = new SimpleList<Process1>();
        this.blockedQueue = new SimpleList<Process1>();
        this.terminatedQueue = new SimpleList<Process1>();
        this.clock = new RealTimeClock(this.cpu, this.dma, 1000);

    }

    // Método para que otros hilos (como la CPU) notifiquen al SO
    public void notifyOS() {
        synchronized (osMonitor) {
            osMonitor.notify();
        }
    }

    public void startOS() {
        if (this.getState() == Thread.State.NEW) {
            this.start();
            this.cpu.start();
            this.dma.start();
            this.clock.start();
        }

        this.isRunning = true;
        this.cpu.playCPU();
        this.dma.playDMA();
        this.clock.playClock();

    }

    public void stopOS() {
        this.isRunning = false;
        this.cpu.stopCPU();
        this.dma.stopDMA();
        this.clock.stopClock();
        // Despertar al SO para que salga del wait()
        synchronized (osMonitor) {
            osMonitor.notify();
        }
    }

    @Override
    public void run() {
        System.out.println("SO: Hilo de gestión del Sistema Operativo iniciado.");
        while (isRunning) {
            // El SO se sincroniza en su propio monitor
            synchronized (osMonitor) {
                try {

                    if (!this.getScheduler().isIsOrdered()) {
                        this.scheduler.sortReadyQueue();
                    }

                    // Simula que se esta ejecutando el proceso del sistema
                    if (readyQueue.isEmpty() || cpu.getCurrentProcess() != null) {
                        // Solo espera si no hay trabajo pendiente para planificar
                        osMonitor.wait();
                    }

                    if (!this.isRunning) {
                        break;
                    }

                    // Planificación de Corto Plazo
                    // Intentar planificar si la CPU está libre y hay procesos listos
                    if (cpu.getCurrentProcess() == null && !readyQueue.isEmpty()) {
                        this.dispatchProcess();
                    }

                    //Planificacion a largo plazo
                    this.getScheduler().manageAdmission();

                    // Incluir aca la logica revision de interrupciones
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    this.isRunning = false;
                }
            }
        }
        System.out.println("SO: Hilo del Sistema Operativo detenido.");
    }

    public void loadConfigFromJSON(File f) {
    }

    public void newProcess(IOAction action, Catalog catalog) {

        // Verifico y obtengo la dirección base usando las instrucciones totales como tamaño
        int baseDirection = this.mp.isSpaceAvailable(1);

        Process1 newProcess;

        // Si no hay espacio en memoria
        if (baseDirection == -1) {
            String name = "Proceso de "+catalog.getName()+"."; 
            newProcess = new Process1(name, -1, action, catalog);
            System.out.println("No hay espacio contiguo suficiente (" + 1 + " unidades) en la Memoria Principal para el proceso " + newProcess.getPName() + ". Proceso no admitido en el sistema. Enviando a cola de nuevos");
            //Agregar a la cola de nuevos del dma
            this.dma.addNewProcess(newProcess);

            // Si hay espacio
        } else {
            String name = "Proceso de "+catalog.getName()+"."; 
            //Crear el objeto Process con la dirección base encontrada
            newProcess = new Process1(name, baseDirection, action, catalog);
            // Coloco el proceso en listo
            newProcess.setPState(ProcessState.READY);

            //Agregar a la cola de listos 
            this.readyQueue.insertLast(newProcess);

            // Asignar el espacio en la memoria principal (Actualiza el array memorySlots)
            this.mp.allocate(baseDirection, 1);

            // Notificar al planificador
            this.scheduler.setIsOrdered(false);
            System.out.println("Proceso " + newProcess.getPName() + " admitido en la Memoria Principal. Enviando a cola de listos");
        }
    }

    public void dispatchProcess() {

        // Escoger uno nuevo con el planificador
        Process1 nextProcess = scheduler.selectNextProcess();

        if (nextProcess != null) {
            
            this.cpu.setCurrentProcess(nextProcess);
            nextProcess.setPState(ProcessState.RUNNING); // Nuevo estado

            System.out.println("SO: Despachando proceso PID " + nextProcess.getPID() + ".");
        } else {
            System.out.println("SO: Despachando el proceso del sistema.");
        }
    }

    /**
     * Maneja un desalojo por Quantum en RR.
     *
     * @param preemptedProcess El proceso a desalojar.
     */
    public void handlePreemption(Process1 preemptedProcess) {
        if (preemptedProcess != null) {
            preemptedProcess.setPState(ProcessState.READY);
            this.getReadyQueue().insertLast(preemptedProcess);
            this.getCpu().setCurrentProcess(null);
            System.out.println("SO: Desalojo de PID " + preemptedProcess.getPID() + ". Movido a READY.");
        }
        notifyOS(); // Despierta el hilo del SO para que llame a dispatchProcess
    }

    public void manageIORequest() {
        Process1 processToSet = this.getCpu().getCurrentProcess();
        processToSet.setPState(ProcessState.BLOCKED); // Cambio el estado

        // Si el DMA esta desocupado le seteo el proceso
        if (this.getDma().isBusy() == false) {
            this.blockedQueue.insertLast(processToSet); // Lo añado a la cola de bloqueados del SO
            this.getDma().setCurrentProcess(this.cpu.getCurrentProcess()); // Envio al DMA al proceso
            this.getDma().receiveTick();
            this.getCpu().setCurrentProcess(null);
        } // Si el DMA esta ocupado bloqueo y encolo el proceso
        else {
            this.blockedQueue.insertLast(processToSet); // Encolo
            this.getCpu().setCurrentProcess(null); // Libero al CPU
        }
    }

    public void manageIOInterruptionByDMA(Process1 terminatedIOProcess) {
        terminatedIOProcess.setExceptionManaged(true); // Indico que se manejo la E/S al proceso

        // Si esta en la cola de bloqueados
        if (terminatedIOProcess.getPState() == ProcessState.BLOCKED) {
            this.getBlockedQueue().delNodewithVal(terminatedIOProcess); // Quito de la cola de bloqueados
            terminatedIOProcess.setPState(ProcessState.READY); // Cambio su estado
            this.getReadyQueue().insertLast(terminatedIOProcess); // Lo agrego a la cola de listos

        } 

        // Si no hay mas procesos bloqueados el DMA queda libre
        if (this.blockedQueue.isEmpty()) {
            this.getDma().setCurrentProcess(null); // Libero al DMA
            this.getDma().setBusy(false);
        } else {
            // Si hay alguien en la cola de bloqueado del sistema operativo lo agarro
            Process1 nextProcessForIO = (Process1) this.getBlockedQueue().GetpFirst().GetData();
            this.getDma().setCurrentProcess(nextProcessForIO); // Envio al DMA al proceso
            this.getDma().receiveTick(); // Le indico al DMA que continue
        }

        if (this.scheduler.getCurrentPolicy() == PolicyType.ROUND_ROBIN || this.scheduler.getCurrentPolicy() == PolicyType.SRT) {
            handlePreemption(this.getCpu().getCurrentProcess());
        }
    }

    public void terminateProcess() {
        //          Terminacion de un proceso
        System.out.println("CPU: Proceso terminado.");
        Process1 terminatedProcess = this.getCpu().getCurrentProcess(); // Cambio el estado
        terminatedProcess.setPState(ProcessState.TERMINATED);
        terminatedProcess.setMAR(-1);
        this.getTerminatedQueue().insertLast(terminatedProcess); //Mando el proceso a listos
        this.getCpu().setCurrentProcess(null);// Libera CPU

    }
    
    // Para crear un catalogo para luego crear el proceso
    public Catalog createCatalogForProcess(IOAction action, String name,String newName, int blocksQuantity, int user, String resourceType){
        Catalog processCatalog = new Catalog(name, newName, blocksQuantity, user, resourceType);

        if (action == IOAction.Create) {
            System.out.println("Creando recurso: " + name);
        } else if (action == IOAction.Update) {
            System.out.println("Actualizando recurso: " + name + " -> " + newName);
        } else if (action == IOAction.Delete) {
            System.out.println("Eliminando recurso: " + name);
        }

        return processCatalog;
    }
    

    // Getters y Setters
    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public DMA getDma() {
        return dma;
    }

    public void setDma(DMA dma) {
        this.dma = dma;
    }

    public MainMemory getMp() {
        return mp;
    }

    public void setMp(MainMemory mp) {
        this.mp = mp;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public SimpleList<Process1> getBlockedQueue() {
        return blockedQueue;
    }

    public void setBlockedQueue(SimpleList<Process1> blockedQueue) {
        this.blockedQueue = blockedQueue;
    }

    public SimpleList<Process1> getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(SimpleList<Process1> readyQueue) {
        this.readyQueue = readyQueue;
    }

    public SimpleList<Process1> getTerminatedQueue() {
        return terminatedQueue;
    }

    public void setTerminatedQueue(SimpleList<Process1> terminatedQueue) {
        this.terminatedQueue = terminatedQueue;
    }

    public void notifyNewProcessArrival(Scheduler scheduler) {
        scheduler.setIsOrdered(false);
    }

    public RealTimeClock getClock() {
        return clock;
    }

    /**
     * Apaga completamente los subhilos del SO: CPU, DMA y Reloj. Usar cuando se
     * quiera destruir la instancia antes de crear otra nueva (reset completo).
     */
    public void shutdownOS() {
        // Marcar para que el hilo del SO termine
        this.isRunning = false;

        // Parar CPU y DMA (estos métodos ya despiertan los hilos para que salgan)
        try {
            this.cpu.stopCPU();
        } catch (Exception ignored) {
        }
        try {
            this.dma.stopDMA();
        } catch (Exception ignored) {
        }

        // Detener permanentemente el reloj
        try {
            this.clock.shutdownClock();
        } catch (Exception ignored) {
        }

        // Despertar al SO para que salga del wait() y termine
        synchronized (osMonitor) {
            osMonitor.notify();
        }
    }
}