/**
 * Class which starts the decided Configuration of the predator prey pursuit problem
 */
class RobotSim
{
	/**
	 * The offset value that the robot should use for motor setup on startup
	 */
	public static double offset;

	/**
	 * The method which either starts a group of robots which are simulated or starts a single robot which is physical.
	 * @throws InterruptedException
	 */
	public static void startUP() throws InterruptedException
	{
		if (Config.Simulated)
		{
			int howManyRobots = 4;
			Robot[] robots = new Robot[howManyRobots];
			for (int i = 0; i < howManyRobots; i++)
			{
				robots[i] = new Robot("localhost", 1234 + i);
			}
			Thread[] threads = new Thread[howManyRobots];
			for (int i = 0; i < howManyRobots; i++)
			{
				threads[i] = new Thread(robots[i]);
			}
			for (int i = 0; i < howManyRobots; i++)
			{
				threads[i].start();
			}
			for (int i = 0; i < howManyRobots; i++)
			{
				threads[i].join();
			}
		}
		else
		{
			double[] offsets = new double[] { 5.6, 5.475, 5.587, 5.55 };
			offset = offsets[Config.robotNumber];
			new Robot(Config.ip, 1234 + Config.robotNumber).run();
		}
	}
}
