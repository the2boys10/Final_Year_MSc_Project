import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * A search technique that penalised the cost of turning as well as moving
 * through other predators if turning takes a full turn, looks for un-searched
 * tiles
 */
public class BFSsearchTurnCost
{
	/**
	 *
	 * @param xCordCurrent
	 *            the xCord of the cell we would like to add to the search list
	 * @param yCordCurrent
	 *            the yCord of the cell we would like to add to the search list
	 * @param fromWhere
	 *            The moves which reached the cell in binary form for the first step
	 *            <ul>
	 *            <li>1:N</li>
	 *            <li>2:E</li>
	 *            <li>4:S</li>
	 *            <li>8:W</li>
	 *            </ul>
	 * @param currentBound
	 *            The list of cells that need to be expanded
	 * @param offTurn
	 *            The value to add to the distance depending on if the robot would
	 *            have to turn
	 * @param superc
	 *            The Robot that is calling.
	 */
	private static void initialiseSearch(int xCordCurrent, int yCordCurrent, int fromWhere,
			LinkedList<Cell> currentBound, int offTurn, Robot superc)
	{
		int indexOf = 10;
		if(fromWhere==1)
			indexOf = 0;
		else if(fromWhere==2)
			indexOf = 1;
		else if(fromWhere==4)
			indexOf = 2;
		else if(fromWhere==8)
			indexOf = 3;
		// if the cell we wish to initialise the search is a predator or empty then add
		// it to the list of cells we wish to expand.
		if (Util.checkContentsWithPred(xCordCurrent, yCordCurrent, superc)
				|| Util.checkContentsWithoutPred(xCordCurrent, yCordCurrent, superc))
		{
			superc.map[xCordCurrent][yCordCurrent].fromWhere3[indexOf] = fromWhere;
			superc.map[xCordCurrent][yCordCurrent].searched = true;
			// if the cell we wish to initialise the search is a predator then add 4 turns
			// plus the value if we need to turn to enter it
			// else add the off turn distance
			if (Util.checkContentsWithPred(xCordCurrent, yCordCurrent, superc))
			{
				superc.map[xCordCurrent][yCordCurrent].fromWhere2[indexOf] = 4 + offTurn;
				superc.map[xCordCurrent][yCordCurrent].distance = 4 + offTurn;
				currentBound.add(superc.map[xCordCurrent][yCordCurrent]);
			} else
			{
				if (offTurn > 0)
					currentBound.add(superc.map[xCordCurrent][yCordCurrent]);
				else
					currentBound.add(superc.map[xCordCurrent][yCordCurrent]);
				superc.map[xCordCurrent][yCordCurrent].fromWhere2[indexOf] = 1 + offTurn;
				superc.map[xCordCurrent][yCordCurrent].distance = 1 + offTurn;
			}
		}
	}

	/**
	 * Method performs the selected search technique on the map returning its most
	 * favourable moves
	 * 
	 * @param xCord
	 *            The starting xCord for the search
	 * @param yCord
	 *            The starting yCord for the search
	 * @param orientation
	 *            The orientation that the robot is currently facing
	 * @param superc
	 *            The Robot that is calling.
	 * @return The movements that the agent feel are beneficial in binary form
	 *         <ul>
	 *         <li>1:N</li>
	 *         <li>2:E</li>
	 *         <li>4:S</li>
	 *         <li>8:W</li>
	 *         </ul>
	 */
	public static int search(int xCord, int yCord, char orientation, Robot superc)
	{
		// clean up the cells in relation to distance and search value
		Util.cleanCells(superc);
		for (int i = 0; i < superc.map.length; i++)
		{
			for (int j = 0; j < superc.map.length; j++)
			{
				for (int k = 0; k < 4; k++)
				{
					superc.map[i][j].distance = 1000;
					superc.map[i][j].fromWhere2[k] = 1000;
					superc.map[i][j].fromWhere3[k] = 0;
				}
			}
		}
		superc.map[xCord][yCord].distance = 0;
		// set the agents own cell to be searched.
		superc.map[xCord][yCord].searched = true;
		// the list of cells that we need to expand
		@SuppressWarnings("Convert2Diamond")
		LinkedList<Cell> currentBound = new LinkedList<Cell>();
		if (orientation == 'N')
		{
			initialiseSearch(xCord - 1, yCord, 1, currentBound, 0, superc);
		} else
		{
			initialiseSearch(xCord - 1, yCord, 1, currentBound, 1, superc);
		}
		if (orientation == 'E')
		{
			initialiseSearch(xCord, yCord + 1, 2, currentBound, 0, superc);
		} else
		{
			initialiseSearch(xCord, yCord + 1, 2, currentBound, 1, superc);
		}
		if (orientation == 'S')
		{
			initialiseSearch(xCord + 1, yCord, 4, currentBound, 0, superc);
		} else
		{
			initialiseSearch(xCord + 1, yCord, 4, currentBound, 1, superc);
		}
		if (orientation == 'W')
		{
			initialiseSearch(xCord, yCord - 1, 8, currentBound, 0, superc);
		} else
		{
			initialiseSearch(xCord, yCord - 1, 8, currentBound, 1, superc);
		}
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
			// if the cell is flagged for searching
			if (!current.flagged)
			{
				int lowestCost = 1000;
				int initialTurn = 0;
				boolean choseToTurn = false;
				for (int i = 0; i < current.fromWhere2.length; i++)
				{
					if (lowestCost > current.fromWhere2[i])
					{
						lowestCost = current.fromWhere2[i];
						initialTurn = current.fromWhere3[i];
						choseToTurn = true;
					} else if (lowestCost == current.fromWhere2[i])
					{
						initialTurn |= current.fromWhere3[i];
						lowestCost = current.fromWhere2[i];
						choseToTurn = true;
					}
				}
				if (currentDistance == lowestCost)
				{
					currentMovements |= initialTurn;
				} else
				{
					if (currentDistance == 0)
					{
						currentDistance = lowestCost;
						currentMovements = initialTurn;
					} else
					{
						break;
					}
				}
			} else
			{
				// expand the current cell and sort the cells that were expanded
				expand(current, current.xCord, current.yCord, current.distance, current.fromWhere, currentBound, superc,
						current.turnFrom);
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

	private static void expandOne(int xCordCurrent, int yCordCurrent, int distance, int fromWhere,
			LinkedList<Cell> currentBound, Robot superc, int fromWhereEnter)
	{
		int indexOf = 10;
		if(fromWhereEnter==1)
			indexOf = 0;
		else if(fromWhereEnter==2)
			indexOf = 1;
		else if(fromWhereEnter==4)
			indexOf = 2;
		else if(fromWhereEnter==8)
			indexOf = 3;
		if ((Util.checkContentsWithPred(xCordCurrent, yCordCurrent, superc)
				|| Util.checkContentsWithoutPred(xCordCurrent, yCordCurrent, superc))
				&& (superc.map[xCordCurrent][yCordCurrent].distance >= distance))
		{
			superc.map[xCordCurrent][yCordCurrent].fromWhere3[indexOf] |= fromWhere;
			superc.map[xCordCurrent][yCordCurrent].fromWhere2[indexOf] = distance+1;
			if (superc.map[xCordCurrent][yCordCurrent].distance == 1000)
			{
				currentBound.add(superc.map[xCordCurrent][yCordCurrent]);
			}
			if(superc.map[xCordCurrent][yCordCurrent].distance>distance+1)
			{
				superc.map[xCordCurrent][yCordCurrent].distance = distance+1;
			}
		}
	}


	/**
	 *
	 * @param current
	 * @param xCord
	 *            The xCord that we are expanding
	 * @param yCord
	 *            The yCord that we are expanding
	 * @param distance
	 *            The current distance that the searched cell is.
	 * @param fromWhere
	 *            The moves which reached the cell in binary form for the first step
	 *            <ul>
	 *            <li>1:N</li>
	 *            <li>2:E</li>
	 *            <li>4:S</li>
	 *            <li>8:W</li>
	 *            </ul>
	 * @param currentBound
	 *            The list of cells that need to be expanded
	 * @param superc
	 *            The robot that is calling the search
	 * @param offTurn
	 *            The value to add to the distance depending on if the robot would
	 *            have to turn
	 */
	private static void expand(Cell current, int xCord, int yCord, int distance, int fromWhere,
			LinkedList<Cell> currentBound, Robot superc, int offTurn)
	{
		int[] temp = findLowestCost(current,0);
		expandOne(xCord - 1, yCord, temp[1], temp[0], currentBound, superc, 1);
		temp = findLowestCost(current,1);
		expandOne(xCord, yCord + 1, temp[1], temp[0], currentBound, superc, 2);
		temp = findLowestCost(current,2);
		expandOne(xCord + 1, yCord, temp[1], temp[0], currentBound, superc, 4);
		temp = findLowestCost(current,3);
		expandOne(xCord, yCord - 1, temp[1], temp[0], currentBound, superc, 8);
	}

	// private static void expand(Cell current, int xCord, int yCord, int distance,
	// int fromWhere,
	// LinkedList<Cell> currentBound, Robot superc, int offTurn) {
	// // if we can reach the cell using North
	// if (Util.notIn(2, offTurn))
	// expandOne(xCord - 1, yCord, distance, fromWhere, currentBound, superc, 0, 1);
	// else
	// expandOne(xCord - 1, yCord, distance, fromWhere, currentBound, superc, 1, 1);
	// // if we can reach the cell using East
	// if (Util.notIn(2, offTurn))
	// expandOne(xCord, yCord + 1, distance, fromWhere, currentBound, superc, 0, 2);
	// else
	// expandOne(xCord, yCord + 1, distance, fromWhere, currentBound, superc, 1, 2);
	// // if we can reach the cell using South
	// if (Util.notIn(4, offTurn))
	// expandOne(xCord + 1, yCord, distance, fromWhere, currentBound, superc, 0, 4);
	// else
	// expandOne(xCord + 1, yCord, distance, fromWhere, currentBound, superc, 1, 4);
	// // if we can reach the cell using West
	// if (Util.notIn(8, offTurn))
	// expandOne(xCord, yCord - 1, distance, fromWhere, currentBound, superc, 0, 8);
	// else
	// expandOne(xCord, yCord - 1, distance, fromWhere, currentBound, superc, 1, 8);
	// }

	private static int[] findLowestCost(Cell current, int direction)
	{
		int lowestCost = current.fromWhere2[direction];
		int initialTurn = current.fromWhere3[direction];;
		boolean choseToTurn = false;
		for (int i = 0; i < current.fromWhere2.length; i++)
		{
			if (lowestCost > current.fromWhere2[i] + 1)
			{
				lowestCost = current.fromWhere2[i];
				initialTurn = current.fromWhere3[i];
				choseToTurn = true;
			} else if (lowestCost == current.fromWhere2[i] + 1)
			{
				initialTurn |= current.fromWhere3[i];
				lowestCost = current.fromWhere2[i];
				choseToTurn = true;
			}
		}
		if(choseToTurn)
		return new int[]
		{ initialTurn, lowestCost+1};
		else
			return new int[]
					{ initialTurn, lowestCost};
	}
}
