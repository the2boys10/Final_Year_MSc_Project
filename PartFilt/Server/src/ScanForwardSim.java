import java.io.IOException;
import java.util.Random;

class ScanForwardSim
{

    public static int scanForward(outBoundConnection superc) throws IOException
    {
        char orientation = superc.inOut.readChar();
        Server.orientations[superc.identity] = orientation;
        int xCord = superc.xCord;
        int yCord = superc.yCord;
        int distance = 0;
        int counter = 10;
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
        return distance;
    }
}
