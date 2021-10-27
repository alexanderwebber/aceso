package com.company;

import java.io.IOException;

public class ACESO {

	public static void main(String[] args) throws IOException, InterruptedException {
		//javax.swing.SwingUtilities.invokeLater(new Client());

		Simulation sim = new Simulation();
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
		
		if(args.length > 0) {
			int runNum = Integer.parseInt(args[0]);

			int numTCells = Integer.parseInt(args[1]);

			int numTumorCells = Integer.parseInt(args[2]);

			int tCellDoublingTime = Integer.parseInt(args[3]);

			int tumorDoublingTime = Integer.parseInt(args[4]);

			int tCellRefractoryPeriod = Integer.parseInt(args[5]);

			sim.numParticles = numTCells;

			sim.numTCells = numTumorCells;

			sim.setNumParticles(numTCells);
s
			sim.setTumorDoublingTime(tumorDoublingTime);

			sim.settCellDoublingTime(tCellDoublingTime);

			sim.fillUnthreaded();

			sim.fallUnthreaded();

			sim.runTCellsIterable(runNum);

		}
//
//		else {
//			sim.fillUnthreaded();
//
//			sim.fallUnthreaded();
//
//			sim.runTCellsIterable(1);
//		}
	}
}
