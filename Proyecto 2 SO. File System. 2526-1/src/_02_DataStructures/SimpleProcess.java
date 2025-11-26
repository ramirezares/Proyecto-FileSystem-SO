/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _02_DataStructures;

import _04_OperatingSystem.ProcessType;

/**
 *
 * @author Danaz
 */
public class SimpleProcess {
    private String name;
    private int instructions;
    private ProcessType type; // "CPU_BOUND" o "IO_BOUND"
    private int cyclesForIO;
    private int ioDuration;

    public SimpleProcess(String name, int instructions, ProcessType type, int cyclesForIO, int ioDuration) {
        this.name = name;
        this.instructions = instructions;
        this.type = type;
        this.cyclesForIO = cyclesForIO;
        this.ioDuration = ioDuration;
    }
    public SimpleProcess() {
        
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInstructions(int instructions) {
        this.instructions = instructions;
    }

    public void setType(ProcessType  type) {
        this.type = type;
    }

    public void setCyclesForIO(int cyclesForIO) {
        this.cyclesForIO = cyclesForIO;
    }

    public void setIoDuration(int ioDuration) {
        this.ioDuration = ioDuration;
    }

    // Getters (importantes para el JSON)
    public String getName() { return name; }
    public int getInstructions() { return instructions; }
    public ProcessType  getType() { return type; }
    public int getCyclesForIO() { return cyclesForIO; }
    public int getIoDuration() { return ioDuration; }
}

