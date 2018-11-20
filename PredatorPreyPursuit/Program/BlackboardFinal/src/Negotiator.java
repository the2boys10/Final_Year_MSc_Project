import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

/**
 * Class which deals with negotiation between agents including collision
 * detection.
 */
class Negotiator
{
    // Used to store the best found policy for each agent so far
    private static final int[] bestFound = new int[Server.amountOfRobots];
    // Used to store the current permutation policy currently being explored
    private static final int[] currentPerm = new int[Server.amountOfRobots];
    // bestCost stores the most maximal set value for example if two agents can move
    // it would store 2.
    private static double bestCost = 0;
    // bestFoundInstant stores the best found instant reward for each agent.
    private static double bestFoundInstant = 0;
    // currentBestFoundInstant stores the current permutations value for the
    // InstantValue
    private static double currentBestFoundInstant = 0;
    private static double currentAlternativeDirection = 0;
    private static double maxAlternativeDirection = 0;
    // currentBestSet stores the current size of the permutation found.
    private static double currentBestSet = 0;
    // amount of agents waiting for negotiation.
    private static int amountWaitingForMapUpdate = 0;

    /**
     * @param superc the predator that is wanting his move to be planned makes calls
     *               for negotiation between multiple agents to be made.
     * @throws InvocationTargetException
     * @throws IOException
     */
    public static void planMove(outBoundConnection superc) throws InvocationTargetException, IOException
    {
        Server.possibleMoves[superc.identity] = superc.inOut.readInt("Reading best moves");
        Server.orientations[superc.identity] = superc.inOut.readChar("reading orientation");
        waitForNegotiation(superc);
        superc.inOut.sendInt(Server.moveToTake[superc.identity], "take this move" + Server.moveToTake[superc.identity]);
        switch (Server.moveToTake[superc.identity])
        {
            case 1:
                Server.orientations[superc.identity] = 'N';
                break;
            case 2:
                Server.orientations[superc.identity] = 'E';
                break;
            case 4:
                Server.orientations[superc.identity] = 'S';
                break;
            case 8:
                Server.orientations[superc.identity] = 'W';
                break;
        }
    }

    /**
     * Causes negotiation to happen adds all move sets for each agent to a LinkedList
     * and tries all possible permutations.
     *
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    private static void negotiate() throws InvocationTargetException, InterruptedException
    {
        // for each agent create a list of possiblemovements.
        @SuppressWarnings("Convert2Diamond") LinkedList<PossibleMovements> temp = new LinkedList<PossibleMovements>();
        for (int i = 0; i < bestFound.length; i++)
        {
            bestFound[i] = 0;
        }
        // take each movement and create objects describing these moves.
        for (int i = 0; i < Server.possibleMoves.length; i++)
        {
            int moves = Server.possibleMoves[i];
            if (notIn(1, moves))
            {
                if (Server.orientations[i] == 'N')
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord - 1][Server.robots[i].yCord], true, i, 1));
                }
                else
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord - 1][Server.robots[i].yCord], false, i, 1));
                }
            }
            if (notIn(2, moves))
            {
                if (Server.orientations[i] == 'E')
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord][Server.robots[i].yCord + 1], true, i, 2));
                }
                else
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord][Server.robots[i].yCord + 1], false, i, 2));
                }
            }
            if (notIn(4, moves))
            {
                if (Server.orientations[i] == 'S')
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord + 1][Server.robots[i].yCord], true, i, 4));
                }
                else
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord + 1][Server.robots[i].yCord], false, i, 4));
                }
            }
            if (notIn(8, moves))
            {
                if (Server.orientations[i] == 'W')
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord][Server.robots[i].yCord - 1], true, i, 8));
                }
                else
                {
                    temp.add(new PossibleMovements(Server.map[Server.robots[i].xCord][Server.robots[i].yCord - 1], false, i, 8));
                }
            }
        }
        // create a stringbuilder describing all agent move choices and output it to gui.
        StringBuilder whatToOutput = new StringBuilder(FrontEnd.textArea2.getText());
        for (int i = 0; i < Server.possibleMoves.length; i++)
        {
            whatToOutput.append(Integer.toBinaryString(Server.possibleMoves[i])).append("\n");
        }
        bestCost = 0;
        bestFoundInstant = 0;
        currentBestSet = 0;
        currentBestFoundInstant = 0;
        currentAlternativeDirection = 0;
        maxAlternativeDirection = 0;
        checkPerms(temp, 0);
        Server.moveToTake = bestFound;
        for (int i = 0; i < Server.possibleMoves.length; i++)
        {
            whatToOutput.append(Integer.toBinaryString(Server.moveToTake[i])).append("\n");
        }
        whatToOutput.append("\n");
        FrontEnd.clearTextFrame(whatToOutput.toString());
    }

    /**
     * /** Checks all possible permutations for the maximal set, recursive in nature
     * Also calls getReward to estimate the reward for making a certain move.
     *
     * @param temp  the possible moves that each agent can make
     * @param depth the current predator we are looking at.
     */
    private static void checkPerms(LinkedList<PossibleMovements> temp, int depth)
    {
        // for each possible movement
        for (int i = 0; i < temp.size(); i++)
        {
            double tempBefore = currentBestFoundInstant;
            boolean occupied = false;
            boolean occupied2 = false;
            boolean occupied3 = false;
            // if the agent we are trying to take moves for is the current item
            if (depth == temp.get(i).identity)
            {
                // if it wants to move into a center cell and it is not facing it
                if (temp.get(i).moveTo.centerCell && !temp.get(i).facingIt)
                {
                    occupied3 = true;
                    currentBestSet++;
                    currentPerm[depth] = temp.get(i).move;
                    if (Config.instantReward && Config.PredPolicy == 2)
                    {
                        currentBestFoundInstant += getReward(temp.get(i).move, Server.robots[temp.get(i).identity].xCord, Server.robots[temp.get(i).identity].yCord, temp.get(i).identity);
                    }
                }
                // if the agent wants to move into a cell which another agent also wants to move into and the agent is not facing it
                else if (temp.get(i).moveTo.goingToOccupy && !temp.get(i).facingIt)
                {
                    occupied2 = true;
                    currentBestSet += 0.5;
                    currentPerm[depth] = temp.get(i).move;
                    if (Config.instantReward && Config.PredPolicy == 2)
                    {
                        currentBestFoundInstant += getReward(temp.get(i).move, Server.robots[temp.get(i).identity].xCord, Server.robots[temp.get(i).identity].yCord, temp.get(i).identity);
                    }
                }
                // if the cell the agent wants to move into is not taken and it is empty
                else if (!temp.get(i).moveTo.goingToOccupy && (temp.get(i).moveTo.contents == 0 || temp.get(i).moveTo.contents == 10) && !temp.get(i).moveTo.centerCell)
                {
                    occupied = true;
                    currentBestSet++;
                    temp.get(i).moveTo.goingToOccupy = true;
                    currentPerm[depth] = temp.get(i).move;
                    if (Config.instantReward && Config.PredPolicy == 2)
                    {
                        currentBestFoundInstant += getReward(temp.get(i).move, temp.get(i).moveTo.xCord, temp.get(i).moveTo.yCord, temp.get(i).identity);
                    }
                }
                else
                {
                    // else try the empty move.
                    currentPerm[depth] = 0;
                }
                if (depth + 1 < Server.amountOfRobots)
                {
                    // recursive check.
                    checkPerms(temp, depth + 1);
                }
                else
                {
                    if (currentBestSet > bestCost || (currentBestSet == bestCost && currentBestFoundInstant > bestFoundInstant) || (currentBestSet == bestCost && currentBestFoundInstant == bestFoundInstant && currentAlternativeDirection > maxAlternativeDirection))
                    {
                        System.arraycopy(currentPerm, 0, bestFound, 0, bestFound.length);
                        bestCost = currentBestSet;
                        bestFoundInstant = currentBestFoundInstant;
                        maxAlternativeDirection = currentAlternativeDirection;
                    }
                    currentAlternativeDirection = 0;
                }
                if (occupied)
                {
                    temp.get(i).moveTo.goingToOccupy = false;
                    currentBestSet--;
                }
                if (occupied2)
                {
                    currentBestSet -= .5;
                }
                if (occupied3)
                {
                    currentBestSet--;
                }
            }
            currentBestFoundInstant = tempBefore;
        }
        double tempBefore = currentBestFoundInstant;
        currentPerm[depth] = 0;
        if (Config.instantReward && Config.PredPolicy == 2)
        {
            currentBestFoundInstant += getReward(0, Server.robots[depth].xCord, Server.robots[depth].yCord, depth);
        }
        if (depth + 1 < Server.amountOfRobots)
        {
            checkPerms(temp, depth + 1);
        }
        else
        {
            if (currentBestSet > bestCost || (currentBestSet == bestCost && currentBestFoundInstant > bestFoundInstant) || (currentBestSet == bestCost && currentBestFoundInstant == bestFoundInstant && currentAlternativeDirection > maxAlternativeDirection))
            {
                System.arraycopy(currentPerm, 0, bestFound, 0, bestFound.length);
                bestCost = currentBestSet;
                bestFoundInstant = currentBestFoundInstant;
                maxAlternativeDirection = currentAlternativeDirection;
            }
            currentAlternativeDirection = 0;
        }
        currentBestFoundInstant = tempBefore;
    }

    /**
     * @param move      the move that the current agent wishes to make
     * @param xCord2    the co-ordinate that the agent would scan from after making the
     *                  move
     * @param yCord2    the co-ordinate that the agent would scan from after making the
     *                  move
     * @param identity2 the agents ID
     * @return the reward from scanning in the direction after moving.
     */
    private static double getReward(int move, int xCord2, int yCord2, int identity2)
    {
        double reward = 0;
        // if there is still a prey on the board.
        if (Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1]].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0] + 1][Server.lastKnownLocation[1]].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0] - 1][Server.lastKnownLocation[1]].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] + 1].possiblyPrey ||
                Server.map[Server.lastKnownLocation[0]][Server.lastKnownLocation[1] - 1].possiblyPrey)
        {
            label1:
            for (int i = 1; i < 3; i++)
            {
                // if the move the agent wishes to take would result in the orientation what benefit would be gained by scanning two cells in front.
                switch (move)
                {
                    case 0:
                        switch (Server.orientations[identity2])
                        {
                            case 'N':
                                if (Server.map[xCord2 - i][yCord2].contents == 0 ||
                                        Server.map[xCord2 - i][yCord2].contents == 10)
                                {
                                    if (Server.map[xCord2 - i][yCord2].possiblyPrey)
                                    {
                                        reward += 1;
                                    }
                                }
                                else
                                {
                                    break label1;
                                }
                                break;
                            case 'E':
                                if (Server.map[xCord2][yCord2 + i].contents == 0 ||
                                        Server.map[xCord2][yCord2 + i].contents == 10)
                                {
                                    if (Server.map[xCord2][yCord2 + i].possiblyPrey)
                                    {
                                        reward += 1;
                                    }
                                }
                                else
                                {
                                    break label1;
                                }
                                break;
                            case 'S':
                                if (Server.map[xCord2 + i][yCord2].contents == 0 ||
                                        Server.map[xCord2 + i][yCord2].contents == 10)
                                {
                                    if (Server.map[xCord2 + i][yCord2].possiblyPrey)
                                    {
                                        reward += 1;
                                    }
                                }
                                else
                                {
                                    break label1;
                                }
                                break;
                            case 'W':
                                if (Server.map[xCord2][yCord2 - i].contents == 0 ||
                                        Server.map[xCord2][yCord2 - i].contents == 10)
                                {
                                    if (Server.map[xCord2][yCord2 - i].possiblyPrey)
                                    {
                                        reward += 1;
                                    }
                                }
                                else
                                {
                                    break label1;
                                }
                                break;
                        }
                        break;
                    case 1:
                        if (Server.map[xCord2 - i][yCord2].contents == 0 ||
                                Server.map[xCord2 - i][yCord2].contents == 10)
                        {
                            if (Server.map[xCord2 - i][yCord2].possiblyPrey)
                            {
                                reward += 1;
                            }
                        }
                        else
                        {
                            break label1;
                        }
                        break;
                    case 2:
                        if (Server.map[xCord2][yCord2 + i].contents == 0 ||
                                Server.map[xCord2][yCord2 + i].contents == 10)
                        {
                            if (Server.map[xCord2][yCord2 + i].possiblyPrey)
                            {
                                reward += 1;
                            }
                        }
                        else
                        {
                            break label1;
                        }
                        break;
                    case 4:
                        if (Server.map[xCord2 + i][yCord2].contents == 0 ||
                                Server.map[xCord2 + i][yCord2].contents == 10)
                        {
                            if (Server.map[xCord2 + i][yCord2].possiblyPrey)
                            {
                                reward += 1;
                            }
                        }
                        else
                        {
                            break label1;
                        }
                        break;
                    case 8:
                        if (Server.map[xCord2][yCord2 - i].contents == 0 ||
                                Server.map[xCord2][yCord2 - i].contents == 10)
                        {
                            if (Server.map[xCord2][yCord2 - i].possiblyPrey)
                            {
                                reward += 1;
                            }
                        }
                        else
                        {
                            break label1;
                        }
                        break;
                }
            }
        }
        // else what benefit would be gained from scanning without a prey on the board.
        else
        {
            label:
            for (int i = 1; i < 3; i++)
            {
                if (move == 0)
                {
                    switch (Server.orientations[identity2])
                    {
                        case 'N':
                            if (Server.map[xCord2 - i][yCord2].contents == 0 ||
                                    Server.map[xCord2 - i][yCord2].contents == 10)
                            {
                                reward += Server.map[xCord2 - i][yCord2].chanceOfOccupancy;
                            }
                            else
                            {
                                break label;
                            }
                            break;
                        case 'E':
                            if (Server.map[xCord2][yCord2 + i].contents == 0 ||
                                    Server.map[xCord2][yCord2 + i].contents == 10)
                            {
                                reward += Server.map[xCord2][yCord2 + i].chanceOfOccupancy;
                            }
                            else
                            {
                                break label;
                            }
                            break;
                        case 'S':
                            if (Server.map[xCord2 + i][yCord2].contents == 0 ||
                                    Server.map[xCord2 + i][yCord2].contents == 10)
                            {
                                reward += Server.map[xCord2 + i][yCord2].chanceOfOccupancy;

                            }
                            else
                            {
                                break label;
                            }
                            break;
                        case 'W':
                            if (Server.map[xCord2][yCord2 - i].contents == 0 ||
                                    Server.map[xCord2][yCord2 - i].contents == 10)
                            {
                                reward += Server.map[xCord2][yCord2 - i].chanceOfOccupancy;
                            }
                            else
                            {
                                break label;
                            }
                            break;
                    }
                }
                switch (move)
                {
                    case 1:
                        if (Server.map[xCord2 - i][yCord2].contents == 0 ||
                                Server.map[xCord2 - i][yCord2].contents == 10)
                        {
                            reward += Server.map[xCord2 - i][yCord2].chanceOfOccupancy;
                        }
                        else
                        {
                            break label;
                        }
                        break;
                    case 2:
                        if (Server.map[xCord2][yCord2 + i].contents == 0 ||
                                Server.map[xCord2][yCord2 + i].contents == 10)
                        {
                            reward += Server.map[xCord2][yCord2 + i].chanceOfOccupancy;
                        }
                        else
                        {
                            break label;
                        }
                        break;
                    case 4:
                        if (Server.map[xCord2 + i][yCord2].contents == 0 ||
                                Server.map[xCord2 + i][yCord2].contents == 10)
                        {
                            reward += Server.map[xCord2 + i][yCord2].chanceOfOccupancy;
                        }
                        else
                        {
                            break label;
                        }
                        break;
                    case 8:
                        if (Server.map[xCord2][yCord2 - i].contents == 0 ||
                                Server.map[xCord2][yCord2 - i].contents == 10)
                        {
                            reward += Server.map[xCord2][yCord2 - i].chanceOfOccupancy;
                        }
                        else
                        {
                            break label;
                        }
                        break;
                }
            }
        }
        return reward;
    }

    /**
     * @param elem   the element we are checking is in the list
     * @param subset the subset we are looking to check for a value
     * @return whether element is not in the list.
     */
    private static boolean notIn(int elem, int subset)
    {
        return (elem & subset) != 0;
    }

    /**
     * Synchronisation method used to make agent wait for their action to be chosen
     * by the lead thread.
     *
     * @param superc
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unused") private static void waitForNegotiation(outBoundConnection superc) throws InvocationTargetException
    {
        Server.lockTurn.lock();
        try
        {
            amountWaitingForMapUpdate++;
            if (amountWaitingForMapUpdate == Server.amountOfRobots)
            {
                Negotiator.negotiate();
                Server.mapNegotiation.signalAll();
                amountWaitingForMapUpdate = 0;
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
