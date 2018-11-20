public class Leader
{
	public static void main(String args[])
	{
		if(Config.Simulated)
		{
			new LeaderSub("localhost",1227).run();
		}
		else
		{
			new LeaderSub("172.20.1.133",1227).run();
		}
	}
}
