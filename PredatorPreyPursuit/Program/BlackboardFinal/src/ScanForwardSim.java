import java.io.IOException;

/**
 * Class used to simulate scanning of the predator and prey sends back the distance that the agent should have read given the current belief state of the server.
 */
class ScanForwardSim
{
    /**
     * Lets the predator scan in-front in a simulated manner, sends the distance that it would scan in terms of cells
     * @param superc The predator that wants to scan forward.
     * @throws IOException
     */
    public static void scanForward(outBoundConnection superc) throws IOException
    {
        char orientation = superc.inOut.readChar("reading orientation");
        int xCord = superc.xCord;
        int yCord = superc.yCord;
        int distance = 0;
        int counter = 20;
        switch (orientation)
        {
            case 'N':
                xCord--;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    xCord--;
                    counter--;
                }
                break;
            case 'E':
                yCord++;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    yCord++;
                    counter--;
                }
                break;
            case 'S':
                xCord++;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    xCord++;
                    counter--;
                }
                break;
            case 'W':
                yCord--;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    yCord--;
                    counter--;
                }
                break;
        }
        superc.inOut.sendInt(distance, "sending distance");
    }

    /**
     * Lets the predator scan in-front in a simulated manner, sends the distance that it would scan in terms of cells
     * @param superc The prey that would like to scan forward.
     * @throws IOException
     */
    public static void scanForward(Prey superc) throws IOException
    {
        char orientation = superc.inOut.readChar("reading orientation");
        int xCord = superc.xCord;
        int yCord = superc.yCord;
        int distance = 0;
        int counter = 3;
        switch (orientation)
        {
            case 'N':
                xCord--;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    xCord--;
                    counter--;
                }
                break;
            case 'E':
                yCord++;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    yCord++;
                    counter--;
                }
                break;
            case 'S':
                xCord++;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    xCord++;
                    counter--;
                }
                break;
            case 'W':
                yCord--;
                counter--;
                while (Server.map[xCord][yCord].contents == 0 && counter > 0)
                {
                    distance++;
                    yCord--;
                    counter--;
                }
                break;
        }
        superc.inOut.sendInt(distance, "sending distance");
    }
}
