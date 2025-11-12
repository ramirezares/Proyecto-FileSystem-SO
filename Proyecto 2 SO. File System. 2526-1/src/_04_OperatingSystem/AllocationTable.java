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
public class AllocationTable {
    
    // Tiene la lista de archivos para mostrarlos en la interfaz
    private SimpleList<File> files;

    
    // COnstructor
    public AllocationTable() {
        this.files = new SimpleList();
    }

    // Getters and Setters
    public SimpleList<File> getFiles() {
        return files;
    }

    public void setFiles(SimpleList<File> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        
        StringBuilder text = new StringBuilder();
        // Encabezado de la tabla
        text.append("--- Tabla de Asignación de Archivos ---\n");
        text.append(String.format("%-20s | %-6s | %-5s | %-25s | %-10s\n", 
                                  "Ruta (Directorio/Nombre)", "Usuario", "Bloqs", "Bloques Asignados", "Primer B."));
        text.append("--------------------------------------------------------------------------------------\n");

        if (this.files.GetpFirst() == null) {
            text.append("... No hay archivos en la tabla ...\n");
            return text.toString();
        }

        for (SimpleNode node = this.files.GetpFirst(); node != null; node = node.GetNxt()){
            File file = (File) node.GetData();

            String fullPath = file.getParentDirectory().getFullPath() + "/" + file.getName();
            int userId = file.getUser();
            int numBlocks = file.getNumberOfBlocks();
            String blockList = file.getBlocksListToString(); // Asume que esto funciona
            int firstBlock = (file.getFirstBlock() != null) ? file.getFirstBlock().getBlockID() : -1;
            
            text.append(String.format("%-20s | U%-5d | %-5d | %-25s | B%-9d\n", 
                                      fullPath, userId, numBlocks, blockList, firstBlock));
        }
        
        return text.toString();
    }
    
    
    
}
