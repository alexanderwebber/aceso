package com.company;

import java.lang.reflect.Array;
import java.util.ArrayList;

class SliceDensityCalculator {
    Simulation S;

    ArrayList<Double> sliceDensityXY = new ArrayList<>();
    ArrayList<Double> sliceDensityXZ = new ArrayList<>();
    ArrayList<Double> sliceDensityYZ = new ArrayList<>();

    SliceDensityCalculator(Simulation S) {
        this.S = S;

    }

    boolean checkAndStopSim() {
        return true;
    }

    double calculateAreaFractionDensityXY() {
        double averageAreaFraction = 0.0;

        // Move across normal axis (Z), calculate density of all particles in XY plane at 1 micron increments
        for(int normalPosition = 0; normalPosition < S.side_length; normalPosition++) {
            double sliceGelAreaSum = 0.0;

            for(int i = 0; i < S.numGels; i++) {
                double plane = Math.abs(normalPosition - S.gels.get(i).getZ());

                if (plane < S.gels.get(i).R) {
                    double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }
            }

            for(int i = 0; i < S.imageParticles.size(); i++) {
                double plane = Math.abs(normalPosition - S.imageParticles.get(i).getZ());

                /*if (plane < S.imageParticles.get(i).R) {
                    double R = (Math.sqrt(S.imageParticles.get(i).R * S.imageParticles.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }*/
            }

            averageAreaFraction += sliceGelAreaSum / (S.side_length * S.side_length);

            sliceDensityXY.add(sliceGelAreaSum / (S.side_length * S.side_length));
        }

        return averageAreaFraction / (S.side_length);
    }

    double calculateAreaFractionDensityXZ() {
        double averageAreaFraction = 0.0;

        // Move across normal axis (Z), calculate density of all particles in XY plane at 1 micron increments
        for(int normalPosition = 0; normalPosition < S.side_length; normalPosition++) {
            double sliceGelAreaSum = 0.0;

            for(int i = 0; i < S.numGels; i++) {
                double plane = Math.abs(normalPosition - S.gels.get(i).getY());

                if (plane < S.gels.get(i).R) {
                    double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }
            }

            for(int i = 0; i < S.imageParticles.size(); i++) {
                double plane = Math.abs(normalPosition - S.imageParticles.get(i).getY());

                /*if (plane < S.imageParticles.get(i).R) {
                    double R = (Math.sqrt(S.imageParticles.get(i).R * S.imageParticles.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }*/
            }

            averageAreaFraction += sliceGelAreaSum / (S.side_length * S.side_length);

            sliceDensityXZ.add(sliceGelAreaSum / (S.side_length * S.side_length));
        }

        return averageAreaFraction / (S.side_length);
    }

    double calculateAreaFractionDensityYZ() {
        double averageAreaFraction = 0.0;

        // Move across normal axis (Z), calculate density of all particles in XY plane at 1 micron increments
        for (int normalPosition = 0; normalPosition < S.side_length; normalPosition += 1) {
            double sliceGelAreaSum = 0.0;

            for (int i = 0; i < S.numGels; i++) {
                double plane = Math.abs(normalPosition - S.gels.get(i).getX());

                if (plane < S.gels.get(i).R) {
                    double R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }
            }

            for(int i = 0; i < S.imageParticles.size(); i++) {
                double plane = Math.abs(normalPosition - S.imageParticles.get(i).getX());

                /*if (plane < S.imageParticles.get(i).R) {
                    double R = (Math.sqrt(S.imageParticles.get(i).R * S.imageParticles.get(i).R - plane * plane));
                    sliceGelAreaSum += Math.PI * (R * R);
                }*/
            }

            averageAreaFraction += sliceGelAreaSum / (S.side_length * S.side_length);

            sliceDensityYZ.add(sliceGelAreaSum / (S.side_length * S.side_length));
        }

        return averageAreaFraction / (S.side_length);
    }

    public ArrayList<Double> getSliceDensityXY() {
        return sliceDensityXY;
    }

    public ArrayList<Double> getSliceDensityXZ() {
        return sliceDensityXZ;
    }

    public ArrayList<Double> getSliceDensityYZ() {
        return sliceDensityYZ;
    }
}