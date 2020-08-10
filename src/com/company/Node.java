package com.company;

public class Node<E> {
    public E element;

    public Node next;

    public Node(E element) {
        element = this.element;

        next = null;
    }

    public void insertNext(E element) {
        if(next == null) {
            next = new Node(element);
        }

        else {
            next.insertNext(element);
        }
    }

    public Node deleteElement(E element) {
        Node response = null;

        if(next != null) {
            if(next.element.equals(element)) {
                response = next;
                next = response.next;
            }
            else {
                response = next.deleteElement(element);
            }
        }

        return response;

    }


}
