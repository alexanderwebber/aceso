package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class DensityTester {
    Simulation s;
    double size = 1000;


    DensityTester(Simulation s) throws IOException {
        this.s = s;

        BufferedImage bimage = new BufferedImage((int)s.sideLength, (int)s.sideLength, BufferedImage.TYPE_INT_RGB);

        //picture stuff
        Graphics2D g = bimage.createGraphics();

        //math loop
        double spacing = s.sideLength / size;
        double vox_length = (s.sideLength / s.vox.voxels_per_side);

        for (int i = 0; i < size; ++i) {
            double x = i * spacing;

            for (int j = 0; j < size; ++j) {
                double y = j * spacing;

                double sumSphereIntersections = 0;
                Particle[] arr = particlesToCheck(x, y);

                for (Particle p : arr) {
                    if (p != null && p.voxel != null) {
                        double dx = x - p.x;
                        double dy = y - p.y;

                        //TODO: WRITE COMMENTS WITH SPENCER
                        //distance calculation
                        double rSquaredMinusDist = p.R * p.R - (Math.pow(dx, 2) + Math.pow(dy, 2));

                        if (rSquaredMinusDist > 0) {
                            sumSphereIntersections += 2 * (Math.sqrt(rSquaredMinusDist));
                        }
                    }

                    //System.out.println(sumSphereIntersections);
                    //System.out.println(s.side_length);
                    double density = sumSphereIntersections / s.sideLength;

                    if(density > 1) {
                        density = 1;
                    }

                    g.setColor(new Color((int)(density * 255),0,0));
                    g.drawLine(i, j, i, j);
                }
            }
        }

        g.dispose();
        File outputfile = new File("density_image.jpg");
        ImageIO.write(bimage, "jpg", outputfile);
    }

    boolean dense() {
        return true;
    }


    Voxel[] inVoxels(double x, double y) {
        double vox_length = (s.sideLength / s.vox.voxels_per_side);

        int i = (int) (x / vox_length);
        i = i >= 0 ? i < s.vox.voxels_per_side ? i : i - s.vox.voxels_per_side : i + s.vox.voxels_per_side;
        int j = (int) (y / vox_length);
        j = j >= 0 ? j < s.vox.voxels_per_side ? j : j - s.vox.voxels_per_side : j + s.vox.voxels_per_side;

        return s.vox.voxels[i][j];
    }

    Particle[] particlesToCheck(double x, double y) {
        HashSet<Particle> ans = new HashSet<>();
        for(Voxel v : inVoxels(x,y)) {
            ans.addAll(Arrays.asList(v.particles));
        }
        Particle[] arr = new Particle[ans.size()];
        int index = 0;
        for (Particle p : ans) {
            if (p != null)
                arr[index++] = p;
        }
        return arr;
    }
}
