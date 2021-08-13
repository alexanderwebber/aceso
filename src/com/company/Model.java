package com.company;

public class Model {
    public Cell[] cells;
    public int numberOfCells;



    public Model(int pNumber) {
        numberOfCells = pNumber;
        cells = new Cell[numberOfCells];
    }
}
