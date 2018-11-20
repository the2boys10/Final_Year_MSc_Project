import java.io.IOException;

import lejos.robotics.navigation.MovePilot;

/**
 * Class which defines the methods that both physical as well as non physical versions of robots should maintain.
 */
public abstract class Movement
{
	/**
	 * What Robot owns this movement class
	 */
	final Robot superc;
	/**
	 * The Lejos move pilot used
	 */
	MovePilot pilot = null;
	/**
	 * The loss generated from turning, calculated through the use of the gyroscope
	 */
	double cumulativeLoss = 0;
	/**
	 * The angle of the robot currently
	 */
	final float[] angle = { 0.0f };
	/**
	 * The tacho count of the robot currently.
	 */
	float gyroTacho = 0;
	/**
	 * Boolean which represents whether the robot has turned in the current round.
	 */
	private boolean turned = false;

	/**
	 *
	 * @param superc The robot that is using this version of the movement class
	 */
	Movement(Robot superc)
	{
		this.superc = superc;
	}

	/**
	 * Method which turns the Robot to the direction required <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
	 * @param move The move that we wish to take in relation to rotation <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
	 * @throws IOException
	 */
	public void makeMove(int move) throws IOException
	{
		turned = false;
		if (Util.notIn(1, move))
		{
			faceNorth();
		}
		else if (Util.notIn(2, move))
		{
			faceEast();
		}
		else if (Util.notIn(4, move))
		{
			faceSouth();
		}
		else if (Util.notIn(8, move))
		{
			faceWest();
		}
		if(!turned)
		{
			superc.inOut.sendString("skip", "would like to skip");
			superc.inOut.readString("Everyone finished");
		}
		else
		{
			superc.isFront = false;
		}
	}

	/**
	 * Method which turns the Robot to the direction required and moves forward if a turn was not required <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
	 * @param move The move that we wish to take <ul><li>1:N</li><li>2:E</li><li>4:S</li><li>8:W</li></ul>
	 * @throws IOException
	 */
	public void makeMove2(int move) throws IOException
	{
		turned = false;
		if (Util.notIn(1, move))
		{
			faceNorth();
		}
		else if (Util.notIn(2, move))
		{
			faceEast();
		}
		else if (Util.notIn(4, move))
		{
			faceSouth();
		}
		else if (Util.notIn(8, move))
		{
			faceWest();
		}
		if(!turned)
		{
			moveForward();
		}
	}

	/**
	 * Faces the robot to the South through rotations
	 * @throws IOException
	 */
	private void faceSouth() throws IOException
	{
		switch (superc.orientation)
		{
			case 'N':
				turnAround();
				turned = true;
				break;
			case 'E':
				turnRight();
				turned = true;
				break;
			case 'W':
				turnLeft();
				turned = true;
				break;
		}
	}

	/**
	 * Faces the robot to the North through rotations
	 * @throws IOException
	 */
	private void faceNorth() throws IOException
	{
		switch (superc.orientation)
		{
			case 'W':
				turnRight();
				turned = true;
				break;
			case 'E':
				turnLeft();
				turned = true;
				break;
			case 'S':
				turnAround();
				turned = true;
				break;
		}
	}

	/**
	 * Faces the robot to the East through rotations
	 * @throws IOException
	 */
	private void faceEast() throws IOException
	{
		switch (superc.orientation)
		{
			case 'N':
				turnRight();
				turned = true;
				break;
			case 'S':
				turnLeft();
				turned = true;
				break;
			case 'W':
				turnAround();
				turned = true;
				break;
		}
	}

	/**
	 * Faces the robot to the West through rotations
	 * @throws IOException
	 */
	private void faceWest() throws IOException
	{
		switch (superc.orientation)
		{
			case 'N':
				turnLeft();
				turned = true;
				break;
			case 'E':
				turnAround();
				turned = true;
				break;
			case 'S':
				turnRight();
				turned = true;
				break;
		}
	}

	/**
	 * Makes the robot move forward if the move is valid i.e non 0
	 * @param move The movement we wish to move forward for.
	 * @throws IOException
	 */
	public void moveForwardMovement(int move) throws IOException
	{
		if(move==0)
		{
			superc.inOut.sendString("skip", "skip");
			superc.inOut.readString("Everyone finished");
		}
		else
		{
			moveForward();
		}
	}

	/**
	 * Method used to move the Robot forward
	 * @throws IOException
	 */
	protected abstract void moveForward() throws IOException;
	/**
	 * Method used to Turn the robot right
	 * @throws IOException
	 */
	protected abstract void turnRight() throws IOException;
	/**
	 * Method used to Turn the robot left
	 * @throws IOException
	 */
	public abstract void turnLeft() throws IOException;
	/**
	 * Method used to Turn the robot around
	 * @throws IOException
	 */
	protected abstract void turnAround() throws IOException;

	/**
	 * Method used to Clean up the sensors used by movement
	 */
	public void cleanUp()
	{

	}

	/**
	 * Method used to get the value that the gyroscope reads
	 */
	public float getGyroAngle()
	{
		return 0;
	}

	/**
	 * Method used to reset the value that the gyroscope reads
	 */
	public void resetGyroTacho()
	{
		
	}
}
