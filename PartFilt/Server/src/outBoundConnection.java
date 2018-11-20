import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Random;

class outBoundConnection implements Runnable
{

    public final int identity;
    private final int port;
    private final int ID;
    public Com inOut;
    public int xCord;
    public int yCord;
    public boolean finished = false;
    private int howManyTurns = 0;
    private int howManyMoveForward = 0;
    private int howManySkips = 0;

    public outBoundConnection(int port, int ID, int xCord, int yCord, int identity)
    {
        this.port = port;
        this.xCord = xCord;
        this.yCord = yCord;
        this.ID = ID;
        this.identity = identity;
        Server.orientations[identity] = 'N';
    }

    public void run()
    {
        try
        {
            inOut = new Com(port);
            synchronized (Server.lockTurn)
            {
                Server.robotsConnected++;
            }
            FrontEnd2.updateInfo(identity, xCord, yCord, "Connected", howManyTurns, howManyMoveForward, howManySkips);
            System.out.println("Robot " + ID + " Ready");
            inOut.sendInt(ID);
            Random rand = new Random();
            int orientationVal = rand.nextInt(4);
                switch (orientationVal)
                {
                    case 0:
                        inOut.sendChar('N');
                        System.out.println(identity + " , N");
                        break;
                    case 1:
                        inOut.sendChar('E');
                        System.out.println(identity + " , E");
                        break;
                    case 2:
                        inOut.sendChar('S');
                        System.out.println(identity + " , S");
                        break;
                    case 3:
                        inOut.sendChar('W');
                        System.out.println(identity + " , W");
                        break;
                    default:
                        inOut.sendChar('N');
                        break;
                }
            inOut.sendObject(Server.map);
            inOut.sendString("nextTurn");
            waitForNextTurn();
            inOut.sendString("firstTurn");
            while (!finished)
            {
                String str = inOut.readString();
                switch (str)
                {
                    case "whereIThink":
                        final Cell[][] currentMap = (Cell[][]) inOut.readObj();
                        for (int i = 0; i < currentMap.length; i++)
                        {
                            for (int j = 0; j < currentMap.length; j++)
                            {
                                Server.map[i][j].chanceOfTemp[ID - 2] = currentMap[i][j].chanceOfTemp[ID - 2];
                                Server.map[i][j].chanceOfSelf[ID - 2] = currentMap[i][j].chanceOfSelf[ID - 2];
                                Server.map[i][j].chanceOfSelfPercent[ID - 2] = currentMap[i][j].chanceOfSelfPercent[ID - 2];
                            }
                        }
                        FrontEnd2.updateValues(currentMap, identity, ID);
                        break;
                    case "exit?":
                        if (Server.stop)
                        {
                            inOut.sendBool(true);
                            finished = true;
                        }
                        else
                        {
                            inOut.sendBool(false);
                        }
                        break;
                    case "scanForward":
                        inOut.sendInt(ScanForwardSim.scanForward(this));
                        break;
                    case "readMap":
                        inOut.sendObject(Server.map);
                        break;
                    case "takeMove":
                        inOut.sendString("takeMove");
                        str = inOut.readString();
                        switch (str)
                        {
                            case "Turn":
                                howManyTurns++;
                                FrontEnd2.updateInfo(identity, xCord, yCord, "Connected", howManyTurns, howManyMoveForward, howManySkips);
                                inOut.readBool();
                                break;
                            case "makeMove":
                                howManyMoveForward++;
                                FrontEnd2.updateInfo(identity, xCord, yCord, "Connected", howManyTurns, howManyMoveForward, howManySkips);
                                char whereTo = inOut.readChar();
                                boolean success = inOut.readBool();
                                if(Config.Simulated)
                                {
                                    int tempX = xCord;
                                    int tempY = yCord;
                                    switch (whereTo)
                                    {
                                        case 'N':
                                            tempX--;
                                            break;
                                        case 'E':
                                            tempY++;
                                            break;
                                        case 'S':
                                            tempX++;
                                            break;
                                        default:
                                            tempY--;
                                            break;
                                    }
                                    Server.orientations[identity] = whereTo;
                                    if (success)
                                    {
                                        Server.map[xCord][yCord].contents = 0;
                                        Server.map[tempX][tempY].contents = ID;
                                        xCord = tempX;
                                        yCord = tempY;
                                    }
                                }
                                break;
                            case "skip":
                                howManySkips++;
                                FrontEnd2.updateInfo(identity, xCord, yCord, "Connected", howManyTurns, howManyMoveForward, howManySkips);
                                break;
                        }
                        FrontEnd2.updateInfo(identity, xCord, yCord, "Connected", howManyTurns, howManyMoveForward, howManySkips);
                        FrontEnd2.updateInfo(identity, xCord, yCord, "Connected", howManyTurns, howManyMoveForward, howManySkips);
                        break;
                    case "finishedEndScanning":
                        updateMap();
                        FrontEnd2.updateInfo(identity, xCord, yCord, "Connected", howManyTurns, howManyMoveForward, howManySkips);
                        waitForNextTurn();
                        inOut.sendString("Continue");
                        break;
                }
            }
            Server.lockTurn.lock();
            try
            {
                Server.nextTurn[identity] = false;
                Server.turnTaken.signalAll();
            }
            finally
            {
                Server.lockTurn.unlock();
            }
            inOut.cleanUp();
            System.out.println(identity + " finished");
        }
        catch (IOException | InterruptedException | InvocationTargetException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }


    private void updateMap()
    {
        for (int i = 0; i < Server.map.length; i++)
        {
            for (int j = 0; j < Server.map.length; j++)
            {
                int howMany = 0;
                for (int k = 0; k < Server.map[i][j].chanceOfSelfPercent[0].length; k++)
                {
                    double maxVal = 0;
                    for (int p = 0; p < Server.map[i][j].chanceOfSelfPercent.length; p++)
                    {
                        if (Server.map[i][j].chanceOfSelfPercent[p][k] > maxVal)
                        {
                            maxVal = Server.map[i][j].chanceOfSelfPercent[p][k];
                        }
                        if (Server.map[i][j].chanceOfSelfPercent[p][k] > 0.02)
                        {
                            howMany++;
                        }
                        if (Server.map[Server.map.length - 1 - i][Server.map.length - j - 1].chanceOfSelfPercent[p][k] > 0.02)
                        {
                            howMany++;
                        }
                        if (Server.map[Server.map.length - j - 1][i].chanceOfSelfPercent[p][k] > 0.02)
                        {
                            howMany++;
                        }
                        if (Server.map[j][Server.map.length - 1 - i].chanceOfSelfPercent[p][k] > 0.02)
                        {
                            howMany++;
                        }
                    }
                    Server.map[i][j].chanceOfOccupancy[k] = maxVal;
                }
                for (int k = 0; k < Server.map[i][j].chanceOfSelfPercent[0].length; k++)
                {
                    Server.map[i][j].sharedCell[k] = howMany > 1;
                }
            }
        }
    }

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
