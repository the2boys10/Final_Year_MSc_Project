import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class which acts as an interface for each Predator
 */
class outBoundConnection implements Runnable
{
    static final Lock lock = new ReentrantLock(true);
    /**
     * What cells have been scanned in the current episode
     */
    @SuppressWarnings("unchecked") private static final LinkedList<Cell>[] newObservations = new LinkedList[4];
    /**
     * How many cells are there around the prey that are currently empty.
     */
    static int amountSurround = 0;
    /**
     * How many Predators are waiting for the next group action
     */
    static int howManyWaiting = 0;
    /**
     * How many Predators are waiting for the next group action
     */
    static int amountLookingAtPrey = 0;
    /**
     * Whats the id of the predator used in the current interface.
     */
    final int ID;
    /**
     * Whats the identity of the current predator interface (id - 2)
     */
    final int identity;
    /**
     * Whats the port that we opened with the robot
     */
    private final int port;
    /**
     * Current action that is being carried out
     */
    public String currentAction;
    /**
     * The communications interface to the individual robot
     */
    Com inOut;
    /**
     * xCord of the predator
     */
    int xCord;
    /**
     * yCord of the predator
     */
    int yCord;
    /**
     * Has the system entered a state where the prey has been successfully captured.
     */
    boolean finished = false;
    /**
     * How many times has the robot turned during the current episode
     */
    int howManyTurns = 0;
    /**
     * How many times has the robot moved forward during the current episode
     */
    int howManyMoveForward = 0;
    /**
     * How many times has the robot skipped a turn during the current episode
     */
    int howManySkips = 0;
    /**
     * Should the robot skip the next command sent by the robot.
     */
    boolean skipRead = false;

    /**
     * Object containing the information required to set up a connection as well as information about the robot itself
     * @param port The port the robot is going to connect through
     * @param ID The ID of the robot connecting
     * @param xCord The xCord of the robot.
     * @param yCord The yCord of the robot.
     * @param identity The identity of the robot.
     */
    public outBoundConnection(int port, int ID, int xCord, int yCord, int identity)
    {
        this.port = port;
        this.xCord = xCord;
        this.yCord = yCord;
        this.ID = ID;
        this.identity = identity;
        Server.orientations[identity] = 'N';
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * Method that launches the current robots connection, deals with communication with each robot, as well as updating the blackboards belief state.
     */
    public void run()
    {
        try
        {

            inOut = new Com(port, this);
            Server.robotsConnected.incrementAndGet();
            currentAction = "Connected";
            FrontEnd.updateInfo(identity, xCord, yCord, currentAction, howManyTurns, howManyMoveForward, howManySkips);
            inOut.sendInt(ID, "Here's your ID");
            inOut.sendObject(Server.map, "Here's the map");
            inOut.sendObject(new int[]{xCord, yCord}, "Here's your location");
            inOut.sendString("nextTurn", "please start");
            if (Config.rapidTesting)
            {
                synchronized (Server.waitForAllWaiting)
                {
                    Server.howManyWait++;
                    if (Server.howManyWait == 5)
                    {
                        SwingUtilities.invokeAndWait((() -> FrontEnd.setContinuousButton.doClick()));
                    }
                }
            }
            waitForNextTurn();
            String str;
            synchronized (Server.waitForScan)
            {
                inOut.sendString("firstTurn", "please start");
                inOut.sendInt(Config.PredPolicy, "sending policy");
                inOut.sendInt(Config.PredSearch, "sending policy");
                inOut.sendInt(Config.PredScanning, "sending scan policy");
                inOut.sendInt(Config.PredScanPolicy, "can pred scan all axis?");
                str = inOut.readString("reading command");
                skipRead = true;
            }
            while (!finished)
            {
                if (!skipRead)
                {
                    str = inOut.readString("Reading command");
                }
                else
                {
                    skipRead = false;
                }
                switch (str)
                {
                    case "Found":
                        Found.checkFound(this);
                        break;
                    case "IsFound":
                        Found.isFound(this);
                        break;
                    case "isCaptured":
                        Found.isCaptured(this);
                        break;
                    case "scanForward":
                        ScanForwardSim.scanForward(this);
                        break;
                    case "updateMap":
                        newObservations[identity] = (LinkedList<Cell>) inOut.readObj("Reading updates");
                        break;
                    case "finishedStartScanning":
                        MapUpdater.waitForMapUpdate(newObservations);
                        inOut.sendString("Continue", "You can continue after scanningStart");
                        break;
                    case "readMap":
                        inOut.sendObject(Server.map, "Sending map");
                        break;
                    case "planMove":
                        Negotiator.planMove(this);
                        break;
                    case "takeMove":
                        str = TakeMove.takeMove(this);
                        break;
                    case "finishedEndScanning":
                        currentAction = "Waiting";
                        FrontEnd.updateInfo(identity, xCord, yCord, currentAction, howManyTurns, howManyMoveForward,
                                howManySkips);
                        MapUpdater.waitForMapUpdate(newObservations);
                        waitForNextTurn();
                        synchronized (Server.waitForScan)
                        {
                            inOut.sendString("Continue", "You can continue with next move");
                            str = inOut.readString("Reading next command");
                            skipRead = true;
                        }
                        break;
                    case "scanClean":
                        currentAction = "Waiting";
                        FrontEnd.updateInfo(identity, xCord, yCord, currentAction, howManyTurns, howManyMoveForward,
                                howManySkips);
                        synchronized (Server.waitForScan)
                        {
                            inOut.sendString("Continue", "You can continue with next move");
                            str = inOut.readString("Reading next command");
                            skipRead = true;
                        }
                        break;
                    case "exit":
                        finished = true;
                        break;
                }
            }
            inOut.cleanUp();
            waitForNextTurn();
            currentAction = "Finished";
            FrontEnd.updateInfo(identity, xCord, yCord, currentAction, howManyTurns, howManyMoveForward, howManySkips);
        }
        catch (IOException | InvocationTargetException | ClassNotFoundException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Deals with synchronization between threads when waiting for the next turn.
     */
    private void waitForNextTurn()
    {
        Server.lockTurn.lock();
        try
        {
            Server.nextTurn[identity] = false;
            Server.turnTaken.signalAll();
            while (!Server.nextTurn[identity])
            {
                Server.PredTurn.await();
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
