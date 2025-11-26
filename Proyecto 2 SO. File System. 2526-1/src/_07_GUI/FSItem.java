/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _07_GUI;

/**
 *
 * @author Danaz
 */
public class FSItem {
    private String name;
    private String user;
    private int blocks;

    public FSItem(String name, String user, int blocks) {
        this.name = name;
        this.user = user;
        this.blocks = blocks;
    }

    public String getName() { return name; }
    public String getUser() { return user; }
    public int getBlocks() { return blocks; }
    
}
