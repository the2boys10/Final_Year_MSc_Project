import java.io.IOException;

import lejos.robotics.SampleProvider;

/**
 * Class which sets up the sensors used for observing the environment
 */
public abstract class Scanner
{
	/**
	 * The robot that owns this scanner
	 */
	final Robot superc;
	/**
	 * The provider that allows the robot to get the current distance
	 */
	SampleProvider distanceP;
	/**
	 * The provider that allows the robot to check if other robots are scanning currently
	 */
	SampleProvider checkP;

	/**
	 * The robot that owns this scanner
	 * @param superc
	 */
	Scanner(Robot superc)
	{
		this.superc = superc;
	}

	/**
	 * Scans in-front updating the tiles that have been scanned in the current round
	 * @throws IOException
	 */
	public void scanFront() throws IOException
	{
		switch (superc.orientation)
		{
			case 'N':
				scanDirection(-1, 0);
				break;
			case 'E':
				scanDirection(0, 1);
				break;
			case 'S':
				scanDirection(1, 0);
				break;
			case 'W':
				scanDirection(0, -1);
				break;
		}
	}

	/**
	 * Cleans up the sensors used to gather information about the environment
	 */
	public void cleanUp()
	{
		
	}

	/**
	 * Scans a specific direction in the environment
	 * @param xReduction The direction in the xAxis <ul><li>-1:West</li><li>1:East</li><li>0:None</li></ul>
	 * @param yReduction
	 * @throws IOException
	 */
	protected abstract void scanDirection(int xReduction, int yReduction) throws IOException;

	/**
	 * Scan forward
	 * @param movForward Is the robot currently transitioning between tiles?
	 * @return the distance scanned
	 * @throws IOException
	 */
	protected abstract int scanForwardDistance(boolean movForward) throws IOException;
}
