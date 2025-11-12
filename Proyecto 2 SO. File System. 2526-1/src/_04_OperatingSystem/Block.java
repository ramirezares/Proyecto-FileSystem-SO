/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

/**
 *
 * @author AresR
 */
public class Block {
    
    // Identificador unico del bloque
    private int blockID;
    
    //Estado del bloque True = ocupado ; False = desocupado
    private boolean state;
    
    // Referencia al archivo que contiene
    private File fileReference;

    public Block(int blockID, boolean state, File fileReference) {
        this.blockID = blockID;
        this.state = state;
        this.fileReference = fileReference;
    }

    public int getBlockID() {
        return blockID;
    }

    public void setBlockID(int blockID) {
        this.blockID = blockID;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public File getFileReference() {
        return fileReference;
    }

    public void setFileReference(File fileReference) {
        this.fileReference = fileReference;
    }
    
    
    
    
}
