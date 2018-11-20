import java.io.IOException;

public class ScannerSimulated extends Scanner
{

	public ScannerSimulated(Robot superc)
	{
		super(superc);
	}

	protected void scanDirection(int xReduction, int yReduction) throws IOException
	{
		superc.inOut.sendString("scanForward", "scanning forward");
		superc.inOut.sendChar(superc.orientation, "Sending orientation");
		int distance = superc.inOut.readInt("reading distance");
		if (distance == 0)
		{
			if (Util.whatsInFront(superc) != 1)
			{
				superc.inOut.sendString("Found", "send think i've found 382");
				superc.inOut.sendObject(new int[]
				{ superc.xCord + xReduction, superc.yCord + yReduction }, "send where i've found 383");
				if (superc.inOut.readBool("Read if is correct finding 384"))
				{
					superc.isFront = true;
				}
			}
		}
		else
		{
			for (int i = 0; i < distance; i++)
			{
				if (xReduction != 0)
				{
					superc.updatedCells.add(superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord]);
					superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord].flagged = true;
					superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord].possiblyPrey = false;
					superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord].centerCell = false;
				}
				else
				{
					superc.updatedCells.add(superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)]);
					superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)].flagged = true;
					superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)].possiblyPrey = false;
					superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)].centerCell = false;
				}
			}
		}
	}

	@Override
	protected int scanForwardDistance(boolean movForward) throws IOException
	{
		superc.inOut.sendString("scanForward", "scanning forward");
		superc.inOut.sendChar(superc.orientation, "Sending orientation");
		return superc.inOut.readInt("reading distance");
	}

}