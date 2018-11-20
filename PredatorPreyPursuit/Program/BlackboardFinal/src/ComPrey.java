import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Robert Johnson Class to assist in the communication to prey
 */
@SuppressWarnings("unused") public class ComPrey
{
    private OutputStream out;
    private InputStream in;
    private DataOutputStream dOut;
    private DataInputStream dIn;
    private ObjectOutputStream dOutObj;
    private ObjectInputStream dInObj;
    private ServerSocket server;

    /**
     * @param port The port that predators should connect to
     * @throws IOException
     */
    public ComPrey(int port) throws IOException
    {
        server = new ServerSocket(port);
        if (Config.rapidTesting)
        {
            synchronized (Server.waitForAllWaiting)
            {
                Server.howManyWait++;
                if (Server.howManyWait == 5)
                {
                    Server.howManyWait = 0;
                    String fileLoc = "/Users/robert/Desktop/Tests/simResults/" + Config.PreyPolicy + "&" + Config.PredPolicy + "&" + Config.PredSearch + "&" + Config.instantReward + "&" + Config.dissipationGrid + "/Test" + Server.whereToSave;
                    ProcessBuilder builder = new ProcessBuilder("java", "-cp", "/Users/robert/Desktop/CommentedVersion/commentcode/PreyNoTurnToTurn2/bin", "Prey");
                    builder.redirectOutput(new File(fileLoc + "/testOut1"));
                    builder.redirectError(new File(fileLoc + "/testOut1"));
                    builder.start(); // may throw IOException
                    ProcessBuilder builder2 = new ProcessBuilder("java", "-cp", "/Users/robert/Desktop/CommentedVersion/commentcode/TurnAndMove3/bin", "Config");
                    builder2.redirectOutput(new File(fileLoc + "/testOut2"));
                    builder2.redirectError(new File(fileLoc + "/testOut2"));
                    builder2.start(); // may throw IOException
                }
            }
        }
        Socket client = server.accept();
        out = client.getOutputStream();
        in = client.getInputStream();
        dOut = new DataOutputStream(out);
        dOutObj = new ObjectOutputStream(dOut);
        dIn = new DataInputStream(in);
        dInObj = new ObjectInputStream(dIn);
    }

    /**
     * @param object    The object to send
     * @param whatToSay The string to print to the server upon sending this object
     * @throws IOException
     */
    public void sendObject(Object object, String whatToSay) throws IOException
    {
        dOutObj.reset();
        dOutObj.writeObject(object);
        dOutObj.flush();
    }

    /**
     * @param object    The string to send
     * @param whatToSay The string to print to the server upon sending this object
     * @throws IOException
     */
    public void sendString(String object, String whatToSay) throws IOException
    {
        dOut.writeUTF(object);
        dOut.flush();
    }

    /**
     * @param value     The int to send
     * @param whatToSay The string to print to the server upon sending this object
     * @throws IOException
     */
    public void sendInt(int value, String whatToSay) throws IOException
    {
        dOut.writeInt(value);
        dOut.flush();
    }

    /**
     * @param value     The char to send
     * @param whatToSay The string to print to the server upon sending this object
     * @throws IOException
     */
    public void sendChar(char value, String whatToSay) throws IOException
    {
        dOut.writeChar(value);
        dOut.flush();
    }

    /**
     * @param value     The bool to send
     * @param whatToSay The string to print to the server upon sending this object
     * @throws IOException
     */
    public void sendBool(boolean value, String whatToSay) throws IOException
    {
        dOut.writeBoolean(value);
        dOut.flush();
    }

    /**
     * Read and return an int from the client
     *
     * @param whatToSay The string to print to the server upon sending this object
     * @return the value read from the client
     * @throws IOException
     */
    public int readInt(String whatToSay) throws IOException
    {
        return dIn.readInt();
    }

    /**
     * @param whatToSay The string to print to the server upon sending this object
     * @return the value read from the client
     * @throws IOException
     */
    public String readString(String whatToSay) throws IOException
    {
        return dIn.readUTF();
    }

    /**
     * @param whatToSay The string to print to the server upon sending this object
     * @return the object read from the client
     * @throws IOException
     */
    public Object readObj(String whatToSay) throws IOException, ClassNotFoundException
    {
        return dInObj.readObject();
    }

    /**
     * @param whatToSay The string to print to the server upon sending this object
     * @return the char read from the client
     * @throws IOException
     */
    public char readChar(String whatToSay) throws IOException
    {
        return dIn.readChar();
    }

    /**
     * @param whatToSay The string to print to the server upon sending this object
     * @return the bool read from the client
     * @throws IOException
     */
    public boolean readBool(String whatToSay) throws IOException
    {
        return dIn.readBoolean();
    }

    /**
     * @return return how many bits are still be read
     * @throws IOException
     */
    public int checkIn() throws IOException
    {
        return dIn.available();
    }

    /**
     * @return return how many bits are still be read (objects)
     * @throws IOException
     */
    public int checkObj() throws IOException
    {
        return dInObj.available();
    }

    /**
     * clean up the server socket as well as any input and output streams
     *
     * @throws IOException
     */
    public void cleanUp() throws IOException
    {
        dInObj.close();
        dIn.close();
        in.close();
        dInObj.close();
        dOut.close();
        out.close();
        server.close();
    }
}
