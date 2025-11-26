/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

/**
 *
 * @author AresR
 */
public class Catalog {
    
    private String nameOfDirectory;
    private String name;
    private String newName;
    private int blocksQuantity;
    private int user;
    private String resourceType;
    
    public Catalog(String nameOfDirectory, String name, String newName, int blocksQuantity, int user, String resourceType) {
        this.nameOfDirectory = nameOfDirectory;
        this.name = name;
        this.newName = newName;
        this.blocksQuantity = blocksQuantity;
        this.user = user;
        this.resourceType = resourceType;
    }

    public String getNameOfDirectory() {
        return nameOfDirectory;
    }

    public void setNameOfDirectory(String nameOfDirectory) {
        this.nameOfDirectory = nameOfDirectory;
    }

    public String getName() {
        return name;
    }

    public String getNewName() {
        return newName;
    }

    public int getBlocksQuantity() {
        return blocksQuantity;
    }

    public int getUser() {
        return user;
    }

    public String getResourceType() {
        return resourceType;
    }
}
