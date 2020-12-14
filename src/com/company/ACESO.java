package com.company;

import java.io.IOException;

public class ACESO {


    public static void main(String[] args) throws IOException {
        javax.swing.SwingUtilities.invokeLater(new Client());

        // no gui
        /*Simulation S = new Simulation();

        S.gui = false;

        S.fillUnthreaded();

        S.fallUnthreaded();

        S.runTCellsUnthreaded();*/

    }
}
