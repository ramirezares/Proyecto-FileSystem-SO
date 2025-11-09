/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package _02_DataStructures;

/**
 *
 * @author AresR
 */
public class SimpleList<T> {
    
    private SimpleNode pFirst;
    private SimpleNode pLast;
    private int size;

    /**
     * Instancia la clase SimpleList definiendo los atributos pFirst y pLast
     * como null y size como 0. Crea la lista vacia.
     */
    public SimpleList() {
        this.pFirst = this.pLast = null;
        size = 0;
    }

    /**
     * Verifica si una instancia de la clase SimpleList se encuentra vacia o no.
     * Verifica si la lista tiene por lo menos un elemento.
     *
     * @return Retorna True si la lista no tiene elementos. En caso contrario
     * retorna false.
     */
    public boolean isEmpty() {
        boolean val = false;
        if (this.size == 0) {
            val = true;
        }
        return val;
    }

    /**
     * Inserta un nuevo valor como primer elemento en una instancia de la clase
     * SimpleList.
     *
     * @param Data Representa el valor que guardara el nodo que se insertara de
     * primero en la cabeza de la lista.
     */
    public void insertHead(T Data) {
        SimpleNode NNode = new SimpleNode(Data);
        if (this.size < 2) {
            if (this.pFirst == null) {
                this.pFirst = this.pLast = NNode;
            } else if (this.pFirst == this.pLast) {
                NNode.SetNxt(this.pFirst);
                this.pFirst = NNode;
            }
        } else {
            SimpleNode aux = this.pFirst;
            NNode.SetNxt(aux);
            this.pFirst = NNode;
        }
        this.size++;
    }

    /**
     * Inserta un nuevo valor como ultimo elemento en una instancia de la clase
     * SimpleList.
     *
     * @param Data Representa el valor que guardara el nodo que se insertara en
     * el final de la lista.
     */
    public void insertLast(T Data) {
        SimpleNode NNode = new SimpleNode(Data);
        NNode.SetData(Data);
        if (size < 2) {
            if (this.pFirst == null) {
                this.pFirst = this.pLast = NNode;
            } else if (this.pFirst == this.pLast) {
                this.pLast.SetNxt(NNode);
                this.pLast = NNode;
            }
        } else {
            this.pLast.SetNxt(NNode);
            this.pLast = NNode;
        }
        this.size++;
    }

    /**
     * Verifica que un valor se encuentre en una instancia de la clase
     * SimpleList.
     *
     * @param Data Representa el valor que se busca en la lista.
     * @return Retorna true si el valor buscado se encuentra en la lista. En
     * caso contrario retorna false.
     */
    public boolean isDataInList(T Data) {
        boolean val = false;
        for (SimpleNode pNode = this.pFirst; pNode != null; pNode = pNode.GetNxt()) {
            if (java.util.Objects.equals(pNode.GetData(), Data)) {
                val = true;
            }
        }
        return val;
    }

    /**
     * Busca el valor espeficicado en una instancia de la clase SimpleList.
     *
     * @param Data Representa el valor que se busca en la lista.
     * @return Retorna un puntero al nodo donde se encuenta, si el valor
     * especificado esta en la lista. En caso contrario muestra el mensaje "El
     * valor no se encuentra en la lista" y retorna null.
     */
    public SimpleNode locateData(T Data) {
        SimpleNode pMatched;
        if (isDataInList(Data)) {
            for (SimpleNode pNode = this.pFirst; pNode != null; pNode = pNode.GetNxt()) {
                if (java.util.Objects.equals(pNode.GetData(), Data)) {
                    pMatched = pNode;
                    return pMatched;
                }
            }
        }
        return null;
    }

    /**
     * Elimina el nodo que contiene el valor especificado en una instancia de la
     * clase SimpleList. En caso de que el valor no se encuentre en la lista
     * muestra el mensaje: "El valor a eliminar no esta en la lista."
     *
     * @param Data Representa el valor que tiene el nodo que se eliminara en la
     * lista.
     *
     */
    public void delNodewithVal(T Data) {
        if (isDataInList(Data)) {
            for (SimpleNode pPrev = null, pCurrent = this.pFirst;
                    pCurrent != null;
                    pPrev = pCurrent, pCurrent = pCurrent.GetNxt()) {

                if (java.util.Objects.equals(pCurrent.GetData(), Data)) {

                    if (pPrev == null) {
                        // CASO 1: Eliminando la CABEZA (pCurrent es pFirst)
                        this.pFirst = pCurrent.GetNxt();
                    } else {
                        // CASO 2: Eliminando un nodo interno o la cola
                        pPrev.SetNxt(pCurrent.GetNxt());
                    }

                    // Actualizar pLast si el nodo eliminado era el último
                    if (pCurrent == this.pLast) {
                        this.pLast = pPrev;
                    }

                    // Importante: Actualizar el tamaño de la lista
                    this.size--;

                    break;
                }
            }
        }
    }

    /**
     * Elimina todos los nodos que se encuentren dentro de una instancia de la
     * clase SimpleList.
     */
    public void destroyer() {
        for (SimpleNode pNode = this.pFirst; pNode != null; pNode = pNode.GetNxt()) {
            this.pFirst = pNode.GetNxt();
        }
        System.out.println("Lista vaciada.");
    }

    /**
     * Busca y devuelve el nodo que se encuentra en un indice indicado.
     *
     * @param index Representa el numero del indice de la lista que se desea
     * obtener
     * @return Retorna el nodo que se encuentra en la posicion indicada, si esta
     * posicion existe dentro de la lista, es decir, si la posicion indicada
     * pertenece al conjunto de numeros que se encuentran entre 0 y el tamaño de
     * la lista. De lo contrario, si la lista esta vacia o si el indice indicado
     * no se encuentra en la lista, retorna null
     */
    public SimpleNode GetValInIndex(int index) {
        SimpleNode matched;
        if (this.size > 0) {
            if (index <= this.size) {
                int aux = 0;
                for (SimpleNode pNode = this.pFirst; pNode != null; pNode = pNode.GetNxt()) {
                    if (aux == index) {
                        matched = pNode;
                        return matched;
                    }
                    aux++;
                }
            }
        }
        return null;
    }

    /**
     * Modifica el valor contenido en un nodo en la posicion indicada.
     *
     * @param index la posicion en la cual se desea colocar el nuevo contenido.
     * @param newData el nuevo contenido a ser introducido en la lista.
     */
    public void SetValInIndex(int index, T newData) {
        if (this.size > 0) {
            if (index <= this.size) {
                int aux = 1;
                for (SimpleNode pNode = this.pFirst; pNode != null; pNode = pNode.GetNxt()) {
                    if (aux == index) {
                        pNode.SetData(newData);
                        break;
                    }
                    aux++;
                }
            }
        }
    }

    /**
     * Obtiene el nodo guardado en el atributo pFirst de una instancia de la
     * clase SimpleList.
     *
     * @return Retorna el nodo guardado en el atributo pFirst
     */
    public SimpleNode GetpFirst() {
        return this.pFirst;
    }

    /**
     * Obtiene el nodo guardado en el atributo pLast de una instancia de la
     * clase SimpleList.
     *
     * @return Retorna el nodo guardado en el atributo pLast
     */
    public SimpleNode GetpLast() {
        return this.pLast;
    }

    /**
     * Obtiene el valor guardado en el atributo Size de una instancia de la
     * clase SimpleList.
     *
     * @return Retorna el entero guardado en el atributo Size que representa el
     * largo de la lista
     */
    public int GetSize() {
        return this.size;
    }

    /**
     * Modifica el atributo pFirst de una instancia de la clase SimpleList,
     * remplazandolo por un nuevo nodo.
     *
     * @param nFirst
     */
    public void SetpFirst(SimpleNode nFirst) {
        this.pFirst = nFirst;

        if (nFirst == null) {
            this.pLast = null;
            this.size = 0;
        }
    }

    /**
     * Modifica el atributo pLast de una instancia de la clase SimpleList,
     * remplazandolo por un nuevo nodo.
     *
     * @param nLast
     */
    public void SetpLast(SimpleNode nLast) {
        this.pLast = nLast;
    }
    
}
