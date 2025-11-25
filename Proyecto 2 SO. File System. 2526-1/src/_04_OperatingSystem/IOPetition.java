/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

/**
 *
 * @author AresR
 */
public class IOPetition {
    
    IOAction action;
    
    Catalog catalog;
    
    Process1 processReference;

    public IOPetition(IOAction action, Catalog catalog, Process1 Processreference) {
        this.action = action;
        this.catalog = catalog;
        this.processReference = Processreference;
    }

    public IOAction getAction() {
        return action;
    }

    public void setAction(IOAction action) {
        this.action = action;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public Process1 getProcessReference() {
        return processReference;
    }

    public void setProcessReference(Process1 processReference) {
        this.processReference = processReference;
    }
    
}
