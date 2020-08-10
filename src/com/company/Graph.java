package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;

//A graphing utility
class Graph extends JFrame {
    private canvas c;
    private double[] data;
    private int points;
    private double time;
    private double dt;
    private double maxy = 0;

    Graph(String filename, double time, double dt) {
        super("Distance Vs. time");
        this.time = time;
        this.dt = dt;
        points = (int) (time / dt);
        data = new double[points];
        c = new canvas();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1000, 1000);
        this.setContentPane(c);
        this.setVisible(true);
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(filename)); //CHANGE TO MATCH FILE PATH
            int count = 0;
            String thisline;
            while (count < points) {
                thisline = csvReader.readLine();
                if (thisline != null) {
                    int comma2 = thisline.lastIndexOf(',');
                    int comma1 = thisline.indexOf(',');
                    data[count] = Double.parseDouble(thisline.substring(comma1 + 1, comma2));
                    if (data[count] > maxy) {
                        maxy = data[count];
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class canvas extends JPanel {
        public void paintComponent(Graphics g) {
            int a = 50;
            int b = getHeight() - 50;
            int l = getHeight() - 80;
            int w = getWidth() - 80;
            double maxx = time;

            //draw axes
            g.drawLine(a, b, a + w, b);
            g.drawLine(a, b + 1, a + w, b + 1);

            g.drawLine(a, b, a, b - l);
            g.drawLine(a - 1, b, a - 1, b - l);

            //Y-axis tick marks
            g.drawString(String.format("%.0f", maxy), a - 30, b - l + 4);
            g.drawLine(a - 3, b - l, a + 1, b - l);

            g.drawString(String.format("%.0f", maxy * 4 / 5), a - 30, b - (l * 4 / 5) + 4);
            g.drawLine(a - 3, b - (l * 4 / 5), a + 1, b - (l * 4 / 5));

            g.drawString(String.format("%.0f", maxy * 3 / 5), a - 10 * (1 + (int) Math.log10(maxy * 3 / 5)), b - (l * 3 / 5) + 4);
            g.drawLine(a - 3, b - (l * 3 / 5), a + 1, b - (l * 3 / 5));

            g.drawString(String.format("%.0f", maxy * 2 / 5), a - 10 * (1 + (int) Math.log10(maxy * 2 / 5)) + 10, b - (l * 2 / 5) + 4);
            g.drawLine(a - 3, b - (l * 2 / 5), a + 1, b - (l * 2 / 5));

            g.drawString(String.format("%.0f", maxy / 5), a - 23, b - (l / 5) + 4);
            g.drawLine(a - 3, b - (l / 5), a + 1, b - (l / 5));

            g.drawString("0", a - 13, b);

            //X-axis tick marks
            g.drawString(String.format("%.0f", maxx), a + w - 18, b + 15);
            g.drawLine(a + w, b + 3, a + w, b - 2);

            g.drawString(String.format("%.0f", maxx / 2), a + (w / 2) - 15, b + 15);
            g.drawLine(a + (w / 2), b + 3, a + (w / 2), b - 2);

            g.drawString("0", a - 3, b + 15);

            //plot data
            g.setColor(Color.ORANGE);
            for (int i = 0; i < points - 2; ++i) {
                int i0 = a + (int) (w * (i / maxx));
                int i1 = b - (int) (l * (data[i] / maxy));
                int i2 = a + (int) (w * ((i + 1) / maxx));
                int i3 = b - (int) (l * (data[i + 1] / maxy));
                g.drawLine(i0, i1, i2, i3);
            }

            //plot comparison y = sqrt(7.587*n)

            g.setColor(Color.RED);
            for (int i = 0; i < points-1; ++i) {
                int i0 = a + (int) (w * (i / maxx));
                int i1 = b - (int) (l * (Math.sqrt(7.587*i) / maxy));
                int i2 = a + (int) (w * ((i + 1) / maxx));
                int i3 = b - (int) (l * (Math.sqrt(7.587*(i+1)) / maxy));
                g.drawLine(i0, i1, i2, i3);
            }
        }
    }
}

class Graph2 extends JFrame {
    private canvas c;
    private double[] data;
    private int points;
    private double time;
    private double dt;
    private double maxy = 0;

    Graph2(String filename, double time, double dt) {
        super("Displacement^2 VS Time");
        this.time = time;
        this.dt = dt;
        points = (int) (time / dt);
        data = new double[points];
        c = new canvas();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1000, 1000);
        this.setContentPane(c);
        this.setVisible(true);
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(filename));
            int count = 0;
            String thisline;
            while (count < points) {
                thisline = csvReader.readLine();
                if (thisline != null) {
                    int comma = thisline.lastIndexOf(',');
                    data[count] = Double.parseDouble(thisline.substring(comma + 1));
                    if (data[count] > maxy) {
                        maxy = data[count];
                    }
                    count++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class canvas extends JPanel {
        public void paintComponent(Graphics g) {

            int a = 50;
            int b = getHeight() - 50;
            int l = getHeight() - 80;
            int w = getWidth() - 80;
            double maxx = time;
            double max = (maxy - (maxy % 10000)) + 10000;

            //draw axes
            g.drawLine(a, b, a + w, b);
            g.drawLine(a, b + 1, a + w, b + 1);

            g.drawLine(a, b, a, b - l);
            g.drawLine(a - 1, b, a - 1, b - l);

            //Y-axis tick marks
            g.drawString(String.format("%.0f", max), a - 45, b - l + 4);
            g.drawLine(a - 2, b - l, a + 2, b - l);

            g.drawString(String.format("%.0f", max * 4 / 5), a - 45, b - (l * 4 / 5) + 4);
            g.drawLine(a - 2, b - (l * 4 / 5), a + 2, b - (l * 4 / 5));

            g.drawString(String.format("%.0f", max * 3 / 5), a - 45, b - (l * 3 / 5) + 4);
            g.drawLine(a - 2, b - (l * 3 / 5), a + 2, b - (l * 3 / 5));

            g.drawString(String.format("%.0f", max * 2 / 5), a - 10 * (1 + (int) Math.log10(maxy * 2 / 5)) + 5, b - (l * 2 / 5) + 4);
            g.drawLine(a - 2, b - (l * 2 / 5), a + 2, b - (l * 2 / 5));

            g.drawString(String.format("%.0f", max * 1 / 5), a - 10 * (1 + (int) Math.log10(maxy * 1 / 5)) + 5, b - (l / 5) + 4);
            g.drawLine(a - 2, b - (l / 5), a + 2, b - (l / 5));

            g.drawString("0", a - 13, b);

            //X-axis tick marks
            g.drawString(String.format("%.0f", maxx), a + w - 18, b + 15);
            g.drawLine(a + w, b + 3, a + w, b - 2);

            g.drawString(String.format("%.0f", maxx / 2), a + (w / 2) - 15, b + 15);
            g.drawLine(a + (w / 2), b + 3, a + (w / 2), b - 2);

            g.drawString("0", a - 3, b + 15);

            //plot data
            g.setColor(Color.ORANGE);
            for (int i = 0; i < points - 1; i++) {
                int i0 = a + (int) (w * (i * 1.0 / points));
                int i1 = b - (int) (l * (data[i] / maxy));
                int i2 = a + (int) (w * ((i + 1) * 1.0 / points));
                int i3 = b - (int) (l * (data[i + 1] / maxy));
                g.drawLine(i0, i1, i2, i3);
            }


            //plot comparison line: y = 7.57n
            /*g.setColor(Color.RED);
            g.drawLine(a, b, a + w, b - (int) (l * 7.57 * time / maxy));*/

        }
    }
}


