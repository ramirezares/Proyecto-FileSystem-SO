/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _03_LowLevelAbstractions;

import _04_OperatingSystem.OperatingSystem;
import _04_OperatingSystem.Process1;
import _04_OperatingSystem.ProcessType;

/**
 *
 * @author DiegoM
 */
public class CPU extends Thread {

    // ---------- Atrs ----------
    // ----- CPU -----
    private int PC;
    private int MAR;
    private volatile boolean isProcessRunning;

    // Contadores de ciclo CPU para planificacion
    private int cycleCounter;
    private int remainingCycles;
    
    // Proceso que se está ejecutando
    // Si es null, el SO está en control.
    private Process1 currentProcess;
    private String currentMode;

    // Monitor para la sincronizacion para usar wait() y notify()
    private final Object syncMonitor = new Object();

    // Para poder referenciar los metodos del SO
    private final OperatingSystem osReference;

    // --------------- Metodos ---------------
    /**
     * Constructor
     */
    public CPU(OperatingSystem osReference) {
        this.PC = 0;
        this.MAR = 0;
        this.isProcessRunning = false;
        this.cycleCounter = 0;
        this.remainingCycles = -1;
        this.osReference = osReference;
        setName("Hilo del CPU");
    }

    // ----- Sincronización -----
    public void receiveTick() {
        synchronized (syncMonitor) {
            syncMonitor.notify();
        }
    }

    /**
     * Hace que el CPU reanude su operacion
     */
    public void playCPU() {
        this.isProcessRunning = true;
        this.receiveTick();
    }

    /**
     * Detiene la ejecucion del CPU
     */
    public void stopCPU() {
        this.isProcessRunning = false;
        // Despertar a la CPU para que salga del wait() y termine el hilo.
        synchronized (syncMonitor) {
            syncMonitor.notify();
        }
    }

    /**
     * Metodo principal para ejecutar un proceso en CPU
     */
    @Override
    public void run() {
        this.isProcessRunning = true;

        while (isProcessRunning) {
            synchronized (syncMonitor) {
                try {
                    if (currentProcess != null && currentProcess.getState() == Process1.State.NEW) {
                        currentProcess.start();
                    }

                    syncMonitor.wait(); // Espera el 'tick' del reloj

                    if (!this.isProcessRunning) {
                        break;
                    } // Si stopCPU fue llamado, sale

                    this.cycleCounter++; // Incrementa el contador global de ciclos de CPU 

                    // Si hay un proceso se ejecutara 
                    if (currentProcess != null) {
                        this.currentMode = "Modo usuario";
                        System.out.println("\n[Ciclo " + this.cycleCounter + "] Ejecutando PID " + currentProcess.getPID()
                                + " (Quantum Restante: " + (remainingCycles > 0 ? remainingCycles : "N/A") + ")");

                        this.PC++; // Incrementa el contador PC
                        this.MAR++; // Incrementa el contador MAR
                        
                        // Proceso de Usuario
                        if (currentProcess != null) {
                            currentProcess.executeOneCycle(); // Ejecutar una instrucción del proceso
                        }

                        // Lee el resultado de la ejecucion
                        boolean processWantsToContinue = true;
                        if (currentProcess != null) {
                        processWantsToContinue = currentProcess.didExecuteSuccessfully();
                        }
                        
                        // Actualizacion de Quantum
                        if (remainingCycles > 0) {
                            remainingCycles--;
                        }

                        // Comprobación de E/S o de terminacion
                        if (processWantsToContinue == false) {

                            // CPU bound solo indicara que no quiere continuar si termino
                            if (currentProcess.getType() == ProcessType.CPU_BOUND) {
                                // Funcion de terminacion de un proceso por el SO
                                this.currentMode = "Modo kernel";
                                this.osReference.terminateProcess();

                            } // Si el proceso ya hizo todas sus instrucciones y ya se manejo su E/S significa que ya termino
                            else if (currentProcess.getExecutedInstructions() == currentProcess.getTotalInstructions() && currentProcess.isExceptionManaged() == true) {
                                // Funcion de terminacion del proceso
                                this.currentMode = "Modo kernel";
                                this.osReference.terminateProcess();
                            } // Si el proceso no ha terminado sus instrucciones pero "no quiere continuar" es que necesita una operacion E/S
                            else {
                                System.out.println("CPU: Proceso requiere E/S. Desalojo y Notifico a SO.");
                                this.currentMode = "Modo kernel";
                                this.osReference.manageIORequest();
                            }
                        }

                        // Si es null se ejecutara el SO
                    } else {
                        this.currentMode = "Modo kernel";
                        this.osReference.notifyOS();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    this.isProcessRunning = false;
                }
            }
        }
    }

    // ----- Getters y setters -----
    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getMAR() {
        return MAR;
    }

    public void setMAR(int MAR) {
        this.MAR = MAR;
    }

    public boolean isIsProcessRunning() {
        return isProcessRunning;
    }

    public void setIsProcessRunning(boolean isProcessRunning) {
        this.isProcessRunning = isProcessRunning;
    }

    public int getCycleCounter() {
        return cycleCounter;
    }

    public void setCycleCounter(int cycleCounter) {
        this.cycleCounter = cycleCounter;
    }

    public int getRemainingCycles() {
        return remainingCycles;
    }

    public void setRemainingCycles(int remainingCycles) {
        this.remainingCycles = remainingCycles;
    }

    public void setCurrentProcess(Process1 process) {
        this.currentProcess = process;
        // Al cargar un proceso, actualizamos los registros de la CPU con los del PCB
        if (process != null) {
            this.PC = process.getPC();
            this.MAR = process.getMAR();
            //this.remainingCycles = quantum;
        }
    }

    public Process1 getCurrentProcess() {
        return currentProcess;
    }

    public String getCurrentMode() {
        return currentMode;
    }
}