import java.io.IOException;

public class ScannerSimulatedlead extends Scannerlead
{

	public ScannerSimulatedlead(LeaderSub superc)
	{
		super(superc);
	}

	@Override
	protected int scanForwardDistance(boolean moveForward) throws IOException
	{
		superc.inOut.sendString("scanForward", "scanning forward");
		superc.inOut.sendChar(superc.orientation, "Sending orientation");
        return superc.inOut.readInt("reading distance");
	}
}