/**
 * A class which provides utility functions
 */
public class Util
{
	/**
	 * Checks if there is supposed to be an empty tile in the cell
	 * @param xCordNext The cell being checked xCord
	 * @param yCordNext The cell being checked yCord
	 * @param superc The Robot utilising the function
	 * @return Whether the cell should be empty
	 */
	public static boolean checkContentsWithoutPred(int xCordNext, int yCordNext, Robot superc)
	{
        return superc.map[xCordNext][yCordNext].contents == 0 || superc.map[xCordNext][yCordNext].contents == 10;
    }

	/**
	 * Checks if there is supposed to be another agent in this tile
	 * @param xCordNext The cell being checked xCord
	 * @param yCordNext The cell being checked yCord
	 * @param superc The Robot utilising the function
	 * @return Whether the cell should be empty
	 */
	public static boolean checkContentsWithPred(int xCordNext, int yCordNext, Robot superc)
	{
        return superc.map[xCordNext][yCordNext].contents != 1 && superc.map[xCordNext][yCordNext].contents != 0 &&
                superc.map[xCordNext][yCordNext].contents != 10;
    }

	/**
	 * Checks what should be in the cell in-front of the robot
	 * @param superc The agent calling this function
	 * @return The value of the contents of the cell in-front <ul><li>0: empty</li><li>1: Block</li><li>2:Agent 1</li><li>3: Agent 2</li><li>4: Agent 3</li><li>5:Agent 4</li><li>10: Prey</li></ul>
	 */
	public static int whatsInFront(Robot superc)
	{
		switch (superc.orientation)
		{
			case 'N':
				if (superc.map[superc.xCord - 1][superc.yCord].centerCell)
				{
					return 10;
				}
				return superc.map[superc.xCord - 1][superc.yCord].contents;
			case 'E':
				if (superc.map[superc.xCord][superc.yCord + 1].centerCell)
				{
					return 10;
				}
				return superc.map[superc.xCord][superc.yCord + 1].contents;
			case 'W':
				if (superc.map[superc.xCord][superc.yCord - 1].centerCell)
				{
					return 10;
				}
				return superc.map[superc.xCord][superc.yCord - 1].contents;
			default:
				if (superc.map[superc.xCord + 1][superc.yCord].centerCell)
				{
					return 10;
				}
				return superc.map[superc.xCord + 1][superc.yCord].contents;
		}
	}

	/**
	 * Clean the searched status of all cells
	 * @param superc The robot wanting to clear its searched status
	 */
	public static void cleanCells(Robot superc)
	{
		for (int i = 0; i < superc.map.length; i++)
		{
			for (int j = 0; j < superc.map.length; j++)
			{
				superc.map[i][j].fromWhere = 0;
				superc.map[i][j].turnFrom = 0;
				superc.map[i][j].distance = 0;
				superc.map[i][j].searched = false;
			}
		}
	}

	/**
	 * Method to check if an element is in the bitwise set
	 * @param elem The element we are checking is in the ser
	 * @param subset The set we are searching for it in
	 * @return Whether the element was indeed in the set.
	 */
	public static boolean notIn(int elem, int subset)
	{
		return (elem & subset) != 0;
	}
}
