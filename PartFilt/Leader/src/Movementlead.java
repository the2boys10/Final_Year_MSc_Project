import lejos.robotics.navigation.MovePilot;

import java.io.IOException;

public abstract class Movementlead
{
	final LeaderSub superc;
	MovePilot pilot = null;
	double cumalativeLoss = 0;
	final float[] angle = { 0.0f };
	float gyroTacho = 0;
	boolean turned = false;

	Movementlead(LeaderSub superc)
	{
		this.superc = superc;
	}
	



	public void faceSouthBefore() throws IOException, ClassNotFoundException
	{
		if (superc.thinkOrientation == 'N')
		{
			turnAround();
		}
		else if (superc.thinkOrientation == 'E')
		{
			turnRight();
		}
		else if (superc.thinkOrientation == 'W')
		{
			turnLeft();
		}
	}

	public void faceNorthBefore() throws IOException, ClassNotFoundException
	{
		if (superc.thinkOrientation == 'W')
		{
			turnRight();
		}
		else if (superc.thinkOrientation == 'E')
		{
			turnLeft();
		}
		else if (superc.thinkOrientation == 'S')
		{
			turnAround();
		}
	}

	public void faceEastBefore() throws IOException, ClassNotFoundException
	{
		if (superc.thinkOrientation == 'N')
		{
			turnRight();
		}
		else if (superc.thinkOrientation == 'S')
		{
			turnLeft();
		}
		else if (superc.thinkOrientation == 'W')
		{
			turnAround();
		}
	}

	public void faceWestBefore() throws IOException, ClassNotFoundException
	{
		if (superc.thinkOrientation == 'N')
		{
			turnLeft();
		}
		else if (superc.thinkOrientation == 'E')
		{
			turnAround();
		}
		else if (superc.thinkOrientation == 'S')
		{
			turnRight();
		}
	}
	
	public abstract void moveForward() throws Exception;
	public abstract void turnRight() throws IOException;
	protected abstract void turnLeft() throws IOException;
	protected abstract void turnAround() throws IOException;
	
	public void cleanUp()
	{

	}

	public void resetGyroTacho()
	{
		
	}




	public float getGyroAngle()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
