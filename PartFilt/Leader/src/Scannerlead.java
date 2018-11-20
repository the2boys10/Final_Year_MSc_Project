import lejos.robotics.SampleProvider;

import java.io.IOException;

public abstract class Scannerlead
{
	final LeaderSub superc;
	SampleProvider distanceP;
	SampleProvider checkP;
	Scannerlead(LeaderSub superc)
	{
		this.superc = superc;
	}
	
	protected abstract int scanForwardDistance(boolean front) throws IOException;

	public void scanDetect()
	{

	};
}
