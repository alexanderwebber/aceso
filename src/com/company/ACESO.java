package com.company;

import java.io.IOException;

public class ACESO {

	public static void main(String[] args) throws IOException, InterruptedException {

		boolean GUI = Boolean.parseBoolean(args[7]);

		if(GUI) {
			javax.swing.SwingUtilities.invokeLater(new Client());
		}

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

		else {
			Simulation sim = new Simulation();

			Visualization panel = new Visualization(sim);

			panel.see_box = true;

			sim.setPanel(panel);

			if(args.length > 0) {
				int runNum = Integer.parseInt(args[0]);

				int numTCellRatio = Integer.parseInt(args[1]);

				//int numTCells = Integer.parseInt((args[1]));

				int numTumorCells = Integer.parseInt(args[2]);

				int tCellDoublingTime = Integer.parseInt(args[3]);

				int tumorDoublingTime = Integer.parseInt(args[4]);

				int tCellRefractoryPeriod = Integer.parseInt(args[5]);

				double tumorGelNoGelRadius = Double.parseDouble(args[6]);

				//updatesim.numTumor = numTumorCells;

				sim.setNumTCellRatio(numTCellRatio);

				//sim.setNumTCells(numTCells);

				sim.setStartingTumorCells(numTumorCells);

				sim.setTCellRefractoryPeriod(tCellRefractoryPeriod);

				sim.setTumorDoublingTime(tumorDoublingTime);

				sim.setTCellDoublingTime(tCellDoublingTime);

				sim.setTumorGelNoGelRadius(tumorGelNoGelRadius);

//			sim.fillUnthreaded();
//
//			sim.fallUnthreaded();

				sim.runTCellsIterable(runNum);

			}

			else {
				sim.fillUnthreaded();

				sim.fallUnthreaded();

				sim.runTCellsIterable(1);
			}
		}

	}
}
