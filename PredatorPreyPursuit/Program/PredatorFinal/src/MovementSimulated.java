import java.io.IOException;

public class MovementSimulated extends Movement
{

	public MovementSimulated(Robot superc)
	{
		super(superc);
	}

	public void moveForward() throws IOException
	{
		superc.inOut.sendString("makeMove", "I would like to move forward");
		superc.map[superc.xCord][superc.yCord].contents = 0;
		switch (superc.orientation)
		{
			case 'N':
				superc.xCord--;
				break;
			case 'E':
				superc.yCord++;
				break;
			case 'S':
				superc.xCord++;
				break;
			default:
				superc.yCord--;
				break;
		}
		superc.inOut.sendObject(new int[] {superc.xCord, superc.yCord}, "want to move here");
		superc.map[superc.xCord][superc.yCord].contents = superc.ID;
		superc.inOut.sendBool(true, "Relaying movement success");
		superc.inOut.readString("I can scan again after move");
	}

	public void turnRight() throws IOException
	{
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
		superc.inOut.sendBool(true,"finished turning");
		superc.inOut.readString("I can scan again after move");
	}

	public void turnLeft() throws IOException
	{
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
		superc.inOut.sendBool(true,"finished turning");
		superc.inOut.readString("I can scan again after move");
	}

	public void turnAround() throws IOException
	{
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
		superc.inOut.sendBool(true,"finished turning");
		superc.inOut.readString("I can scan again after move");
	}
}