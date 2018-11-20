import java.io.Serializable;
import java.util.Arrays;

public class Cell implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4562331819476067308L;
	int contents;
	public final double[][] chanceOfSelf = new double[][] {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	public final double[][] chanceOfTemp = new double[][] {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	public final double[][] chanceOfSelfPercent = new double[][] {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	public double[] chanceOfOccupancy = new double[] {0,0,0,0};
	public boolean[] sharedCell = new boolean[] {false,false,false,false};

	public String toString()
	{
		return "[" + Arrays.toString(chanceOfSelfPercent[0]) + "]";
	}
}