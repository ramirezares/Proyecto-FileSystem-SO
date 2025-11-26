/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

import _02_DataStructures.SimpleList;
import _02_DataStructures.SimpleNode;
import _03_LowLevelAbstractions.Disk;

/**
 *
 * @author AresR
 */
public class DiskHandler extends Thread {

    // ----- Atrs -----
    private Disk disk;

    private AllocationTable allocationTable;

    private Directory rootDirectory; // Directorio base

    private int lastBlockReferenced;

    // Log para cuando una operacion no se puede hacer (Error)
    // ----- Métodos ----- 
    /**
     * Constructor.
     *
     * @param disk La instancia del disco simulado.
     * @param allocationTable La tabla de asignación.
     * @param totalBlocks El número total de bloques para inicializar el disco.
     */
    public DiskHandler(Disk disk, AllocationTable allocationTable, int totalBlocks) {
        this.disk = disk;
        this.allocationTable = allocationTable;
        this.rootDirectory = new Directory("root", null, 0);
    }

    private boolean hasPermission(Directory directory, int userId) {
        if (userId == 0) {
            return true; // Admin siempre tiene permiso
        }
        if (directory.getUser() == 0) {
            return true; // Directorio público (como root)
        }
        if (directory.getUser() == userId) {
            return true; // Es el propietario del directorio
        }

        // Si no cumple nada, no tiene permiso
        // Colocar para el logger
        System.err.println("DiskHandler: Permiso denegado. Usuario " + userId + " no tiene acceso a " + directory.getFullPath());
        return false;
    }

    /**
     * Método principal llamado por FileSystem para ejecutar una petición de
     * E/S.
     *
     * @param petition La petición de E/S a ejecutar.
     * @return true si la operación fue exitosa, false en caso contrario (ej.
     * falta de espacio).
     */
    public boolean executeOperation(IOPetition petition) {
        try {
            Thread.sleep(1000); // 1 segundo de retardo por operación
        } catch (InterruptedException e) {
            
        }
        
        IOAction action = petition.getAction();
        Catalog catalog = petition.getCatalog();

        String nameOfDirectory = catalog.getNameOfDirectory();

        // Busco el directorio usando la ruta del catálogo
        Directory parentDirectory = findDirectoryByPath(nameOfDirectory);

        if (parentDirectory == null) {
            System.err.println("DiskHandler: No se encontró el directorio en la ruta: " + nameOfDirectory);
            return false;
        }

        // El método hasPermission ya imprime el error
        if (!hasPermission(parentDirectory, catalog.getUser())) {
            return false;
        }

        switch (action) {
            case READ_FILE:
                return readFile(catalog, parentDirectory);

            case CREATE_FILE:
                // El catalogo debe traer la info: nombre, tamaño (blocksQuantity), directorio 
                // y siempre trae usuario
                return createFile(catalog, parentDirectory);

            case DELETE_FILE:
                // El catalogo debe traer la info: nombre del archivo a borrar, directorio
                return deleteFile(catalog, parentDirectory);

            case UPDATE_FILE:
                // El catalogo debe traer la info: nombre actual y newName, directorio
                return updateFile(catalog, parentDirectory);

            case CREATE_DIR: // Crear directorio
                // El catalogo debe traer la info: 'nameOfDirectory' es el padre, 'name' es el nuevo directorio
                return createDirectory(catalog, parentDirectory);

            case DELETE_DIR: // Eliminar directorio
                // 'nameOfDirectory' es el padre, 'name' es el directorio a borrar
                return deleteDirectory(catalog, parentDirectory);

            default:
                System.err.println("DiskHandler: Acción desconocida: " + action);
                return false;
        }
    }

    /**
     * Método para crear un archivo.
     */
    public boolean createFile(Catalog catalog, Directory parentDirectory) {
        // Verificamos si hay espacio suficiente
        if (this.disk.getNumberAvailable() < catalog.getBlocksQuantity()) {
            System.err.println("DiskHandler: No hay espacio suficiente para crear " + catalog.getName());
            return false;
        }

        // Ya verificamos si tiene permiso para este directorio afuera
        // Verificamos que no haya otro archivo con igual nombre
        if (parentDirectory.findFileByName(catalog.getName()) != null) {
            System.err.println("DiskHandler: Ya existe un archivo con ese nombre.");
            return false;
        }

        // Buscamos los bloques libres ya que si hay espacio
        SimpleList<Block> allocatedBlocks = findFreeBlocks(catalog.getBlocksQuantity());
        if (allocatedBlocks == null) {
            System.err.println("DiskHandler: Error al encontrar bloques libres (fragmentación?).");
            return false;
        }

        // Creamos el archivo
        Block firstBlock = (Block) allocatedBlocks.GetpFirst().GetData();
        File_Proyect newFile = new File_Proyect(
                catalog.getName(),
                catalog.getBlocksQuantity(),
                firstBlock,
                allocatedBlocks,
                parentDirectory,
                catalog.getUser()
        );

        // Marcamos los bloques como ocupados en el disco
        for (SimpleNode node = allocatedBlocks.GetpFirst(); node != null; node = node.GetNxt()) {
            Block block = (Block) node.GetData();
            block.setState(true); // Ocupado
            block.setFileReference(newFile); // Referencia al archivo
        }

        // Actualizamos contador de bloques disponibles
        this.disk.setNumberAvailable(this.disk.getNumberAvailable() - catalog.getBlocksQuantity());

        // Añadimos a la Tabla de asignación 
        this.allocationTable.getFiles().insertLast(newFile);

        // Buscamos y añadimos al directorio correspondiente
        parentDirectory.getFiles().insertLast(newFile);

        // Muevo el cabezal
        if (newFile.getFirstBlock() != null) {
            this.lastBlockReferenced = newFile.getFirstBlock().getBlockID();
        }

        System.out.println("DiskHandler: Archivo creado " + newFile.getName());
        return true; // Éxito
    }

    /**
     * Método para leer un archivo (simulación).
     */
    private boolean readFile(Catalog catalog, Directory parentDirectory) {

        File_Proyect fileToRead = parentDirectory.findFileByName(catalog.getName());
        if (fileToRead == null) {
            System.err.println("DiskHandler: No se encontró el archivo a leer " + catalog.getName());
            return false; // Falla
        }

        // Verificamos si tiene permiso
        if (fileToRead.getUser() != catalog.getUser() && catalog.getUser() != 0) {
            System.err.println("DiskHandler: Permiso denegado. Usuario " + catalog.getUser() + " no es el propietario del archivo.");
            return false;
        }

        // Muevo el cabezal
        if (fileToRead.getFirstBlock() != null) {
            this.lastBlockReferenced = fileToRead.getFirstBlock().getBlockID();
        }

        // Simulación: Imprimir los bloques que se "leerían"
        System.out.println("DiskHandler: Leyendo archivo " + fileToRead.getName() + " en bloques: " + fileToRead.getBlocksListToString());
        return true;
    }

    /**
     * Método para actualizar (solo renombrar) un archivo. La AllocationTable se
     * actualiza sola porque tiene la referencia
     */
    private boolean updateFile(Catalog catalog, Directory parentDirectory) {

        // Encontramos el archivo
        File_Proyect fileToUpdate = parentDirectory.findFileByName(catalog.getName());

        if (fileToUpdate == null) {
            System.err.println("DiskHandler: No se encontró el archivo a actualizar " + catalog.getName() + " en " + parentDirectory.getFullPath());
            return false; // Falla
        }

        // Verificamos si tiene permiso
        if (fileToUpdate.getUser() != catalog.getUser() && catalog.getUser() != 0) {
            System.err.println("DiskHandler: Permiso denegado. Usuario " + catalog.getUser() + " no es el propietario del archivo.");
            return false;
        }

        // Actualizamos el nombre
        String oldName = fileToUpdate.getName();
        fileToUpdate.setName(catalog.getNewName());

        // Muevo el cabezal
        if (fileToUpdate.getFirstBlock() != null) {
            this.lastBlockReferenced = fileToUpdate.getFirstBlock().getBlockID();
        }

        System.out.println("DiskHandler: Archivo renombrado de " + oldName + " a " + catalog.getNewName());
        return true;
    }

    /**
     * Método para eliminar un archivo.
     */
    private boolean deleteFile(Catalog catalog, Directory parentDirectory) {

        // Encontrar el archivo
        File_Proyect fileToDelete = parentDirectory.findFileByName(catalog.getName());

        if (fileToDelete == null) {
            System.err.println("DiskHandler: No se encontró el archivo a eliminar " + catalog.getName() + " en " + parentDirectory.getFullPath());
            return false;
        }

        return deleteFileInternal(fileToDelete);
    }

    /**
     * Método interno para eliminar un archivo (lo separe para usarlo tambien
     * para DELETE_DIR). Libera bloques, actualiza contadores y lo quita de las
     * listas.
     */
    private boolean deleteFileInternal(File_Proyect fileToDelete) {
        // Liberamos sus bloques
        int releasedBlocks = 0;

        for (SimpleNode node = fileToDelete.getListOfBlocks().GetpFirst(); node != null; node = node.GetNxt()) {
            Block block = (Block) node.GetData();
            block.setState(false); // Libre
            block.setFileReference(null); // Sin referencia
            releasedBlocks++;
        }

        // Actualizamos el contador de bloques disponibles
        this.disk.setNumberAvailable(this.disk.getNumberAvailable() + releasedBlocks);

        // Quitamos el archivo de la tabla de asignación
        this.allocationTable.getFiles().delNodewithVal(fileToDelete);

        // Quitamos el archivo del directorio padre
        fileToDelete.getParentDirectory().removeFile(fileToDelete); // Usa el método de Directory

        // Muevo el cabezal
        if (fileToDelete.getFirstBlock() != null) {
            this.lastBlockReferenced = fileToDelete.getFirstBlock().getBlockID();
        }

        System.out.println("DiskHandler: Archivo eliminado " + fileToDelete.getName());
        return true; // Éxito
    }

    /**
     * Método para crear un nuevo directorio.
     */
    public boolean createDirectory(Catalog catalog, Directory parentDirectory) {
        String newDirName = catalog.getName();

        // Ya valide el permiso en el directorio que contiene a este en el execute
        // Verifico si ya existe
        if (parentDirectory.findSubDirectoryByName(newDirName) != null) {
            System.err.println("DiskHandler: Ya existe un directorio con el nombre " + newDirName + " en " + parentDirectory.getFullPath());
            return false;
        }

        // Creo el nuevo directorio
        Directory newDir = new Directory(newDirName, parentDirectory, catalog.getUser());
        parentDirectory.addSubDirectory(newDir);

        System.out.println("DiskHandler: Directorio creado " + newDirName + " en " + parentDirectory.getFullPath());
        return true;
    }

    /**
     * Método para eliminar un directorio (y todo su contenido).
     */
    private boolean deleteDirectory(Catalog catalog, Directory parentDirectory) {
        String dirName = catalog.getName();

        // Encontrar el directorio a eliminar
        Directory dirToDelete = parentDirectory.findSubDirectoryByName(dirName);
        if (dirToDelete == null) {
            System.err.println("DiskHandler: No se encontró el directorio a eliminar " + dirName + " en " + parentDirectory.getFullPath());
            return false;
        }

        // Ya valide el permiso de acceso en el directorio que contiene a este en el execute
        // No se puede eliminar el root
        if (dirToDelete == this.rootDirectory) {
            System.err.println("DiskHandler: Permiso denegado. No se puede eliminar el directorio root.");
            return false;
        }

        // Valido si el usuario es dueño del directorio o es el admin
        if (dirToDelete.getUser() != catalog.getUser() && catalog.getUser() != 0) {
            System.err.println("DiskHandler: Permiso denegado. Usuario " + catalog.getUser() + " no es el propietario del directorio " + dirToDelete.getName());
            return false;
        }

        // Llamo al metodo de ayuda recursivo (Esta abajo)
        deleteDirectoryRecursive(dirToDelete);

        // Quito el directorio (ya vacío) 
        parentDirectory.removeSubDirectory(dirToDelete);
        System.out.println("DiskHandler: Directorio eliminado " + dirName);
        return true;
    }

    /**
     * Método recursivo para borrar el contenido de un directorio.
     */
    private void deleteDirectoryRecursive(Directory dir) {

        // Borro todos los subdirectorios recursivamente
        // Se hace un "while" en lugar de "for" porque estamos modificando la lista
        while (dir.getSubDirectories().GetpFirst() != null) {
            Directory subDir = (Directory) dir.getSubDirectories().GetpFirst().GetData();
            deleteDirectoryRecursive(subDir); // Ir a lo más profundo primero
            dir.removeSubDirectory(subDir); // Eliminar el subdirectorio ya vacío
        }

        // Borro todos los archivos
        while (dir.getFiles().GetpFirst() != null) {
            File_Proyect file = (File_Proyect) dir.getFiles().GetpFirst().GetData();
            deleteFileInternal(file); // Esto libera bloques y lo quita de las listas
        }
    }

    /**
     * Busca una cantidad de bloques libres en el disco.
     *
     * @param count Cuántos bloques se necesitan.
     * @return Una SimpleList con los bloques encontrados, o null si no hay.
     */
    private SimpleList<Block> findFreeBlocks(int count) {
        SimpleList<Block> freeBlocks = new SimpleList<>();
        int found = 0;

        for (SimpleNode node = this.disk.getListOfBlocks().GetpFirst(); node != null; node = node.GetNxt()) {
            Block block = (Block) node.GetData();
            if (!block.isState()) { // Si no está ocupado (false)
                freeBlocks.insertLast(block);
                found++;
                if (found == count) {
                    return freeBlocks; // Encontramos suficientes
                }
            }
        }
        // Si llegamos aquí, no encontramos suficientes bloques
        return null;
    }

    /**
     * Encuentra un objeto Directorio basado en una ruta de string.
     *
     * @param path La ruta completa, ej. "root/User1/Docs".
     * @return El objeto Directory para "Docs", o null si la ruta es inválida.
     */
    private Directory findDirectoryByPath(String path) {
        // Separar la ruta por "/"
        String[] parts = path.split("/");

        // Empezar siempre desde el root
        Directory currentDirectory = this.rootDirectory;

        // Verificar que la ruta comience con "root"
        if (parts.length == 0 || !parts[0].equals("root")) {
            if (path.equals("root")) {
                return this.rootDirectory; // Caso especial: la ruta ES "root"
            }
            System.err.println("findDirectoryByPath: La ruta debe comenzar con 'root'.");
            return null;
        }

        // Recorrer las partes de la ruta, saltando "root" (i=1)
        for (int i = 1; i < parts.length; i++) {
            String dirName = parts[i];
            // Buscar el siguiente subdirectorio
            Directory nextDirectory = currentDirectory.findSubDirectoryByName(dirName);

            if (nextDirectory == null) {
                // Parte de la ruta no existe
                System.err.println("findDirectoryByPath: No se pudo encontrar '" + dirName + "' en '" + currentDirectory.getFullPath() + "'.");

                // Si el directorio no existe retorna null
                return null;
            }
            // Avanzar al siguiente directorio
            currentDirectory = nextDirectory;
        }

        // Si el bucle termina, encontramos el directorio
        return currentDirectory;
    }

    public Directory getRootDirectory() {
        return rootDirectory;
    }

    public int getLastBlockReferenced() {
        return lastBlockReferenced;
    }

    // Setter
    public void setLastBlockReferenced(int lastBlockReferenced) {
        this.lastBlockReferenced = lastBlockReferenced;
    }

// Wrapper público para poder usar findDirectoryByPath desde la GUI
    public Directory getDirectoryByPath(String path) {
        return findDirectoryByPath(path);
    }

    // --- Operaciones directas para ADMIN (sin pasar por procesos) ---
    public boolean deleteFileDirect(File_Proyect fileToDelete) {
        if (fileToDelete == null) {
            return false;
        }
        // Reutilizamos la lógica ya implementada
        return deleteFileInternal(fileToDelete);
    }

    public boolean deleteDirectoryDirect(Directory dirToDelete) {
        if (dirToDelete == null) {
            return false;
        }

        // No permitimos borrar root
        if (dirToDelete == this.rootDirectory) {
            System.err.println("DiskHandler: No se puede eliminar el directorio root.");
            return false;
        }

        // 1. Borrar todo el contenido recursivo (directorios + archivos)
        deleteDirectoryRecursive(dirToDelete);

        // 2. Quitar el directorio de su padre
        Directory parent = dirToDelete.getParentDirectory();
        if (parent != null) {
            parent.removeSubDirectory(dirToDelete);
        }

        System.out.println("DiskHandler: Directorio eliminado " + dirToDelete.getName());
        return true;
    }

}
