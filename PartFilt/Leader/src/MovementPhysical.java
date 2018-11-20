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

import java.io.IOException;

public class MovementPhysical extends Movementlead
{
	private Wheel leftWheel = null;
	private Wheel rightWheel = null;
	private Chassis myChassis = null;
	private EV3ColorSensor colorRight = null;
	private EV3ColorSensor colorLeft = null;
	private EV3GyroSensor gyro = null;
	private SampleProvider gyroSamples = null;
	private final NXTRegulatedMotor leftMotor;
	private final NXTRegulatedMotor rightMotor;

	public MovementPhysical(LeaderSub superc, Port colorLeftPort, Port colorRightPort, Port gyroPort, NXTRegulatedMotor leftWheelPort, NXTRegulatedMotor rightWheelPort)
	{
		super(superc);
		gyro = new EV3GyroSensor(gyroPort);
		gyroSamples = gyro.getAngleMode();
		colorRight = new EV3ColorSensor(colorRightPort);
		colorLeft = new EV3ColorSensor(colorLeftPort);
		leftWheel = WheeledChassis.modelWheel(rightWheelPort, 4.32).offset(-5.760); // 5.760
		rightWheel = WheeledChassis.modelWheel(leftWheelPort, 4.32).offset(5.760);
		myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(myChassis);
		leftMotor = leftWheelPort;
		rightMotor = rightWheelPort;
		colorRight.getColorID();
		colorLeft.getColorID();
	}

	public void moveForward() throws Exception
	{
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("makeMove", "I would like to move forward");
		boolean successMove = true;
		int temp = 1;
		temp = superc.scanner.scanForwardDistance(false);
		superc.inOut.sendChar(superc.thinkOrientation, "Here's my orientation");
		Delay.msDelay(100);
		cumalativeLoss += (getGyroAngle());
		resetGyroTacho();
		int counter = 0;
		while (Math.abs(cumalativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumalativeLoss) / 6, 1) * cumalativeLoss);
			Delay.msDelay(100);
			cumalativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumalativeLoss) > 4)
				pilot.travel(1);
		}
		if (temp != 0)
		{
			pilot.setLinearSpeed(10);
			pilot.travel(25, true);
			while (pilot.isMoving())
			{
				if (colorRight.getColorID() == 7)
				{
					pilot.stop();
					pilot.setLinearSpeed(2.5);
					if (colorRight.getColorID() != 7)
					{
						pilot.travel(-2.5, true);
						while (colorRight.getColorID() != 7)
						{

						}
						pilot.stop();
					}
					if (colorRight.getColorID() == 7)
					{
						pilot.travel(-10, true);
						while (colorRight.getColorID() == 7)
						{

						}
						pilot.stop();
					}
				}
				if (colorLeft.getColorID() == 7)
				{
					pilot.stop();
					pilot.setLinearSpeed(2.5);
					if (colorLeft.getColorID() != 7)
					{
						pilot.travel(-2.5, true);
						while (colorLeft.getColorID() != 7)
						{

						}
						pilot.stop();
					}
					if (colorLeft.getColorID() == 7)
					{
						pilot.travel(-10, true);
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
			while (colorLeft.getColorID() != 7 || colorRight.getColorID() != 7)
			{
				if (colorLeft.getColorID() != 7 && successMove)
				{
					leftMotor.setSpeed(40);
					leftMotor.forward();
					while (leftMotor.isMoving())
					{
						if (colorLeft.getColorID() == 7)
						{
							leftMotor.stop();
						}
						else if (leftMotor.getTachoCount() - tachoA > 300)
						{
							leftMotor.stop();
							pilot.travel(-8);
							counter = 0;
							do
							{
								counter++;
								Delay.msDelay(100);
								cumalativeLoss += (getGyroAngle());
								resetGyroTacho();
								pilot.rotate(Math.min(Math.abs(cumalativeLoss) / 6, 1) * cumalativeLoss);
							}
							while (Math.abs(cumalativeLoss) > 4 && counter < 10);
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
				if (colorRight.getColorID() != 7 && successMove)
				{
					rightMotor.setSpeed(40);
					rightMotor.forward();
					while (rightMotor.isMoving())
					{
						if (colorRight.getColorID() == 7)
						{
							rightMotor.stop();
						}
						else if (rightMotor.getTachoCount() - tachoB > 300)
						{
							rightMotor.stop();
							pilot.travel(-8);
							counter = 0;
							do
							{
								counter++;
								Delay.msDelay(100);
								cumalativeLoss += (getGyroAngle());
								resetGyroTacho();
								pilot.rotate(Math.min(Math.abs(cumalativeLoss) / 6, 1) * cumalativeLoss);
							}
							while (Math.abs(cumalativeLoss) > 4 && counter < 10);
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
				if (!successMove)
				{
					break;
				}
			}
			if (successMove)
			{
				cumalativeLoss = 0;
				Delay.msDelay(100);
				resetGyroTacho();
				cumalativeLoss = 0;
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
		superc.inOut.sendBool(true, "Relaying movement success");
	}

	public void turnRight() throws IOException
	{
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("Turn", "I would like to turn");
		if (superc.thinkOrientation == 'N')
		{
			superc.thinkOrientation = 'E';
		}
		else if (superc.thinkOrientation == 'E')
		{
			superc.thinkOrientation = 'S';
		}
		else if (superc.thinkOrientation == 'S')
		{
			superc.thinkOrientation = 'W';
		}
		else if (superc.thinkOrientation == 'W')
		{
			superc.thinkOrientation = 'N';
		}
		pilot.rotate(-90);
		Delay.msDelay(100);
		cumalativeLoss += (-90 + getGyroAngle());
		resetGyroTacho();
		int counter = 0;
		while (Math.abs(cumalativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumalativeLoss) / 6, 1) * cumalativeLoss);
			Delay.msDelay(100);
			cumalativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumalativeLoss) > 4)
				pilot.travel(1);
		}
		superc.inOut.sendBool(true,"finished turning");
	}

	public void turnLeft() throws IOException
	{
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("Turn", "I would like to turn");
		if (superc.thinkOrientation == 'N')
		{
			superc.thinkOrientation = 'W';
		}
		else if (superc.thinkOrientation == 'E')
		{
			superc.thinkOrientation = 'N';
		}
		else if (superc.thinkOrientation == 'S')
		{
			superc.thinkOrientation = 'E';
		}
		else if (superc.thinkOrientation == 'W')
		{
			superc.thinkOrientation = 'S';
		}
		pilot.rotate(90);
		Delay.msDelay(100);
		cumalativeLoss += 90 + getGyroAngle();
		resetGyroTacho();
		int counter = 0;
		while (Math.abs(cumalativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumalativeLoss) / 6, 1) * cumalativeLoss);
			Delay.msDelay(100);
			cumalativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumalativeLoss) > 3)
				pilot.travel(1);
		}
		superc.inOut.sendBool(true,"finished turning");
	}

	public void turnAround() throws IOException
	{
		superc.inOut.sendString("takeMove", "I wanna take move");
		superc.inOut.readString("I can take move");
		superc.inOut.sendString("Turn", "I would like to turn");
		if (superc.thinkOrientation == 'N')
		{
			superc.thinkOrientation = 'S';
		}
		else if (superc.thinkOrientation == 'E')
		{
			superc.thinkOrientation = 'W';
		}
		else if (superc.thinkOrientation == 'S')
		{
			superc.thinkOrientation = 'N';
		}
		else if (superc.thinkOrientation == 'W')
		{
			superc.thinkOrientation = 'E';
		}
		pilot.rotate(-180);
		Delay.msDelay(100);
		cumalativeLoss += (-180 + getGyroAngle());
		resetGyroTacho();
        int counter = 0;
		while (Math.abs(cumalativeLoss) > 4 && counter < 10)
		{
			counter++;
			pilot.rotate(Math.min(Math.abs(cumalativeLoss) / 6, 1) * cumalativeLoss);
			Delay.msDelay(100);
			cumalativeLoss += (getGyroAngle());
			resetGyroTacho();
			if (Math.abs(cumalativeLoss) > 3)
				pilot.travel(1);
		}
		superc.inOut.sendBool(true,"finished turning");
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
