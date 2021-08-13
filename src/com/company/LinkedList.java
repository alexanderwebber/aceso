package com.company;

import java.util.ArrayList;

public class LinkedList<E>
{
    public Node<E> head;

    int numOfElements;
    public LinkedList() {
        numOfElements = 0;
        head = null;
    }

    public void insertElement(E element) {
        if(head == null) {
            head = new Node(element);
        }

        else {
            head.insertNext(element);
        }
        numOfElements++;
    }

    public Node deleteElement(E element) {
        Node response = null;

        if(head == null) {
            return null;
        }
        else {
            if(head.element.equals(element)) {
                response = head;
                head = response.next;
                numOfElements--;
            }
            else {
                response = head.deleteElement(element);
            }
        }

        if(response != null) {
            numOfElements--;
        }

        return response;
    }

}
