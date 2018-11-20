import java.io.IOException;

public class MovementSimulated extends Movement
{

	public MovementSimulated(Robot superc)
	{
		super(superc);
	}

	public void moveForward() throws IOException
    {
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("makeMove", "I would like to move forward");
		superc.inOut.sendChar(superc.orientation, "Here's my orientation");
		superc.inOut.sendBool(true, "Relaying movement success");
	}

	public void turnRight() throws IOException
    {
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("Turn", "I would like to turn");
		switch (superc.orientation)
		{
			case 'N':
				superc.orientation = 'E';
				break;
			case 'E':
				superc.orientation = 'S';
				break;
			case 'S':
				superc.orientation = 'W';
				break;
			case 'W':
				superc.orientation = 'N';
				break;
		}
		switch (superc.thinkOrientation)
		{
			case 'N':
				superc.thinkOrientation = 'E';
				break;
			case 'E':
				superc.thinkOrientation = 'S';
				break;
			case 'S':
				superc.thinkOrientation = 'W';
				break;
			case 'W':
				superc.thinkOrientation = 'N';
				break;
		}
		superc.inOut.sendBool(true,"finished turning");
	}

	public void turnLeft() throws IOException
    {
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("Turn", "I would like to turn");
		switch (superc.orientation)
		{
			case 'N':
				superc.orientation = 'W';
				break;
			case 'E':
				superc.orientation = 'N';
				break;
			case 'S':
				superc.orientation = 'E';
				break;
			case 'W':
				superc.orientation = 'S';
				break;
		}
		switch (superc.thinkOrientation)
		{
			case 'N':
				superc.thinkOrientation = 'W';
				break;
			case 'E':
				superc.thinkOrientation = 'N';
				break;
			case 'S':
				superc.thinkOrientation = 'E';
				break;
			case 'W':
				superc.thinkOrientation = 'S';
				break;
		}
		superc.inOut.sendBool(true,"finished turning");
	}

	public void turnAround() throws IOException
    {
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("Turn", "I would like to turn");
		switch (superc.orientation)
		{
			case 'N':
				superc.orientation = 'S';
				break;
			case 'E':
				superc.orientation = 'W';
				break;
			case 'S':
				superc.orientation = 'N';
				break;
			case 'W':
				superc.orientation = 'E';
				break;
		}
		switch (superc.thinkOrientation)
		{
			case 'N':
				superc.thinkOrientation = 'S';
				break;
			case 'E':
				superc.thinkOrientation = 'W';
				break;
			case 'S':
				superc.thinkOrientation = 'N';
				break;
			case 'W':
				superc.thinkOrientation = 'E';
				break;
		}
		superc.inOut.sendBool(true,"finished turning");
	}
}