import java.io.IOException;

public class ScannerSimulated extends Scanner
{

	public ScannerSimulated(Robot superc)
	{
		super(superc);
	}

	@Override
	protected int scanForwardDistance(boolean movingForward) throws IOException
	{
		superc.inOut.sendString("scanForward", "scanning forward");
		superc.inOut.sendChar(superc.orientation, "Sending orientation");
        return superc.inOut.readInt("reading distance");
	}
}