/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package _01_ApplicationPackage;

import _04_OperatingSystem.Catalog;
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
    public static void main(String[] args) {
        System.out.println("=== INICIO: Prueba rápida del SO y DMA (usando createCatalogForProcess + newProcess) ===");

        // Crear el SO y arrancarlo (arranca CPU, DMA y Reloj internamente)
        OperatingSystem os = new OperatingSystem();

        // Crear y registrar procesos usando las funciones del SO. La memoria
        // principal solo admite 2 procesos (MainMemory.MEMORY_SIZE == 2) por lo
        // que el tercer proceso quedará en la cola de nuevos del DMA.
        Catalog c1 = os.createCatalogForProcess(IOAction.Create, "f1.txt", null, 1, 100, "text/plain");
        os.newProcess(IOAction.Create, c1);

        Catalog c2 = os.createCatalogForProcess(IOAction.Update, "f2.txt", null, 1, 101, "text/plain");
        os.newProcess(IOAction.Create, c2);

        // Este tercero debería no encontrar espacio en memoria y ser enviado a la cola de nuevos del DMA
        Catalog c3 = os.createCatalogForProcess(IOAction.Delete, "f3.txt", null, 1, 102, "text/plain");
        os.newProcess(IOAction.Create, c3);

        System.out.println("Se han creado 3 procesos mediante OperatingSystem.createCatalogForProcess + newProcess().");

        System.out.println("");
        System.out.println("=== Inicio de la prueba ===");
        os.startOS();
        
        // Apagar el SO y sus sub-hilos
        //os.shutdownOS();
       
    }

}
