/**
 * Class which stores configurations on the program, also used to start the program.
 */
class Config
{
	// 131 = 05 prey 5.431
	// 129 = 08 4 5.6
	// 130 = 14 1 5.475
	// 132 = 4 2 5.587
	// 128 = ? 3 5.55
	/**
	 * The ip of the main server
	 */
	public static final String ip = "172.20.1.133";
	/**
	 * The Number of the robot we wish to send the program to.
	 */
	public static final int robotNumber = 3;
	/**
	 * Sets whether the program should be used in a simulated manner, in which case all robots will be simulated using a single launch.
	 */
	public static final boolean Simulated = true;


	/**
	 * Launches this configuration
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException
	{
		RobotSim.startUP();
	}
}
