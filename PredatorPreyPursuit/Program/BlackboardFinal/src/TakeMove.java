import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class which controls and updates the next move taken by the predators
 */
class TakeMove
{
    /**
     * Method takes the predator and updates its local variables in relation with the messages sent by both the predators and prey.
     * @param superc The predator calling this method.
     * @return
     * @throws InvocationTargetException
     * @throws InterruptedException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static String takeMove(outBoundConnection superc) throws InvocationTargetException, InterruptedException, IOException, ClassNotFoundException
    {
        String temp;
        outBoundConnection.amountLookingAtPrey = 0;
        waitForAll();
        superc.inOut.sendString("takeMove", "You can take your move " + superc.identity);
        temp = superc.inOut.readString("Reading move to take");
        switch (temp)
        {
            case "Turn":
                superc.currentAction = "Turning";
                superc.howManyTurns++;
                FrontEnd.updateInfo(superc.identity, superc.xCord, superc.yCord, superc.currentAction,
                        superc.howManyTurns, superc.howManyMoveForward, superc.howManySkips);
                superc.inOut.readBool("Have you finished");
                break;
            case "makeMove":
                superc.currentAction = "Forward";
                superc.howManyMoveForward++;
                FrontEnd.updateInfo(superc.identity, superc.xCord, superc.yCord, superc.currentAction,
                        superc.howManyTurns, superc.howManyMoveForward, superc.howManySkips);
                int[] whereTo = (int[]) superc.inOut.readObj("where are you moving to?");
                boolean success = superc.inOut.readBool("Was success?");
                if (success)
                {
                    Server.map[superc.xCord][superc.yCord].contents = 0;
                    Server.map[whereTo[0]][whereTo[1]].contents = superc.ID;
                    Server.map[whereTo[0]][whereTo[1]].flagged = true;
                    Server.map[whereTo[0]][whereTo[1]].possiblyPrey = false;
                    Server.map[whereTo[0]][whereTo[1]].centerCell = false;
                    Server.map[whereTo[0]][whereTo[1]].chanceOfOccupancy = 0;
                    superc.xCord = whereTo[0];
                    superc.yCord = whereTo[1];
                }
                break;
            case "skip":
                superc.currentAction = "Skipping";
                superc.howManySkips++;
                FrontEnd.updateInfo(superc.identity, superc.xCord, superc.yCord, superc.currentAction,
                        superc.howManyTurns, superc.howManyMoveForward, superc.howManySkips);
                break;
        }
        waitForAll();
        superc.currentAction = "Waiting";
        FrontEnd.updateInfo(superc.identity, superc.xCord, superc.yCord, superc.currentAction, superc.howManyTurns, superc.howManyMoveForward, superc.howManySkips);
        synchronized (Server.waitForScan)
        {
            superc.inOut.sendString("All moved", "everyone moved");
            superc.currentAction = "Scanning";
            FrontEnd.updateInfo(superc.identity, superc.xCord, superc.yCord, superc.currentAction, superc.howManyTurns, superc.howManyMoveForward, superc.howManySkips);
            temp = superc.inOut.readString("Reading next command");
            superc.skipRead = true;
        }
        return temp;
    }

    /**
     * Wait for all agents before starting to move anyone.
     * @throws InvocationTargetException
     */
    private static void waitForAll() throws InvocationTargetException
    {
        Server.lockTurn.lock();
        try
        {
            outBoundConnection.howManyWaiting++;
            if (outBoundConnection.howManyWaiting == Server.amountOfRobots)
            {
                FrontEnd.updateMapsAndInfo();
                Server.mapNegotiation.signalAll();
                outBoundConnection.howManyWaiting = 0;
            }
            else
            {
                Server.mapNegotiation.await();
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
