import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Com
{
    private OutputStream out;
    private InputStream in;
    private DataOutputStream dOut;
    private DataInputStream dIn;
    private ObjectOutputStream dOutObj;
    private ObjectInputStream dInObj;
    private ServerSocket server;


    public Com(int port) throws IOException
    {
        server = new ServerSocket(port);
        Socket client = server.accept();
        out = client.getOutputStream();
        in = client.getInputStream();
        dOut = new DataOutputStream(out);
        dOutObj = new ObjectOutputStream(dOut);
        dIn = new DataInputStream(in);
        dInObj = new ObjectInputStream(dIn);
    }

    public void sendObject(Object object) throws IOException
    {
        dOutObj.reset();
        dOutObj.writeObject(object);
        dOutObj.flush();
    }

    public void sendString(String object) throws IOException
    {
        dOut.writeUTF(object);
        dOut.flush();
    }

    public void sendInt(int value) throws IOException
    {
        dOut.writeInt(value);
        dOut.flush();
    }

    public void sendChar(char value) throws IOException
    {
        dOut.writeChar(value);
        dOut.flush();
    }

    public void sendBool(boolean value) throws IOException
    {
        dOut.writeBoolean(value);
        dOut.flush();
    }

    public String readString() throws IOException
    {
        return dIn.readUTF();
    }

    public Object readObj() throws IOException, ClassNotFoundException
    {
        return dInObj.readObject();
    }

    public char readChar() throws IOException
    {
        return dIn.readChar();
    }

    public boolean readBool() throws IOException
    {
        return dIn.readBoolean();
    }

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
