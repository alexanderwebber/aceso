package com.company;

import java.util.ArrayList;

class SliceDensityCalculator {
    Simulation S;

    SliceDensityCalculator(Simulation S) {
        this.S = S;

    }

    boolean checkAndStopSim() {
        return true;
    }

    ArrayList<Double> calculateAreaFractionDensityXY() {
        ArrayList<Double> sliceDensityXY = new ArrayList<>();

        double averageAreaFraction = 0.0;

        // Move across normal axis (Z), calculate density of all particles in XY plane at 1 micron increments
        for(int normalPosition = 1; normalPosition < S.sideLength; normalPosition++) {
            double sliceGelAreaSum = 0.0;

            for(int i = 0; i < S.numGels; i++) {
                double plane = Math.abs(normalPosition - S.gels.get(i).getZ());

                if (plane < S.gels.get(i).getR()) {
                    double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }

                // "Images"
//                plane = Math.abs(S.side_length - normalPosition - S.gels.get(i).getX());
//
//                // Near 0
//                if(normalPosition >= 0 && normalPosition <= S.gels.get(i).getR()) {
//                    if (plane < S.gels.get(i).R) {
//                        double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
//                        sliceGelAreaSum += Math.PI * (R * R);
//                    }
//                }
//
//                // Near side length
//                else if(normalPosition >= S.side_length - S.gels.get(i).getR() && normalPosition <= S.side_length) {
//
//                    if (plane < S.gels.get(i).R) {
//                        double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
//                        sliceGelAreaSum += Math.PI * (R * R);
//                    }
//                }
            }

            averageAreaFraction += sliceGelAreaSum / (S.sideLength * S.sideLength);

            sliceDensityXY.add(sliceGelAreaSum / (S.sideLength * S.sideLength));
        }

        return sliceDensityXY;
    }

    ArrayList<Double> calculateAreaFractionDensityXZ() {
        ArrayList<Double> sliceDensityXZ = new ArrayList<>();
        double averageAreaFraction = 0.0;

        // Move across normal axis (Y), calculate density of all particles in XZ plane at 1 micron increments
        for(int normalPosition = 1; normalPosition < S.sideLength; normalPosition++) {
            double sliceGelAreaSum = 0.0;

            for(int i = 0; i < S.numGels; i++) {
                double plane = Math.abs(normalPosition - S.gels.get(i).getY());

                if (plane < S.gels.get(i).R) {
                    double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }

//                // "Images"
//                plane = Math.abs(S.side_length - normalPosition - S.gels.get(i).getX());
//
//                // Near 0
//                if(normalPosition >= 0 && normalPosition <= S.gels.get(i).getR()) {
//
//                    if (plane < S.gels.get(i).R) {
//                        double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
//                        sliceGelAreaSum += Math.PI * (R * R);
//                    }
//                }
//
//                // Near side length
//                if(normalPosition >= S.side_length - S.gels.get(i).getR() && normalPosition <= S.side_length) {
//
//                    if (plane < S.gels.get(i).R) {
//                        double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
//                        sliceGelAreaSum += Math.PI * (R * R);
//                    }
//                }

            }

            averageAreaFraction += sliceGelAreaSum / (S.sideLength * S.sideLength);

            sliceDensityXZ.add(sliceGelAreaSum / (S.sideLength * S.sideLength));
        }

        return sliceDensityXZ;
    }

    ArrayList<Double>calculateAreaFractionDensityYZ() {
        ArrayList<Double> sliceDensityYZ = new ArrayList<>();

        double averageAreaFraction = 0.0;

        // Move across normal axis (X), calculate density of all particles in YZ plane at 1 micron increments
        for (int normalPosition = 1; normalPosition < S.sideLength; normalPosition++) {
            double sliceGelAreaSum = 0.0;

            for (int i = 0; i < S.numGels; i++) {
                double plane = Math.abs(normalPosition - S.gels.get(i).getX());


                if (plane < S.gels.get(i).R) {
                    double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }

//                // "Images"
//                plane = Math.abs(S.side_length - normalPosition - S.gels.get(i).getX());
//
//                // Near 0
//                if(normalPosition >= 0 && normalPosition <= S.gels.get(i).getR()) {
//
//                    if (plane < S.gels.get(i).R) {
//                        double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
//                        sliceGelAreaSum += Math.PI * (R * R);
//                    }
//                }
//
//                // Near side length
//                if(normalPosition >= S.side_length - S.gels.get(i).getR() && normalPosition <= S.side_length) {
//
//                    if (plane < S.gels.get(i).R) {
//                        double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
//                        sliceGelAreaSum += Math.PI * (R * R);
//                    }
//                }
            }

            averageAreaFraction += sliceGelAreaSum / (S.sideLength * S.sideLength);

            sliceDensityYZ.add(sliceGelAreaSum / (S.sideLength * S.sideLength));
        }

        return sliceDensityYZ;
    }
}