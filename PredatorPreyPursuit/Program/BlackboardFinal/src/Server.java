import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The central hub used to create the threads used to control the predators and prey, deals with ordering of predator and prey turns.
 */
class Server
{
    public static final Lock waitForAllWaiting = new ReentrantLock(true);
    /**
     * The map in the current episode in int form
     * <ul><li>1: Block</li><li>0: Empty</li></ul>
     */
    //static final int[][] mapContents = new int[][]{{1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 1, 0, 0, 0, 0, 1}, {1, 0, 1, 0, 1, 1, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 1, 1, 1, 1, 0, 1, 1}, {1, 0, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1}};
    static final int[][] mapContents = new int[][]{{1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 1, 0, 0, 1, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 0, 1, 0, 0, 1, 0, 1}, {1, 0, 0, 0, 0, 0, 0, 1}, {1, 1, 1, 1, 1, 1, 1, 1}};
    /**
     * The map in the current episode made of Cell objects
     * <ul><li>1: Block</li><li>0: Empty</li></ul>
     */
    static final Cell[][] map = new Cell[8][8];
    /**
     * The amount of predators in the current episode
     */
    static final int amountOfRobots = 4;
    static final Lock lockTurn = new ReentrantLock(true);
    static final Lock waitForScan = new ReentrantLock(true);
    static final Condition PredTurn = lockTurn.newCondition();
    static final Condition turnTaken = lockTurn.newCondition();
    /**
     * An array representing the agents that need to take their turn
     */
    static final boolean[] nextTurn = new boolean[amountOfRobots + 1];
    /**
     * The locations of each predator
     */
    static final int[][] robotLocations = new int[][]{{1, 1}, {1, 6}, {6, 1}, {6, 6}};
    /**
     * The last known location of the prey
     */
    static final int[] lastKnownLocation = new int[]{1, 1};
    /**
     * The moves that each predator would like to take
     */
    static final int[] possibleMoves = new int[amountOfRobots];
    /**
     * The orientation of each predator
     */
    static final char[] orientations = new char[4];
    static final Condition mapUpdateFinished = lockTurn.newCondition();
    static final Condition mapNegotiation = lockTurn.newCondition();
    static final Condition turnTakenPrey = lockTurn.newCondition();
    static final Condition turnPrey = lockTurn.newCondition();
    static final Lock frontEndLock = new ReentrantLock(true);
    static final Condition frontEndLockSync = frontEndLock.newCondition();
    static final AtomicInteger robotsConnected = new AtomicInteger(0);
    /**
     * The amount of predators that has been placed on the board.
     */
    public static int amountOfPredatorsOnBoard;
    /**
     * Where should we save the statistics from the current episode
     */
    public static String whereToSave = "";
    /**
     * How many predators are currently waiting to start the current episode
     */
    public static int howManyWait = 0;
    /**
     * Array of objects relating to each Predator
     */
    static outBoundConnection[] robots;
    /**
     * The move we want each agent to take in the current round
     */
    static int[] moveToTake = new int[amountOfRobots];
    /**
     * The amount of occupiable spaces in the current map.
     */
    static double amountOfOccupiableSpaces = 0;
    /**
     * Message telling the server who's turn to take next
     */
    static String message = "";
    /**
     * Boolean representing whether we should wait on the front end.
     */
    static boolean frontEndGoAhead = false;
    /**
     * Array relating to each Predator
     */
    private static Thread[] robotsThread;
    /**
     * The object relating to the prey.
     */
    private static Prey prey;
    /**
     * The thread relating to the prey.
     */
    private static Thread preyThread;

    /**
     * Method that starts the simulation
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws UnsupportedLookAndFeelException
     */
    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException, InvocationTargetException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
    {
        if (Config.rapidTesting)
        {
            whereToSave = args[0];
            Config.PreyPolicy = Integer.parseInt(args[1]);
            Config.PredPolicy = Integer.parseInt(args[2]);
            Config.PredSearch = Integer.parseInt(args[3]);
            Config.instantReward = Integer.parseInt(args[4]) == 1;
            Config.dissipationGrid = Integer.parseInt(args[5]) == 1;
        }
        if (Config.preyLocation[0] != -1)
        {
            mapContents[Config.preyLocation[0]][Config.preyLocation[1]] = 10;
        }
        FrontEnd.makeUI();
        waitForFrontEnd();
        initialiseMap();
//        for(int i=0; i < map.length; i++)
//        {
//        	for(int j=0; j < map.length; j++)
//            {
//            	map[i][j].chanceOfOccupancy=0;
//            	map[i][j].flagged=true;
//            }
//        }
//        map[4][1].flagged=false;
//        map[5][1].flagged=false;
//        map[4][1].chanceOfOccupancy=100;
//        map[5][1].chanceOfOccupancy=100;
        createPredators();
        FrontEnd.changeGrid1IntoLabels();
        FrontEnd.updateMapsAndInfo();
        startPredAndPrey();
        FrontEnd.reEnableButtons();
        FrontEnd.takeScreen();
        mainLoop();
        System.exit(0);
    }

    /**
     * Method which controls the ordering of the current episode until all methods have finished then outputs the statistics.
     * @throws InvocationTargetException
     * @throws InterruptedException
     * @throws IOException
     */
    private static void mainLoop() throws InvocationTargetException, InterruptedException, IOException
    {
        int success = 1;
        int countOfTurns = 0;
        boolean forceOut = false;
        label:
        while (checkIfSystemIsAlive() && !forceOut)
        {
            waitForFrontEnd();
            switch (message)
            {
                case "PredTurn":
                    outBoundConnection.amountSurround = 0;
                    outBoundConnection.amountLookingAtPrey = 0;
                    FrontEnd.clearTextFrame("");
                    PredNextTurn();
                    restartPredFound();
                    FrontEnd.takeScreen();
                    break;
                case "PreyTurn":
                    FrontEnd.clearTextFrame("");
                    preyTurn();
                    MapUpdater.waitForPreyMapUpdate();
                    countOfTurns++;
                    FrontEnd.takeScreen();
                    if (countOfTurns > 25 && Config.PreyPolicy == 4)
                    {
                        break label;
                    }
                    break;
                case "Continuous":
                    FrontEnd.enablePauseButton();
                    while (message.equals("Continuous") && checkIfSystemIsAlive())
                    {
                        outBoundConnection.amountSurround = 0;
                        outBoundConnection.amountLookingAtPrey = 0;
                        FrontEnd.updateMapsAndInfo();
                        FrontEnd.clearTextFrame("");
                        PredNextTurn();
                        restartPredFound();
                        FrontEnd.updateMapsAndInfo();
                        FrontEnd.takeScreen();
                        preyTurn();
                        MapUpdater.waitForPreyMapUpdate();
                        FrontEnd.updateMapsAndInfo();
                        FrontEnd.takeScreen();
                        countOfTurns++;
                        if (countOfTurns > 25 && Config.PreyPolicy == 4)
                        {
                            forceOut = true;
                            break;
                        }
                    }
                    break;
                case "Exit":
                    success = 0;
                    break label;
            }
            FrontEnd.updateMapsAndInfo();
            FrontEnd.reEnableButtons();
        }
        String fileLoc;
        if (Config.rapidTesting)
        {
            fileLoc = "/Users/robert/Desktop/Tests/simResults/" + Config.PreyPolicy + "&" + Config.PredPolicy + "&" + Config.PredSearch + "&" + Config.instantReward + "&" + Config.dissipationGrid;
        }
        else
        {
            fileLoc = FrontEnd.whereToSaveStats;
        }
        FileWriter pw = new FileWriter(fileLoc + "/tests.csv", true);
        double amountOfTurnsAvg = (double) (robots[0].howManyTurns + robots[1].howManyTurns + robots[2].howManyTurns + robots[3].howManyTurns) / 4.0;
        double amountOfForwardAvg = (double) (robots[0].howManyMoveForward + robots[1].howManyMoveForward + robots[2].howManyMoveForward + robots[3].howManyMoveForward) / 4.0;
        double amountOfSkipsAvg = (double) (robots[0].howManySkips + robots[1].howManySkips + robots[2].howManySkips + robots[3].howManySkips) / 4.0;
        BufferedWriter bw = new BufferedWriter(pw);
        String sb = String.valueOf(success) + "," + robots[0].howManyTurns + "," + robots[1].howManyTurns + "," + robots[2].howManyTurns + "," + robots[3].howManyTurns + "," + amountOfTurnsAvg + "," + robots[0].howManyMoveForward + "," +
                robots[1].howManyMoveForward + "," + robots[2].howManyMoveForward + "," + robots[3].howManyMoveForward + "," + amountOfForwardAvg + "," + robots[0].howManySkips + "," + robots[1].howManySkips + "," + robots[2].howManySkips +
                "," + robots[3].howManySkips + "," + amountOfSkipsAvg + "," + "," + (amountOfSkipsAvg + amountOfForwardAvg + amountOfTurnsAvg) + "\n";
        bw.write(sb);
        bw.close();
        pw.close();
    }

    /**
     * Reset the values relating to whether the prey has been seen by a specific agent in the current round.
     */
    private static void restartPredFound()
    {
        Found.whichAgentFound[0] = false;
        Found.whichAgentFound[1] = false;
        Found.whichAgentFound[2] = false;
        Found.whichAgentFound[3] = false;
    }

    /**
     * Wait for the front end to choose the next method
     */
    private static void waitForFrontEnd()
    {
        Server.frontEndLock.lock();
        try
        {
            if (!frontEndGoAhead)
            {
                Server.frontEndLockSync.await();
            }
            frontEndGoAhead = false;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Server.frontEndLock.unlock();
        }
    }

    /**
     * Wait for the front end to choose the next method
     */
    private static void PredNextTurn() throws InterruptedException
    {
        lockTurn.lock();
        try
        {
            for (int j = 0; j < amountOfRobots; j++)
            {
                nextTurn[j] = true;
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

    /**
     * Check if there are any predators still taking their turn
     */
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

    /**
     * Check if there are any agents still alive in the current system i.e not finished
     */
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
            if (preyThread != null && preyThread.isAlive())
            {
                canClose = false;
            }
        }
        return !canClose;
    }

    /**
     * Change the map from a 2d array of ints to the actual map.
     */
    private static void initialiseMap()
    {
        for (int[] mapContent : mapContents)
        {
            for (int j = 0; j < mapContents.length; j++)
            {
                if (mapContent[j] == 0)
                {
                    amountOfOccupiableSpaces++;
                }
            }
        }
        for (int i = 0; i < mapContents.length; i++)
        {
            for (int j = 0; j < mapContents.length; j++)
            {
                if (mapContents[i][j] == 0)
                {
                    map[i][j] = new Cell(i, j, mapContents[i][j], 1000.0 / (amountOfOccupiableSpaces));
                }
                else
                {
                    map[i][j] = new Cell(i, j, mapContents[i][j], 0);
                }
                if (mapContents[i][j] == 10)
                {
                    map[i][j] = new Cell(i, j, mapContents[i][j], 1000.0 / amountOfOccupiableSpaces);
                }
            }
        }
    }

    /**
     * Create the predators and add them to a list of threads
     */
    private static void createPredators()
    {
        robots = new outBoundConnection[amountOfRobots];
        for (int i = 0; i < amountOfRobots; i++)
        {
            nextTurn[i] = false;
            map[robotLocations[i][0]][robotLocations[i][1]].contents = i + 2;
            map[robotLocations[i][0]][robotLocations[i][1]].flagged = true;
            map[robotLocations[i][0]][robotLocations[i][1]].chanceOfOccupancy = 0;
            robots[i] = new outBoundConnection(1234 + i, i + 2, robotLocations[i][0], robotLocations[i][1], i);
        }
        robotsThread = new Thread[amountOfRobots];
        for (int i = 0; i < amountOfRobots; i++)
        {
            robotsThread[i] = new Thread(robots[i]);
        }
    }

    /**
     * Start the predators and prey threads
     */
    private static void startPredAndPrey()
    {
        for (int i = 0; i < amountOfRobots; i++)
        {
            robotsThread[i].start();
        }
        prey = new Prey(1238, Config.preyLocation);
        preyThread = new Thread(prey);
        preyThread.start();
    }

    /**
     * Tell the prey thread to take its turn.
     */
    private static void preyTurn()
    {
        Server.lockTurn.lock();
        try
        {
            Server.turnPrey.signalAll();
            if (!prey.finished)
            {
                Server.turnTakenPrey.await();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Server.lockTurn.unlock();
        }
    }
}
