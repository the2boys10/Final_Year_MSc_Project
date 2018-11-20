import java.io.IOException;

/**
 * @author Robert Johnson Class that deals with predators that think they have
 * found a sign of prey
 */
class Found
{
    static final boolean[] whichAgentFound = new boolean[4];

    /**
     * @param superc the predator who thinks they have seen the prey
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void checkFound(outBoundConnection superc) throws ClassNotFoundException, IOException
    {
        // Get the location that the predator thinks it saw the prey within
        int[] locationWeThink = (int[]) superc.inOut.readObj("location");
        // check that the contents of the cell is empty
        if (Server.map[locationWeThink[0]][locationWeThink[1]].contents == 0 || Server.map[locationWeThink[0]][locationWeThink[1]].contents == 10)
        {
            // tell the agent they have found something
            superc.inOut.sendBool(true, "you have found something");
            // lock and update the prey's location
            synchronized (outBoundConnection.lock)
            {
                // if someone has seen the prey this turn and it is the same cell as this agent then increase the chance of occupancy
                if (outBoundConnection.amountLookingAtPrey != 0 &&
                        Server.map[locationWeThink[0]][locationWeThink[1]].centerCell && !whichAgentFound[superc.identity])
                {
                    outBoundConnection.amountLookingAtPrey++;
                    whichAgentFound[superc.identity] = true;
                    if (Config.dissipationGrid)
                    {
                        for (int i = 0; i < Server.map.length; i++)
                        {
                            for (int j = 0; j < Server.map.length; j++)
                            {
                                Server.map[i][j].chanceOfOccupancy = 0;
                                Server.map[i][j].flagged = true;
                            }
                        }
                        Server.map[locationWeThink[0]][locationWeThink[1]].chanceOfOccupancy = 1000;
                        Server.map[locationWeThink[0]][locationWeThink[1]].flagged = false;
                    }
                }
                // else if no one has seen the prey this round then update the map  and only increase the chance of occupancy slightly.
                else if (outBoundConnection.amountLookingAtPrey == 0)
                {
                    for (int i = 0; i < Server.map.length; i++)
                    {
                        for (int j = 0; j < Server.map.length; j++)
                        {
                            Server.map[i][j].possiblyPrey = false;
                            Server.map[i][j].centerCell = false;
                        }
                    }
                    Server.map[locationWeThink[0]][locationWeThink[1]].chanceOfOccupancy += 10;
                    Server.map[locationWeThink[0]][locationWeThink[1]].chanceOfOccupancy *= 4;
                    Server.map[locationWeThink[0]][locationWeThink[1]].possiblyPrey = true;
                    Server.map[locationWeThink[0]][locationWeThink[1]].centerCell = true;
                    setPossiblyPrey(locationWeThink[0] + 1, locationWeThink[1]);
                    setPossiblyPrey(locationWeThink[0] - 1, locationWeThink[1]);
                    setPossiblyPrey(locationWeThink[0], locationWeThink[1] + 1);
                    setPossiblyPrey(locationWeThink[0], locationWeThink[1] - 1);
                    Server.lastKnownLocation[0] = locationWeThink[0];
                    Server.lastKnownLocation[1] = locationWeThink[1];
                    outBoundConnection.amountLookingAtPrey++;
                    whichAgentFound[superc.identity] = true;
                }
                else if (Server.map[locationWeThink[0]][locationWeThink[1]].centerCell)
                {
                    setPossiblyPrey(locationWeThink[0] + 1, locationWeThink[1]);
                    setPossiblyPrey(locationWeThink[0] - 1, locationWeThink[1]);
                    setPossiblyPrey(locationWeThink[0], locationWeThink[1] + 1);
                    setPossiblyPrey(locationWeThink[0], locationWeThink[1] - 1);
                }
            }
        }
        else
        {
            superc.inOut.sendBool(false, "you have not found anything");
        }
    }

    /**
     * @param xValue The xCord of the cell to set as a possibly prey
     * @param yValue The yCord of the cell to set as a possibly prey
     */
    private static void setPossiblyPrey(int xValue, int yValue)
    {
        if (Server.map[xValue][yValue].contents == 0)
        {
            Server.map[xValue][yValue].possiblyPrey = true;
        }
        if (Server.map[xValue][yValue].contents != 1)
        {
            outBoundConnection.amountSurround++;
        }
    }

    /**
     * Checks if the prey has been seen in the current round or any time previously.
     *
     * @param superc The predator that thinks it has found the prey
     * @throws IOException
     */
    public static void isFound(outBoundConnection superc) throws IOException
    {
        if (Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1]].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0] + 1][Server.lastKnownLocation[1]].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0] - 1][Server.lastKnownLocation[1]].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] + 1].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] - 1].possiblyPrey)
        {
            superc.inOut.sendBool(true, "The prey has been seen");
        }
        else
        {
            superc.inOut.sendBool(false, "The prey has not been seen");
        }
    }

    /**
     * Checks if the prey has been captured using both the amount of predators that
     * have stated that they have seen the prey in the location last seen
     *
     * @param superc The predator that thinks it has found the prey
     * @throws IOException
     */
    public static void isCaptured(outBoundConnection superc) throws IOException
    {
        // if the cells around the prey are occupied by other predators and all are facing it and have seen it this turn reply that the prey has been captured.
        if (Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1]].possiblyPrey && Server.map[Server.lastKnownLocation[0] + 1][Server.lastKnownLocation[1]].contents != 0 && Server.map[Server.lastKnownLocation[0] + 1][Server.lastKnownLocation[1]].contents != 10 &&
                Server.map[Server.lastKnownLocation[0] - 1][Server.lastKnownLocation[1]].contents != 0 && Server.map[Server.lastKnownLocation[0] - 1][Server.lastKnownLocation[1]].contents != 10
                && Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] + 1].contents != 0 && Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] + 1].contents != 10 && Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] - 1].contents != 0 &&
                Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] - 1].contents != 10 && outBoundConnection.amountSurround == outBoundConnection.amountLookingAtPrey)
        {
            superc.inOut.sendBool(true, "Prey has been captured");
        }
        else
        {
            superc.inOut.sendBool(false, "Prey has not been captured");
        }
    }
}
