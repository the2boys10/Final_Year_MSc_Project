import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server
{
    public static final int[][] mapContents = new int[][]{{1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 1, 1, 1, 1, 1, 1, 1}};
    public static final Cell[][] map = new Cell[8][8];
    public static final Lock lockTurn = new ReentrantLock(true);
    public static final Condition PredTurn = lockTurn.newCondition();
    public static final Condition turnTaken = lockTurn.newCondition();
    public static final int[][] robotLocations = new int[][]{{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};
    public static final Lock frontEndLock = new ReentrantLock(true);
    public static final Condition frontEndLockSync = frontEndLock.newCondition();
    private static final int amountOfRobots = 4;
    public static final boolean[] nextTurn = new boolean[amountOfRobots + 1];
    public static final char[] orientations = new char[amountOfRobots];
    public static int amountOfPredatorsOnBoard = 0;
    public static String message = "";
    public static boolean frontEndGoAhead = false;
    public static int robotsConnected = 0;
    public static boolean stop = false;
    private static Thread[] robotsThread;
    private static outBoundConnection[] robots;
    private static int whichNext = 0;

    public static void main(String args[])
    {
        try
        {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        FrontEnd2 frontEnd = new FrontEnd2("PredPrey");
        try
        {
            SwingUtilities.invokeAndWait(frontEnd);
            waitForFrontEnd();
            initialiseMap();
            createPredators();
            FrontEnd2.changeGrid1IntoLabels();
            FrontEnd2.updateMapsAndInfo();
            startPredators();
            FrontEnd2.reEnableButtons();
            FrontEnd2.takeScreen();
            int RoundNumber = 0;
            boolean allThreadsDead = false;
            message = "Continuous";
            while (!allThreadsDead)
            {
                waitForFrontEnd();
                if (message.equals("PredTurn"))
                {
                    PredNextTurn();
                    FrontEnd2.takeScreen();
                }
                else if (message.equals("Continuous"))
                {
                    while (message.equals("Continuous") && !allThreadsDead)
                    {
                        if (RoundNumber > 400)
                        {
                            stop = true;
                        }
                        FrontEnd2.enablePause();
                        FrontEnd2.updateMapsAndInfo();
                        RoundNumber++;
                        PredNextTurn();
                        FrontEnd2.updateMapsAndInfo();
                        FrontEnd2.takeScreen();
                        allThreadsDead = checkIfSystemIsAlive();
                        if(Config.Simulated)
                        {
                            double maxVal = 0;
                            for (int i = 0; i < map[robots[0].xCord][robots[0].yCord].chanceOfOccupancy.length; i++)
                            {
                                if (maxVal < map[robots[0].xCord][robots[0].yCord].chanceOfOccupancy[i])
                                {
                                    maxVal = map[robots[0].xCord][robots[0].yCord].chanceOfOccupancy[i];
                                }
                            }
                            if (maxVal > 0.9 && RoundNumber > 4)
                            {
                                maxVal = 0;
                                for (int i = 0; i < map[robots[1].xCord][robots[1].yCord].chanceOfOccupancy.length; i++)
                                {
                                    if (maxVal < map[robots[1].xCord][robots[1].yCord].chanceOfOccupancy[i])
                                    {
                                        maxVal = map[robots[1].xCord][robots[1].yCord].chanceOfOccupancy[i];
                                    }
                                }
                                if (maxVal > 0.9)
                                {
                                    maxVal = 0;
                                    for (int i = 0; i < map[robots[2].xCord][robots[2].yCord].chanceOfOccupancy.length; i++)
                                    {
                                        if (maxVal < map[robots[2].xCord][robots[2].yCord].chanceOfOccupancy[i])
                                        {
                                            maxVal = map[robots[2].xCord][robots[2].yCord].chanceOfOccupancy[i];
                                        }
                                    }
                                    if (maxVal > 0.9)
                                    {
                                        maxVal = 0;
                                        for (int i = 0; i < map[robots[3].xCord][robots[3].yCord].chanceOfOccupancy.length; i++)
                                        {
                                            if (maxVal < map[robots[3].xCord][robots[3].yCord].chanceOfOccupancy[i])
                                            {
                                                maxVal = map[robots[3].xCord][robots[3].yCord].chanceOfOccupancy[i];
                                            }
                                        }
                                        if (maxVal > 0.9)
                                        {
                                            FrontEnd2.takeScreen();
                                            stop = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                FrontEnd2.updateMapsAndInfo();
                FrontEnd2.reEnableButtons();
                allThreadsDead = checkIfSystemIsAlive();
            }
            System.exit(0);
        }
        catch (InvocationTargetException | InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void waitForFrontEnd()
    {
        frontEndLock.lock();
        try
        {
            if (!frontEndGoAhead)
            {
                frontEndLockSync.await();
            }
            frontEndGoAhead = false;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            frontEndLock.unlock();
        }
    }

    private static void PredNextTurn() throws InterruptedException
    {
        for (int i = 0; i < 1; i++)
        {
            lockTurn.lock();
            try
            {
                nextTurn[whichNext] = true;
                whichNext++;
                if (whichNext == amountOfRobots)
                {
                    whichNext = 0;
                }
                PredTurn.signalAll();
                while (checkIfAnyTrue(nextTurn.length - 1))
                {
                    turnTaken.await();
                }
            }
            finally
            {
                lockTurn.unlock();
            }
        }
    }

    private static boolean checkIfAnyTrue(int xBound)
    {
        boolean check = false;
        for (int i = 0; i < xBound; i++)
        {
            if (Server.nextTurn[i] && !robots[i].finished)
            {
                check = true;
                break;
            }
        }
        return check;
    }

    private static boolean checkIfSystemIsAlive()
    {
        boolean canClose = true;
        synchronized (lockTurn)
        {
            for (int i = 0; i < amountOfRobots; i++)
            {
                if (robotsThread[i].isAlive())
                {
                    canClose = false;
                    break;
                }
            }
        }
        return canClose;
    }

    private static void initialiseMap()
    {
        for (int i = 0; i < mapContents.length; i++)
        {
            for (int j = 0; j < mapContents.length; j++)
            {
                if (mapContents[i][j] == 0)
                {
                    map[i][j] = new Cell(mapContents[i][j]);
                }
                else
                {
                    map[i][j] = new Cell(mapContents[i][j]);
                }
                if (mapContents[i][j] == 10)
                {
                    map[i][j] = new Cell(mapContents[i][j]);
                }
            }
        }
    }

    private static void createPredators()
    {
        robots = new outBoundConnection[amountOfRobots];
        for (int i = 0; i < amountOfRobots; i++)
        {
            nextTurn[i] = false;
            if (Config.Simulated)
            {
                map[robotLocations[i][0]][robotLocations[i][1]].contents = i + 2;
                robots[i] = new outBoundConnection(1224 + i, i + 2, robotLocations[i][0], robotLocations[i][1], i);
            }
            else
            {
                robots[i] = new outBoundConnection(1224 + i, i + 2, -1, -1, i);
            }
        }
        robotsThread = new Thread[amountOfRobots];
        for (int i = 0; i < amountOfRobots; i++)
        {
            robotsThread[i] = new Thread(robots[i]);
        }
    }

    private static void startPredators()
    {
        for (int i = 0; i < amountOfRobots; i++)
        {
            robotsThread[i].start();
        }
    }
}
