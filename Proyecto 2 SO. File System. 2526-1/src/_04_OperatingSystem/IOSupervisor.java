/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

import _02_DataStructures.SimpleList;
import _02_DataStructures.SimpleNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author AresR
 */
public class IOSupervisor {
    
    // Para el algoritmo SCAN: true = subiendo (bloques++), false = bajando (bloques--)
    private boolean movingUp;

    public IOSupervisor() {
        this.movingUp = true; // Empezamos "subiendo"
    }

    /**
     * Método principal que reordena la lista de peticiones según la política.
     * * @param petitions La lista enlazada de peticiones del FileSystem.
     * @param policy La política actual (FIFO, SSTF, etc.).
     * @param currentHeadPosition La posición del último bloque referenciado (del DiskHandler).
     * @param allocationTable Necesaria para saber en qué bloque empiezan los archivos (para calcular distancias).
     */
    public void planificatePetitions(SimpleList<IOPetition> petitions, DiskPolicyType policy, int currentHeadPosition, AllocationTable allocationTable) {
        
        // Si la lista está vacía o tiene 1 elemento, no hay nada que ordenar
        if (petitions.isEmpty() || petitions.GetpFirst() == petitions.GetpLast()) {
            return;
        }

        // Extraigo todas las peticiones a una lista temporal ordenar
        List<IOPetition> tempList = new ArrayList<>();
        while (!petitions.isEmpty()) {
            SimpleNode node = petitions.GetpFirst();
            tempList.add((IOPetition) node.GetData());
            petitions.delNodewithVal((IOPetition) node.GetData()); // Vaciamos la lista original
        }

        switch (policy) {
            case FIFO:
                // Ordenamos por PID ascendente.
                tempList.sort(Comparator.comparingInt(p -> p.getProcessReference().getPID()));
                break;
                
            case LIFO:
                // Ordenamos por PID descendente (el último PID creado es el primero en atenderse).
                tempList.sort((p1, p2) -> Integer.compare(p2.getProcessReference().getPID(), p1.getProcessReference().getPID()));
                break;
                
            case SSTF:
                // Shortest Seek Time First/Tiempo de servicio mas corto: Ordenar por cercanía al cabezal actual
                tempList.sort((p1, p2) -> {
                    int dist1 = Math.abs(getTargetBlock(p1, allocationTable, currentHeadPosition) - currentHeadPosition);
                    int dist2 = Math.abs(getTargetBlock(p2, allocationTable, currentHeadPosition) - currentHeadPosition);
                    return Integer.compare(dist1, dist2);
                });
                break;
                
            case SCAN:
                // Algoritmo del elevador
                tempList = planSCAN(tempList, currentHeadPosition, allocationTable);
                break;
                
            default:
                break;
        }

        // Reconstruyo la lista con el nuevo orden
        for (IOPetition p : tempList) {
            petitions.insertLast(p);
        }
    }

    /**
     * Lógica para SCAN.
     * Divide la lista en dos (los que están "arriba" y los que están "abajo"),
     * y decide a cuáles atender primero según la dirección actual.
     */
    private List<IOPetition> planSCAN(List<IOPetition> allRequests, int currentHead, AllocationTable table) {
        List<IOPetition> upRequests = new ArrayList<>();
        List<IOPetition> downRequests = new ArrayList<>();

        // Clasificar peticiones según si están por encima o por debajo del cabezal
        for (IOPetition p : allRequests) {
            int target = getTargetBlock(p, table, currentHead);
            if (target >= currentHead) {
                upRequests.add(p);
            } else {
                downRequests.add(p);
            }
        }

        // Ordenar: Los de arriba de menor a mayor, los de abajo de mayor a menor
        upRequests.sort(Comparator.comparingInt(p -> getTargetBlock(p, table, currentHead)));
        downRequests.sort((p1, p2) -> Integer.compare(getTargetBlock(p2, table, currentHead), getTargetBlock(p1, table, currentHead)));

        List<IOPetition> result = new ArrayList<>();

        if (movingUp) {
            // Si estamos subiendo: Atender los de arriba, luego invertir dirección y atender los de abajo
            result.addAll(upRequests);
            result.addAll(downRequests);
            // Si no había nada arriba, técnicamente cambiamos de dirección para la próxima
            if (upRequests.isEmpty() && !downRequests.isEmpty()) {
                movingUp = false; 
            }
        } else {
            // Si estamos bajando: Atender los de abajo, luego invertir dirección y atender los de arriba
            result.addAll(downRequests);
            result.addAll(upRequests);
            // Si no había nada abajo, cambiamos dirección
            if (downRequests.isEmpty() && !upRequests.isEmpty()) {
                movingUp = true;
            }
        }

        return result;
    }

    /**
     * Helper crucial: Determina cuál es el "bloque objetivo" de una petición.
     * - Si es READ/UPDATE/DELETE: Busca el primer bloque del archivo en la tabla.
     * - Si es CREATE o DIR: No tiene bloque asignado aún, asumimos distancia 0 (prioridad máxima)
     * o devolvemos el cabezal actual para que se atienda de inmediato.
     */
    private int getTargetBlock(IOPetition petition, AllocationTable table, int currentHead) {
        String fileName = petition.getCatalog().getName();
        IOAction action = petition.getAction();

        // Si es crear, no sabemos dónde quedará, así que le damos prioridad máxima (cercanía 0)
        // haciéndolo igual al cabezal actual.
        if (action == IOAction.CREATE_FILE || action == IOAction.CREATE_DIR || action == IOAction.DELETE_DIR) {
            return currentHead; 
        }

        // Si es una operación sobre un archivo existente, buscamos dónde empieza
        SimpleList<File> files = table.getFiles();
        for (SimpleNode node = files.GetpFirst(); node != null; node = node.GetNxt()) {
            File f = (File) node.GetData();
            if (f.getName().equals(fileName)) {
                if (f.getFirstBlock() != null) {
                    return f.getFirstBlock().getBlockID();
                }
            }
        }

        // Fallback: si no se encuentra (raro), retornamos el cabezal actual
        return currentHead;
    }

    public boolean isMovingUp() {
        return movingUp;
    }

    public void setMovingUp(boolean movingUp) {
        this.movingUp = movingUp;
    }    
}
