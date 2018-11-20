import java.io.Serializable;
import java.util.Arrays;

public class Cell implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = -4562331819476067308L;
    public final double[][] chanceOfSelf = new double[][]{{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
    public final double[][] chanceOfSelfPercent = new double[][]{{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
    public final double[][] chanceOfTemp = new double[][]{{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
    public double[] chanceOfOccupancy = new double[]{1, 1, 1, 1};
    public final boolean[] sharedCell = new boolean[]{false, false, false, false};
    int contents;

    public Cell(int contents)
    {
        this.contents = contents;
        if (contents == 1)
        {
            for (int i = 0; i < chanceOfSelfPercent.length; i++)
            {
                for (int k = 0; k < chanceOfSelfPercent[i].length; k++)
                {
                    chanceOfSelfPercent[i][k] = 0;
                    chanceOfSelf[i][k] = 0;
                    chanceOfTemp[i][k] = 0;
                }
            }
        }

    }

    public String toString()
    {
        StringBuilder printout = new StringBuilder();
        int i;
        for (i = 0; i < chanceOfOccupancy.length - 1; i++)
        {
            printout.append((int) Math.round(chanceOfOccupancy[i] * 100)).append(",");
        }
        printout.append((int) Math.round(chanceOfOccupancy[i] * 100));
        return "[" + printout + "," + Arrays.toString(sharedCell) + "]";
    }
}