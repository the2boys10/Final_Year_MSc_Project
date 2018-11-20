import lejos.robotics.navigation.MovePilot;

import java.io.IOException;

public abstract class Movement
{
	final Robot superc;
	MovePilot pilot = null;
	double cumalativeLoss = 0;
	final float[] angle = { 0.0f };
	float gyroTacho = 0;

	Movement(Robot superc)
	{
		this.superc = superc;
	}


	public void faceSouthBefore() throws IOException
	{
		switch (superc.thinkOrientation)
		{
			case 'N':
				turnAround();
				break;
			case 'E':
				turnRight();
				break;
			case 'W':
				turnLeft();
				break;
		}
	}

	public void faceNorthBefore() throws IOException
	{
		switch (superc.thinkOrientation)
		{
			case 'W':
				turnRight();
				break;
			case 'E':
				turnLeft();
				break;
			case 'S':
				turnAround();
				break;
		}
	}

	public void faceEastBefore() throws IOException
	{
		switch (superc.thinkOrientation)
		{
			case 'N':
				turnRight();
				break;
			case 'S':
				turnLeft();
				break;
			case 'W':
				turnAround();
				break;
		}
	}

	public void faceWestBefore() throws IOException
	{
		switch (superc.thinkOrientation)
		{
			case 'N':
				turnLeft();
				break;
			case 'E':
				turnAround();
				break;
			case 'S':
				turnRight();
				break;
		}
	}
	
	public abstract void moveForward() throws IOException;
	public abstract void turnRight() throws IOException;
	protected abstract void turnLeft() throws IOException;
	protected abstract void turnAround() throws IOException;

	public float getGyroAngle()
	{
		return 0;
	}

	public void resetGyroTacho()
	{
		
	}
}
