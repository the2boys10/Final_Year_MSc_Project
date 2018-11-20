import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;

/**
 * Class which encompasses the actions as well as beliefs of the current agent
 */
class Robot implements Runnable
{
    /**
     * The port that the agent is connecting to the server through
     */
	private final int port;
    /**
     * The ip of the main server
     */
	private final String ip;
    /**
     * The communications object that the current agent is using
     */
	public Com inOut;
    /**
     * The ID of the current agent
     */
	public int ID;
    /**
     * The beliefs that the current agent holds about the positions about other agents and prey
     */
	public Cell[][] map;
    /**
     * The current xCord of the agent
     */
	int xCord;
    /**
     * The current yCord of the agent
     */
	int yCord;
    /**
     * Flag to determine if the prey has been found and captured
     */
	private boolean finished = false;
    /**
     * The orientation of the current agent
     */
	char orientation = 'N';
    /**
     * The scanner that the agent will be using for observing the environment
     */
	public Scanner scanner;
    /**
     * The movement object used to move the robot forward or face certain directions
     */
	public Movement mover;
    /**
     * Cells which have been observed in the current scan.
     */
	@SuppressWarnings("Convert2Diamond") public final LinkedList<Cell> updatedCells = new LinkedList<Cell>();
    /**
     * Has the prey been found?
     */
	private boolean Found;
    /**
     * Has the agent observed something in-front of itself
     */
	public boolean isFront = false;
    /**
     * Did we need to set the cell in-front of us to an object due to it being erroneous
     */
	private Cell whichCellChanged = null;
    /**
     * Do we wish to scan using the robots sensors or communication to the server <ul><li>1: Use own sensors</li><li>2: Use data from server</li></ul>
     */
	public int scanningPhys = 1;
    /**
     * What policy the agent should be following
     */
	private int policy;
    /**
     * What strategy the agent should be following
     */
	private int strategy;
    /**
     * Should the agent scan in all directions?
     */
	private int ScanAllAxis = 1;

    /**
     *
     * @param ip The ip that the main server is using
     * @param port The port that the main server is using
     */
	public Robot(String ip, int port)
	{
		this.port = port;
		this.ip = ip;
	}

    /**
     * Sets up all physical components, connections of the robot, also determines the policy and search strategy the agent should use.
     */
	public void run()
	{
		try
		{
			// If we are using the robot display the prey and set up the sensors else create a mover which is simulated
			if (Config.Simulated)
			{
				mover = new MovementSimulated(this);
				scanner = new ScannerSimulated(this);
			}
			else
			{
				Button.LEDPattern(5);
				EV3 ev3 = (EV3) BrickFinder.getLocal();
				TextLCD lcd = ev3.getTextLCD();
				lcd.drawString("I'm Pred", 2, 3);
				mover = new MovementPhysical(this, SensorPort.S1, SensorPort.S4, SensorPort.S2, Motor.A, Motor.D);
				scanner = new ScannerPhysical(this, SensorPort.S3, Motor.B);
			}
			inOut = new Com(ip, port);
			// THIS PRINT STATEMENT IS NEEDED IF AUTO LAUNCHING IS SET TO TRUE ON THE SERVER
			// IT IS THE SIGNAL THAT THE ROBOT HAS STARTED UP SUCCESSFULLY.
			System.out.println("connected");
			ID = inOut.readInt("Reading my ID");
			map = (Cell[][]) inOut.readObj("Reading Map");
			int[] position = (int[]) inOut.readObj("reading location");
			xCord = position[0];
			yCord = position[1];
			// If we have been told to scan in a simulated fashion remake the scanner
			if(!Config.Simulated)
			{
				EV3 ev3 = (EV3) BrickFinder.getLocal();
				TextLCD lcd = ev3.getTextLCD();
				lcd.drawString("put me at " + xCord + " , " + yCord, 2, 4);
			}
			inOut.readString("starting");
			inOut.readString("firstTurn");
			// reset the techo counter for the gyroscope (allows for robot to be moved at start of simulation
			mover.resetGyroTacho();
			policy = inOut.readInt("policy");
			strategy = inOut.readInt("searchStrategy");
			scanningPhys = inOut.readInt("scanStrategy");
			ScanAllAxis = inOut.readInt("scanStrategy");
			if (scanningPhys == 2)
			{
				scanner = new ScannerSimulated(this);
			}
			mainLoop();
			inOut.sendString("exit", "please exit");
			inOut.cleanUp();
			scanner.cleanUp();
			mover.cleanUp();
		}
		catch (IOException | ClassNotFoundException e)
		{
			// THIS PRINT STATEMENT IS NEEDED IF AUTO LAUNCHING IS SET TO TRUE ON THE SERVER
			// IT IS THE SIGNAL THAT THE ROBOT HAS FAILED.
			System.out.println("failed");
			if (Config.Simulated)
			{
				FileWriter fw;
				try
				{
					fw = new FileWriter("exception.txt", true);
					PrintWriter pw = new PrintWriter(fw);
					e.printStackTrace(pw);
					pw.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			// THIS PRINT STATEMENT IS NEEDED IF AUTO LAUNCHING IS SET TO TRUE ON THE SERVER
			// IT IS THE SIGNAL THAT THE ROBOT HAS FAILED.
			System.out.println("failed");
		}
	}

    /**
     * Method which determines the next move of the agent, repeats until all agents have fully surrounded the prey.
     * @throws IOException
     * @throws ClassNotFoundException
     */
	private void mainLoop() throws IOException, ClassNotFoundException
	{
		// whilst the predators have not caught the prey
		while (!finished)
		{
			// At the start of the turn set there to be an empty space in-front of the agent
			isFront = false;
			// The cell in-front of us is meant to be empty scan else skip scanning
			if (Util.whatsInFront(this) == 0 || Util.whatsInFront(this) == 10)
			{
				scanner.scanFront();
				inOut.sendString("updateMap", "updating map");
				inOut.sendObject(updatedCells, "updating cells");
				updatedCells.clear();
			}
			// tell the server to update the global map.
			inOut.sendString("finishedStartScanning", "please update your map");
			inOut.readString("I can continue?");
			// if we are able to scan all axis then turn and scan them all and update the server
			if (ScanAllAxis == 2)
			{
				for (int i = 0; i < 3; i++)
				{
					inOut.sendString("takeMove", "I wanna take move");
					inOut.readString("I can take move");
					mover.turnLeft();
					if (Util.whatsInFront(this) == 0 || Util.whatsInFront(this) == 10)
					{
						scanner.scanFront();
						inOut.sendString("updateMap", "updating map");
						inOut.sendObject(updatedCells, "updating cells");
						updatedCells.clear();
					}
					inOut.sendString("finishedStartScanning", "please update your map");
					inOut.readString("I can continue?");
				}

			}
			inOut.sendString("IsFound", "is it found?");
			Found = inOut.readBool("is it found?");
			if (Found)
			{
				inOut.sendString("isCaptured", "is it captured?");
				if (inOut.readBool("is it captured?"))
				{
					finished = true;
					break;
				}
			}
			inOut.sendString("readMap", "I want an up to date map");
			map = (Cell[][]) inOut.readObj("Read map");
			if (isFront)
			{
				if (Util.whatsInFront(this) == 0)
				{
					switch (orientation)
					{
						case 'N':
							whichCellChanged = map[xCord - 1][yCord];
							break;
						case 'E':
							whichCellChanged = map[xCord][yCord + 1];
							break;
						case 'W':
							whichCellChanged = map[xCord][yCord - 1];
							break;
						default:
							whichCellChanged = map[xCord + 1][yCord];
							break;
					}
					whichCellChanged.contents = 1;
				}
			}
			int movement = useSearch();
			inOut.sendString("planMove", "plan my move");
			inOut.sendInt(movement, "sending possible moves");
			inOut.sendChar(orientation, "sending orientation");
			int move = inOut.readInt("what move to take");
			inOut.sendString("takeMove", "I wanna take move");
			inOut.readString("I can take move");
			if (move != 0)
			{
				if (policy == 2)
					mover.makeMove(move);
				else
					mover.makeMove2(move);
			}
			else
			{
				inOut.sendString("skip", "would like to skip");
				inOut.readString("Everyone finished");
			}
			if (policy == 2)
			{
				if (Util.whatsInFront(this) == 0 || Util.whatsInFront(this) == 10)
				{
					scanner.scanFront();
					inOut.sendString("updateMap", "updating map");
					inOut.sendObject(updatedCells, "updating cells");
					updatedCells.clear();
				}
				inOut.sendString("finishedStartScanning", "please update your map");
				inOut.readString("I can continue?");
				inOut.sendString("readMap", "I want an up to date map");
				map = (Cell[][]) inOut.readObj("Reading Map");
				if (whichCellChanged != null)
				{
					whichCellChanged.contents = 1;
				}
				inOut.sendString("IsFound", "is it found?");
				Found = inOut.readBool("is it found?");
				int movement2 = useSearch();
				if (!(Util.notIn(move, movement2) && !isFront))
				{
					move = 0;
				}
				inOut.sendString("planMove", "plan my move");
				inOut.sendInt(move, "sending possible moves");
				inOut.sendChar(orientation, "sending orientation");
				move = inOut.readInt("what move to take");
				inOut.sendString("takeMove", "I wanna take move");
				inOut.readString("I can take move");
				mover.moveForwardMovement(move);
			}
			inOut.sendString("readMap", "I want an up to date map");
			map = (Cell[][]) inOut.readObj("Reading Map");
			whichCellChanged = null;
			if (Util.whatsInFront(this) == 0 || Util.whatsInFront(this) == 10)
			{
				inOut.sendString("scanClean", "Want to scan");
				inOut.readString("can scan now");
				scanner.scanFront();
				inOut.sendString("updateMap", "updating map");
				inOut.sendObject(updatedCells, "updating cells");
				updatedCells.clear();
			}
			if (ScanAllAxis == 2)
			{
				for (int i = 0; i < 3; i++)
				{
					inOut.sendString("takeMove", "I wanna take move");
					inOut.readString("I can take move");
					mover.turnLeft();
					if (Util.whatsInFront(this) == 0 || Util.whatsInFront(this) == 10)
					{
						scanner.scanFront();
						inOut.sendString("updateMap", "updating map");
						inOut.sendObject(updatedCells, "updating cells");
						updatedCells.clear();
					}
					inOut.sendString("finishedStartScanning", "please update your map");
					inOut.readString("I can continue?");
				}

			}
			inOut.sendString("finishedEndScanning", "finished scanning");
			inOut.readString("waiting for next ");
		}
	}

    /**
     * Method that determines the search strategy to use
     * @return The moves that the agent feels are the most beneficial.
     */
	private int useSearch()
	{
		int movement2 = 0;
		if (!Found)
		{
			switch (strategy)
			{
				case 1:
					movement2 = BFSsearchNoIncentive.search(xCord, yCord, this);
					break;
				case 2:
					movement2 = BFSsearchTurnCostNoIncentive.search(xCord, yCord, orientation, this);
					break;
				case 3:
					movement2 = BFSsearch.search(xCord, yCord, this);
					break;
				case 4:
					movement2 = BFSsearchTurnCost.search(xCord, yCord, orientation, this);
					break;
			}
		}
		else
		{
			switch (strategy)
			{
				case 1:
					movement2 = BFSsearchFoundNoIncentive.search(xCord, yCord, this);
					break;
				case 2:
					movement2 = BFSsearchTurnCostFoundNoIncentive.search(xCord, yCord, orientation, this);
					break;
				case 3:
					movement2 = BFSsearchFound.search(xCord, yCord, this);
					break;
				case 4:
					movement2 = BFSsearchTurnCostFound.search(xCord, yCord, orientation, this);
					break;
			}
		}
		return movement2;
	}
}
