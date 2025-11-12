/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _03_LowLevelAbstractions;

import _04_OperatingSystem.FileSystem;
import static java.lang.Thread.MAX_PRIORITY;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simula el reloj del sistema operativo, contando los ciclos de 1 en 1 ( de
 * duracion ajustable)
 *
 * @author AresR
 */
public class RealTimeClock extends Thread {

    // Contador global de ciclos transcurridos. AtomicLong para que la actualización sea atomica
    private static AtomicLong totalCyclesElapsed;
    public static final long DEFAULT_DURATION = 1000; // Duración por defecto: 1000 ms

    // Duración actual del ciclo en milisegundos. Es tipo volatile para que se lea el valor más reciente si es modificado desde otro hilo
    private volatile long clockDuration;    // Esta es la que se cambia dinamicamente
    private volatile boolean isRunning;
    
    private final Object pauseMonitor = new Object(); // Para pausar/reanudar sin terminar el hilo
    private volatile boolean paused = false;

    private final CPU cpuTarget; // Referencia al hilo CPU que debe notificar
    private final DMA dmaTarget; // Referencia al hilo DMA que debe notificar
    private final FileSystem fileSystemReference; // Referencia al hilo File System que debe notificar
    private Runnable onTickListener;// callback externo

    /**
     * Constructor que acepta un manejador de ticks y, opcionalmente, una
     * duración inicial.
     *
     * @param cpuTarget Objeto cpu a notificar
     * @param dmaTarget Objeto dma a notificar
     * @param duration Duración indicada del ciclo en ms. Usará 1000ms por
     * defecto
     */
    public RealTimeClock(CPU cpuTarget, DMA dmaTarget, FileSystem fileSystemReference, long duration) {
        this.cpuTarget = cpuTarget;
        this.dmaTarget = dmaTarget;
        this.fileSystemReference = fileSystemReference;
        this.clockDuration = duration > 0 ? duration : DEFAULT_DURATION;
        RealTimeClock.totalCyclesElapsed = new AtomicLong(0);
        setName("Thread del Reloj");
    }

    /**
     * Bucle principal que simula el paso del tiempo.
     */
    @Override
    public void run() {
        System.out.printf("Reloj de iniciado.\n", clockDuration);
        this.setPriority(MAX_PRIORITY);
        this.isRunning = true;

        while (isRunning) {
            try {
                // Si estamos en pausa, esperar hasta reanudar
                synchronized (pauseMonitor) {
                    while (paused && isRunning) {
                        pauseMonitor.wait();
                    }
                }

                if (!isRunning) break;

                // Espera el tiempo. Lee el valor volatile
                long duration = this.clockDuration;
                Thread.sleep(duration);

                long currentCycle = totalCyclesElapsed.incrementAndGet();
                System.out.println("Ciclo:" + currentCycle);  // COMENTAR LUEGO DE FINALIZAR

                // Sincronizar al CPU y al DMA
                this.cpuTarget.receiveTick();
                this.dmaTarget.receiveTick();
                if (onTickListener != null) {
                    onTickListener.run(); // notifica al simulador
                }
                if (!this.fileSystemReference.getPetitions().isEmpty()
                            || this.fileSystemReference.getCurrentPetition() != null) {
                        this.fileSystemReference.executeIOOperation();
                    }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.isRunning = false;
            }
        }
    }

    /**
     * Modifica la duración del ciclo en tiempo de ejecución. El cambio se
     * aplica en la siguiente iteración del Thread.sleep().
     *
     * @param newDurationMs Nueva duración en milisegundos (ms).
     */
    public void setClockDuration(long newDurationMs) {
        if (newDurationMs > 0) {
            this.clockDuration = newDurationMs;
            System.out.printf("RELOJ Duración del ciclo modificada a: %d ms.\n", newDurationMs);
        } else {
            System.err.println("RELOJ Advertencia: La duración del ciclo debe ser positiva.");
        }
    }
    
    /**
     * Hace que el reloj reanude su operacion
     */
    public void playClock() {
        // Reanuda desde pausa. Si el hilo fue arrancado antes, solo libera la espera.
        this.paused = false;
        synchronized (pauseMonitor) {
            pauseMonitor.notifyAll();
        }
    }
    
    /**
     * Detiene la cuenta del reloj
     */
    public void stopClock() {
        // Para compatibilidad con llamadas previas a "stopClock",
        // aquí hacemos una pausa en lugar de terminar el hilo.
        this.paused = true;
    }

    /**
     * Detiene permanentemente el hilo del reloj (si realmente se desea terminarlo).
     */
    public void shutdownClock() {
        this.isRunning = false;
        // Despertar si está pausado para que salga del bucle
        synchronized (pauseMonitor) {
            pauseMonitor.notifyAll();
        }
        this.interrupt();
    }

    /**
     * Devuelve el valor actual del contador de ciclos.
     *
     * @return
     */
    public long getTotalCyclesElapsed() {
        return this.totalCyclesElapsed.get();
    }

    /**
     *
     * @return
     */
    public long getClockDuration() {
        return this.clockDuration;
    }

    public void setOnTickListener(Runnable listener) {
        this.onTickListener = listener;
    }
}