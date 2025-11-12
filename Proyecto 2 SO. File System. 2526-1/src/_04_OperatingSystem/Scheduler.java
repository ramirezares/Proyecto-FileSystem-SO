/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

import _02_DataStructures.SimpleList;
import _02_DataStructures.SimpleNode;
import _04_OperatingSystem.Process1;

/**
 *
 * @author AresR
 */
public class Scheduler {

    //          ----- Atributos -----
    private PolicyType currentPolicy;
    private final OperatingSystem osReference;
    // Indica si la cola de listos ya está ordenada
    private boolean isOrdered;
    private String lastEventLog;

    // Para RR
    private final int quantum = 5;
    // Para control de admisiones periódicas
    private int lastAdmissionCycle = -1;

    //          --------------- Metodos ---------------
    /**
     * Constructor
     *
     * @param osReference Referencia al objeto SO para acceder a sus atributos
     * @param currentPolicy Politica escogida para iniciar el sistema
     */
    public Scheduler(OperatingSystem osReference, PolicyType currentPolicy) {
        this.osReference = osReference;
        this.currentPolicy = currentPolicy;
        this.isOrdered = false;
        this.lastEventLog = "Sistema Iniciado.";
    }

    // ---------- Planificacion a corto plazo ----------
    /**
     * Seleccionar nuevo proceso de la cola de listos para ponerlo en ejecución
     *
     * @return proceso a darle el control del CPU
     */
    public Process1 selectNextProcess() {
        // Obtener la Cola de Listos del SO
        SimpleList<Process1> readyProcesses = osReference.getReadyQueue(); // Asume que el OS tiene este getter

        if (readyProcesses == null || readyProcesses.isEmpty()) {
            return null;
        }

        // Si la lista no está ordenada, la ordenamos 
        if (!this.isOrdered) {
            sortReadyQueue();
            this.isOrdered = true;
        }

        // Tomo el proceso. La cola debe estar ordenada segun el algoritmo de ordenamiento
        Process1 nextProcess = (Process1) readyProcesses.GetpFirst().GetData();

        // Cambio su estado y lo elimino de la cola
        nextProcess.setPState(ProcessState.RUNNING);
        this.osReference.getReadyQueue().delNodewithVal(nextProcess);

        //System.out.println("Seleccionado el proceso " + nextProcess.getPID() + " para ejecutarse");
        this.logEvent("Proceso " + nextProcess.getPName() + " seleccionado para ejecutarse en el CPU");
        return nextProcess;
    }

    /**
     * Metodo para ordenar la cola que llama al metodo de la politica segun sea
     * el caso
     */
    public void sortReadyQueue() {
        SimpleList<Process1> readyProcesses = osReference.getReadyQueue();
        if (readyProcesses.GetSize() <= 1) {
            this.isOrdered = true;
            return;
        }

        switch (currentPolicy) {
            case Priority:
                sortPriority();
                break;

            case FIFO:
                sortFIFO();
                break;

            case ROUND_ROBIN:
                sortRoundRobin();
                break;

            case SPN:
                sortSPN();
                break;

            case SRT:
                sortSRT();
                break;

            case HRRN:
                sortHRRN();
                break;
        }
        //System.out.println("Cola de listos reordenada con política " + currentPolicy);
        this.isOrdered = true;
    }

    private void sortPriority() {
        // Ordena por prioridad siendo la mas importante la menor (1)
        ordenateWithBubbleSort(PolicyType.Priority);
    }

    private void sortFIFO() {
        // Toma en cuenta el PID del proceso ya que este es unico 
        // y esta en orden de creacion
        ordenateWithBubbleSort(PolicyType.FIFO);
    }

    private void sortRoundRobin() {
        // Ordena por PID tambien ya que no es RR Virtual, es decir solo se le da un quantum a cada proceso
        // siguiendo cualquier orden mientras se le de el mismo tiempo a cada proceso
        ordenateWithBubbleSort(PolicyType.ROUND_ROBIN);
    }

    private void sortSPN() {
        // SPN shortest process next se usara el mas corto, es decir que tenga 
        // el menor totalInstruction
        ordenateWithBubbleSort(PolicyType.SPN);
    }

    private void sortSRT() {
        // Para SRT shortest remaining time se usara el que tenga el menor 
        // remainingInstruction
        ordenateWithBubbleSort(PolicyType.SRT);
    }

    private void sortHRRN() {
        ordenateWithBubbleSort(PolicyType.HRRN);
    }

    /**
     * Metodo que centraliza el ordenar la lista
     */
    private void ordenateWithBubbleSort(PolicyType policy) {
        // Obtengo la lista del SO
        SimpleList<Process1> readyProcesses = osReference.getReadyQueue();
        int size = readyProcesses.GetSize();

        // Hago un arreglo temporal
        Process1[] processArray = new Process1[size];

        SimpleNode<Process1> current = readyProcesses.GetpFirst();
        int index = 0;
        while (current != null) {
            processArray[index] = current.GetData();
            current = current.GetNxt();
            index++;
        }

        // Ordeno el arreglo temporal
        bubbleSort(processArray, policy);

        // Vuelvo a llenar la cola de listos
        readyProcesses.SetpFirst(null);
        readyProcesses.SetpLast(null);
        for (Process1 p : processArray) {
            readyProcesses.insertLast(p);
        }
    }

    /**
     * Ordena un array de procesos usando Bubble Sort usando dos metodos
     */
    private void bubbleSort(Process1[] processes, PolicyType policy) {
        int n = processes.length;
        // Pasadas a todo el arreglo
        for (int i = 0; i < n - 1; i++) {
            // Iteracion por cada elemento 
            for (int j = 0; j < n - i - 1; j++) {
                Process1 a = processes[j];
                Process1 b = processes[j + 1];

                // Si b es mejor que a (es decir, a no está en el orden correcto respecto a b) 
                // los intercambiamos.
                if (!isABetterThanB(a, b, policy)) {
                    Process1 temp = a;
                    processes[j] = b;
                    processes[j + 1] = temp;
                }
            }
        }
    }

    /**
     * Determina si el Proceso 'a' debe ir antes que el Proceso 'b' para
     * organizar la lista
     */
    private boolean isABetterThanB(Process1 a, Process1 b, PolicyType policy) {
        double valA = getComparisonValue(a, policy);
        double valB = getComparisonValue(b, policy);

        if (policy == PolicyType.HRRN) {
            if (valA != valB) { // HRRN: Buscamos el valor maximo.
                return valA > valB;
            }
        } else {
            if (valA != valB) { // Priority, SPN, SRT, FIFO, RR: Buscamos el valor MÍNIMO.
                return valA < valB;
            }
        }
        return a.getPID() < b.getPID(); // Desempate por PID si tienen el mismo valor
    }

    /**
     * Devuelve el valor numérico clave para la comparación según la política.
     */
    private double getComparisonValue(Process1 p, PolicyType policy) {
        switch (policy) {
            case Priority:
                //return p.getPPriority();
            case SPN:
                return p.getTotalInstructions();
            case SRT:
                return p.getRemainingInstructions();
            case FIFO:
            case ROUND_ROBIN:
                return p.getPID();
            case HRRN:
                //return p.getResponseRate();
            default:
                return Double.MAX_VALUE;
        }
    }

    // ---------- Planificacion a largo plazo ----------
    /**
     * Admision de procesos de nuevo al sistema
     *
     * @return
     */
    public void manageAdmission() {

        // Admite un proceso si hay 25% de espacio en MP y el sistema no esta full de procesos
        if (this.osReference.getMp().admiteNewProcess() && !this.osReference.getDma().getNewProcesses().isEmpty()) {

            //System.out.println("Planificador a largo plazo: admisión normal");
            // Utilizara simplemente el FIFO
            Process1 newProcessToMP = (Process1) this.osReference.getDma().getNewProcesses().GetpFirst().GetData();

            // Si hay espacio suficiente en memoria principal
            int baseDirection = this.osReference.getMp().isSpaceAvailable(newProcessToMP.getTotalInstructions());

            // Si no hay espacio en memoria
            if (baseDirection == -1) {
                //System.out.println("No hay espacio contiguo suficiente (" + newProcessToMP.getTotalInstructions() + " unidades) en la Memoria Principal para planificarlo desde el Largo plazo.");
                // Si no hay espacio no se hace nada

                // Considerar la cola de listo suspendidos. Por ahora solo listo por simplicidad 
                // Si hay espacio
            } else {
                this.osReference.getReadyQueue().insertLast(newProcessToMP); // Muevo el proceso de la cola de nuevo a la cola de listos
                this.osReference.getDma().getNewProcesses().delNodewithVal(newProcessToMP);

                newProcessToMP.setPState(ProcessState.READY); // Coloco el proceso en listo

                newProcessToMP.setPC(baseDirection + 1); // modifico su PC y su mar
                newProcessToMP.setMAR(baseDirection);
                newProcessToMP.setBaseDirection(baseDirection); // Lo ubico en la memoria

                // Asignar el espacio en la memoria principal (Actualiza el array memorySlots)
                this.osReference.getMp().allocate(baseDirection, newProcessToMP.getTotalInstructions());

                // Notificar al planificador
                this.setIsOrdered(false);
                //System.out.println("PID " + newProcessToMP.getPName() + " admitido en la Memoria Principal. Enviando a cola de listos.");
                this.logEvent("Proceso " + newProcessToMP.getPName() + " enviado a cola de listos.");
            }
        }
    }

    private void logEvent(String event) {
        int currentCycle = this.osReference.getCpu().getCycleCounter();
        // Sobrescribe el ultimo evento que guardo el planificador
        this.lastEventLog = String.format("%d: %s", currentCycle, event);
    }

    // Getters y Setters
    public PolicyType getCurrentPolicy() {
        return currentPolicy;
    }

    public void setCurrentPolicy(PolicyType newPolicy) {
        this.currentPolicy = newPolicy;

        this.isOrdered = false; // Para que vuelva a ordenar la cola por el cambio de politica

        //System.out.println("Política cambiada a " + newPolicy);
    }

    public int getQuantum() {
        return quantum;
    }

    public boolean isIsOrdered() {
        return isOrdered;
    }

    public void setIsOrdered(boolean isOrdered) {
        this.isOrdered = isOrdered;
    }

    public String getEventLog() {
        return this.lastEventLog;
    }
}