/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _04_OperatingSystem;

/**
 *
 * @author AresR
 */
public enum PolicyType {
    Priority,   // Nonpreemptive Priority
    FIFO,       // First In First Out 
    ROUND_ROBIN, 
    SPN,        // Short Process next
    SRT,        // Shortest Remaining Time
    HRRN        // Highest Response Ratio Next

}
