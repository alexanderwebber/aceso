package com.company;

import java.io.IOException;

public class ACESO {

	public static void main(String[] args) throws IOException, InterruptedException {
		//javax.swing.SwingUtilities.invokeLater(new Client());

		//double averageRadius = Double.parseDouble(args[0]);
		
		
		
		Simulation sim = new Simulation();
		
		
		//sim.fillUnthreaded();
		//sim.settleUnthreaded();
		
		//sim.fallUnthreaded();
		for(int runNum = 0; runNum < 100; runNum++) {
			sim.runTCellsIterable(runNum);
		}
		
		 
		//sim.runSim();
		
	}
}
