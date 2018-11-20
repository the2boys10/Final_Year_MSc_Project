import lejos.robotics.SampleProvider;

import java.io.IOException;

public abstract class Scanner
{
	final Robot superc;
	SampleProvider distanceP;
	SampleProvider checkP;
	Scanner(Robot superc)
	{
		this.superc = superc;
	}
	
	protected abstract int scanForwardDistance(boolean front) throws IOException;

    public void scanDetect()
	{

	};
}
