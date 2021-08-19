package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;

class Client extends JFrame implements Runnable {
    static long START_TIME;
    static boolean running;
    static Thread drawThread;
    Simulation S;
    Visualization panel;


    Client() throws IOException {

        super("A  C  E  S  O");

        S = new Simulation();
        panel = new Visualization(S);

        this.setLayout(new BorderLayout());
        getContentPane().add(new SimulationViewPanel(panel), BorderLayout.NORTH);
        getContentPane().add(panel, BorderLayout.CENTER);
        //getContentPane().add(new FillSettingsNonViz(S));
        //getContentPane().add(new ACESO_label(), BorderLayout.BEFORE_FIRST_LINE);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 1500);

        running = true;
        START_TIME = System.nanoTime();
        setVisible(true);

        drawThread = new Thread(() -> {
            while (running) {
                getContentPane().repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        drawThread.start();
    }
    static class ACESO_label extends JLabel {
        ACESO_label() {
            super("A  C  E  S  O");
            setFont(new Font("", Font.BOLD, 48));
            setForeground(new Color(97, 4, 4, 246));
            setBounds(17, 10, 445, 69);
        }
    }

    @Override
    public void run() {
    }
}



class SimulationViewPanel extends JPanel {
    Visualization panel;
    SimulationViewPanel(Visualization panel) {

        this.panel = panel;

        this.setLayout(new FlowLayout());
        setBorder(BorderFactory.createTitledBorder(this.getBorder(), "Create New Gel Bed", 0, 0, new Font("", Font.PLAIN, 12), Color.white));
        setBackground(new Color(0x6A2D2D2D, true));
        setSize(1000, 1000);
        setBounds(30, 90, 1000, 1000);

        //this.getOutputPath();

        add(new ParametersPanel());
        add(new StartButtonsPanel());
        add(new ViewChooser());
        add(new VisibilityPanel());
        add(new OutputButtonsPanel());
        add(new SimStatePanel());
        //add(new ResetViewButton());

        //add(new ResetButton());





        //S.setSavePath(getOutputPath());


    }

    class StartButtonsPanel extends JPanel {
        StartButtonsPanel() {
            setLayout(new GridLayout(3, 1));
            add(new FillRandomButton());
            add(new FillFCCButton());
            //add(new FallButton());
            add(new RunTCellsButton());

        }


    }

    class OutputButtonsPanel extends JPanel {
        OutputButtonsPanel() {
            setLayout(new GridLayout(2, 1));
            add(new DensityButton());
            add(new SetFilePathButton());
        }
    }

    public static Path getOutputPath() {

        JFileChooser folderChooser = new JFileChooser();

        folderChooser.setSelectedFile(new java.io.File("."));

        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        folderChooser.setDialogTitle("Choose output folder location");

        int returnVal = folderChooser.showOpenDialog(null);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return folderChooser.getSelectedFile().toPath();

    }

    class SetFilePathButton extends JButton {
        SetFilePathButton() {
            super("Set File Output");
            addActionListener(actionEvent -> {
                panel.S.setSavePath(getOutputPath());
            });
        }
    }

    class ViewChooser extends JPanel {
        JRadioButton slicer_view;
        JRadioButton box_view;
        JCheckBox gel_visibility;
        ViewChooser() {
            setOpaque(false);
            setPreferredSize(new Dimension(100,55));
            setLayout(new FlowLayout(FlowLayout.LEFT));
            slicer_view = new JRadioButton("Slicer View", false);
            slicer_view.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    setSlicerView();
                }});
            slicer_view.setOpaque(false);
            slicer_view.setForeground(Color.white);
            //box view
            box_view = new JRadioButton("Box View", true);
            box_view.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    setBoxView();
                }});
            box_view.setOpaque(false);
            box_view.setForeground(Color.white);
            //button group for radiobutton functionality
            ButtonGroup view_modes = new ButtonGroup();
            view_modes.add(box_view);
            view_modes.add(slicer_view);
            //add to panel
            add(box_view);
            add(slicer_view);
        }
    }
    public void setSlicerView() {
        panel.box_view = false;
        panel.slicer_view = true;
    }
    public void setBoxView() {
        panel.box_view = true;
        panel.slicer_view = false;
    }
    class VisibilityPanel extends JPanel {
        JCheckBox edge_visibility;
        JCheckBox gel_visibility;
        VisibilityPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(160,55));
            setLayout(new FlowLayout(FlowLayout.LEFT));
            edge_visibility = new JCheckBox("Edges Visible", true);
            edge_visibility.addItemListener(itemEvent -> panel.see_box = itemEvent.getStateChange() == ItemEvent.SELECTED);
            edge_visibility.setOpaque(false);
            edge_visibility.setForeground(Color.white);
            //gel visible
            gel_visibility = new JCheckBox("Gel Visible", true);
            gel_visibility.addItemListener(itemEvent -> panel.see_gels = itemEvent.getStateChange() == ItemEvent.SELECTED);
            gel_visibility.setOpaque(false);
            gel_visibility.setForeground(Color.white);
            //add to panel
            add(gel_visibility);
            add(edge_visibility);
        }
    }

    class DensityButton extends JButton {
        Thread t = new Thread(()-> {
            synchronized (this) {
                try {
                    new DensityTester(panel.S);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        DensityButton() {
            super("Density ");
            addActionListener(actionEvent -> {
                t.run();
            });
        }
    }

    class Dimensionpanel extends JPanel {
        Dimensionpanel() {
            setBorder(BorderFactory.createTitledBorder(this.getBorder(), "Box & Fill Settings", 0, 0, new Font("", Font.PLAIN, 12), Color.white));
        }
    }

    class ParametersPanel extends JPanel {
        ParametersPanel() {
            super();
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder(this.getBorder(), "Parameters Panel", 0, 0, new Font("", Font.PLAIN, 12), Color.white));
            setBackground(new Color(0x6A2D2D2D, true));
            add(new NumberOfTCellsPanel());
            //add(new NumberOfGelsPanel());
            add(new GelSizePanel());
            add(new TimeSettingsPanel());

            //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        class BoxSideLengthPanel extends JPanel {
            JSpinner boxSideLengthSpinner;
            JLabel boxSideLengthLabel;

            BoxSideLengthPanel() {
                setOpaque(false);

                //dt label
                boxSideLengthLabel = new JLabel("Box Side Length: ");
                boxSideLengthLabel.setFont(new Font("", Font.BOLD, 11));
                boxSideLengthLabel.setForeground(Color.white);
                //dt spinner
                boxSideLengthSpinner = new JSpinner(new SpinnerNumberModel(panel.S.sideLength, 0, 10000, 1));
                boxSideLengthSpinner.addChangeListener(ChangeEvent -> panel.S.setSide((double) boxSideLengthSpinner.getValue()));
                //dt_panel

                JPanel boxSideLengthPanel = new JPanel();
                boxSideLengthPanel.setOpaque(false);
                boxSideLengthPanel.add(boxSideLengthLabel);
                boxSideLengthPanel.add(boxSideLengthSpinner);
                boxSideLengthPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                // made a sub-panel for formatting called "time_settings"
                JPanel boxSideLengthSettings = new JPanel();
                //boxSideLengthSettings.setLayout(new FlowLayout(FlowLayout.RIGHT));
                boxSideLengthSettings.setBackground(new Color(0, 0, 0, 0));
                boxSideLengthSettings.setPreferredSize(new Dimension(200, 40));
                boxSideLengthSettings.add(boxSideLengthPanel);
                add(boxSideLengthSettings);
                this.setLayout(new FlowLayout(FlowLayout.LEFT));
            }
        }

        class TimeSettingsPanel extends JPanel {
            JSpinner limit_spinner;
            JSpinner dt_spinner;
            JLabel dt_label;
            JLabel limit_label;

            TimeSettingsPanel() {
                setOpaque(false);

                //dt label
                dt_label = new JLabel("dt: ");
                dt_label.setFont(new Font("", Font.BOLD, 11));
                dt_label.setForeground(Color.white);
                //dt spinner
                dt_spinner = new JSpinner(new SpinnerNumberModel(panel.S.sim_time, 0, 100, .01));
                dt_spinner.addChangeListener(ChangeEvent -> panel.S.sim_time = (double) dt_spinner.getValue());
                //dt_panel
                JPanel dt_panel = new JPanel();
                dt_panel.setOpaque(false);
                dt_panel.add(dt_label);
                dt_panel.add(dt_spinner);
                //timepanel
                JPanel time_panel = new JPanel();
                time_panel.setBackground(new Color(0, 0, 0, 0));

                //time_limit label
                limit_label = new JLabel("Time Steps (20 Seconds): ");
                limit_label.setFont(new Font("", Font.BOLD, 11));
                limit_label.setForeground(Color.white);
                time_panel.add(limit_label);


                //time_limit spinner
                limit_spinner = new JSpinner(new SpinnerNumberModel(panel.S.timeLimitTCells, 0, 1000000000, 1000));
                limit_spinner.addChangeListener(ChangeEvent -> panel.S.timeLimitTCells = (int) limit_spinner.getValue());
                limit_spinner.setPreferredSize(new Dimension(100, 20));
                time_panel.add(limit_spinner);

                // made a sub-panel for formatting called "time_settings"
                JPanel time_settings = new JPanel();
                time_settings.setBackground(new Color(0, 0, 0, 0));
                time_settings.setPreferredSize(new Dimension(300, 40));
                //time_settings.add(dt_panel);
                time_settings.add(time_panel);
                add(time_settings);
                this.setLayout(new FlowLayout(FlowLayout.LEFT));
                time_settings.setLayout(new FlowLayout(FlowLayout.LEFT));
                time_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
                dt_panel.setLayout(new FlowLayout(FlowLayout.LEFT));

            }
        }

        class NumberOfTCellsPanel extends JPanel {
            JSpinner numTCellsSpinner;
            JLabel numTCellsLabel;

            NumberOfTCellsPanel() {
                setOpaque(false);

                //dt label
                numTCellsLabel = new JLabel("# of T Cells: ");
                numTCellsLabel.setFont(new Font("", Font.BOLD, 11));
                numTCellsLabel.setForeground(Color.white);
                //dt spinner
                numTCellsSpinner = new JSpinner(new SpinnerNumberModel(panel.S.getNumTCells(), 0, 10000, 1));
                numTCellsSpinner.addChangeListener(ChangeEvent -> panel.S.setNumTCells((double)numTCellsSpinner.getValue()));
                //dt_panel

                JPanel numTCellPanel = new JPanel();
                numTCellPanel.setOpaque(false);
                numTCellPanel.add(numTCellsLabel);
                numTCellPanel.add(numTCellsSpinner);
                numTCellPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                // made a sub-panel for formatting called "time_settings"
                JPanel numTCellsSettings = new JPanel();
                //numTCellsSettings.setLayout(new FlowLayout(FlowLayout.RIGHT));
                numTCellsSettings.setBackground(new Color(0, 0, 0, 0));
                numTCellsSettings.setPreferredSize(new Dimension(200, 40));
                numTCellsSettings.add(numTCellPanel);
                add(numTCellsSettings);
                this.setLayout(new FlowLayout(FlowLayout.LEFT));
            }
        }

        class NumberOfGelsPanel extends JPanel {
            JSpinner numGelsSpinner;
            JLabel numGelsLabel;

            /*NumberOfGelsPanel() {
                setOpaque(false);

                //dt label
                numGelsLabel = new JLabel("# of Gels: ");
                numGelsLabel.setFont(new Font("", Font.BOLD, 11));
                numGelsLabel.setForeground(Color.white);
                //dt spinner
                numGelsSpinner = new JSpinner(new SpinnerNumberModel(S.numGelsToSet, 0, 10000, 1));
                //numGelsSpinner.addChangeListener(ChangeEvent -> S.numGelsToSet = (double) numGelsSpinner.getValue());
                //dt_panel

                JPanel numGelsPanel = new JPanel();
                numGelsSetOpaque(false);
                numGelsPanel.add(numGelsLabel);
                numGelsPanel.add(numGelsSpinner);
                numGelsSetLayout(new FlowLayout(FlowLayout.LEFT));

                // made a sub-panel for formatting called "time_settings"
                JPanel numGelsSettings = new JPanel();
                //numGelsSettings.setLayout(new FlowLayout(FlowLayout.RIGHT));
                numGelsSettings.setBackground(new Color(0, 0, 0, 0));
                numGelsSettings.setPreferredSize(new Dimension(200, 40));
                numGelsSettings.add(numGelsPanel);
                add(numGelsSettings);
                this.setLayout(new FlowLayout(FlowLayout.LEFT));
            }*/
        }

        class GelSizePanel extends JPanel {
            JSpinner gelRadiusLowerBoundSpinner;
            JSpinner gelRadiusRangeSpinner;
            JLabel gelRadiusLowerBoundLabel;
            JLabel gelRadiusRangeLabel;

            GelSizePanel() {
                setOpaque(false);

                //dt label
                gelRadiusLowerBoundLabel = new JLabel("Gel Average Radius (microns): ");
                gelRadiusLowerBoundLabel.setFont(new Font("", Font.BOLD, 11));
                gelRadiusLowerBoundLabel.setForeground(Color.white);
                //dt spinner
                gelRadiusLowerBoundSpinner = new JSpinner(new SpinnerNumberModel(panel.S.rAverageRadius, 0, 200, 0.1));
                gelRadiusLowerBoundSpinner.addChangeListener(ChangeEvent -> panel.S.rAverageRadius = (double) gelRadiusLowerBoundSpinner.getValue());
                //dt_panel

                JPanel gelLowerPanel = new JPanel();
                gelLowerPanel.setOpaque(false);
                gelLowerPanel.add(gelRadiusLowerBoundLabel);
                gelLowerPanel.add(gelRadiusLowerBoundSpinner);
                gelLowerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                //gel range /mu(r) panel
                JPanel gelUpperPanel = new JPanel();
                gelUpperPanel.setBackground(new Color(0, 0, 0, 0));
                //time_limit label
                gelRadiusRangeLabel = new JLabel("Gel Range / mu(r): ");
                gelRadiusRangeLabel.setFont(new Font("", Font.BOLD, 11));
                gelRadiusRangeLabel.setForeground(Color.white);
                gelUpperPanel.add(gelRadiusRangeLabel);


                //gel range / mu(r) spinner
                gelRadiusRangeSpinner = new JSpinner(new SpinnerNumberModel(panel.S.rangeOverAverageR, 0, 100, 0.1));
                gelRadiusRangeSpinner.addChangeListener(ChangeEvent -> panel.S.rangeOverAverageR = ((double)gelRadiusRangeSpinner.getValue()));
                gelRadiusRangeSpinner.setPreferredSize(new Dimension(100, 20));
                gelUpperPanel.add(gelRadiusRangeSpinner);

                // made a sub-panel for formatting called "gel range / mu(r)"
                JPanel gel_settings = new JPanel();
                gel_settings.setBackground(new Color(0, 0, 0, 0));
                gel_settings.setPreferredSize(new Dimension(300, 80));
                gel_settings.add(gelLowerPanel);
                gel_settings.add(gelUpperPanel);
                add(gel_settings);
                this.setLayout(new FlowLayout(FlowLayout.LEFT));
                gel_settings.setLayout(new FlowLayout(FlowLayout.LEFT));;
                gelUpperPanel.setLayout(new FlowLayout(FlowLayout.LEFT));


            }
        }
    }

    class SimStatePanel extends JPanel {
        SimStatePanel() {
            super();
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder(this.getBorder(), "Sim State", 0, 0, new Font("", Font.PLAIN, 12), Color.white));
            setBackground(new Color(0x6A2D2D2D, true));

            add(new NumGelsLabel());
            add(new NumTCellsLabel());
            add(new NumTumorCellsLabel());
            add(new Volumepercentage());
            add(new TimeStepPanel());
            add(new GelAvgSizeLabel());
            add(new GelStdDevLabel());
            add(new MSD());

        }
        class NumGelsLabel extends  JLabel {
            NumGelsLabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Num Gels: " + panel.S.numGels);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Num Gels: " + panel.S.numGels);
            }
        }

        class MSD extends JLabel {
        	MSD() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("MSD: " + panel.S.averageDisplacementPanel);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("MSD: " + panel.S.averageDisplacementPanel);
            }
        }

        class NumTCellsLabel extends JLabel {
            NumTCellsLabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Number of T-Cells: " + panel.S.numParticles);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Number of T-Cells: " + panel.S.numParticles);
            }
        }

        class NumTumorCellsLabel extends JLabel {
            NumTumorCellsLabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Number of Tumor Cells: " + panel.S.getNumTumor());
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Number of Tumor Cells: " + panel.S.getNumTumor());
            }
        }

        class Volumepercentage extends  JLabel {
            Volumepercentage() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Percent Filled: " + (int)(panel.S.sumSphereVolumes() * 100 / panel.S.volume) + " %");
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Percent Filled: " + (int)(panel.S.sumSphereVolumes() * 100 / panel.S.volume) + " %");
            }
        }
        class Volumelabel extends JLabel {
            Volumelabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Volume: " + (int) panel.S.sumSphereVolumes());
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Volume: " + (int) panel.S.sumSphereVolumes());
            }
        }

        class TimeStepPanel extends JLabel {
            TimeStepPanel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Time Step: " + panel.S.sim_time);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Time Step: " + panel.S.sim_time);
            }
        }

        class GelAvgSizeLabel extends JLabel {
            GelAvgSizeLabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Gel Weighted Avg Radius: " + String.format("%.1f", panel.S.calculateWeightedAvgRadius()));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Gel Weighted Avg Radius: " + String.format("%.1f", panel.S.calculateWeightedAvgRadius()));
            }
        }

        class GelStdDevLabel extends JLabel {
            GelStdDevLabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Range / mu(r): " + String.format("%.1f", panel.S.outputRangeOverAverageR()));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Range / mu(r): " + String.format("%.1f", panel.S.outputRangeOverAverageR()));
            }
        }
    }

    class FillFCCButton extends JButton {
        FillFCCButton() {
            super("Fill FCC");
            addActionListener(actionEvent -> {
                panel.S.fillFCC();
            });
        }

    }

    class FillRandomButton extends JButton {
        FillRandomButton() {
            super("Fill Random");
            addActionListener(actionEvent -> {
                panel.S.fillUnthreaded();
                try {
                    panel.S.fallUnthreaded();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                try {
//                    panel.S.fillGelsByCSV();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            });
        }
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            if (panel.S.fillThread.isAlive()) {
//                setText("Still settling");
//            } else {
//                setText("Fill Random");
//            }
//        }
    }
    class AddButton extends JButton {
        AddButton() {
            super("Add one");
            addActionListener(actionEvent -> {
                panel.S.addGel();
            });
        }
    }
    class RunTCellsButton extends JButton {
        RunTCellsButton() {
            super("Run T Cells");
            addActionListener(actionEvent -> {
                panel.S.runTCells();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(panel.S.tCellThread.isAlive()) {
                setText("Running T Cells");
            }
            else {
                setText("Run T Cells");
            }
        }
    }

    class FallButton extends JButton {
        FallButton() {
            super("Start Falling");
            addActionListener(actionEvent -> {
                if (panel.S.fallThread.isAlive()) {
                    panel.S.fallThread.stop();
                } else {
                    panel.S.fall();
                }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (panel.S.fallThread.isAlive()) {
                setText("Stop falling");
            } else {
                setText("Start falling");

            }
        }
    }
    class addGelButton extends JButton {
        addGelButton() {
            super("Add gel");
            addActionListener(actionEvent -> {
                panel.S.addGel();
            });
        }
    }

}

class FillSettingsNonViz extends JPanel {
    Simulation S;
    FillSettingsNonViz(Simulation S) {
        this.S = S;
        this.setLayout(new FlowLayout());
        setBorder(BorderFactory.createTitledBorder(this.getBorder(), "Create New Gel Bed", 0, 0, new Font("", Font.PLAIN, 12), Color.white));
        setBackground(new Color(0x6A2D2D2D, true));
        setSize(1500, 1500);
        setBounds(30, 90, 500, 500);

        //this.getOutputPath();

        add(new Volumepanel());
        add(new FillButton());
        add(new FillFCCButton());
        add(new FallButton());

        //add(new ViewChooser());
        //add(new ResetViewButton());
        add(new SetFilePathButton());
        //add(new ResetButton());
        add(new DensityButton());
        add(new RunTCellsButton());
        add(new GelSizePanel());
        add(new TimeSettingsPanel());
    }

    public static Path getOutputPath() {

        JFileChooser jd = new JFileChooser();

        jd.setDialogTitle("Choose output file");

        int returnVal = jd.showSaveDialog(null);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return jd.getSelectedFile().toPath();

    }

    class SetFilePathButton extends JButton {
        SetFilePathButton() {
            super("Set File Output");
            addActionListener(actionEvent -> {
                S.setSavePath(getOutputPath());
            });
        }
    }

    class DensityButton extends JButton {
        Thread t = new Thread(()-> {
            synchronized (this) {
                try {
                    new DensityTester(S);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        DensityButton() {
            super("Density ");
            addActionListener(actionEvent -> {
                t.run();

            });
        }
    }

    class Dimensionpanel extends JPanel {
        Dimensionpanel() {
            setBorder(BorderFactory.createTitledBorder(this.getBorder(), "Box & Fill Settings", 0, 0, new Font("", Font.PLAIN, 12), Color.white));
        }
    }

    class Volumepanel extends JPanel {
        Volumepanel() {
            super();
            setOpaque(false);
            setPreferredSize(new Dimension(140, 90));
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setBorder(BorderFactory.createTitledBorder(this.getBorder(), "Volume Panel", 0, 0, new Font("", Font.PLAIN, 12), Color.white));
            setBackground(new Color(0x6A2D2D2D, true));
            add(new Numgelslabel());
            add(new Volumelabel());
            add(new Volumepercentage());
        }
        class Numgelslabel extends  JLabel {
            Numgelslabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Num Gels: " + S.numGels);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Num Gels: " + S.numGels);
            }
        }
        class Volumepercentage extends  JLabel {
            Volumepercentage() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Percent Filled: " + (int)(S.sum_sphere_volume*100/S.volume) + " %");
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Percent Filled: " + (int)(S.sum_sphere_volume*100/S.volume) + " %");
            }
        }
        class Volumelabel extends JLabel {
            Volumelabel() {
                super();
                setOpaque(false);
                setForeground(Color.white);
                setText("Volume: " + (int) S.sum_sphere_volume);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setText("Volume: " + (int) S.sum_sphere_volume);
            }
        }
    }

    class FillButton extends JButton {
        FillButton() {
            super("Start Filling");
            addActionListener(actionEvent -> {
                S.fill();
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (S.fillThread.isAlive()) {
                setText("Still settling");
            } else {
                setText("Start filling");
            }
        }
    }

    class FillFCCButton extends JButton {
        FillFCCButton() {
            super("Fill FCC");
            addActionListener(actionEvent -> {
                S.fillHex();
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (S.fillThread.isAlive()) {
                setText("Still settling");
            } else {
                setText("Start filling");
            }
        }
    }
    class AddButton extends JButton {
        AddButton() {
            super("Add one");
            addActionListener(actionEvent -> {
                S.addGel();
            });
        }
    }



    class RunTCellsButton extends JButton {
        RunTCellsButton() {
            super("Run T Cells");
            addActionListener(actionEvent -> {
                S.runTCells();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(S.tCellThread.isAlive()) {
                setText("Running T Cells");
            }
            else {
                setText("Run T Cells");
            }
        }
    }

    class FallButton extends JButton {
        FallButton() {
            super("Start Falling");
            addActionListener(actionEvent -> {
                if (S.fallThread.isAlive()) {
                    S.fallThread.interrupt();
                } else {
                    S.fall();
                }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (S.fallThread.isAlive()) {
                setText("Stop falling");
            } else {
                setText("Start falling");

            }
        }
    }
    class addGelButton extends JButton {
        addGelButton() {
            super("Add gel");
            addActionListener(actionEvent -> {
                S.addGel();
            });
        }
    }

    class TimeSettingsPanel extends JPanel {
        JSpinner limit_spinner;
        JSpinner dt_spinner;
        JLabel dt_label;
        JLabel limit_label;

        TimeSettingsPanel() {
            setOpaque(false);

            //dt label
            dt_label = new JLabel("dt: ");
            dt_label.setFont(new Font("", Font.BOLD, 14));
            dt_label.setForeground(Color.white);
            //dt spinner
            dt_spinner = new JSpinner(new SpinnerNumberModel(S.dt, 0, 100, .01));
            dt_spinner.addChangeListener(ChangeEvent -> S.dt = (double) dt_spinner.getValue());
            //dt_panel
            JPanel dt_panel = new JPanel();
            dt_panel.setOpaque(false);
            dt_panel.add(dt_label);
            dt_panel.add(dt_spinner);
            //timepanel
            JPanel time_panel = new JPanel();
            time_panel.setBackground(new Color(0, 0, 0, 0));
            //time_limit label
            limit_label = new JLabel("Time Limit: ");
            limit_label.setFont(new Font("", Font.BOLD, 14));
            limit_label.setForeground(Color.white);
            time_panel.add(limit_label);
            //time_limit spinner
            limit_spinner = new JSpinner(new SpinnerNumberModel(10000, 0, 10000000, 10000));
            limit_spinner.addChangeListener(ChangeEvent -> S.fall_time_limit = ((double) (int) limit_spinner.getValue()));
            limit_spinner.setPreferredSize(new Dimension(100, 20));
            time_panel.add(limit_spinner);
            // made a sub-panel for formatting called "time_settings"
            JPanel time_settings = new JPanel();
            time_settings.setLayout(new FlowLayout(FlowLayout.LEFT));
            time_settings.setBackground(new Color(0, 0, 0, 0));
            time_settings.setPreferredSize(new Dimension(300, 200));
            time_settings.add(dt_panel);
            time_settings.add(time_panel);
            add(time_settings);
        }
    }

    class GelSizePanel extends JPanel {
        JSpinner gelRadiusLowerBoundSpinner;
        JSpinner gelRadiusRangeSpinner;
        JLabel gelRadiusLowerBoundLabel;
        JLabel gelRadiusRangeLabel;

        GelSizePanel() {
            setOpaque(false);

            //dt label
            gelRadiusLowerBoundLabel = new JLabel("Gel Radius Lower Bound: ");
            gelRadiusLowerBoundLabel.setFont(new Font("", Font.BOLD, 14));
            gelRadiusLowerBoundLabel.setForeground(Color.white);
            //dt spinner
            gelRadiusLowerBoundSpinner = new JSpinner(new SpinnerNumberModel(S.rAverageRadius, 0, 100, 0.1));
            gelRadiusLowerBoundSpinner.addChangeListener(ChangeEvent -> S.rAverageRadius = (double) gelRadiusLowerBoundSpinner.getValue());
            //dt_panel

            JPanel gelLowerPanel = new JPanel();
            gelLowerPanel.setOpaque(false);
            gelLowerPanel.add(gelRadiusLowerBoundLabel);
            gelLowerPanel.add(gelRadiusLowerBoundSpinner);

            //timepanel
            JPanel gelUpperPanel = new JPanel();
            gelUpperPanel.setBackground(new Color(0, 0, 0, 0));
            //time_limit label
            gelRadiusRangeLabel = new JLabel("Range: ");
            gelRadiusRangeLabel.setFont(new Font("", Font.BOLD, 14));
            gelRadiusRangeLabel.setForeground(Color.white);
            gelUpperPanel.add(gelRadiusRangeLabel);
            //time_limit spinner
            gelRadiusRangeSpinner = new JSpinner(new SpinnerNumberModel(S.rangeOverAverageR, 0, 100, 0.1));
            gelRadiusRangeSpinner.addChangeListener(ChangeEvent -> S.rangeOverAverageR = ((double)gelRadiusRangeSpinner.getValue()));
            gelRadiusRangeSpinner.setPreferredSize(new Dimension(100, 20));
            gelUpperPanel.add(gelRadiusRangeSpinner);

            // made a sub-panel for formatting called "time_settings"
            JPanel gel_settings = new JPanel();
            gel_settings.setLayout(new FlowLayout(FlowLayout.LEFT));
            gel_settings.setBackground(new Color(0, 0, 0, 0));
            gel_settings.setPreferredSize(new Dimension(300, 200));
            gel_settings.add(gelLowerPanel);
            gel_settings.add(gelUpperPanel);
            add(gel_settings);
        }
    }
}


