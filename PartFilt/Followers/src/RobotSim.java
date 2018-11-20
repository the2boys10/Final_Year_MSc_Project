@SuppressWarnings("unused")
public class RobotSim
{
//    public static final String pred1IP = "172.20.1.129";
//    /**
//     * The Ip of predator 2
//     */
//    public static final String pred2IP = "172.20.1.130";
//    /**
//     * The Ip of predator 3
//     */
//    public static final String pred3IP = "172.20.1.132";
//    /**
//     * The Ip of predator 4
//     */
//    public static final String pred4IPLead = "172.20.1.128";
	public static void main(String args[]) throws InterruptedException
	{
		if(Config.Simulated)
		{
			int howManyRobots = 3;
			Robot[] robots = new Robot[howManyRobots];
			for (int i = 0; i < howManyRobots; i++)
			{
				robots[i] = new Robot("localhost", 1224 + i);
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
			int howManyRobots = 1;
			int robotNumber = 1;
			new Robot("172.20.1.133",1224+robotNumber).run();
		}
		System.exit(0);
	}
}
