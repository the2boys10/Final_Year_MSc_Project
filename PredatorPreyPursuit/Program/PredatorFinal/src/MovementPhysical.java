import java.io.IOException;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

/**
 * Class which sets up the physical components of the EV3 in relation to movement such as the motors and color sensor
 */
public class MovementPhysical extends Movement
{
	private final EV3ColorSensor colorRight;
	private final EV3ColorSensor colorLeft;
	private final EV3GyroSensor gyro;
	private final SampleProvider gyroSamples;
	private final NXTRegulatedMotor leftMotor;
	private final NXTRegulatedMotor rightMotor;

	/**
	 * Sets up the EV3 pilot as well as setting up all the components
	 * @param superc The robot calling the class
	 * @param colorLeftPort The left color sensor of the robot
	 * @param colorRightPort The right color sensor of the robot
	 * @param gyroPort The port that the Gyroscope is plugged into
	 * @param leftWheelPort The port the left motor is plugged into
	 * @param rightWheelPort The port the right motor is plugged into
	 */
	public MovementPhysical(Robot superc, Port colorLeftPort, Port colorRightPort, Port gyroPort, NXTRegulatedMotor leftWheelPort, NXTRegulatedMotor rightWheelPort)
	{
		super(superc);
		gyro = new EV3GyroSensor(gyroPort);
		gyroSamples = gyro.getAngleMode();
		colorRight = new EV3ColorSensor(colorRightPort);
		colorLeft = new EV3ColorSensor(colorLeftPort);
		Wheel leftWheel = WheeledChassis.modelWheel(rightWheelPort, 4.32).offset(-RobotSim.offset);
		Wheel rightWheel = WheeledChassis.modelWheel(leftWheelPort, 4.32).offset(RobotSim.offset);
		Chassis myChassis = new WheeledChassis(new Wheel[]{leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(myChassis);
		leftMotor = leftWheelPort;
		rightMotor = rightWheelPort;
		colorRight.getColorID();
		colorLeft.getColorID();
	}

	public void moveForward() throws IOException
	{
		superc.inOut.sendString("makeMove", "I would like to move forward");
		// create a boolean to store whether the movement had been successful
		boolean successMove = true;
		// set our current location to be empty since we are moving
		superc.map[superc.xCord][superc.yCord].contents = 0;
		// variable to change the scanners provider type to scanning
		int temp = 1;
		if(superc.scanningPhys==1)
			temp = superc.scanner.scanForwardDistance(false);
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
		// send the new location
		superc.inOut.sendObject(new int[] { superc.xCord, superc.yCord }, "want to move here");
		switch (superc.orientation)
		{
			case 'N':
				superc.xCord++;
				break;
			case 'E':
				superc.yCord--;
				break;
			case 'S':
				superc.xCord--;
				break;
			default:
				superc.yCord++;
				break;
		}
		Delay.msDelay(100);
		cumulativeLoss += (getGyroAngle());
		resetGyroTacho();
		int counter = 0;
		// try to fix our orientation with the gyroscope, move forward if we have failed, try up to 10 times
		while (Math.abs(cumulativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumulativeLoss) / 6, 1) * cumulativeLoss);
			Delay.msDelay(100);
			cumulativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumulativeLoss) > 4)
				pilot.travel(1);
		}
		// If we have not seen something directly in-front of us start the movement process
		if (temp != 0)
		{
			// lower our speed and travel forward, during the movement check if either of the color sensors has hit the line.
			pilot.setLinearSpeed(10);
			pilot.travel(25, true);
			while (pilot.isMoving())
			{
				// if the right color sensor has hit the line then stop the pilot and move back slightly then move forward whilst only focusing on the right color sensor
				if (colorRight.getColorID() == 7)
				{
					pilot.stop();
					pilot.setLinearSpeed(2.5);
					if (colorRight.getColorID() != 7)
					{
						pilot.travel(-5, true);
						//noinspection StatementWithEmptyBody
						while (colorRight.getColorID() != 7)
						{

						}
						pilot.stop();
					}
					if (colorRight.getColorID() == 7)
					{
						pilot.travel(-10, true);
						//noinspection StatementWithEmptyBody
						while (colorRight.getColorID() == 7)
						{

						}
						pilot.stop();
					}
				}
				// if the left color sensor has hit the line then stop the pilot and move back slightly then move forward whilst only focusing on the left color sensor
				if (colorLeft.getColorID() == 7)
				{
					pilot.stop();
					pilot.setLinearSpeed(2.5);
					if (colorLeft.getColorID() != 7)
					{
						pilot.travel(-5, true);
						//noinspection StatementWithEmptyBody
						while (colorLeft.getColorID() != 7)
						{

						}
						pilot.stop();
					}
					if (colorLeft.getColorID() == 7)
					{
						pilot.travel(-10, true);
						//noinspection StatementWithEmptyBody
						while (colorLeft.getColorID() == 7)
						{

						}
						pilot.stop();
					}
				}
			}
			pilot.setLinearSpeed(20);
			double tachoA = leftMotor.getTachoCount();
			double tachoB = rightMotor.getTachoCount();
			// while both color sensors have not hit the line
			while (colorLeft.getColorID() != 7 || colorRight.getColorID() != 7)
			{
				// if the left color sensor has not hit the line then move the left motor forward and stop it when the
				// sensor hits the line
				if (colorLeft.getColorID() != 7)
				{
					leftMotor.setSpeed(40);
					leftMotor.forward();
					while (leftMotor.isMoving())
					{
						if (colorLeft.getColorID() == 7)
						{
							leftMotor.stop();
						}
						// If it dosn't after a certain number of rotation then reverse, fix orientation using gyroscope and rescan to check for obstacles
						// or the predator, if there is nothing in-front of us, reattempt line alignment
						else if (leftMotor.getTachoCount() - tachoA > 300)
						{
							leftMotor.stop();
							pilot.travel(-8);
							counter = 0;
							do
							{
								counter++;
								Delay.msDelay(100);
								cumulativeLoss += (getGyroAngle());
								resetGyroTacho();
								pilot.rotate(Math.min(Math.abs(cumulativeLoss) / 6, 1) * cumulativeLoss);
							}
							while (Math.abs(cumulativeLoss) > 4 && counter < 10);
							if(superc.scanningPhys==1)
								temp = superc.scanner.scanForwardDistance(false);
							if (temp == 0)
							{
								successMove = false;
								pilot.travel(-6);
								break;
							}
							pilot.travel(6);
							tachoA = leftMotor.getTachoCount();
							tachoB = rightMotor.getTachoCount();
						}
					}
				}
				// if the left color sensor has not hit the line then move the left motor forward and stop it when the
				// sensor hits the line
				if (colorRight.getColorID() != 7&& successMove)
				{
					rightMotor.setSpeed(40);
					rightMotor.forward();
					while (rightMotor.isMoving())
					{
						if (colorRight.getColorID() == 7)
						{
							rightMotor.stop();
						}
						// If it dosn't after a certain number of rotation then reverse, fix orientation using gyroscope and rescan to check for obstacles
						// or the predator, if there is nothing in-front of us, reattempt line alignment else stop using line alignment and move backwards.
						else if (rightMotor.getTachoCount() - tachoB > 300)
						{
							rightMotor.stop();
							pilot.travel(-8);
							counter = 0;
							do
							{
								counter++;
								Delay.msDelay(100);
								cumulativeLoss += (getGyroAngle());
								resetGyroTacho();
								pilot.rotate(Math.min(Math.abs(cumulativeLoss) / 6, 1) * cumulativeLoss);
							}
							while (Math.abs(cumulativeLoss) > 4 && counter < 10);
							if(superc.scanningPhys==1)
								temp = superc.scanner.scanForwardDistance(false);
							if (temp == 0)
							{
								successMove = false;
								pilot.travel(-6);
								break;
							}
							pilot.travel(6);
							tachoA = leftMotor.getTachoCount();
							tachoB = rightMotor.getTachoCount();
						}
					}
				}
				if(!successMove)
				{
					break;
				}
			}
			// if we have moved successfully finish off the move by resetting the gyroscope and moving forward into the cell completely.
			if (successMove)
			{
				cumulativeLoss = 0;
				Delay.msDelay(100);
				resetGyroTacho();
				cumulativeLoss = 0;
				if(superc.scanningPhys==1)
					temp = superc.scanner.scanForwardDistance(true);
				if (temp == 0)
				{
					successMove = false;
					pilot.travel(-17);
				}
				if (successMove)
				{
					pilot.travel(10);
				}
			}
		}
		else
		{
			successMove = false;
		}
		if (successMove)
		{
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
		}
		// relay whether the move was successful.
		superc.map[superc.xCord][superc.yCord].contents = superc.ID;
		superc.inOut.sendBool(successMove, "Relaying movement success");
		superc.inOut.readString("I can scan again after move");
	}

	public void turnRight() throws IOException
	{
		// Update our orientation
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
		// rotate the agent and update our gyroscope reading
		pilot.rotate(-90);
		Delay.msDelay(100);
		cumulativeLoss += (-90 + getGyroAngle());
		resetGyroTacho();
		int counter = 0;
		// attempt to re-correct multiple times using gyroscope if needed, move forward if multiple correction steps are required.
		while (Math.abs(cumulativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumulativeLoss) / 6, 1) * cumulativeLoss);
			Delay.msDelay(100);
			cumulativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumulativeLoss) > 4)
				pilot.travel(1);
		}
		superc.inOut.sendBool(true, "finished turning");
		superc.inOut.readString("I can scan again after move");
	}

	public void turnLeft() throws IOException
	{
		// Update our orientation
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
		// rotate the agent and update our gyroscope reading
		pilot.rotate(90);
		Delay.msDelay(100);
		cumulativeLoss += 90 + getGyroAngle();
		resetGyroTacho();
		int counter = 0;
		// attempt to re-correct multiple times using gyroscope if needed, move forward if multiple correction steps are required.
		while (Math.abs(cumulativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumulativeLoss) / 6, 1) * cumulativeLoss);
			Delay.msDelay(100);
			cumulativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumulativeLoss) > 3)
				pilot.travel(1);
		}
		superc.inOut.sendBool(true, "finished turning");
		superc.inOut.readString("I can scan again after move");
	}

	public void turnAround() throws IOException
	{
		// Update our orientation
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
		// rotate the agent and update our gyroscope reading
		pilot.rotate(-180);
		Delay.msDelay(100);
		cumulativeLoss += (-180 + getGyroAngle());
		resetGyroTacho();
        int counter = 0;
		// attempt to re-correct multiple times using gyroscope if needed, move forward if multiple correction steps are required.
		while (Math.abs(cumulativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumulativeLoss) / 6, 1) * cumulativeLoss);
			Delay.msDelay(100);
			cumulativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumulativeLoss) > 3)
				pilot.travel(1);
		}
		superc.inOut.sendBool(true, "finished turning");
		superc.inOut.readString("I can scan again after move");
	}

	public void cleanUp()
	{
		colorLeft.close();
		colorRight.close();
		gyro.close();
	}

	private float getGyroAngleRaw()
	{
		gyroSamples.fetchSample(angle, 0);
		return angle[0];
	}

	public float getGyroAngle()
	{
		float rawAngle = getGyroAngleRaw();
		return rawAngle - gyroTacho;
	}

	public void resetGyroTacho()
	{
		gyroTacho += getGyroAngle();
	}
}
