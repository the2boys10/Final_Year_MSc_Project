import java.io.Serializable;

/**
 * Class which holds detailed information relating to each tile within the system
 */
class Cell implements Serializable
{
    /**
     * The serial ID of the object
     */
    private static final long serialVersionUID = -4562331819476067308L;
    /**
     * xCord of the cell
     */
    final int xCord;
    /**
     * yCord of the cell
     */
    final int yCord;
    /**
     * contents of the cell <ul><li>0: Empty</li><li>1: Block</li><li>2-5: Predators</li><li>10:Prey</li></ul>
     */
    int contents;
    /**
     * Flagged value of the cell
     */
    boolean flagged = false;
    /**
     * Flag whether there is a possible prey in this cell
     */
    boolean possiblyPrey = false;
    /**
     * Flag whether this was the location that the prey was found in last
     */
    boolean centerCell = false;
	int[] fromWhere2 = new int[] {0,0,0,0};
	int[] fromWhere3 = new int[] {0,0,0,0};
    /**
     * Is this cell set for occupancy during negotiation
     */
    boolean goingToOccupy = false;
    /**
     * What is the chance that the prey is in this cell
     */
    double chanceOfOccupancy;

    /**
     * @param xCord        The xCoordinate of the cell
     * @param yCord        The yCoordinate of the cell
     * @param contents     The contents of the cell,<ul><li>Empty = 0</li><li>Block = 1</li><li>Predator = 2-5</li><li>Prey = 0</li></ul>
     * @param distribution The initial value of the chance of prey.
     */
    public Cell(int xCord, int yCord, int contents, double distribution)
    {
        this.xCord = xCord;
        this.yCord = yCord;
        this.contents = contents;
        chanceOfOccupancy = distribution;
    }

    /**
     * Prints the content of the string.
     */
    public String toString()
    {
        return contents + "";
    }
}