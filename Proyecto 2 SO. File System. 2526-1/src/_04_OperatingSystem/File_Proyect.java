/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

import _02_DataStructures.SimpleList;
import _02_DataStructures.SimpleNode;

/**
 *
 * @author AresR
 */
public class File_Proyect {
    
    // Nombre
    private String name;
    
    // Tamaño en bloques
    private int numberOfBlocks;
    
    // Primer bloque
    private Block firstBlock;
    
    // Lista enlazada de bloques simulada
    private SimpleList<Block> listOfBlocks;
    
    // Directorio al que pertenece
    private Directory ParentDirectory;
    
    // Id del usuario al que pertenece
    private int user;

    /**
     * Constructor
     * El primer bloque, la lista de bloques, el directorio y el usuario 
     * deben ser determinadas por el FileSystem para ubicar el archivo en disco
     * 
     * @param name
     * @param numberOfBlocks
     * @param firstBlock
     * @param listOfBlocks
     * @param parentDirectory
     * @param userID
     */
    public File_Proyect(String name, int numberOfBlocks, Block firstBlock, SimpleList<Block> listOfBlocks, Directory parentDirectory, int userID) {
        this.name = name;
        this.numberOfBlocks = numberOfBlocks;
        this.firstBlock = firstBlock;
        this.listOfBlocks = listOfBlocks;
        this.ParentDirectory = parentDirectory;
        this.user = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public Block getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(Block firstBlock) {
        this.firstBlock = firstBlock;
    }

    public SimpleList<Block> getListOfBlocks() {
        return listOfBlocks;
    }
    
    public String getBlocksListToString() {
        
        StringBuilder text = new StringBuilder();
        
        
        for (SimpleNode node = this.listOfBlocks.GetpFirst(); node != null; node = node.GetNxt()){
            Block block = (Block) node.GetData();
            
            text.append(block.getBlockID());
            Block pLast = (Block) this.listOfBlocks.GetpLast().GetData();
            if(!pLast.equals(block)){
                text.append(",");
            }
        
        }
        
        return text.toString();
    }

    public void setListOfBlocks(SimpleList<Block> listOfBlocks) {
        this.listOfBlocks = listOfBlocks;
    }

    public Directory getParentDirectory() {
        return ParentDirectory;
    }

    public void setParentDirectory(Directory ParentDirectory) {
        this.ParentDirectory = ParentDirectory;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }
    
    
    
}
