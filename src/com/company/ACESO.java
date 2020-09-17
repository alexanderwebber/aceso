package com.company;

import java.io.IOException;

public class ACESO {
    static Simulation S = new Simulation();

    public static void main(String[] args) throws IOException {
        //javax.swing.SwingUtilities.invokeLater(new Client());

        // no gui
        S.fill();

        S.fall();

        //S.runTCells();

    }
}
