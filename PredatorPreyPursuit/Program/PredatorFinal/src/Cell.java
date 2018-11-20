import java.io.Serializable;

/**
 * Class which holds detailed information relating to each tile within the system
 */
public class Cell implements Serializable
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
     * Searched value of the cell
     */
	boolean searched = false;
    /**
     * Flagged value of the cell
     */
	boolean flagged = false;
    /**
     * The binary representation of what direction this cell was reached from the start cell
     */
	int fromWhere = 0;
    /**
     * The binary representation of what direction this cell was reached from the previous cell
     */
	int turnFrom = 0;
	int[] fromWhere2 = new int[] {0,0,0,0};
	int[] fromWhere3 = new int[] {0,0,0,0};
    /**
     * The distance away from the origin
     */
	int distance = 0;
    /**
     * Flag whether there is a possible prey in this cell
     */
	boolean possiblyPrey = false;
    /**
     * Flag whether this was the location that the prey was found in last
     */
	boolean centerCell = false;

    /**
     *
     * @param xCord The xCord of the cell
     * @param yCord The yCord of the cell
     */
	@SuppressWarnings("unused") public Cell(int xCord, int yCord)
	{
		this.xCord = xCord;
		this.yCord = yCord;
	}
}