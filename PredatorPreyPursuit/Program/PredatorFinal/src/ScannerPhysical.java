import java.io.IOException;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.utility.Delay;

/**
 * Class used to setup the physical components such as the front motor and ultrasonic sensor
 */
public class ScannerPhysical extends Scanner
{

	private final EV3UltrasonicSensor ultraSonic;
	private final NXTRegulatedMotor frontMotor;

	/**
	 * Method that sets up each provider and sensor.
	 * @param superc The Robot that this sensor belongs to
	 * @param ultraSonicPort The ultrasonic sensor port used
	 * @param frontMotor The front motor port used
	 */
	public ScannerPhysical(Robot superc, Port ultraSonicPort, NXTRegulatedMotor frontMotor)
	{
		super(superc);
		ultraSonic = new EV3UltrasonicSensor(ultraSonicPort);
		distanceP = ultraSonic.getDistanceMode();
		checkP = ultraSonic.getListenMode();
		checkP.fetchSample(new float[1], 0);
		this.frontMotor = frontMotor;
	}

	@Override
	protected void scanDirection(int xReduction, int yReduction) throws IOException
	{
		// create an array to store the results from the distanceProvider
		float[] temp = new float[1];
		distanceP.fetchSample(temp, 0);
		int counter = 0;
		// if the scanner is stuck in detection mode
		while ((temp[0] == 1 || temp[0] == 0) && counter < 6)
		{
			superc.mover.pilot.rotate(5);
			superc.mover.pilot.rotate(-5);
			distanceP.fetchSample(temp, 0);
			counter++;
		}
		// scan the area in-front of the agent
		int distance = scanClean(false);
		// if the distance is 0 make extra sure.
		if (distance == 0 && (Util.whatsInFront(superc) == 0 || Util.whatsInFront(superc) == 10))
		{
			Delay.msDelay(100);
			distance = scanClean(false);
			scanDetect();
			if (distance == 0)
			{
				superc.inOut.sendString("Found", "send think i've found 382");
				superc.inOut.sendObject(new int[] { superc.xCord + xReduction, superc.yCord + yReduction }, "send where i've found 383");
				if (superc.inOut.readBool("Read if is correct finding 384"))
				{
					superc.isFront = true;
				}
				return;
			}
		}
		// switch to detect mode and add scanned cells to a list
		scanDetect();
		if (distance > 10)
		{
			distance = 8;
		}
		for (int i = 0; i < distance; i++)
		{
			if (xReduction != 0)
			{
				if ((superc.xCord + (xReduction * i) + xReduction) >= 0 && (superc.xCord + (xReduction * i) + xReduction) < 8)
				{
					superc.updatedCells.add(superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord]);
					superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord].flagged = true;
					superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord].possiblyPrey = false;
					superc.map[(superc.xCord + (xReduction * i) + xReduction)][superc.yCord].centerCell = false;
				}
			}
			else
			{
				if ((superc.yCord + (yReduction * i) + yReduction) >= 0 && (superc.yCord + (yReduction * i) + yReduction) < 8)
				{
					superc.updatedCells.add(superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)]);
					superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)].flagged = true;
					superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)].possiblyPrey = false;
					superc.map[superc.xCord][(superc.yCord + (yReduction * i) + yReduction)].centerCell = false;
				}
			}
		}
	}

	/**
	 * Samples the environment multiple times to try to gather a more accurate result
	 * @param movingForward Is the agent currently transitioning between tiles
	 * @return the distance scanned
	 */
	private int scanClean(boolean movingForward)
	{
		// create an empty array to score the scanning results
		float[] temp = new float[1];
		superc.mover.cumulativeLoss += (superc.mover.getGyroAngle());
		superc.mover.resetGyroTacho();
		int counter = 0;
		// rotate agent to the correct orientation, re-attempt multiple times if necessary.
		while (Math.abs(superc.mover.cumulativeLoss) > 4 && counter < 10)
		{
			counter++;
			superc.mover.pilot.rotate(Math.min(Math.abs(superc.mover.cumulativeLoss) / 6, 1) * superc.mover.cumulativeLoss);
			superc.mover.cumulativeLoss += (superc.mover.getGyroAngle());
			Delay.msDelay(100);
			superc.mover.resetGyroTacho();
			if (Math.abs(superc.mover.cumulativeLoss) > 4)
				superc.mover.pilot.travel(1);
		}
		// set the distance that each cell is worth when not moving forward
		double[] distance = new double[] { 23, 50, 75, 100, 125, 150 };
		// if we are moving forward then lower or distances
		if (movingForward)
		{
			distance = new double[] { 9, 37, 62, 87, 112, 137 };
		}
		// create a list of samples to work out which distance is the most reasonable using mode
		int[] categorise = new int[6];
		// rotate the from motor to the 0 position and scan 3 times.
		frontMotor.rotateTo(0, true);
		Delay.msDelay(20);
		for (int i = 0; i < 3; i++)
		{
			distanceP.fetchSample(temp, 0);
			temp[0] = temp[0] * 100;
			if (temp[0] < 150)
			{
				int j;
				for (j = 0; j < distance.length; j++)
				{
					if (temp[0] < distance[j])
					{
						break;
					}
				}
				categorise[j]++;
			}
			Delay.msDelay(50);
		}
		// rotate the from motor to the -15 degrees position and scan 3 times.
		frontMotor.rotateTo(-15, true);
		Delay.msDelay(20);
		for (int i = 0; i < 3; i++)
		{
			distanceP.fetchSample(temp, 0);
			temp[0] = temp[0] * 100;
			if (temp[0] < 150)
			{
				int j;
				for (j = 0; j < distance.length; j++)
				{
					if (temp[0] < distance[j])
					{
						break;
					}
				}
				categorise[j]++;
			}
			Delay.msDelay(50);
		}
		// rotate the from motor to the 15 degrees position and scan 3 times.
		frontMotor.rotateTo(15, true);
		Delay.msDelay(20);
		for (int i = 0; i < 3; i++)
		{
			distanceP.fetchSample(temp, 0);
			temp[0] = temp[0] * 100;
			if (temp[0] < 150)
			{
				int j;
				for (j = 0; j < distance.length; j++)
				{
					if (temp[0] < distance[j])
					{
						break;
					}
				}
				categorise[j]++;
			}
			Delay.msDelay(50);
		}
		// rotate the motor to 0 position and calculate the mode value
		frontMotor.rotateTo(0, true);
		int maxValue = 0;
		int maxLength = 0;
		if (categorise[0] == 0)
		{
			for (int i = 0; i < categorise.length; i++)
			{
				if (maxValue < categorise[i])
				{
					maxValue = categorise[i];
					maxLength = i;
				}
			}
		}
		return maxLength;
	}

	public int scanForwardDistance(boolean movForward)
	{
		float[] temp = new float[1];
		distanceP.fetchSample(temp, 0);
		int counter = 0;
		while ((temp[0] == 1 || temp[0] == 0) && counter < 6)
		{
			superc.mover.pilot.rotate(5);
			superc.mover.pilot.rotate(-5);
			distanceP.fetchSample(temp, 0);
			counter++;
		}
		int temp2 = scanClean(movForward);
		if (temp2 == 0 && Util.whatsInFront(superc) == 0)
		{
			temp2 = scanClean(movForward);
		}
		scanDetect();
		return temp2;
	}

	/**
	 * Method to switch to searching for other agents to stop self sampling.
	 */
	private void scanDetect()
	{
		float[] temp = new float[1];
		checkP.fetchSample(temp, 0);
		int counter = 0;
		while (temp[0] != 0 && counter < 10)
		{
			superc.mover.pilot.rotate(5);
			superc.mover.pilot.rotate(-5);
			checkP.fetchSample(temp, 0);
			counter++;
		}
	}

	public void cleanUp()
	{
		ultraSonic.close();
	}
}
