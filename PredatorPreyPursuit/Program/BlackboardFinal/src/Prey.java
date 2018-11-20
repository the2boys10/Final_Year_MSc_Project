import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class which deals as an interface with the prey deals with communication to and from the prey.
 */
class Prey implements Runnable
{
    public static final Lock preyNextChoice = new ReentrantLock(true);
    public static final Condition waitForChoice = preyNextChoice.newCondition();
    /**
     * What is the next message we would like to send the prey (Used to directly control the prey for game purposes)
     */
    public static String message = "None";
    /**
     * What is the port that we want to open for the prey
     */
    private final int port;
    /**
     * Communications interface for the prey
     */
    public ComPrey inOut;
    /**
     * xCord of the prey
     */
    public int xCord;
    /**
     * yCord of the prey
     */
    public int yCord;
    /**
     * Has the prey finished trying to escape.
     */
    boolean finished = false;

    /**
     * Constructor used to setup the port and location of the prey.
     * @param port The port the prey is intending to connect through
     * @param preyLocation The location of the prey.
     */
    public Prey(int port, int[] preyLocation)
    {
        this.port = port;
        xCord = preyLocation[0];
        yCord = preyLocation[1];
    }

    /**
     * Method used to start the communication with prey and deal with communication messages to and from prey.
     */
    public void run()
    {
        try
        {
            inOut = new ComPrey(port);
            Server.robotsConnected.incrementAndGet();
            FrontEnd.updatePreyInfo("Connected");
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
            waitForNextRound();
            inOut.sendString("nextTurn", "please start");
            inOut.sendInt(Config.PreyPolicy, "send Prey policy");
            inOut.sendInt(Config.PreyScanning, "send scanning policy");
            inOut.sendString("takeNext", "takeYourNextTurn");
            if (Config.PreyPolicy < 4)
            {
                while (!finished)
                {
                    String str = inOut.readString("Reading command");
                    switch (str)
                    {
                        case "exit":
                            finished = true;
                            break;
                        case "nextMove":
                            inOut.sendBool(true, "Tell can move");
                            String preyNext = inOut.readString("What is it doing");
                            if (preyNext.equals("moved"))
                            {
                                final char orientation = inOut.readChar("what way?");
                                boolean successful = inOut.readBool("success?");
                                if (successful)
                                {
                                    if (xCord != -1)
                                    {
                                        if (orientation == 'N' && Server.map[xCord - 1][yCord].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            xCord--;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else if (orientation == 'E' && Server.map[xCord][yCord + 1].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            yCord++;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else if (orientation == 'S' && Server.map[xCord + 1][yCord].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            xCord++;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else if (orientation == 'W' && Server.map[xCord][yCord - 1].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            yCord--;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            xCord = -1;
                                            yCord = -1;
                                        }
                                    }
                                }
                                FrontEnd.updatePreyInfo("Moving " + orientation);
                                FrontEnd.AddToTextFrame("\nPrey moving " + orientation);
                            }
                            else
                            {
                                FrontEnd.updatePreyInfo("Skipped");
                            }
                            inOut.readBool("finished");
                            FrontEnd.updatePreyInfo("Waiting");
                            waitForNextRound();
                            inOut.sendString("takeNext", "takeYourNextTurn");
                            break;
                        case "scan":
                            if (xCord != -1)
                            {
                                ScanForwardSim.scanForward(this);
                            }
                            else
                            {
                                inOut.readChar("reading orientation");
                                inOut.sendInt(1, "Distance");
                            }
                            break;
                    }
                }
            }
            else
            {
                inOut.sendString("start", "starting");
                waitForChoice();
                inOut.sendString(message, "sending choice");
                while (!finished)
                {
                    String str = inOut.readString("Reading command");
                    switch (str)
                    {
                        case "Failed":
                            waitForChoice();
                            inOut.sendString(message, "sending choice");
                            break;
                        case "FinTurn":
                            waitForNextRound();
                            waitForChoice();
                            inOut.sendString(message, "sending choice");
                            break;
                        case "nextMove":
                            inOut.sendBool(true, "Tell can move");
                            String preyNext = inOut.readString("What is it doing");
                            if (preyNext.equals("moved"))
                            {
                                final char orientation = inOut.readChar("what way?");
                                boolean successful = inOut.readBool("success?");
                                if (successful)
                                {
                                    if (xCord != -1)
                                    {
                                        if (orientation == 'N' && Server.map[xCord - 1][yCord].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            xCord--;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else if (orientation == 'E' && Server.map[xCord][yCord + 1].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            yCord++;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else if (orientation == 'S' && Server.map[xCord + 1][yCord].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            xCord++;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else if (orientation == 'W' && Server.map[xCord][yCord - 1].contents == 0)
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            yCord--;
                                            Server.map[xCord][yCord].contents = 10;
                                        }
                                        else
                                        {
                                            Server.map[xCord][yCord].contents = 0;
                                            xCord = -1;
                                            yCord = -1;
                                        }
                                    }
                                }
                                FrontEnd.updatePreyInfo("Moving " + orientation);
                                FrontEnd.AddToTextFrame("\nPrey moving " + orientation);
                            }
                            break;
                        case "exit":
                            finished = true;
                            break;
                        case "scan":
                            if (xCord != -1)
                            {
                                ScanForwardSim.scanForward(this);
                            }
                            else
                            {
                                inOut.readChar("reading orientation");
                                inOut.sendInt(1, "Distance");
                            }
                            break;
                    }
                }

            }
            FrontEnd.updatePreyInfo("Finished");
            waitForNextRound();
            inOut.cleanUp();
        }
        catch (IOException | InvocationTargetException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Method used to wait for the user to choose its next move (When he is directly controlling the agent.)
     */
    private void waitForChoice() throws InvocationTargetException
    {
        preyNextChoice.lock();
        try
        {
            FrontEnd.turnOnMoveButtonsPrey();
            waitForChoice.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            preyNextChoice.unlock();
        }
    }

    /**
     * Method used to wait for the next prey round.
     */
    private void waitForNextRound()
    {
        Server.lockTurn.lock();
        try
        {
            Server.turnTakenPrey.signalAll();
            Server.turnPrey.await();
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
