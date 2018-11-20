import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.utility.Delay;

public class ScannerPhysical extends Scanner
{

	private final NXTRegulatedMotor frontMotor;

	public ScannerPhysical(Robot superc, Port ultraSonicPort, NXTRegulatedMotor frontMotor)
	{
		super(superc);
		EV3UltrasonicSensor ultraSonic = new EV3UltrasonicSensor(ultraSonicPort);
		distanceP = ultraSonic.getDistanceMode();
		checkP = ultraSonic.getListenMode();
		checkP.fetchSample(new float[1], 0);
		this.frontMotor = frontMotor;
	}


	private int scanClean(boolean movingForward)
	{
		float[] temp = new float[1];
		superc.mover.cumalativeLoss += (superc.mover.getGyroAngle());
		superc.mover.resetGyroTacho();
		int counter = 0;
		while (Math.abs(superc.mover.cumalativeLoss) > 4 && counter < 10)
		{
			counter++;
			superc.mover.pilot.rotate(Math.min(Math.abs(superc.mover.cumalativeLoss) / 6, 1) * superc.mover.cumalativeLoss);
			superc.mover.cumalativeLoss += (superc.mover.getGyroAngle());
			Delay.msDelay(100);
			superc.mover.resetGyroTacho();
			if (Math.abs(superc.mover.cumalativeLoss) > 4)
				superc.mover.pilot.travel(1);
		}
		double[] distance = new double[] { 23, 50, 75, 100, 125, 150 };
		if (movingForward)
		{
			distance = new double[] { 9, 37, 62, 87, 112, 137 };
		}
		int[] categorise = new int[6];
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
			superc.mover.pilot.rotate(4);
			superc.mover.pilot.rotate(-4);
			distanceP.fetchSample(temp, 0);
			counter++;
		}
        return scanClean(movForward);
	}
	
	public void scanDetect()
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

}
