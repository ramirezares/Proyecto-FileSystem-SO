/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package _01_ApplicationPackage;

import _04_OperatingSystem.Catalog;
import _04_OperatingSystem.FileSystem;
import _04_OperatingSystem.IOAction;
import _04_OperatingSystem.OperatingSystem;

/**
 *
 * @author AresR
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        
        // 1. Configuración Inicial
        System.out.println("Iniciando simulación completa del SO...");
        OperatingSystem os = new OperatingSystem();
        
        // Iniciamos el OS y todos sus hilos (CPU, DMA, FileSystem, Clock)
        os.startOS();
        
        // Obtenemos la referencia al FileSystem (que contiene el disco y la tabla)
        // (Asegúrate de añadir getFileSystem() a tu OperatingSystem.java)
        FileSystem fs = os.getFileSystem();;
        
        // --- INICIO DE PRUEBAS ---
        // Damos tiempo a que los hilos se procesen (OS -> CPU -> I/O Request -> DMA -> FileSystem)
        int aLittleWhile = 500; // ms

        // 3. PRUEBA 1: Crear un directorio para User 1
        System.out.println(">>> PRUEBA 1: Admin (U0) crea directorio 'User1Docs' para User 1 en 'root'");
        // Usamos el helper del OS para crear el catálogo
        Catalog cat1 = os.createCatalogForProcess(IOAction.CREATE_DIR, "root", "User1Docs", "", 0, 1, "Directory");
        // Creamos el proceso en el OS
        os.newProcess(IOAction.CREATE_DIR, cat1);
        

        // 4. PRUEBA 2: User 1 crea un archivo en su directorio 'User1Docs'
        System.out.println(">>> PRUEBA 2: User 1 crea 'fileA.txt' (3 bloques) en 'root/User1Docs'");
        Catalog cat2 = os.createCatalogForProcess(IOAction.CREATE_FILE, "root/User1Docs", "fileA.txt", "", 3, 1, "File");
        os.newProcess(IOAction.CREATE_FILE, cat2);
        

        // 5. PRUEBA 3: User 2 crea su propio directorio
        System.out.println(">>> PRUEBA 3: Admin (U0) crea directorio 'User2Docs' para User 2 en 'root'");
        Catalog cat3 = os.createCatalogForProcess(IOAction.CREATE_DIR, "root", "User2Docs", "", 0, 2, "Directory");
        os.newProcess(IOAction.CREATE_DIR, cat3);
        
        // 6. PRUEBA 4: User 2 crea un archivo grande en su directorio
        System.out.println(">>> PRUEBA 4: User 2 crea 'data.bin' (8 bloques) en 'root/User2Docs'");
        Catalog cat4 = os.createCatalogForProcess(IOAction.CREATE_FILE, "root/User2Docs", "data.bin", "", 8, 2, "File");
        os.newProcess(IOAction.CREATE_FILE, cat4);
        
        
        // 7. PRUEBA 5: (PERMISO DENEGADO) User 1 intenta crear un archivo en el directorio de User 2
        System.out.println(">>> PRUEBA 5: User 1 intenta crear 'hack.txt' en 'root/User2Docs' (DEBE FALLAR)");
        Catalog cat5 = os.createCatalogForProcess(IOAction.CREATE_FILE, "root/User2Docs", "hack.txt", "", 2, 1, "File"); // User 1 (ID: 1)
        os.newProcess(IOAction.CREATE_FILE, cat5);

        // 8. PRUEBA 6: (PERMISO DENEGADO) User 1 intenta borrar el directorio de User 2
        System.out.println(">>> PRUEBA 6: User 1 intenta borrar 'root/User2Docs' (DEBE FALLAR)");
        Catalog cat6 = os.createCatalogForProcess(IOAction.DELETE_DIR, "root", "User2Docs", "", 0, 1, "Directory"); // User 1 (ID: 1)
        os.newProcess(IOAction.DELETE_DIR, cat6);
        
        
        // 9. PRUEBA 7: (ÉXITO) User 1 borra su PROPIO archivo 'fileA.txt'
        System.out.println(">>> PRUEBA 7: User 1 borra 'fileA.txt' de 'root/User1Docs' (DEBE FUNCIONAR)");
        Catalog cat7 = os.createCatalogForProcess(IOAction.DELETE_FILE, "root/User1Docs", "fileA.txt", "", 0, 1, "File"); // User 1 (ID: 1)
        os.newProcess(IOAction.DELETE_FILE, cat7);
        
        // 10. PRUEBA 8: (ÉXITO) Admin (User 0) borra el directorio de User 2 (y su contenido)
        System.out.println(">>> PRUEBA 8: Admin (User 0) borra 'root/User2Docs' (DEBE FUNCIONAR)");
        Catalog cat8 = os.createCatalogForProcess(IOAction.DELETE_DIR, "root", "User2Docs", "", 0, 0, "Directory"); // User 0 (Admin)
        os.newProcess(IOAction.DELETE_DIR, cat8);

        // --- FIN DE LA SIMULACIÓN ---
        System.out.println("Simulación terminada. Deteniendo OS...");
        // Usamos el método de apagado completo que tienes en tu OS
        //os.shutdownOS(); 
        
        
//        System.out.println("=== INICIO: Prueba rápida del SO y DMA (usando createCatalogForProcess + newProcess) ===");
//
//        // Crear el SO y arrancarlo (arranca CPU, DMA y Reloj internamente)
//        OperatingSystem os = new OperatingSystem();
//
//        // Crear y registrar procesos usando las funciones del SO. La memoria
//        // principal solo admite 2 procesos (MainMemory.MEMORY_SIZE == 2) por lo
//        // que el tercer proceso quedará en la cola de nuevos del DMA.
//        Catalog c1 = os.createCatalogForProcess(IOAction.CREATE_FILE, "root", "f1.txt", null, 1, 100, "text/plain");
//        os.newProcess(IOAction.CREATE_FILE, c1);
//
//        Catalog c2 = os.createCatalogForProcess(IOAction.UPDATE_FILE, "root", "f2.txt", null, 1, 101, "text/plain");
//        os.newProcess(IOAction.UPDATE_FILE, c2);
//
//        // Este tercero debería no encontrar espacio en memoria y ser enviado a la cola de nuevos del DMA
//        Catalog c3 = os.createCatalogForProcess(IOAction.DELETE_FILE, "root", "f3.txt", null, 1, 102, "text/plain");
//        os.newProcess(IOAction.DELETE_FILE, c3);
//
//        System.out.println("Se han creado 3 procesos mediante OperatingSystem.createCatalogForProcess + newProcess().");
//
//        System.out.println("");
//        System.out.println("=== Inicio de la prueba ===");
//        os.startOS();
//        
//        // Apagar el SO y sus sub-hilos
//        //os.shutdownOS();
       
    }

}
