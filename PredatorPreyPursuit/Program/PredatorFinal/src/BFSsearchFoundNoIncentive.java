import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * A search technique that penalised the cost of turning if turning takes a full turn, looks for possible prey locations
 */
public class BFSsearchFoundNoIncentive
{
    /**
     * @param xCordCurrent the xCord of the cell we would like to add to the search list
     * @param yCordCurrent the yCord of the cell we would like to add to the search list
     * @param fromWhere    The moves which reached the cell in binary form for the first step <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
     * @param currentBound The list of cells that need to be expanded
     * @param superc       The Robot that is calling.
     */
    private static void initialiseSearch(int xCordCurrent, int yCordCurrent, int fromWhere,
                                         LinkedList<Cell> currentBound, Robot superc)
    {
        if (Util.checkContentsWithPred(xCordCurrent, yCordCurrent, superc) || Util.checkContentsWithoutPred(xCordCurrent, yCordCurrent, superc))
        {
            superc.map[xCordCurrent][yCordCurrent].fromWhere = fromWhere;
            superc.map[xCordCurrent][yCordCurrent].searched = true;
            superc.map[xCordCurrent][yCordCurrent].distance = 1;
            currentBound.add(superc.map[xCordCurrent][yCordCurrent]);
        }
    }

    /**
     * Method performs the selected search technique on the map returning its most favourable moves
     *
     * @param xCord  The starting xCord for the search
     * @param yCord  The starting yCord for the search
     * @param superc The Robot that is calling.
     * @return The movements that the agent feel are beneficial in binary form <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
     */
    public static int search(int xCord, int yCord, Robot superc)
    {
        // clean up the cells in relation to distance and search value
        Util.cleanCells(superc);
        // set the agents own cell to be searched.
        superc.map[xCord][yCord].searched = true;
        // the list of cells that we need to expand
        @SuppressWarnings("Convert2Diamond") LinkedList<Cell> currentBound = new LinkedList<Cell>();
        initialiseSearch(xCord - 1, yCord, 1, currentBound, superc);
        initialiseSearch(xCord, yCord + 1, 2, currentBound, superc);
        initialiseSearch(xCord + 1, yCord, 4, currentBound, superc);
        initialiseSearch(xCord, yCord - 1, 8, currentBound, superc);
        // set the current distance of the nearest un-searched cell to 0.
        int currentDistance = 0;
        int currentMovements = 0;
        // sort the cells in order of distance
        Collections.sort(currentBound, new Comparator<Cell>()
        {
            public int compare(Cell x, Cell y)
            {
                return x.distance - y.distance;
            }
        });
        // while there are more cells to be expanded
        while (currentBound.size() > 0)
        {
            // remove the first cell to expand
            Cell current = currentBound.removeFirst();
            // if the cell is flagged for containing prey
            if (current.possiblyPrey)
            {
                if (currentDistance == current.distance)
                {
                    currentMovements |= current.fromWhere;
                }
                else
                {
                    if (currentDistance == 0)
                    {
                        currentDistance = current.distance;
                        currentMovements = current.fromWhere;
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else
            {
                // expand the current cell and sort the cells that were expanded
                expand(current.xCord, current.yCord, current.distance, current.fromWhere, currentBound, superc);
                Collections.sort(currentBound, new Comparator<Cell>()
                {
                    public int compare(Cell x, Cell y)
                    {
                        return x.distance - y.distance;
                    }
                });
            }
        }
        return currentMovements;
    }

    /**
     * Method takes the cell that we wish to add to our expansion list
     *
     * @param xCordCurrent The xCord that we wish to add to our expansion  list
     * @param yCordCurrent The yCord that we wish to add to our expansion  list
     * @param distance     The current distance that the searched cell is.
     * @param fromWhere    The moves which reached the cell in binary form for the first step <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
     * @param currentBound The list of cells that need to be expanded
     * @param superc       The robot that is calling the search
     */
    private static void expandOne(int xCordCurrent, int yCordCurrent, int distance, int fromWhere, LinkedList<Cell> currentBound, Robot superc)
    {
        // if the cell we would like to expand is empty or a predator and the distance = distance + 1
        if ((Util.checkContentsWithoutPred(xCordCurrent, yCordCurrent, superc) || (Util.checkContentsWithPred(xCordCurrent, yCordCurrent, superc) && superc.map[xCordCurrent][yCordCurrent].contents != superc.ID)) && (superc.map[xCordCurrent][yCordCurrent].distance == distance + 1 ||
                !superc.map[xCordCurrent][yCordCurrent].searched))
        {
            superc.map[xCordCurrent][yCordCurrent].fromWhere |= fromWhere;
            if (!superc.map[xCordCurrent][yCordCurrent].searched)
            {
                currentBound.add(superc.map[xCordCurrent][yCordCurrent]);
                superc.map[xCordCurrent][yCordCurrent].distance = distance + 1;
                superc.map[xCordCurrent][yCordCurrent].searched = true;
            }
        }
    }

    /**
     * @param xCord        The xCord that we are expanding
     * @param yCord        The yCord that we are expanding
     * @param distance     The current distance that the searched cell is.
     * @param fromWhere    The moves which reached the cell in binary form for the first step <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
     * @param currentBound The list of cells that need to be expanded
     * @param superc       The robot that is calling the search
     */
    private static void expand(int xCord, int yCord, int distance, int fromWhere, LinkedList<Cell> currentBound, Robot superc)
    {
        expandOne(xCord + 1, yCord, distance, fromWhere, currentBound, superc);
        expandOne(xCord - 1, yCord, distance, fromWhere, currentBound, superc);
        expandOne(xCord, yCord + 1, distance, fromWhere, currentBound, superc);
        expandOne(xCord, yCord - 1, distance, fromWhere, currentBound, superc);
    }
}
