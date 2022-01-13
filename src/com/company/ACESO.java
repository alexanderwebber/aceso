package com.company;

import java.io.IOException;

public class ACESO {

	public static void main(String[] args) throws IOException, InterruptedException {
		//javax.swing.SwingUtilities.invokeLater(new Client());


//
//		sim.fillFCC();
//
//		sim.runTCells();
		
		// Multiple runs of different radius 
//		if(args.length > 0) {
//			double averageRadius = Double.parseDouble(args[0]);
//
//			sim.rAverageRadius = averageRadius;
//
//			int runNum = Integer.parseInt(args[1]);
//
//			sim.fillFCC();
//
//			sim.runTCellsIterable(runNum);
//
//		}
//
//		else {
//			sim.fillFCC();
//			sim.runTCellsIterable(0);
//		}


		Simulation sim = new Simulation();
		if(args.length > 0) {
			int runNum = Integer.parseInt(args[0]);

			int numTCells = Integer.parseInt(args[1]);

			int numTumorCells = Integer.parseInt(args[2]);

			int tCellDoublingTime = Integer.parseInt(args[3]);

			int tumorDoublingTime = Integer.parseInt(args[4]);

			int tCellRefractoryPeriod = Integer.parseInt(args[5]);

			//updatesim.numTumor = numTumorCells;

			sim.setNumTCells(numTCells);

			sim.setStartingTumorCells(numTumorCells);

			sim.setTCellRefractoryPeriod(tCellRefractoryPeriod);

			sim.setTumorDoublingTime(tumorDoublingTime);

			sim.setTCellDoublingTime(tCellDoublingTime);

			sim.fillUnthreaded();

			sim.fallUnthreaded();

			sim.runTCellsIterable(runNum);

		}

		else {
			sim.fillUnthreaded();

			sim.fallUnthreaded();

			sim.runTCellsIterable(1);
		}
	}
}
