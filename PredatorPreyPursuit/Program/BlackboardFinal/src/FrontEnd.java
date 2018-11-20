import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Robert Johnson Contains the front end configuration as well as any
 * changes which occur during the process of the program.
 */
class FrontEnd extends JFrame implements Runnable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final JButton[][] mapContents = new JButton[Server.mapContents.length][Server.mapContents[0].length];
    private static final JLabel[][] mapContentsAfterInit = new JLabel[Server.mapContents.length][Server.mapContents[0].length];
    private static final JLabel[][] mapFlagged = new JLabel[Server.mapContents.length][Server.mapContents[0].length];
    private static final JLabel[][] mapOccupied = new JLabel[Server.mapContents.length][Server.mapContents[0].length];
    private static final JLabel[][] mapPossiblyPrey = new JLabel[Server.mapContents.length][Server.mapContents[0].length];
    private static final JLabel[][] predatorInfo = new JLabel[4][7];
    private static final JPanel map1Contents = new JPanel();
    static JButton setContinuousButton = null;
    static String whereToSaveStats = "";
    static JTextArea textArea2 = null;
    private static String action = "";
    private static JButton setNextPreyButton = null;
    private static JButton setExitButton = null;
    private static JButton setNextPredButton = null;
    private static JButton setPauseButton = null;
    private static JButton setPredButton = null;
    private static JButton setBlocksButton = null;
    private static JButton setEmptyButton = null;
    private static JButton setConfirm = null;
    private static JLabel preyInfo = null;
    private static String whereToSave = "";
    private static int screenShotNumber = 0;
    private static FrontEnd frame = null;
    private static JButton northButton = null;
    private static JButton eastButton = null;
    private static JButton southButton = null;
    private static JButton westButton = null;
    private static JButton passButton = null;
    private static JButton temp6 = null;
    private final GridLayout mapGrid = new GridLayout(2, 2, 10, 10);
    private final GridLayout mapLayout = new GridLayout(Server.mapContents.length, Server.mapContents[0].length);
    private FrontEnd(String name)
    {
        super(name);
        setResizable(false);
    }

    /**
     * Create the GUI and show it. For thread safety, this method is invoked from
     * the event dispatch thread.
     *
     */
    private static void createAndShowGUI()
    {
        // Create and set up the window.
        frame = new FrontEnd("PredPreySim");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // Set up the content pane.
        frame.addComponentsToPane(frame.getContentPane());
        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Make the user interface
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws UnsupportedLookAndFeelException
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void makeUI() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            UnsupportedLookAndFeelException, InvocationTargetException, InterruptedException
    {
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        FrontEnd front = new FrontEnd("PredPrey");
        SwingUtilities.invokeAndWait(front);
    }

    /**
     * @param textToAdd text to add to the area containing move sets by predator and prey.
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void clearTextFrame(String textToAdd) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait(() ->
        {
            textArea2.setText(textToAdd);
            frame.validate();
            frame.repaint();
        });
    }

    /**
     * Re-enable the buttons controlling the flow of the episode
     *
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void reEnableButtons() throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait(() ->
        {
            setNextPredButton.setEnabled(true);
            setExitButton.setEnabled(true);
            setNextPreyButton.setEnabled(true);
            setContinuousButton.setEnabled(true);
            setPauseButton.setEnabled(true);
            frame.validate();
            frame.repaint();
        });
    }

    /**
     * Method that takes a screenshot of the user interface.
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static void takeScreen() throws InterruptedException, InvocationTargetException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            frame.validate();
            frame.repaint();
            Container content = frame.getContentPane();
            BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            content.printAll(g2d);
            g2d.dispose();
            File outputFile = new File(whereToSave + "/saved" + screenShotNumber + ".png");
            screenShotNumber++;
            try
            {
                ImageIO.write(img, "png", outputFile);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }));
    }

    /**
     * Method to update the physical look of the user interface, grid contents
     *
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void updateMapsAndInfo() throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            for (int i = 0; i < Server.mapContents.length; i++)
            {
                for (int j = 0; j < Server.mapContents[0].length; j++)
                {
                    mapContentsAfterInit[i][j].setOpaque(true);
                    if (Server.map[i][j].contents == 1)
                    {
                        mapContentsAfterInit[i][j].setBackground(Color.BLACK);
                    }
                    else if (Server.map[i][j].contents != 0)
                    {
                        switch (Server.map[i][j].contents)
                        {
                            case 2:
                                mapContentsAfterInit[i][j].setBackground(Color.CYAN);
                                mapContentsAfterInit[i][j].setText(Server.orientations[0] + "");
                                break;
                            case 3:
                                mapContentsAfterInit[i][j].setBackground(Color.GREEN);
                                mapContentsAfterInit[i][j].setText(Server.orientations[1] + "");
                                break;
                            case 4:
                                mapContentsAfterInit[i][j].setBackground(Color.PINK);
                                mapContentsAfterInit[i][j].setText(Server.orientations[2] + "");
                                break;
                            case 5:
                                mapContentsAfterInit[i][j].setBackground(Color.YELLOW);
                                mapContentsAfterInit[i][j].setText(Server.orientations[3] + "");
                                break;
                            case 10:
                                mapContentsAfterInit[i][j].setBackground(Color.ORANGE);
                                mapContentsAfterInit[i][j].setText("Prey");
                                break;
                        }
                    }
                    else
                    {
                        mapContentsAfterInit[i][j].setText("");
                        mapContentsAfterInit[i][j].setBackground(Color.WHITE);
                    }
                }
            }
            for (int i = 0; i < Server.mapContents.length; i++)
            {
                for (int j = 0; j < Server.mapContents[0].length; j++)
                {
                    if (Server.map[i][j].contents == 1)
                    {
                        mapFlagged[i][j].setBackground(Color.BLACK);
                    }
                    else if (!Server.map[i][j].flagged)
                    {
                        mapFlagged[i][j].setBackground(Color.GRAY);
                    }
                    else
                    {
                        mapFlagged[i][j].setBackground(Color.WHITE);
                    }
                }
            }
            for (int i = 0; i < Server.mapContents.length; i++)
            {
                for (int j = 0; j < Server.mapContents[0].length; j++)
                {
                    if (Server.map[i][j].contents == 1)
                    {
                        mapOccupied[i][j].setBackground(Color.BLACK);
                    }
                    else
                    {
                        mapOccupied[i][j].setBackground(new Color(
                                Math.min((int) (255.0 * (Server.map[i][j].chanceOfOccupancy / 100.0)), 255),
                                Math.max(255 - (int) (255.0 * (Server.map[i][j].chanceOfOccupancy / 100.0)), 0), 0));
                        mapOccupied[i][j].setText((Math.round(Server.map[i][j].chanceOfOccupancy / 10)) + "%");
                    }
                }
            }
            for (int i = 0; i < Server.mapContents.length; i++)
            {
                for (int j = 0; j < Server.mapContents[0].length; j++)
                {
                    if (Server.map[i][j].contents == 1)
                    {
                        mapPossiblyPrey[i][j].setBackground(Color.BLACK);
                    }
                    else if (!Server.map[i][j].possiblyPrey)
                    {
                        mapPossiblyPrey[i][j].setBackground(Color.white);
                    }
                    else if (Server.map[i][j].possiblyPrey)
                    {
                        if (Server.map[i][j].centerCell)
                        {
                            mapPossiblyPrey[i][j].setBackground(Color.GREEN);
                        }
                        else
                        {
                            mapPossiblyPrey[i][j].setBackground(Color.YELLOW);
                        }
                    }
                }
            }
            frame.validate();
            frame.repaint();
        }));
    }

    /**
     * Change the starting grid of buttons into grid of labels.
     *
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void changeGrid1IntoLabels() throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            map1Contents.removeAll();
            Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
            for (int i = 0; i < Server.mapContents.length; i++)
            {
                for (int j = 0; j < Server.mapContents[0].length; j++)
                {
                    mapContentsAfterInit[i][j] = new JLabel("");
                    mapContentsAfterInit[i][j].setPreferredSize(new Dimension(40, 40));
                    mapContentsAfterInit[i][j].setBorder(blackBorder);
                    mapContentsAfterInit[i][j].setHorizontalAlignment(JLabel.CENTER);
                    map1Contents.add(mapContentsAfterInit[i][j]);
                }
            }
            frame.validate();
            frame.repaint();
        }));
    }

    /**
     * Enables the pause button
     *
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void enablePauseButton() throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            setPauseButton.setEnabled(true);
            frame.validate();
            frame.repaint();
        }));
    }

    /**
     * Method to update the information of a single predator.
     *
     * @param identity        The identity of the predator to update the info of.
     * @param xCord           The xCord of the predator
     * @param yCord           The yCord of the predator
     * @param status          The status of the predator
     * @param numberOfTurns   The number of turns that the predator has taken
     * @param numberOfForward The number of forward movements that the predator has taken
     * @param howManySkips    The number of skips that the predator has taken
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void updateInfo(int identity, int xCord, int yCord, String status, int numberOfTurns,
                                  int numberOfForward, int howManySkips) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            predatorInfo[identity][0].setText("xPos:" + xCord);
            predatorInfo[identity][1].setText("yPos:" + yCord);
            predatorInfo[identity][2].setText("Status:" + status);
            predatorInfo[identity][3].setText("Orientation:" + Server.orientations[identity]);
            predatorInfo[identity][4].setText("numberOfTurns:" + numberOfTurns);
            predatorInfo[identity][5].setText("numberOfForward:" + numberOfForward);
            predatorInfo[identity][6].setText("numberOfSkips:" + howManySkips);
            frame.validate();
            frame.repaint();
        }));
    }

    /**
     * Method to update the status of the prey.
     *
     * @param status The new status of the prey
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void updatePreyInfo(String status) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            preyInfo.setText("Status:" + status);
            frame.validate();
            frame.repaint();
        }));
    }

    /**
     * Method to append text to the frame containing the move set of predators and
     * prey.
     *
     * @param whatToAdd
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void AddToTextFrame(String whatToAdd) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            textArea2.setText(textArea2.getText() + whatToAdd);
            frame.validate();
            frame.repaint();
        }));
    }

    public static void turnOnMoveButtonsPrey() throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait((() ->
        {
            northButton.setEnabled(true);
            eastButton.setEnabled(true);
            southButton.setEnabled(true);
            westButton.setEnabled(true);
            passButton.setEnabled(true);
            temp6.setEnabled(true);
            frame.validate();
            frame.repaint();
        }));
    }

    /**
     * @param pane The main pane of the UI.
     */
    private void addComponentsToPane(final Container pane)
    {
        if (Config.rapidTesting)
        {
            frame.setState(java.awt.Frame.ICONIFIED);
        }
        JPanel majorContainer = new JPanel();
        majorContainer.setLayout(new GridBagLayout());
        final JPanel mapContainer = new JPanel();
        mapContainer.setLayout(mapGrid);
        map1Contents.setLayout(mapLayout);
        TitledBorder border2 = new TitledBorder("Contents");
        map1Contents.setBorder(border2);
        final JPanel map2Flagged = new JPanel();
        map2Flagged.setLayout(mapLayout);
        TitledBorder border3 = new TitledBorder("Flagged tiles");
        map2Flagged.setBorder(border3);
        final JPanel map3Occupancy = new JPanel();
        map3Occupancy.setLayout(mapLayout);
        TitledBorder border4 = new TitledBorder("Occupancy Values");
        map3Occupancy.setBorder(border4);
        final JPanel map4PossiblyPrey = new JPanel();
        map4PossiblyPrey.setLayout(mapLayout);
        TitledBorder border5 = new TitledBorder("Possibly Prey");
        map4PossiblyPrey.setBorder(border5);
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
        for (int i = 0; i < Server.mapContents.length; i++)
        {
            for (int j = 0; j < Server.mapContents[0].length; j++)
            {
                mapContents[i][j] = new JButton("");
                mapContents[i][j].setPreferredSize(new Dimension(40, 40));
                mapContents[i][j].setBorder(blackBorder);
                mapContents[i][j].setAction(new mapAction(Server.mapContents[i][j] + "", i, j));
                if (i == 0 || i == Server.mapContents.length - 1 || j == 0 || j == Server.mapContents[0].length - 1)
                {
                    mapContents[i][j].setEnabled(false);
                }
                if (i == 0 || i == Server.mapContents.length - 1 || j == 0 || j == Server.mapContents[0].length - 1)
                {
                    mapContents[i][j].setEnabled(false);
                }
                map1Contents.add(mapContents[i][j]);
                mapFlagged[i][j] = new JLabel("");
                mapFlagged[i][j].setPreferredSize(new Dimension(40, 40));
                mapFlagged[i][j].setHorizontalAlignment(JLabel.CENTER);
                mapFlagged[i][j].setBorder(blackBorder);
                mapFlagged[i][j].setOpaque(true);
                map2Flagged.add(mapFlagged[i][j]);
                mapOccupied[i][j] = new JLabel("");
                mapOccupied[i][j].setPreferredSize(new Dimension(40, 40));
                mapOccupied[i][j].setHorizontalAlignment(JLabel.CENTER);
                mapOccupied[i][j].setBorder(blackBorder);
                mapOccupied[i][j].setOpaque(true);
                map3Occupancy.add(mapOccupied[i][j]);
                mapPossiblyPrey[i][j] = new JLabel("");
                mapPossiblyPrey[i][j].setPreferredSize(new Dimension(40, 40));
                mapPossiblyPrey[i][j].setHorizontalAlignment(JLabel.CENTER);
                mapPossiblyPrey[i][j].setBorder(blackBorder);
                mapPossiblyPrey[i][j].setOpaque(true);
                map4PossiblyPrey.add(mapPossiblyPrey[i][j]);
            }
        }
        for (int i = 0; i < Server.robotLocations.length; i++)
        {
            if (Server.robotLocations[i][0] != -1)
            {
                Server.mapContents[Server.robotLocations[i][0]][Server.robotLocations[i][1]] = i + 2;
                mapContents[Server.robotLocations[i][0]][Server.robotLocations[i][1]].setText((i + 2) + "");
                Server.amountOfPredatorsOnBoard++;
            }
        }

        mapContainer.add(map1Contents);
        mapContainer.add(map2Flagged);
        mapContainer.add(map3Occupancy);
        mapContainer.add(map4PossiblyPrey);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        majorContainer.add(mapContainer, c);
        final JPanel rightMenuContainer = new JPanel();
        rightMenuContainer.setLayout(new BoxLayout(rightMenuContainer, BoxLayout.Y_AXIS));
        rightMenuContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints d = new GridBagConstraints();
        GridBagConstraints e = new GridBagConstraints();
        d.gridx = 0;
        d.gridy = 0;
        e.gridx = 1;
        e.gridy = 0;
        e.ipady = 20;
        final JPanel agentInfoPanel = new JPanel();
        agentInfoPanel.setLayout(new GridBagLayout());
        JLabel pred1 = new JLabel("Predator 1");
        pred1.setBackground(Color.CYAN);
        pred1.setOpaque(true);
        pred1.setHorizontalAlignment(JLabel.CENTER);
        agentInfoPanel.add(pred1, d);
        final JPanel predator1Container = new JPanel();
        predator1Container.setLayout(new BoxLayout(predator1Container, BoxLayout.Y_AXIS));
        predator1Container.setBorder(new EmptyBorder(0, 10, 0, 0));
        if (Server.robotLocations[0][0] != -1)
        {
            predatorInfo[0][0] = new JLabel("xPos:" + Server.robotLocations[0][0]);
            predatorInfo[0][1] = new JLabel("yPos:" + Server.robotLocations[0][1]);
        }
        else
        {
            predatorInfo[0][0] = new JLabel("xPos:None");
            predatorInfo[0][1] = new JLabel("yPos:None");
        }
        predatorInfo[0][2] = new JLabel("Orientation:N");
        predatorInfo[0][3] = new JLabel("Status:Unconnected");
        predatorInfo[0][4] = new JLabel("numberOfTurns:0");
        predatorInfo[0][5] = new JLabel("numberOfMoves:0");
        predatorInfo[0][6] = new JLabel("numberOfSkips:0");
        predator1Container.add(predatorInfo[0][0]);
        predator1Container.add(predatorInfo[0][1]);
        predator1Container.add(predatorInfo[0][2]);
        predator1Container.add(predatorInfo[0][3]);
        predator1Container.add(predatorInfo[0][4]);
        predator1Container.add(predatorInfo[0][5]);
        predator1Container.add(predatorInfo[0][6]);
        agentInfoPanel.add(predator1Container, e);
        d.gridx = 0;
        d.gridy = 1;
        e.gridx = 1;
        e.gridy = 1;
        JLabel pred2 = new JLabel("Predator 2");
        pred2.setBackground(Color.GREEN);
        pred2.setOpaque(true);
        pred2.setHorizontalAlignment(JLabel.CENTER);
        agentInfoPanel.add(pred2, d);
        final JPanel predator2Container = new JPanel();
        predator2Container.setLayout(new BoxLayout(predator2Container, BoxLayout.Y_AXIS));
        predator2Container.setBorder(new EmptyBorder(0, 10, 0, 0));
        if (Server.robotLocations[1][0] != -1)
        {
            predatorInfo[1][0] = new JLabel("xPos:" + Server.robotLocations[1][0]);
            predatorInfo[1][1] = new JLabel("yPos:" + Server.robotLocations[1][1]);
        }
        else
        {
            predatorInfo[1][0] = new JLabel("xPos:None");
            predatorInfo[1][1] = new JLabel("yPos:None");
        }
        predatorInfo[1][2] = new JLabel("Orientation:N");
        predatorInfo[1][3] = new JLabel("Status:Unconnected");
        predatorInfo[1][4] = new JLabel("numberOfTurns:0");
        predatorInfo[1][5] = new JLabel("numberOfMoves:0");
        predatorInfo[1][6] = new JLabel("numberOfSkips:0");
        predator2Container.add(predatorInfo[1][0]);
        predator2Container.add(predatorInfo[1][1]);
        predator2Container.add(predatorInfo[1][2]);
        predator2Container.add(predatorInfo[1][3]);
        predator2Container.add(predatorInfo[1][4]);
        predator2Container.add(predatorInfo[1][5]);
        predator2Container.add(predatorInfo[1][6]);
        agentInfoPanel.add(predator2Container, e);
        d.gridx = 0;
        d.gridy = 2;
        e.gridx = 1;
        e.gridy = 2;
        JLabel pred3 = new JLabel("Predator 3");
        pred3.setBackground(Color.PINK);
        pred3.setOpaque(true);
        pred3.setHorizontalAlignment(JLabel.CENTER);
        agentInfoPanel.add(pred3, d);
        final JPanel predator3Container = new JPanel();
        predator3Container.setLayout(new BoxLayout(predator3Container, BoxLayout.Y_AXIS));
        predator3Container.setBorder(new EmptyBorder(0, 10, 0, 0));
        if (Server.robotLocations[2][0] != -1)
        {
            predatorInfo[2][0] = new JLabel("xPos:" + Server.robotLocations[2][0]);
            predatorInfo[2][1] = new JLabel("yPos:" + Server.robotLocations[2][1]);
        }
        else
        {
            predatorInfo[2][0] = new JLabel("xPos:None");
            predatorInfo[2][1] = new JLabel("yPos:None");
        }
        predatorInfo[2][2] = new JLabel("Orientation:N");
        predatorInfo[2][3] = new JLabel("Status:Unconnected");
        predatorInfo[2][4] = new JLabel("numberOfTurns:0");
        predatorInfo[2][5] = new JLabel("numberOfMoves:0");
        predatorInfo[2][6] = new JLabel("numberOfSkips:0");
        predator3Container.add(predatorInfo[2][0]);
        predator3Container.add(predatorInfo[2][1]);
        predator3Container.add(predatorInfo[2][2]);
        predator3Container.add(predatorInfo[2][3]);
        predator3Container.add(predatorInfo[2][4]);
        predator3Container.add(predatorInfo[2][5]);
        predator3Container.add(predatorInfo[2][6]);
        agentInfoPanel.add(predator3Container, e);
        d.gridx = 0;
        d.gridy = 3;
        e.gridx = 1;
        e.gridy = 3;
        JLabel pred4 = new JLabel("Predator 4");
        pred4.setBackground(Color.YELLOW);
        pred4.setOpaque(true);
        pred4.setHorizontalAlignment(JLabel.CENTER);
        agentInfoPanel.add(pred4, d);
        final JPanel predator4Container = new JPanel();
        predator4Container.setLayout(new BoxLayout(predator4Container, BoxLayout.Y_AXIS));
        predator4Container.setBorder(new EmptyBorder(0, 10, 0, 0));
        if (Server.robotLocations[3][0] != -1)
        {
            predatorInfo[3][0] = new JLabel("xPos:" + Server.robotLocations[3][0]);
            predatorInfo[3][1] = new JLabel("yPos:" + Server.robotLocations[3][1]);
        }
        else
        {
            predatorInfo[3][0] = new JLabel("xPos:None");
            predatorInfo[3][1] = new JLabel("yPos:None");
        }
        predatorInfo[3][2] = new JLabel("Orientation:N");
        predatorInfo[3][3] = new JLabel("Status:Unconnected");
        predatorInfo[3][4] = new JLabel("numberOfTurns:0");
        predatorInfo[3][5] = new JLabel("numberOfMoves:0");
        predatorInfo[3][6] = new JLabel("numberOfSkips:0");
        predator4Container.add(predatorInfo[3][0]);
        predator4Container.add(predatorInfo[3][1]);
        predator4Container.add(predatorInfo[3][2]);
        predator4Container.add(predatorInfo[3][3]);
        predator4Container.add(predatorInfo[3][4]);
        predator4Container.add(predatorInfo[3][5]);
        predator4Container.add(predatorInfo[3][6]);
        agentInfoPanel.add(predator4Container, e);
        d.gridx = 0;
        d.gridy = 4;
        e.gridx = 1;
        e.gridy = 4;
        agentInfoPanel.add(new JLabel("Prey"), d);
        preyInfo = new JLabel("Status:Unconnected");
        agentInfoPanel.add(preyInfo, e);

        final JPanel createMapButtons = new JPanel();
        createMapButtons.setLayout(new GridLayout(2, 2));
        setPredButton = new JButton("SetPredators");
        setPredButton.setPreferredSize(new Dimension(100, 20));
        setPredButton.setAction(new switchState("SetPred", "Pred"));
        createMapButtons.add(setPredButton);
        setBlocksButton = new JButton("SetBlocks");
        setBlocksButton.setPreferredSize(new Dimension(100, 20));
        setBlocksButton.setAction(new switchState("SetBlock", "Block"));
        createMapButtons.add(setBlocksButton);
        setEmptyButton = new JButton("SetEmpty");
        setEmptyButton.setPreferredSize(new Dimension(100, 20));
        setEmptyButton.setAction(new switchState("SetEmpty", "Empty"));
        createMapButtons.add(setEmptyButton);

        final JPanel createOrderingButtons = new JPanel();
        createOrderingButtons.setLayout(new GridLayout(3, 2));
        createOrderingButtons.setBorder(new EmptyBorder(25, 0, 0, 0));
        setNextPreyButton = new JButton("Next prey");
        setNextPreyButton.setAction(new setNextAction("PreyTurn"));
        setNextPreyButton.setPreferredSize(new Dimension(100, 20));
        setNextPreyButton.setEnabled(false);
        createOrderingButtons.add(setNextPreyButton);
        setNextPredButton = new JButton("Next predator");
        setNextPredButton.setAction(new setNextAction("PredTurn"));
        setNextPredButton.setPreferredSize(new Dimension(100, 20));
        setNextPredButton.setEnabled(false);
        createOrderingButtons.add(setNextPredButton);
        setContinuousButton = new JButton("Continuous");
        setContinuousButton.setAction(new setNextAction("Continuous"));
        setContinuousButton.setPreferredSize(new Dimension(100, 20));
        setContinuousButton.setEnabled(false);
        createOrderingButtons.add(setContinuousButton);
        setPauseButton = new JButton("Pause");
        setPauseButton.setAction(new setNextAction("Pause"));
        setPauseButton.setPreferredSize(new Dimension(100, 20));
        setPauseButton.setEnabled(false);
        createOrderingButtons.add(setPauseButton);
        setExitButton = new JButton("Exit");
        setExitButton.setAction(new setNextAction("Exit"));
        setExitButton.setPreferredSize(new Dimension(100, 20));
        setExitButton.setEnabled(false);
        createOrderingButtons.add(setExitButton);

        rightMenuContainer.add(createMapButtons);
        JPanel confirmButton = new JPanel();
        confirmButton.setBorder(new EmptyBorder(20, 0, 0, 0));
        setConfirm = new JButton("Confirm map");
        setConfirm.setPreferredSize(new Dimension(100, 20));
        setConfirm.setAction(new confirmMap());
        confirmButton.add(setConfirm);
        rightMenuContainer.add(confirmButton);
        rightMenuContainer.add(createOrderingButtons);
        rightMenuContainer.add(agentInfoPanel);
        majorContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        c.gridx = 1;
        c.gridy = 0;
        majorContainer.add(rightMenuContainer, c);
        JPanel textAreaPanel = new JPanel();
        textAreaPanel.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        JTextArea textArea = new JTextArea(20, 25);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(true);
        textArea.setBorder(new EmptyBorder(20, 0, 20, 0));
        textAreaPanel.add(scrollPane, g);

        g.gridx = 0;
        g.gridy = 1;
        textArea2 = new JTextArea(20, 25);
        JScrollPane scrollPane2 = new JScrollPane(textArea2);
        textArea2.setEditable(true);
        textArea2.setBorder(new EmptyBorder(20, 0, 20, 0));
        textAreaPanel.add(scrollPane2, g);
        c.gridx = 2;
        c.gridy = 0;
        majorContainer.add(textAreaPanel, c);
        c.gridx = 3;
        c.gridy = 0;
        if (!Config.rapidTesting)
        {
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("/Users/robert/Desktop/Tests"));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                whereToSave = fc.getSelectedFile().getAbsolutePath();
                whereToSaveStats = fc.getSelectedFile().getParent();
            }
        }
        else
        {
            String fileLoc = "/Users/robert/Desktop/Tests/simResults/" + Config.PreyPolicy + "&" + Config.PredPolicy
                    + "&" + Config.PredSearch + "&" + Config.instantReward + "&" + Config.dissipationGrid + "/Test"
                    + Server.whereToSave;
            File dir = new File(fileLoc);
            dir.mkdirs();
            whereToSaveStats = fileLoc;
            whereToSave = fileLoc;
        }
        JButton print = new JButton("Print");
        print.setPreferredSize(new Dimension(100, 20));
        print.setAction(new printMaps());
        rightMenuContainer.add(print);
        pane.add(majorContainer);
        if (Config.PreyPolicy == 4)
        {
            JFrame frame2 = new JFrame("Prey actions");
            frame2.setLocation(1300, 0);

            frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            panel.setOpaque(true);
            northButton = new JButton("N");
            northButton.setAction(new chooseNextPreyAction("North"));
            northButton.setPreferredSize(new Dimension(60, 40));
            northButton.setEnabled(false);
            eastButton = new JButton("E");
            eastButton.setAction(new chooseNextPreyAction("East"));
            eastButton.setPreferredSize(new Dimension(60, 40));
            eastButton.setEnabled(false);
            southButton = new JButton("S");
            southButton.setAction(new chooseNextPreyAction("South"));
            southButton.setPreferredSize(new Dimension(60, 40));
            southButton.setEnabled(false);
            westButton = new JButton("W");
            westButton.setAction(new chooseNextPreyAction("West"));
            westButton.setPreferredSize(new Dimension(60, 40));
            westButton.setEnabled(false);
            passButton = new JButton("P");
            passButton.setAction(new chooseNextPreyAction("Pass"));
            passButton.setPreferredSize(new Dimension(60, 30));
            passButton.setEnabled(false);
            temp6 = new JButton("Exit");
            temp6.setAction(new chooseNextPreyAction("Exit"));
            temp6.setPreferredSize(new Dimension(60, 30));
            temp6.setEnabled(false);

            GridBagConstraints k = new GridBagConstraints();
            JPanel panel2 = new JPanel();
            panel2.setLayout(new GridBagLayout());
            GridBagConstraints p = new GridBagConstraints();
            p.fill = GridBagConstraints.HORIZONTAL;
            k.fill = GridBagConstraints.HORIZONTAL;
            k.insets = new Insets(5, 5, 5, 5);
            k.gridx = 1;
            k.gridy = 0;
            panel.add(northButton, k);
            k.gridx = 2;
            k.gridy = 1;
            panel.add(eastButton, k);
            k.gridx = 1;
            k.gridy = 2;
            panel.add(southButton, k);
            k.gridx = 0;
            k.gridy = 1;
            panel.add(westButton, k);
            k.gridx = 1;
            k.gridy = 1;
            panel.add(passButton, k);
            JPanel panel3 = new JPanel();
            panel3.add(temp6);
            p.gridx = 0;
            p.gridy = 0;
            panel2.add(panel, p);
            p.gridx = 0;
            p.gridy = 1;
            panel2.add(panel3, p);
            frame2.getContentPane().add(BorderLayout.CENTER, panel2);
            frame2.pack();
            frame2.setLocationByPlatform(true);
            frame2.setVisible(true);
            frame2.setResizable(false);
        }
        if (Config.rapidTesting)
        {
            setConfirm.doClick();
        }
    }

    /**
     * Method to launch the user interface in.
     */
    public void run()
    {

        createAndShowGUI();
    }

    /**
     * Class to store the button actions used to set cells in the grid at the start
     * of the simulation
     */
    class mapAction extends AbstractAction
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        final int xCord;
        final int yCord;

        /**
         * @param contents The content we wish to place in the cell
         * @param xCord    The xCord of the cell we are looking at
         * @param yCord    The yCord of the cell we are looking at
         */
        mapAction(String contents, int xCord, int yCord)
        {
            super(contents);
            this.xCord = xCord;
            this.yCord = yCord;
        }

        /**
         * Tries to place the contents into the cell, will place the contents if valid.
         */
        public void actionPerformed(ActionEvent e)
        {

            switch (action)
            {
                case "Pred":
                    if (Server.amountOfPredatorsOnBoard < 4)
                    {
                        if (Server.mapContents[xCord][yCord] == 0 || Server.mapContents[xCord][yCord] == 1)
                        {
                            int i;
                            for (i = 0; i < Server.robotLocations.length; i++)
                                if (Server.robotLocations[i][0] == -1)
                                {
                                    break;
                                }
                            predatorInfo[i][0].setText("xPos:" + xCord);
                            predatorInfo[i][1].setText("yPos:" + yCord);
                            predatorInfo[i][2].setText("Orientation:N");
                            predatorInfo[i][3].setText("Status:Unconnected");
                            Server.mapContents[xCord][yCord] = 2 + i;
                            Server.robotLocations[i][0] = xCord;
                            Server.robotLocations[i][1] = yCord;
                            Server.amountOfPredatorsOnBoard++;
                        }
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Your adding more than 4 predators?");
                    }
                    break;
                case "Block":
                    if (Server.mapContents[xCord][yCord] == 0 || Server.mapContents[xCord][yCord] == 1)
                    {
                        Server.mapContents[xCord][yCord] = 1;
                    }
                    else
                    {
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][0].setText("xPos:None");
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][1].setText("yPos:None");
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][2].setText("Orientation:N");
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][3].setText("Status:Unconnected");
                        Server.robotLocations[Server.mapContents[xCord][yCord] - 2][0] = -1;
                        Server.robotLocations[Server.mapContents[xCord][yCord] - 2][1] = -1;
                        Server.mapContents[xCord][yCord] = 1;
                        Server.amountOfPredatorsOnBoard--;
                    }
                    break;
                case "Empty":
                    if (Server.mapContents[xCord][yCord] == 0 || Server.mapContents[xCord][yCord] == 1)
                    {
                        Server.mapContents[xCord][yCord] = 0;
                    }
                    else
                    {
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][0].setText("xPos:None");
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][1].setText("yPos:None");
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][2].setText("Orientation:N");
                        predatorInfo[Server.mapContents[xCord][yCord] - 2][3].setText("Status:Unconnected");
                        Server.robotLocations[Server.mapContents[xCord][yCord] - 2][0] = -1;
                        Server.robotLocations[Server.mapContents[xCord][yCord] - 2][1] = -1;
                        Server.robotLocations[Server.mapContents[xCord][yCord] - 2][0] = -1;
                        Server.mapContents[xCord][yCord] = 0;
                        Server.amountOfPredatorsOnBoard--;
                    }
                    break;
            }
            mapContents[xCord][yCord].setText(Server.mapContents[xCord][yCord] + "");
        }
    }

    class chooseNextPreyAction extends AbstractAction
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        final String message;

        chooseNextPreyAction(String contents)
        {
            super(contents);
            message = contents;
        }

        /**
         * Tries to place the contents into the cell, will place the contents if valid.
         */
        public void actionPerformed(ActionEvent e)
        {
            northButton.setEnabled(false);
            eastButton.setEnabled(false);
            southButton.setEnabled(false);
            westButton.setEnabled(false);
            passButton.setEnabled(false);
            temp6.setEnabled(false);
            Prey.message = message;
            Prey.preyNextChoice.lock();
            try
            {
                Prey.waitForChoice.signalAll();
            }
            finally
            {
                Prey.preyNextChoice.unlock();
            }
        }
    }

    /**
     * Class used to print the map upon clicking on button.
     */
    class printMaps extends AbstractAction
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        printMaps()
        {
            super("Print");
        }

        public void actionPerformed(ActionEvent e)
        {
            Container content = frame.getContentPane();
            BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            content.printAll(g2d);
            g2d.dispose();
            File outputFile = new File(whereToSave + "/saved" + screenShotNumber + ".png");
            screenShotNumber++;
            try
            {
                ImageIO.write(img, "png", outputFile);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Class to set the action for next movement used within the episode assigned to
     * buttons
     */
    class setNextAction extends AbstractAction
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        final String action;

        setNextAction(String contents)
        {
            super(contents);
            this.action = contents;
        }

        public void actionPerformed(ActionEvent e)
        {
            Server.frontEndLock.lock();
            try
            {
                if (Server.robotsConnected.get() == 5)
                {
                    Server.message = action;
                    setNextPredButton.setEnabled(false);
                    setNextPreyButton.setEnabled(false);
                    setContinuousButton.setEnabled(false);
                    setPauseButton.setEnabled(false);
                    setExitButton.setEnabled(false);
                    Server.frontEndLockSync.signalAll();
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "You haven't connected all robots");
                }
            }
            finally
            {
                Server.frontEndLock.unlock();
            }
        }
    }

    /**
     * Action to confirm the map and start the simulation
     */
    class confirmMap extends AbstractAction
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        confirmMap()
        {
            super("Confirm map");
        }

        public void actionPerformed(ActionEvent e)
        {
            if (Server.amountOfPredatorsOnBoard == 4)
            {
                Server.frontEndLock.lock();
                try
                {
                    setPredButton.setEnabled(false);
                    setBlocksButton.setEnabled(false);
                    setEmptyButton.setEnabled(false);
                    setConfirm.setEnabled(false);
                    Server.frontEndGoAhead = true;
                    Server.frontEndLockSync.signalAll();
                }
                finally
                {
                    Server.frontEndLock.unlock();
                }
                if (!Config.Simulated)
                {
                    new Thread(() ->
                    {
                        // 131 = 05 prey 5.431
                        // 129 = 08 4 5.6
                        // 130 = 14 1 5.475
                        // 132 = 4 2 5.587
                        // 128 = ? 3 5.55
                        Thread[] startUps = new Thread[5];
                        startUps[0] = new Thread(new makeSureSSH(Config.preyIP, "Prey.jar"));
                        startUps[1] = new Thread(new makeSureSSH(Config.pred1IP, "Config.jar"));
                        startUps[2] = new Thread(new makeSureSSH(Config.pred2IP, "Config.jar"));
                        startUps[3] = new Thread(new makeSureSSH(Config.pred3IP, "Config.jar"));
                        startUps[4] = new Thread(new makeSureSSH(Config.pred4IP, "Config.jar"));
                        for (Thread startup : startUps)
                        {
                            startup.start();
                        }
                        for (Thread startup : startUps)
                        {
                            try
                            {
                                startup.join();
                            }
                            catch (InterruptedException e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                        Toolkit.getDefaultToolkit().beep();
                    }).start();
                }
            }
        }
    }

    /**
     * Class that is used to ssh into each robot and verify that it has connected to
     * the server.
     */
    class makeSureSSH implements Runnable
    {
        final String IP;
        final String jar;

        makeSureSSH(String IP, String jar)
        {
            this.IP = IP;
            this.jar = jar;
        }

        @Override
        public void run()
        {
            try
            {
                Process p = Runtime.getRuntime().exec("ssh root@" + IP);
                PrintStream out = new PrintStream(p.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                out.println("jrun -jar /home/lejos/programs/" + jar);
                out.flush();
                while (true)
                {
                    String s = in.readLine();
                    if (s.equals("connected"))
                    {
                        break;
                    }
                    else
                    {
                        out.println("jrun -jar /home/lejos/programs/" + jar);
                        out.flush();
                    }
                }
                in.close();
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Control the action state toggled currently
     */
    class switchState extends AbstractAction
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        final String state;

        switchState(String contents, String state)
        {
            super(contents);
            this.state = state;
        }

        public void actionPerformed(ActionEvent e)
        {
            action = state;
        }
    }
}