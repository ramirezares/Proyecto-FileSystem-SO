/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _02_DataStructures;

/**
 *
 * @author AresR
 */
public class SimpleNode<T> {
    
    private T Data;
    private SimpleNode Nxt;

    public SimpleNode() {
        this.Data = null;
        this.Nxt = null;
    }

    /**
     * Instancia la clase SimpleNode otorgando un valor al atributo Data, Nxt se
     * define como null.
     *
     * @param Data Representa el tipo de dato que guardara el nodo.
     */
    public SimpleNode(T Data) {
        this.Data = Data;
        this.Nxt = null;
    }

    /**
     * Instancia la clase SimpleNode otorgando un valor a los atributos Data y
     * Nxt.
     *
     * @param Data Representa el tipo de dato que guardara el nodo.
     * @param Nxt representa el puntero que guarda/apunta el siguiente nodo
     */
    public SimpleNode(T Data, SimpleNode Nxt) {
        this.Data = Data;
        this.Nxt = Nxt;
    }

    /**
     * Permite acceder al valor almacenado en el atributo Data de una instancia
     * de la clase SimpleNode
     *
     * @return El valor almacenado en el atributo Data.
     */
    public T GetData() {
        return this.Data;
    }

    /**
     * Permite acceder al valor almacenado en el atributo Nxt de una instancia
     * de la clase SimpleNode, el cual representa el siguiente nodo.
     *
     * @return El valor almacenado en el atributo Nxt.
     */
    public SimpleNode GetNxt() {
        return this.Nxt;
    }

    /**
     * Permite modificar el valor almacenado en el atributo Data de una
     * instancia de la clase SimpleNode.
     */
    public void SetData(T NData) {
        this.Data = NData;
    }

    /**
     * Permite modificar el valor almacenado en el atributo Nxt de una instancia
     * de la clase SimpleNode, el cual representa el siguiente nodo. Realiza la
     * asignacion del nuevo nodo al que apunta.
     */
    public void SetNxt(SimpleNode NNxt) {
        this.Nxt = NNxt;
    }
    
}
