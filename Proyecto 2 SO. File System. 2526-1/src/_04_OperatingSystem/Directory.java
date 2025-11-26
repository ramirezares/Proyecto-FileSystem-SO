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
public class Directory {
    
    // ----- Atrs ----- 
    private String name;
    private Directory parentDirectory; // Directorio padre (null si es el root)
    private SimpleList<Directory> subDirectories; // Lista de subdirectorios
    private SimpleList<File> files; // Lista de archivos en este directorio
    private int user; // Id del usuario 

    // ----- Métodos ----- 

    /**
     * Constructor 
     * @param name El nombre del directorio.
     * @param parentDirectory Directorio que almacena esta instancia de objeto directorio
     * sera null si el directorio en cuestion es el directorio padre.
     * @param userID El ID del usuario propietario.
     */
    public Directory(String name, Directory parentDirectory, int userID) {
        this.name = name;
        this.parentDirectory = parentDirectory;
        this.subDirectories = new SimpleList<>();
        this.files = new SimpleList<>();
        this.user = userID;
    }

    /**
     * Añade un archivo a este directorio.
     * @param file El archivo a añadir.
     */
    public void addFile(File file) {
        this.files.insertLast(file);
        file.setParentDirectory(this); // Asegura que el archivo sepa quién es su padre
    }

    /**
     * Elimina un archivo de este directorio.
     * @param file El archivo a eliminar.
     */
    public void removeFile(File file) {
        this.files.delNodewithVal(file); // Usando el método de tu SimpleList
    }

    /**
     * Añade un subdirectorio a este directorio.
     * @param directory El subdirectorio a añadir.
     */
    public void addSubDirectory(Directory directory) {
        this.subDirectories.insertLast(directory);
    }

    /**
     * Elimina un subdirectorio de este directorio.
     * @param directory El subdirectorio a eliminar.
     */
    public void removeSubDirectory(Directory directory) {
        this.subDirectories.delNodewithVal(directory);
    }
    
    /**
     * Busca un archivo en este directorio por su nombre.
     * @param name El nombre del archivo a buscar.
     * @return El objeto File si se encuentra, de lo contrario null.
     */
    public File findFileByName(String name) {
        for (SimpleNode node = this.files.GetpFirst(); node != null; node = node.GetNxt()) {
            File file = (File) node.GetData();
            if (file.getName().equals(name)) {
                return file;
            }
        }
        return null; // No encontrado
    }

    /**
     * Busca un subdirectorio en este directorio por su nombre.
     * @param name El nombre del subdirectorio a buscar.
     * @return El objeto Directory si se encuentra, de lo contrario null.
     */
    public Directory findSubDirectoryByName(String name) {
        for (SimpleNode node = this.subDirectories.GetpFirst(); node != null; node = node.GetNxt()) {
            Directory dir = (Directory) node.GetData();
            if (dir.getName().equals(name)) {
                return dir;
            }
        }
        return null; // No encontrado
    }
    
    /**
     * Genera la ruta completa del directorio 
     * @return String con la ruta, ej: "root/U1/Docs"
     */
    public String getFullPath() {
        if (parentDirectory == null) {
            return name; // es root
        }
        
        // Busca la ruta
        return parentDirectory.getFullPath() + "/" + name;
    }

    // --- Getters y Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public SimpleList<Directory> getSubDirectories() {
        return subDirectories;
    }

    public SimpleList<File> getFiles() {
        return files;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    /**
     * Método para imprimir el árbol de directorios desde este
     * directorio.
     */
    public void printDirectoryTree() {
        System.out.println("--- Estructura de Directorios ---");
        printTreeRecursive("");
    }

    /**
     * Ayudante recursivo para imprimir la estructura.
     * @param indent La sangría actual (crece con la profundidad).
     */
    private void printTreeRecursive(String indent) {
        // Imprime el directorio actual con su propietario
        System.out.println(indent + "+-- " + this.name + "/ (U" + this.user + ")");

        // Preparar sangría para los hijos
        String childIndent = indent + "|   ";

        // Imprimir todos los archivos en este directorio
        for (SimpleNode node = this.files.GetpFirst(); node != null; node = node.GetNxt()) {
            File file = (File) node.GetData();
            System.out.println(childIndent + "- " + file.getName() + " (U" + file.getUser() + ")");
        }

        // Llamar recursivamente para todos los subdirectorios
        for (SimpleNode node = this.subDirectories.GetpFirst(); node != null; node = node.GetNxt()) {
            Directory subDir = (Directory) node.GetData();
            subDir.printTreeRecursive(childIndent);
        }
    }

    public Directory getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(Directory parentDirectory) {
        this.parentDirectory = parentDirectory;
    }
    
}
