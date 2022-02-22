package com.company;

import com.company.Utilities.QuickSort;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Visualization extends JPanel {
    Simulation S;
    boolean box_view = true;
    boolean slicer_view = false;
    boolean see_box = true;
    boolean see_gels = true;
    boolean see_tcell = true;
    boolean see_tumor = true;
    boolean see_images = true;

    //SimulationSettings settings;
    static Drawable[] drawthis = new Drawable[1000000];
    static int drawObjects;
    String filename = "box_particles.csv";
    Rotator o = new Rotator();
    Shifter k = new Shifter();
    SlicerShifter sl = new SlicerShifter();
    double cos_theta;
    double sin_theta;
    double cos_phi;
    double sin_phi;
    JLabel timeLabel = new JLabel("0.0h");
    GridBagConstraints gc = new GridBagConstraints();


    Visualization(Simulation S) {
        this.S = S;
        setSize(1500, 1500);
        setLayout(new GridBagLayout());
        addKeyListener(k);
        addMouseListener(o);
        addMouseMotionListener(o);
        addMouseWheelListener(o);
        addMouseWheelListener(sl);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(2, 0, 0, 2);

        add(timeLabel, gc);

    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        String timeValue = String.format("%.1f", (S.sim_time * (20.0 / 3600.0)));

        if(S.sim_time * (20.0 / 3600.0) % 0.5 == 0) {
            timeLabel.setText("    " + timeValue + "h");
            timeLabel.setFont(new Font("Serif", Font.BOLD, 28));
            timeLabel.setForeground(Color.WHITE);
        }

        g.setColor(Color.black);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (box_view) {
            drawObjects = 0;
            cos_theta = Math.cos(o.theta);
            sin_theta = Math.sin(o.theta);
            cos_phi = Math.cos(o.phi);
            sin_phi = Math.sin(o.phi);
            if (see_box)
                addBox(S);
            if (see_gels)
                addGels();
            if (see_tcell)
                addParticles();
            if (see_tumor) {
                drawTumoroids();
            }

            try {
                new QuickSort(drawthis, 0, drawObjects);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < drawObjects; ++i) {
                if (drawthis[i] != null) {
                    drawthis[i].draw(g);
                }
            }
        }

        else if (slicer_view) {
            g.setColor(Color.white);
            g.drawString(String.format("z = %f", sl.getZ()), getWidth() - 100, getHeight() - 25);
            double scale = .5;
            if (see_box) {
                int x0 = (int) (-S.sideLength * scale / 2.0 + getWidth() / 2.0);
                int y0 = (int) (-S.sideLength * scale / 2.0 + getHeight() / 2.0);
                int x1 = (int) (S.sideLength * scale / 2.0 + getWidth() / 2.0);
                int y1 = (int) (S.sideLength * scale / 2.0 + getHeight() / 2.0);
                g.setColor(Color.blue);
                g.drawLine(x0, y0, x0, y1);
                g.drawLine(x0, y0, x1, y0);
                g.drawLine(x0, y1, x1, y1);
                g.drawLine(x1, y0, x1, y1);
                x0 -= 160 * scale;
                y0 -= 160 * scale;
                x1 += 160 * scale;
                y1 += 160 * scale;
                g.setColor(Color.green);
                g.drawLine(x0, y0, x0, y1);
                g.drawLine(x0, y0, x1, y0);
                g.drawLine(x0, y1, x1, y1);
                g.drawLine(x1, y0, x1, y1);
            }
            if (see_gels) {
                for (int i = 0; i < S.numGels; ++i) {
                    Gel temp = new Gel();
                    double d = Math.abs(sl.getZ() - S.gels.get(i).z);
                    if (d < S.gels.get(i).R) {
                        temp.R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - d * d));
                        temp.x = (S.gels.get(i).x - temp.R - S.sideLength / 2);
                        temp.y = (S.gels.get(i).y - temp.R - S.sideLength / 2);
                        temp.x *= scale;
                        temp.y *= scale;
                        temp.R *= scale;

                        temp.x += getWidth() / 2.0;
                        temp.y += getHeight() / 2.0;

                        temp.draw(g);

                        // TODO: Don't forget this for image drawing
                        if (S.gels.get(i).R + S.gels.get(i).x > S.sideLength) {
                            temp.x -= S.sideLength / 2;
                            temp.drawImage(g);
                            temp.x += S.sideLength / 2;
                        }
                        if (S.gels.get(i).R + S.gels.get(i).y > S.sideLength) {
                            temp.y -= S.sideLength / 2;
                            temp.drawImage(g);
                            temp.y += S.sideLength / 2;
                        }
                        
                        if (S.gels.get(i).x - S.gels.get(i).R < 0) {
                            temp.x += S.sideLength / 2;
                            temp.drawImage(g);
                            temp.x -= S.sideLength / 2;
                        }
                        if (S.gels.get(i).y - S.gels.get(i).R < 0) {
                            temp.y += S.sideLength / 2;
                            temp.drawImage(g);
                            temp.y -= S.sideLength / 2;
                        }
                        
                        if (S.gels.get(i).y - S.gels.get(i).R < 0 && S.gels.get(i).x - S.gels.get(i).R < 0) {
                            temp.y += S.sideLength / 2;
                            temp.x += S.sideLength / 2;
                            
                            temp.drawImage(g);
                            
                            temp.y -= S.sideLength / 2;
                            temp.x -= S.sideLength / 2;
                        }
                    }
                }
            }



            if (see_tcell) {
                for (int i = 0; i < S.numParticles; ++i) {
                    TCell temp = new TCell();
                    double d = Math.abs(sl.getZ() - S.tCells[i].z);
                    if (d < S.tCells[i].R) {
                        temp.R = (Math.sqrt(S.tCells[i].R * S.tCells[i].R - d * d));
                        temp.x = (S.tCells[i].x - temp.R - S.sideLength / 2);
                        temp.y = (S.tCells[i].y - temp.R - S.sideLength / 2);
                        //scaaaaale
                        temp.x *= scale;
                        temp.y *= scale;
                        temp.R *= scale;

                        temp.x += getWidth() / 2.0;
                        temp.y += getHeight() / 2.0;

                        temp.draw(g);
                    }
                }
            }

            if (see_tumor) {
                for (int i = 0; i < S.getNumTumor(); ++i) {
                    Tumoroid temp = new Tumoroid();
                    double d = Math.abs(sl.getZ() - S.getTumoroids().get(i).getZ());
                    if (d < S.getTumoroids().get(i).R) {
                        temp.R = (Math.sqrt(S.getTumoroids().get(i).getR() * S.getTumoroids().get(i).getR() - d * d));
                        temp.x = (S.getTumoroids().get(i).getX() - temp.R - S.sideLength / 2);
                        temp.y = (S.getTumoroids().get(i).getY() - temp.R - S.sideLength / 2);
                        //scaaaaale
                        temp.x *= scale;
                        temp.y *= scale;
                        temp.R *= scale;

                        temp.x += getWidth() / 2.0;
                        temp.y += getHeight() / 2.0;

                        temp.draw(g);
                    }
                }
            }
        }

    }
    private void addBox(Box B) {
        double s = B.sideLength / 2;
        double[][] corners =  new double[][] {
                {-s, -s, -s},
                {-s, -s, s},
                {-s, s, -s},
                {-s, s, s},
                {s, -s, -s},
                {s, -s, s},
                {s, s, -s},
                {s, s, s}
        };

        for (double[] corner : corners) {
            double x = corner[0];
            double y = corner[1];
            double z = corner[2];
            //transform
            corner[0] = x * cos_theta - y * sin_theta;
            corner[1] = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            corner[2] = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;
            //shift
            corner[0] += k.offset_x;
            corner[1] += k.offset_y;
            //scale
            corner[0] *= o.r / 2000;
            corner[1] *= o.r / 2000;
            corner[2] *= o.r / 2000;
            //center
            corner[0] += this.getWidth() / 2;
            corner[1] += this.getHeight() / 2;
        }
        drawthis[drawObjects++] = new LineSegment(corners[0][0], corners[0][1], corners[0][2], corners[1][0], corners[1][1], corners[1][2]);
        drawthis[drawObjects++] = new LineSegment(corners[0][0], corners[0][1], corners[0][2], corners[2][0], corners[2][1], corners[2][2]);
        drawthis[drawObjects++] = new LineSegment(corners[0][0], corners[0][1], corners[0][2], corners[4][0], corners[4][1], corners[4][2]);
        drawthis[drawObjects++] = new LineSegment(corners[1][0], corners[1][1], corners[1][2], corners[3][0], corners[3][1], corners[3][2]);
        drawthis[drawObjects++] = new LineSegment(corners[1][0], corners[1][1], corners[1][2], corners[5][0], corners[5][1], corners[5][2]);
        drawthis[drawObjects++] = new LineSegment(corners[2][0], corners[2][1], corners[2][2], corners[3][0], corners[3][1], corners[3][2]);
        drawthis[drawObjects++] = new LineSegment(corners[2][0], corners[2][1], corners[2][2], corners[6][0], corners[6][1], corners[6][2]);
        drawthis[drawObjects++] = new LineSegment(corners[3][0], corners[3][1], corners[3][2], corners[7][0], corners[7][1], corners[7][2]);
        drawthis[drawObjects++] = new LineSegment(corners[4][0], corners[4][1], corners[4][2], corners[5][0], corners[5][1], corners[5][2]);
        drawthis[drawObjects++] = new LineSegment(corners[4][0], corners[4][1], corners[4][2], corners[6][0], corners[6][1], corners[6][2]);
        drawthis[drawObjects++] = new LineSegment(corners[5][0], corners[5][1], corners[5][2], corners[7][0], corners[7][1], corners[7][2]);
        drawthis[drawObjects++] = new LineSegment(corners[6][0], corners[6][1], corners[6][2], corners[7][0], corners[7][1], corners[7][2]);
    }

    private void addGels() {
        for (int i = 0; i < S.numGels - 1; i++) {
            Gel temp = new Gel();

            temp.R = S.gels.get(i).R;
            double x = S.gels.get(i).x - S.sideLength / 2;
            double y = S.gels.get(i).y - S.sideLength / 2;
            double z = S.gels.get(i).z - S.sideLength / 2;

            //transform
            temp.x = x * cos_theta - y * sin_theta;
            temp.y = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            temp.z = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;

            //shift
            temp.x += k.offset_x;
            temp.y += k.offset_y;

            //scale
            temp.x *= o.r / 2000;
            temp.y *= o.r / 2000;
            temp.z *= o.r / 2000;
            temp.R *= o.r / 2000;

            //center
            temp.x +=  (double) (getWidth() / 2) - temp.R;
            temp.y +=  (double) (getHeight() / 2) - temp.R;

            drawthis[drawObjects++] = temp;
        }
    }

    private void addParticles() {
        for (int i = 0; i < S.numParticles; ++i) {
            TCell temp = new TCell();
            
            temp.setStatus(S.tCells[i].getStatus());
            temp.R = S.tCells[i].R;
            double x = S.tCells[i].x - S.sideLength / 2;
            double y = S.tCells[i].y - S.sideLength / 2;
            double z = S.tCells[i].z - S.sideLength / 2;
            
            //transform
            temp.x = x * cos_theta - y * sin_theta;
            temp.y = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            temp.z = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;
            
            //shift
            temp.x += k.offset_x;
            temp.y += k.offset_y;
            
            //scale
            temp.x *= o.r / 2000;
            temp.y *= o.r / 2000;
            temp.z *= o.r / 2000;
            temp.R *= o.r / 2000;
            
            //center
            temp.x +=  (double) (getWidth() / 2) - temp.R;
            temp.y +=  (double) (getHeight() / 2) - temp.R;
            drawthis[drawObjects++] = temp;
        }
    }

   private void drawTumoroids() {

        for (int i = 0; i < S.getNumTumor(); i++) {
            Tumoroid temp = new Tumoroid();
            temp.setR(S.getTumoroids().get(i).getR());

            // Add status
            if(S.getTumoroids().get(i).getStatus().equals("alive")) {
                temp.setStatus("alive");
            } else if (S.getTumoroids().get(i).getStatus().equals("being_attacked")) {
                temp.setStatus("being_attacked");
            } else if (S.getTumoroids().get(i).getStatus().equals("dead")) {
                temp.setStatus("dead");
            }

            double x = S.getTumoroids().get(i).getX() - S.sideLength / 2;
            double y = S.getTumoroids().get(i).getY() - S.sideLength / 2;
            double z = S.getTumoroids().get(i).getZ() - S.sideLength / 2;

            //transfooooooooorm
            temp.x = x * cos_theta - y * sin_theta;
            temp.y = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            temp.z = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;

            //scaaaaaale
            temp.x *= o.r / 2000;
            temp.y *= o.r / 2000;
            temp.z *= o.r / 2000;
            temp.R *= o.r / 2000;

            //centeeeeer
            temp.x = ((int) temp.x + k.offset_x - (int) temp.R) + (double) this.getWidth() / 2;
            temp.y = ((int) temp.y + k.offset_y - (int) temp.R) + (double) this.getHeight() / 2;

            drawthis[drawObjects++] = temp;
        }
    }

    class SlicerShifter extends MouseAdapter {
        double z = 500;
        double scroll_amount = 5;
        double getZ() {return z;}

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            super.mouseWheelMoved(e);
            if (e.getPreciseWheelRotation() > 0) {
                if (z >= S.sideLength - 2) {
                    z = S.sideLength;
                }
                else {
                    z += scroll_amount;
                }
            }
            else {
                if (z <= 2) {
                    z = 0;
                }
                else {
                    z -= scroll_amount;
                }
            }
        }
    }

    public canvas getC() {
        return this.c;
    }

    public void printBMP(int simTime) {
        BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);

        Graphics gBMP = image.getGraphics();

        gBMP.setColor(Color.BLACK);

        getC().paint(gBMP);

        try {
            String numberAsString = String.format ("%04d", simTime / 50);
            ImageIO.write(image, "BMP", new File("BMPs/fifty_kill_" + numberAsString + ".bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class canvas extends JPanel
    {
        simulation simulation;
        public void paintComponent(Graphics g)
        {
            int size = Box.numGels + Box.numParticles + Box.numTumor;
            if (simulation != null) {
                g.setColor(Color.white);
                g.drawString(String.format("Number of particles: %d", Box.numParticles), 10, 25);
                g.drawString(String.format("Number of tumor cells removed: %d", Box.numTumorRemoved), 10, 40);
                g.drawString(String.format("Time: %.0f", simulation.getTime()),10,55);
                g.drawString(String.format("Time elapsed: %.3f seconds", simulation.getReal_time()),10,70);
            }
            else {
                g.drawString(String.format("Percent Filled: %.3f", Box.getFilled() * 100),10,25);
                g.drawString(String.format("Num Gels: %d", Box.numGels),10,40);
                g.drawString(String.format("Time elapsed: %.3f seconds", Box.getElapsed()),10,55);
            }

            double[][] corners = drawBox();

            // For visualization and BMP printing, set either full box or only back wall
            if(fullBox == true) {
                g.setColor(Color.white);
                g.drawLine((int)corners[0][0] + k.offset_x,(int)corners[0][1] + k.offset_y,(int)corners[1][0] + k.offset_x,(int)corners[1][1] + k.offset_y);
                g.drawLine((int)corners[0][0] + k.offset_x,(int)corners[0][1] + k.offset_y,(int)corners[2][0] + k.offset_x,(int)corners[2][1] + k.offset_y);
                g.drawLine((int)corners[0][0] + k.offset_x,(int)corners[0][1] + k.offset_y,(int)corners[4][0] + k.offset_x,(int)corners[4][1] + k.offset_y);
                g.drawLine((int)corners[1][0] + k.offset_x,(int)corners[1][1] + k.offset_y,(int)corners[3][0] + k.offset_x,(int)corners[3][1] + k.offset_y);
                g.drawLine((int)corners[1][0] + k.offset_x,(int)corners[1][1] + k.offset_y,(int)corners[5][0] + k.offset_x,(int)corners[5][1] + k.offset_y);
                g.drawLine((int)corners[2][0] + k.offset_x,(int)corners[2][1] + k.offset_y,(int)corners[3][0] + k.offset_x,(int)corners[3][1] + k.offset_y);
                g.drawLine((int)corners[2][0] + k.offset_x,(int)corners[2][1] + k.offset_y,(int)corners[6][0] + k.offset_x,(int)corners[6][1] + k.offset_y);
                g.drawLine((int)corners[3][0] + k.offset_x,(int)corners[3][1] + k.offset_y,(int)corners[7][0] + k.offset_x,(int)corners[7][1] + k.offset_y);
                g.drawLine((int)corners[4][0] + k.offset_x,(int)corners[4][1] + k.offset_y,(int)corners[5][0] + k.offset_x,(int)corners[5][1] + k.offset_y);
                g.drawLine((int)corners[4][0] + k.offset_x,(int)corners[4][1] + k.offset_y,(int)corners[6][0] + k.offset_x,(int)corners[6][1] + k.offset_y);
                g.drawLine((int)corners[5][0] + k.offset_x,(int)corners[5][1] + k.offset_y,(int)corners[7][0] + k.offset_x,(int)corners[7][1] + k.offset_y);
                g.drawLine((int)corners[6][0] + k.offset_x,(int)corners[6][1] + k.offset_y,(int)corners[7][0] + k.offset_x,(int)corners[7][1] + k.offset_y);
            }
            else {
                g.setColor(Color.BLACK);
                g.drawLine((int)corners[0][0] + k.offset_x,(int)corners[0][1] + k.offset_y,(int)corners[1][0] + k.offset_x,(int)corners[1][1] + k.offset_y);
                g.drawLine((int)corners[0][0] + k.offset_x,(int)corners[0][1] + k.offset_y,(int)corners[2][0] + k.offset_x,(int)corners[2][1] + k.offset_y);
                g.drawLine((int)corners[0][0] + k.offset_x,(int)corners[0][1] + k.offset_y,(int)corners[4][0] + k.offset_x,(int)corners[4][1] + k.offset_y);
                g.drawLine((int)corners[1][0] + k.offset_x,(int)corners[1][1] + k.offset_y,(int)corners[3][0] + k.offset_x,(int)corners[3][1] + k.offset_y);
                g.drawLine((int)corners[1][0] + k.offset_x,(int)corners[1][1] + k.offset_y,(int)corners[5][0] + k.offset_x,(int)corners[5][1] + k.offset_y);
                g.setColor(Color.white);
                g.drawLine((int)corners[2][0] + k.offset_x,(int)corners[2][1] + k.offset_y,(int)corners[3][0] + k.offset_x,(int)corners[3][1] + k.offset_y);
                g.drawLine((int)corners[2][0] + k.offset_x,(int)corners[2][1] + k.offset_y,(int)corners[6][0] + k.offset_x,(int)corners[6][1] + k.offset_y);
                g.drawLine((int)corners[3][0] + k.offset_x,(int)corners[3][1] + k.offset_y,(int)corners[7][0] + k.offset_x,(int)corners[7][1] + k.offset_y);
                g.setColor(Color.BLACK);
                g.drawLine((int)corners[4][0] + k.offset_x,(int)corners[4][1] + k.offset_y,(int)corners[5][0] + k.offset_x,(int)corners[5][1] + k.offset_y);
                g.drawLine((int)corners[4][0] + k.offset_x,(int)corners[4][1] + k.offset_y,(int)corners[6][0] + k.offset_x,(int)corners[6][1] + k.offset_y);
                g.drawLine((int)corners[5][0] + k.offset_x,(int)corners[5][1] + k.offset_y,(int)corners[7][0] + k.offset_x,(int)corners[7][1] + k.offset_y);
                g.setColor(Color.white);
                g.drawLine((int)corners[6][0] + k.offset_x,(int)corners[6][1] + k.offset_y,(int)corners[7][0] + k.offset_x,(int)corners[7][1] + k.offset_y);
            }

            int num_gels = Box.numGels;
            int num_part = Box.numParticles;
            coordinate_transform(num_gels, num_part, Box.numTumor);

            //Setup Variables for the Rendering
            double actinRadius = 0.0;
            double xActin = 0.0;
            double yActin = 0.0;
            double alphaFill = 0.0;
            double alphaStroke = 0.0;

            //Cell Rendering variables
            double cellRadius = 0.0;
            double nucRadius = 0.0;
            double cellXo = 0.0;
            double cellYo = 0.0;

            //Drawing Checks
            double checkRadius = 0.0;
            double maxRadius = 0.0;

            for (int i = 0; i < size; i++) { //loop through gels+particles
                if (drawthis[i].type.equals("particle")) { //is a particle
                    cellRadius = drawthis[i].R;
                    cellXo = drawthis[i].x;
                    cellYo = drawthis[i].y;

                    //draw background for immune cell
                    for (int frames = 0; frames < 20; ++frames) {
                        boolean draw = true;
                        while(draw) {
                            actinRadius = 0.4 * cellRadius + Math.random() * cellRadius * 0.6;
                            xActin = cellXo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius);
                            yActin = cellYo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius);
                            alphaFill = 0.1;

                            g.setColor(new Color(0, 0, 0, (int)(alphaFill * 255)));
                            checkRadius = (cellXo - xActin) * (cellXo - xActin) + (cellYo - yActin) * (cellYo - yActin);
                            maxRadius = (cellRadius - actinRadius) * (cellRadius - actinRadius);
                            if (checkRadius < maxRadius) {
                                g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                draw = false;
                            }
                        }
                    }
                    //draw immune cell
                    for (int frames = 0; frames < 50; ++frames) {
                        boolean draw = true;
                        while (draw) {
                            actinRadius = 0.4 * cellRadius + Math.random() * cellRadius * 0.6;
                            xActin = cellXo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius)* o.r/2000;
                            yActin = cellYo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius)* o.r/2000;
                            alphaFill = 0.04;
                            alphaStroke = 0.1;

                            checkRadius = (cellXo - xActin) * (cellXo - xActin) + (cellYo - yActin) * (cellYo - yActin);
                            maxRadius = (cellRadius - actinRadius) * (cellRadius - actinRadius);
                            if (checkRadius < maxRadius) {
                                if(drawthis[i].getStatus() == "active") {
                                    g.setColor(new Color(200, 0, 0, (int) (alphaFill * 255)));
                                    g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    g.setColor(new Color(200, 0, 0, (int) (alphaStroke * 255)));
                                    g.drawOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    draw = false;
                                }

                                else if(drawthis[i].getStatus() == "attacking") {
                                    g.setColor(new Color(255, 255, 0, (int) (alphaFill * 255)));
                                    g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    g.setColor(new Color(255, 255, 0, (int) (alphaStroke * 255)));
                                    g.drawOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    draw = false;
                                }

                                else if(drawthis[i].getStatus() == "resting") {
                                    g.setColor(new Color(255, 165, 0, (int) (alphaFill * 255)));
                                    g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    g.setColor(new Color(255, 165, 0, (int) (alphaStroke * 255)));
                                    g.drawOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    draw = false;
                                }

                                else if(drawthis[i].getStatus() == "exhausted") {
                                    g.setColor(new Color(220, 220, 220, (int) (alphaFill * 255)));
                                    g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    g.setColor(new Color(220, 220, 220, (int) (alphaStroke * 255)));
                                    g.drawOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    draw = false;
                                }
                            }
                        }
                    }
                }

                else if (drawthis[i].type.equals("tumor")) {
                    cellRadius = drawthis[i].R;
                    cellXo = drawthis[i].x;
                    cellYo = drawthis[i].y;

                    //draw background for tumor cell
                    for (int frames = 0; frames < 20; ++frames) {
                        boolean draw = true;
                        while(draw) {
                            actinRadius = 0.4 * cellRadius + Math.random() * cellRadius * 0.6;
                            xActin = cellXo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius);
                            yActin = cellYo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius);
                            alphaFill = 0.1;

                            if(drawthis[i].getStatus() == "alive") {
                                g.setColor(new Color(0, 0, 0, (int)(alphaFill * 255)));
                                checkRadius = (cellXo - xActin) * (cellXo - xActin) + (cellYo - yActin) * (cellYo - yActin);
                                maxRadius = (cellRadius - actinRadius) * (cellRadius - actinRadius);
                                if (checkRadius < maxRadius) {
                                    g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    draw = false;
                                }
                            }

                            else if(drawthis[i].getStatus() == "being_attacked") {
                                g.setColor(new Color(0, 0, 0, (int)(alphaFill * 255)));
                                checkRadius = (cellXo - xActin) * (cellXo - xActin) + (cellYo - yActin) * (cellYo - yActin);
                                maxRadius = (cellRadius - actinRadius) * (cellRadius - actinRadius);
                                if (checkRadius < maxRadius) {
                                    g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    draw = false;
                                }
                            }

                            else if(drawthis[i].getStatus() == "dead") {
                                g.setColor(new Color(255, 255, 255, (int)(alphaFill * 255)));
                                checkRadius = (cellXo - xActin) * (cellXo - xActin) + (cellYo - yActin) * (cellYo - yActin);
                                maxRadius = (cellRadius - actinRadius) * (cellRadius - actinRadius);
                                if (checkRadius < maxRadius) {
                                    g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                    draw = false;
                                }
                            }
                        }
                    }
                    //draw tumor cell
                    for (int frames = 0; frames < 50; ++frames) {
                        boolean draw = true;
                        while (draw) {
                            actinRadius = 0.4 * cellRadius + Math.random() * cellRadius * 0.6;
                            xActin = cellXo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius)* o.r/2000;
                            yActin = cellYo + ((Math.random() - 0.5) * 2.0) * (cellRadius - actinRadius)* o.r/2000;
                            alphaFill = 0.04;
                            alphaStroke = 0.1;

                            checkRadius = (cellXo - xActin) * (cellXo - xActin) + (cellYo - yActin) * (cellYo - yActin);
                            maxRadius = (cellRadius - actinRadius) * (cellRadius - actinRadius);
                            if (checkRadius < maxRadius) {
                                g.setColor(new Color(0, 200, 0, (int) (alphaFill * 255)));
                                g.fillOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                g.setColor(new Color(0, 200, 0, (int) (alphaStroke * 255)));
                                g.drawOval((int)(xActin - actinRadius) + k.offset_x, (int)(yActin - actinRadius) + k.offset_y, (int)(2 * actinRadius), (int)(2 * actinRadius));
                                draw = false;
                            }
                        }
                    }
                }
                /*
                    g.setColor(new Color(255, 62, 51, 181)); //outline
                    g.drawOval((int) drawthis[i].x + k.offset_x - (int) (drawthis[i].R * o.r/2000), (int) drawthis[i].y + k.offset_y - (int) (drawthis[i].R * o.r/2000), (int) (drawthis[i].R * o.r/1000), (int) (drawthis[i].R * o.r/1000));
                    g.setColor(new Color(175, 37, 37, 95));
                    g.fillOval((int) drawthis[i].x + k.offset_x - (int) (drawthis[i].R * o.r/2000), (int) drawthis[i].y + k.offset_y - (int) (drawthis[i].R * o.r/2000), Math.max((int) (drawthis[i].R * o.r/1000), 1), Math.max((int) (drawthis[i].R * o.r/1000), 1));
                }*/
                else {
                    g.setColor(new Color(54, 45, 255, 0)); //outline
                    g.drawOval((int) drawthis[i].x + k.offset_x - (int) (drawthis[i].R), (int) drawthis[i].y + k.offset_y - (int) (drawthis[i].R), (int) (2*drawthis[i].R), (int) (2*drawthis[i].R));

                    g.setColor(new Color(229, 237, 255, 0)); //fill
                    g.fillOval((int) drawthis[i].x + k.offset_x - (int) (drawthis[i].R), (int) drawthis[i].y + k.offset_y - (int) (drawthis[i].R), (int) (2*drawthis[i].R), (int) (2*drawthis[i].R));
                }
            }
        }
    }


    class Rotator extends MouseAdapter {
        static final double PI = 3.14159265358979323846264338327950288419716939937511;
        double r = 1000, theta = -.07, phi = -0.03;
        int x0, y0;

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            x0 = e.getX();
            y0 = e.getY();
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            theta += PI * (e.getX() - x0) / 1800;
            phi += PI * (y0 - e.getY()) / 1800;
            x0 = e.getX();
            y0 = e.getY();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            super.mouseWheelMoved(e);
            int scroll_amount = 40;

            if (e.getPreciseWheelRotation() > 0) {
                r += scroll_amount;
            } else {
                r -= scroll_amount;
            }
        }
    }
    class Shifter extends KeyAdapter {
        int offset_x = 0;
        int offset_y = 0;
        int shift_amount = 10;
        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (e.getKeyCode() == 37) { // left arrow
                offset_x -= shift_amount;
            }
            if (e.getKeyCode() == 38) { // up arrow
                offset_y -= shift_amount;
            }
            if (e.getKeyCode() == 39) { // right arrow
                offset_x += shift_amount;
            }
            if (e.getKeyCode() == 40) { // down arrow
                offset_y += shift_amount;
            }
        }
    }
}



/*
class SimulationVisualization extends Visualization {
    SimulationVisualization(String filename) {
        this.filename = filename;
        S = new Simulation(filename);
        settings = new SimulationSettings(this);
        add(settings);
    }

    SimulationVisualization() {
        S = new Simulation();
        o = new Rotator();
        k = new Shifter();
        sl = new SlicerShifter();
        addKeyListener(k);
        addMouseListener(o);
        addMouseMotionListener(o);
        addMouseWheelListener(o);
        addMouseWheelListener(sl);
        settings = new SimulationSettings(this);
        add(settings);
    }
}*/
