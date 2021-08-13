package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

class Breadcrumbs extends JFrame {
    static final double PI = 3.14159265358979323846264338327950288419716939937511;
    canvas c;
    Rotator o;
    Shifter k;
    String filename;
    Color[] colors;
    double[][][] data;
    Box B;
    JPanel J;
    private int num_trails;
    private int points;
    private Random R;

    Breadcrumbs(double time, double dt, int num_trails, Box B, String filename) {
        super("Path visualization");
        this.B = B;
        this.filename = filename;
        points = (int) (time / dt);
        data = new double[num_trails][points][3];

        try {
            if (num_trails < 0) {
                throw new IllegalArgumentException("cannot have a negative number of particles");
            } else {
                for (int i = num_trails; i > 0; --i) {
                    this.num_trails = num_trails;
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        colors = new Color[num_trails];
        R = new Random();

        for (int i = 0; i < num_trails; ++i) {
            int red = R.nextInt(255);
            int green = R.nextInt(255);
            int blue = R.nextInt(255);

            colors[i] = new Color(red, green, blue);
        }

        c = new canvas();
        o = new Rotator();
        k = new Shifter();

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1000, 1000);
        this.setContentPane(c);
        this.setVisible(true);
        addMouseListener(o);
        addMouseMotionListener(o);
        addMouseWheelListener(o);
        addKeyListener(k);
        loadParticles(num_trails);
        System.out.println("breadcrumb running");
    }

    /*
    Breadcrumbs(String filename, double time, double dt, int particlenum) {
        super("Path visualization");
        this.filename = filename;
        points = (int)(time/dt);
        data = new double[1][points][3];
        loadParticles(particlenum);
        c = new canvas();
        o = new rotator();
        k = new shifter();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1000,1000);
        this.setContentPane(c);
        this.setVisible(true);
        addMouseListener(o);
        addMouseMotionListener(o);
        addMouseWheelListener(o);
        addKeyListener(k);
    }
     */

    private static void Shellsortbyz(line_segment[] drawthis, int size) {
        int inner, outer;
        line_segment temp;
        double tempz;

        int h = 1;
        while (h <= size / 3) {
            h = h * 3 + 1;
        }
        while (h > 0) {
            for (outer = h; outer < size; ++outer) {
                temp = drawthis[outer];
                tempz = drawthis[outer].z;
                inner = outer;

                while (inner > h - 1 && drawthis[inner - h].z >= tempz) {
                    drawthis[inner] = drawthis[inner - h];
                    inner -= h;
                }
                drawthis[inner] = temp;
            }
            h = (h - 1) / 3;
        }
    }

    private static void Shellsortbyz(Gel[] drawthis, int size) {
        int inner, outer;
        Gel temp;
        double tempz;

        int h = 1;
        while (h <= size / 3) {
            h = h * 3 + 1;
        }
        while (h > 0) {
            for (outer = h; outer < size; outer++) {
                temp = drawthis[outer];
                tempz = drawthis[outer].z;
                inner = outer;

                while (inner > h - 1 && drawthis[inner - h].z >= tempz) {
                    drawthis[inner] = drawthis[inner - h];
                    inner -= h;
                }
                drawthis[inner] = temp;
            }
            h = (h - 1) / 3;
        }
    }

    void loadParticles(int num_particles) {
        for (int trail = 0; trail < num_particles; trail++) {
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader(filename));
                String thisline;
                for (int move = 0; move < points; move++) {
                    thisline = csvReader.readLine();
                    if (thisline != null) {
                        int comma0 = 0;
                        int comma1 = thisline.indexOf(',');
                        for (int j = 0; j < trail * 3; ++j) {
                            comma0 = comma1;
                            comma1 = comma0 + 1 + thisline.substring(comma0 + 1).indexOf(',');
                        }
                        data[trail][move][0] = Double.parseDouble(thisline.substring(comma0 + 1, comma1));
                        comma0 = comma1;
                        comma1 = comma0 + 1 + thisline.substring(comma0 + 1).indexOf(',');

                        data[trail][move][1] = Double.parseDouble(thisline.substring(comma0 + 1, comma1));
                        comma0 = comma1;
                        comma1 = comma0 + 1 + thisline.substring(comma0 + 1).indexOf(',');

                        data[trail][move][2] = Double.parseDouble(thisline.substring(comma0 + 1, comma1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private double[][] drawBox() {
        double cos_theta = Math.cos(o.theta);
        double sin_theta = Math.sin(o.theta);
        double cos_phi = Math.cos(o.phi);
        double sin_phi = Math.sin(o.phi);

        double s = 500;
        double[][] corners = {
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
            corner[0] = x * cos_theta - y * sin_theta;
            corner[1] = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            corner[2] = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;

            //scaaaaaale
            corner[0] *= o.r / 2000;
            corner[1] *= o.r / 2000;

            //centeeeeer
            corner[0] += (double) c.getWidth() * 0.5;
            corner[1] += (double) c.getHeight() * 0.5;
        }
        return corners;
    }

    private class line_segment {
        double x0;
        double x1;
        double y0;
        double y1;
        double z;
        int trail;

        line_segment(double x0, double y0, double x1, double y1, double z, int trail) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.z = z;
            this.trail = trail;
        }
    }

    /*
    void loadParticle(int particlenum) {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(filename));
            int count = 0;
            String thisline;
            while (count < points) {
                thisline = csvReader.readLine();
                if (thisline!=null) {
                    int comma0 = 0;
                    int comma1 = thisline.indexOf(',');
                    for (int i = 0; i < particlenum * 3; ++i) {
                        comma0 = comma1;
                        comma1 = comma0 +1+ thisline.substring(comma0+1).indexOf(',');
                    }
                    data[count][0] = Double.parseDouble(thisline.substring(comma0+1, comma1));
                    comma0 = comma1;
                    comma1 = comma0 +1+ thisline.substring(comma0+1).indexOf(',');

                    data[count][1] = Double.parseDouble(thisline.substring(comma0+1, comma1));
                    comma0 = comma1;
                    comma1 = comma0 +1+ thisline.substring(comma0+1).indexOf(',');

                    data[count][2] = Double.parseDouble(thisline.substring(comma0+1, comma1));
                }
                count++;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    */
    private class Rotator extends MouseAdapter {
        double r = 500, theta = 0, phi = 0;
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
            c.repaint();
            x0 = e.getX();
            y0 = e.getY();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int scroll_amount = 40;
            super.mouseWheelMoved(e);
            if (e.getPreciseWheelRotation() > 0) {
                r += scroll_amount;
            } else {
                r -= scroll_amount;
            }
            c.repaint();
        }
    }

    private class Shifter extends KeyAdapter {
        int offset_x = 0;
        int offset_y = 0;
        int shift_amount = 10;

        @Override
        public void keyPressed(KeyEvent e) {
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
            c.repaint();
        }
    }

    private class canvas extends JPanel {
        line_segment[] breadcrumbs = new line_segment[num_trails * points];
        Gel[] drawthis = new Gel[10000];

        public void paintComponent(Graphics g) {
            //need this on windows
            g.clearRect(0, 0, this.getWidth(), this.getHeight());
            //math
            double cos_theta = Math.cos(o.theta);
            double sin_theta = Math.sin(o.theta);
            double cos_phi = Math.cos(o.phi);
            double sin_phi = Math.sin(o.phi);

            int size = 0;

            for (int trail = 0; trail < num_trails; ++trail) {
            	
            	if(trail == 0) {
            		continue;
            	}
            	
                for (int move = 0; move + 1 < points; ++move) {
                    int l = 50;
                    if (Math.abs(data[trail][move][0] - data[trail][move + 1][0]) < l && Math.abs(data[trail][move][1] - data[trail][move + 1][1]) < l && Math.abs(data[trail][move][2] - data[trail][move + 1][2]) < l) {
                        double x = data[trail][move][0] - 500;
                        double y = data[trail][move][1] - 500;
                        double z = data[trail][move][2] - 500;

                        double x0 = x * cos_theta - y * sin_theta;
                        double y0 = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
                        double z0 = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;

                        x = data[trail][move + 1][0] - 500;
                        y = data[trail][move + 1][1] - 500;
                        z = data[trail][move + 1][2] - 500;

                        double x1 = x * cos_theta - y * sin_theta;
                        double y1 = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
                        double z1 = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;

                        //scaaaaaale
                        x0 *= o.r / 2000;
                        y0 *= o.r / 2000;
                        x1 *= o.r / 2000;
                        y1 *= o.r / 2000;

                        //centeeeeer
                        x0 += (double) c.getWidth() * 0.5;
                        y0 += (double) c.getHeight() * 0.5;
                        x1 += (double) c.getWidth() * 0.5;
                        y1 += (double) c.getHeight() * 0.5;

                        breadcrumbs[size++] = new line_segment(x0, y0, x1, y1, (z0 + z1) * 0.5, trail);
                    }
                }
            }
            
            //this is for tumors
            for (int tumor = 0; tumor < B.getTumoroids().size(); ++tumor) {
                Gel temp = new Gel();
                temp.R = B.getTumoroids().get(tumor).getR();
                double x = B.getTumoroids().get(tumor).getX() - B.sideLength * 0.5;
                double y = B.getTumoroids().get(tumor).getY() - B.sideLength * 0.5;
                double z = B.getTumoroids().get(tumor).getZ() - B.sideLength * 0.5;

                //transfooooooooorm
                temp.x = x * cos_theta - y * sin_theta;
                temp.y = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
                temp.z = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;

                //scaaaaaale
                temp.x *= o.r / 2000;
                temp.y *= o.r / 2000;

                //centeeeeer
                temp.x = (temp.x) + (double) c.getWidth() * 0.5;
                temp.y = (temp.y) + (double) c.getHeight() * 0.5;

                drawthis[tumor] = temp;
            }


            //sort
            //Shellsortbyz(breadcrumbs, size);
            Shellsortbyz(drawthis, B.getTumoroids().size());

            //draw
            double[][] corners = drawBox();
            g.setColor(Color.BLUE);
            g.drawLine((int) corners[0][0] + k.offset_x, (int) corners[0][1] + k.offset_y, (int) corners[1][0] + k.offset_x, (int) corners[1][1] + k.offset_y);
            g.drawLine((int) corners[0][0] + k.offset_x, (int) corners[0][1] + k.offset_y, (int) corners[2][0] + k.offset_x, (int) corners[2][1] + k.offset_y);
            g.drawLine((int) corners[0][0] + k.offset_x, (int) corners[0][1] + k.offset_y, (int) corners[4][0] + k.offset_x, (int) corners[4][1] + k.offset_y);
            g.setColor(Color.BLACK);
            g.drawLine((int) corners[1][0] + k.offset_x, (int) corners[1][1] + k.offset_y, (int) corners[3][0] + k.offset_x, (int) corners[3][1] + k.offset_y);
            g.drawLine((int) corners[1][0] + k.offset_x, (int) corners[1][1] + k.offset_y, (int) corners[5][0] + k.offset_x, (int) corners[5][1] + k.offset_y);
            g.drawLine((int) corners[2][0] + k.offset_x, (int) corners[2][1] + k.offset_y, (int) corners[3][0] + k.offset_x, (int) corners[3][1] + k.offset_y);
            g.drawLine((int) corners[2][0] + k.offset_x, (int) corners[2][1] + k.offset_y, (int) corners[6][0] + k.offset_x, (int) corners[6][1] + k.offset_y);
            g.drawLine((int) corners[3][0] + k.offset_x, (int) corners[3][1] + k.offset_y, (int) corners[7][0] + k.offset_x, (int) corners[7][1] + k.offset_y);
            g.drawLine((int) corners[4][0] + k.offset_x, (int) corners[4][1] + k.offset_y, (int) corners[5][0] + k.offset_x, (int) corners[5][1] + k.offset_y);
            g.drawLine((int) corners[4][0] + k.offset_x, (int) corners[4][1] + k.offset_y, (int) corners[6][0] + k.offset_x, (int) corners[6][1] + k.offset_y);
            g.drawLine((int) corners[5][0] + k.offset_x, (int) corners[5][1] + k.offset_y, (int) corners[7][0] + k.offset_x, (int) corners[7][1] + k.offset_y);
            g.drawLine((int) corners[6][0] + k.offset_x, (int) corners[6][1] + k.offset_y, (int) corners[7][0] + k.offset_x, (int) corners[7][1] + k.offset_y);

            //draw
            for (int trail = 0; trail < num_trails; ++trail) {
                for (int move = 1; move < points; ++move) {
                    if (breadcrumbs[trail * points + move] != null) {
                        int x0 = (int) breadcrumbs[trail * points + move].x0;
                        int y0 = (int) breadcrumbs[trail * points + move].y0;
                        int x1 = (int) breadcrumbs[trail * points + move].x1;
                        int y1 = (int) breadcrumbs[trail * points + move].y1;
                        if ((x0 != 0 && y0 != 0) && (x1 != y1)) {
                            g.setColor(colors[breadcrumbs[trail * points + move].trail]);
                            //g.setColor(Color.RED);
                            g.drawLine(x0 + k.offset_x, y0 + k.offset_y, x1 + k.offset_x, y1 + k.offset_y);
                        }
                    }
                }
            }
            for (int i = 0; i < B.getTumoroids().size(); ++i) {
            	g.setColor(new Color(0, 255, 0, 90)); //fill
                g.fillOval((int) drawthis[i].x + k.offset_x - (int) (drawthis[i].R * o.r / 2000),
                        (int) drawthis[i].y + k.offset_y - (int) (drawthis[i].R * o.r / 2000),
                        (int) (drawthis[i].R * o.r / 1000),
                        (int) (drawthis[i].R * o.r / 1000));
            }

            
        }
    }
}
