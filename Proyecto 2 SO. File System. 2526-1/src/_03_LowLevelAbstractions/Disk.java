/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _03_LowLevelAbstractions;

import _02_DataStructures.SimpleList;
import _02_DataStructures.SimpleNode;
import _04_OperatingSystem.Block;

/**
 *
 * @author AresR
 */
public class Disk {
    
    // Tiene un numero de bloques
    private int numberOfBlocks;
    
    // Numero de disponibles
    private int numberAvailable;
        
    // Tiene la lista de bloques del sistema, usaremos 32 porque 64 son muchos bloques a mostrar en la pantalla
    private SimpleList<Block> listOfBlocks;

    public Disk(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
        this.numberAvailable = numberOfBlocks;
        this.listOfBlocks = null;
        initializeDisk(numberOfBlocks);
    }
    
    private void initializeDisk(int totalBlocks) {
        SimpleList<Block> blocks = new SimpleList<>();
        for (int i = 0; i < totalBlocks; i++) {
            // Inicialmente, todos los bloques están libres (state=false) y sin archivo
            blocks.insertLast(new Block(i, false, null));
        }
        this.setListOfBlocks(blocks);
        this.setNumberOfBlocks(totalBlocks);
        this.setNumberAvailable(totalBlocks);
    }
    
    /**
     * Genera una representación visual del disco en la terminal.
     * Muestra 8 bloques por fila.
     * [F] = Libre, [O] = Ocupado
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Simulación de Disco (SD) - ").append(numberAvailable).append("/").append(numberOfBlocks).append(" libres.\n");
        
        int blocksPerRow = 8; // Para un formato 8x4 = 32 bloques
        if (this.listOfBlocks == null || this.listOfBlocks.GetpFirst() == null) {
            return "Disco no inicializado.";
        }
        
        int count = 0;
        for (SimpleNode node = this.listOfBlocks.GetpFirst(); node != null; node = node.GetNxt()) {
            Block block = (Block) node.GetData();
            
            // Define el contenido del bloque
            String content = block.isState() ? "O" : "F"; // Ocupado o Libre
            
            // Añade el ID del archivo si está ocupado
            if (block.isState() && block.getFileReference() != null) {
                 content = "U" + block.getFileReference().getUser(); // Muestra Usuario
            }

            sb.append(String.format("[B%02d:%s]", block.getBlockID(), content));
            
            count++;
            if (count % blocksPerRow == 0) {
                sb.append("\n"); // Nueva línea cada 8 bloques
            } else {
                sb.append(" "); // Espacio entre bloques
            }
        }
        
        return sb.toString();
    }
    

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public int getNumberAvailable() {
        return numberAvailable;
    }

    public void setNumberAvailable(int numberAvailable) {
        this.numberAvailable = numberAvailable;
    }

    public SimpleList<Block> getListOfBlocks() {
        return listOfBlocks;
    }

    public void setListOfBlocks(SimpleList<Block> listOfBlocks) {
        this.listOfBlocks = listOfBlocks;
    }
}
