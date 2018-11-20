import java.util.LinkedList;

/**
 * Class which deals with all map updates relating from prey dissipation as well
 * as predator discoveries
 *
 * @author Robert Johnson
 */
class MapUpdater
{
    // amount of predators waiting for map update, used for synchronisation
    private static int amountWaitingForMapUpdate = 0;

    /**
     * Method updates the map based on scans made by predators
     *
     * @param newObservations Observations made by the predators
     */
    private static void updateMapPredatorViewings(LinkedList<Cell>[] newObservations)
    {
        // for the all the agents, get the first scan and set centercells to be false as well as other flagged values
        for (LinkedList<Cell> newObservation : newObservations)
        {
            if (newObservation != null && newObservation.size() > 0)
            {
                Cell currentCell = newObservation.getFirst();
                Server.map[currentCell.xCord][currentCell.yCord].flagged = true;
                Server.map[currentCell.xCord][currentCell.yCord].chanceOfOccupancy = 0;
                Server.map[currentCell.xCord][currentCell.yCord].possiblyPrey = false;
                if (Server.map[currentCell.xCord][currentCell.yCord].centerCell)
                {
                    Server.map[currentCell.xCord][currentCell.yCord].centerCell = false;
                }
            }
        }
        // for the rest don't effect center cell as we want to be 100% sure it has been scanned.
        for (LinkedList<Cell> newObservation : newObservations)
        {
            while (newObservation != null && newObservation.size() > 0)
            {
                Cell currentCell = newObservation.removeFirst();
                Server.map[currentCell.xCord][currentCell.yCord].flagged = true;
                Server.map[currentCell.xCord][currentCell.yCord].chanceOfOccupancy = 0;
                if (!Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1]].centerCell)
                {
                    Server.map[currentCell.xCord][currentCell.yCord].possiblyPrey = false;
                }
            }
        }
        // count how much occupancychance is still on the boad and distribute it evenly
        double totalCountOccupancy = 0;
        for (int i = 0; i < Server.map.length; i++)
        {
            for (int j = 0; j < Server.map[i].length; j++)
            {
                if (Server.map[i][j].chanceOfOccupancy > 0)
                {
                    totalCountOccupancy += Server.map[i][j].chanceOfOccupancy;
                }
            }
        }
        double currentMax = 0;
        if (totalCountOccupancy > 0)
        {
            totalCountOccupancy = 1000.0 / totalCountOccupancy;
            for (int i = 0; i < Server.map.length; i++)
            {
                for (int j = 0; j < Server.map[i].length; j++)
                {
                    if (Server.map[i][j].chanceOfOccupancy > 10)
                    {
                        Server.map[i][j].chanceOfOccupancy = Server.map[i][j].chanceOfOccupancy * totalCountOccupancy;
                    }
                    else
                    {
                        Server.map[i][j].flagged = true;
                    }
                    if (Server.map[i][j].chanceOfOccupancy > currentMax)
                    {
                        currentMax = Server.map[i][j].chanceOfOccupancy;
                    }
                }
            }
            // set cells within a bound to be unsearched
            for (int i = 0; i < Server.map.length; i++)
            {
                for (int j = 0; j < Server.map[i].length; j++)
                {
                    if ((Server.map[i][j].contents == 0 || Server.map[i][j].contents == 10) && Server.map[i][j].chanceOfOccupancy > Math.max(currentMax - 20, 0))
                    {
                        Server.map[i][j].flagged = false;
                    }
                }
            }
        }
        else
        {
            for (int i = 0; i < Server.map.length; i++)
            {
                for (int j = 0; j < Server.map.length; j++)
                {
                    if (Server.map[i][j].contents == 0 || Server.map[i][j].contents == 10)
                    {
                        Server.map[i][j].chanceOfOccupancy = 1000 / Server.amountOfOccupiableSpaces;
                        Server.map[i][j].flagged = false;
                    }
                }
            }
        }
    }

    /**
     * Method which updates the map using either dissipation method or full update
     * method
     */
    static void waitForPreyMapUpdate()
    {
        // create a copy of occupancychances for the map
        double[][] occupancyVals = new double[Server.map.length][Server.map.length];
        for (int i = 0; i < Server.map.length; i++)
        {
            for (int j = 0; j < Server.map.length; j++)
            {
                occupancyVals[i][j] = Server.map[i][j].chanceOfOccupancy;
            }
        }
        // spread using a dissipation grid
        int howManyOcc = 0;
        for (int i = 0; i < Server.map.length; i++)
        {
            for (int j = 0; j < Server.map.length; j++)
            {
                int amountExits = 0;
                if ((Server.map[i][j].contents == 0 || Server.map[i][j].contents == 10) && occupancyVals[i][j] > 0)
                {
                    if (Config.dissipationGrid)
                    {
                        if (Server.map[i + 1][j].contents == 0 || Server.map[i + 1][j].contents == 10)
                        {
                            amountExits++;
                        }
                        if (Server.map[i - 1][j].contents == 0 || Server.map[i - 1][j].contents == 10)
                        {
                            amountExits++;
                        }
                        if (Server.map[i][j + 1].contents == 0 || Server.map[i][j + 1].contents == 10)
                        {
                            amountExits++;
                        }
                        if (Server.map[i][j - 1].contents == 0 || Server.map[i][j - 1].contents == 10)
                        {
                            amountExits++;
                        }
                        switch (Config.PreyPolicy)
                        {
                            case 4:
                            case 3:
                                if (Server.map[i + 1][j].contents == 0 || Server.map[i + 1][j].contents == 10)
                                {
                                    Server.map[i + 1][j].chanceOfOccupancy += occupancyVals[i][j] / amountExits;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / amountExits;
                                }
                                if (Server.map[i - 1][j].contents == 0 || Server.map[i - 1][j].contents == 10)
                                {
                                    Server.map[i - 1][j].chanceOfOccupancy += occupancyVals[i][j] / amountExits;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / amountExits;
                                }
                                if (Server.map[i][j + 1].contents == 0 || Server.map[i][j + 1].contents == 10)
                                {
                                    Server.map[i][j + 1].chanceOfOccupancy += occupancyVals[i][j] / amountExits;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / amountExits;
                                }
                                if (Server.map[i][j - 1].contents == 0 || Server.map[i][j - 1].contents == 10)
                                {
                                    Server.map[i][j - 1].chanceOfOccupancy += occupancyVals[i][j] / amountExits;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / amountExits;
                                }
                                break;
                            case 2:
                                if (Server.map[i + 1][j].contents == 0 || Server.map[i + 1][j].contents == 10)
                                {
                                    Server.map[i + 1][j].chanceOfOccupancy += occupancyVals[i][j] / (amountExits + 1);
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / (amountExits + 1);
                                }
                                if (Server.map[i - 1][j].contents == 0 || Server.map[i - 1][j].contents == 10)
                                {
                                    Server.map[i - 1][j].chanceOfOccupancy += occupancyVals[i][j] / (amountExits + 1);
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / (amountExits + 1);
                                }
                                if (Server.map[i][j + 1].contents == 0 || Server.map[i][j + 1].contents == 10)
                                {
                                    Server.map[i][j + 1].chanceOfOccupancy += occupancyVals[i][j] / (amountExits + 1);
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / (amountExits + 1);
                                }
                                if (Server.map[i][j - 1].contents == 0 || Server.map[i][j - 1].contents == 10)
                                {
                                    Server.map[i][j - 1].chanceOfOccupancy += occupancyVals[i][j] / (amountExits + 1);
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / (amountExits + 1);
                                }
                                break;
                            case 1:
                                if (Server.map[i + 1][j].contents == 0 || Server.map[i + 1][j].contents == 10)
                                {
                                    Server.map[i + 1][j].chanceOfOccupancy += occupancyVals[i][j] / 4;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / 4;
                                }
                                if (Server.map[i - 1][j].contents == 0 || Server.map[i - 1][j].contents == 10)
                                {
                                    Server.map[i - 1][j].chanceOfOccupancy += occupancyVals[i][j] / 4;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / 4;
                                }
                                if (Server.map[i][j + 1].contents == 0 || Server.map[i][j + 1].contents == 10)
                                {
                                    Server.map[i][j + 1].chanceOfOccupancy += occupancyVals[i][j] / 4;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / 4;
                                }
                                if (Server.map[i][j - 1].contents == 0 || Server.map[i][j - 1].contents == 10)
                                {
                                    Server.map[i][j - 1].chanceOfOccupancy += occupancyVals[i][j] / 4;
                                    Server.map[i][j].chanceOfOccupancy -= occupancyVals[i][j] / 4;
                                }
                                break;
                        }
                    }
                    // add one to the amount of cells which have a occupancy chance greater than 0
                    howManyOcc++;
                }
            }
        }
        // if there are no cells which have an occupancy chance greater than 0 then refresh the map completely
        if (howManyOcc == 0)
        {
            for (int i = 0; i < Server.map.length; i++)
            {
                for (int j = 0; j < Server.map.length; j++)
                {
                    if (Server.map[i][j].contents == 0 || Server.map[i][j].contents == 10)
                    {
                        Server.map[i][j].chanceOfOccupancy = 1000 / Server.amountOfOccupiableSpaces;
                        Server.map[i][j].flagged = false;
                    }
                }
            }
        }
    }

    /**
     * Method used for synchronisation between multiple predators when map is being
     * updated.
     *
     * @param newObservations Observations made by the predators
     */
    public static void waitForMapUpdate(LinkedList<Cell>[] newObservations)
    {
        Server.lockTurn.lock();
        try
        {
            amountWaitingForMapUpdate++;
            if (amountWaitingForMapUpdate == Server.amountOfRobots)
            {
                MapUpdater.updateMapPredatorViewings(newObservations);
                Server.mapUpdateFinished.signalAll();
                amountWaitingForMapUpdate = 0;
            }
            else
            {
                Server.mapUpdateFinished.await();
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
