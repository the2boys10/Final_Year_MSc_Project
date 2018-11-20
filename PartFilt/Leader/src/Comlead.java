import java.io.*;
import java.net.Socket;

public class Comlead
{
	private OutputStream out;
	private InputStream in;
	private DataOutputStream dOut;
	private DataInputStream dIn;
	private ObjectOutputStream dOutObj;
	private ObjectInputStream dInObj;
	private Socket sock;

	private LeaderSub superc;
	
	
	public Comlead(String ip, int port, LeaderSub superc) throws IOException
	{
		sock = new Socket(ip, port);
		in = sock.getInputStream();
		dIn = new DataInputStream(in);
		dInObj = new ObjectInputStream(dIn);
		out = sock.getOutputStream();
		dOut = new DataOutputStream(out);
		dOutObj = new ObjectOutputStream(dOut);
		this.superc = superc;

	}

	public void sendObject(Object object) throws IOException
	{
		dOutObj.reset();
		dOutObj.writeObject(object);
		dOutObj.flush();
	}

	public void sendString(String object, String whatToSay) throws IOException
	{
		dOut.writeUTF(object);
		dOut.flush();
	}

	public void sendChar(char value, String whatToSay) throws IOException
	{
		dOut.writeChar(value);
		dOut.flush();
	}

	public void sendBool(boolean value, String whatToSay) throws IOException
	{
		dOut.writeBoolean(value);
		dOut.flush();
	}

	public int readInt(String whatToSay) throws IOException
	{
		return dIn.readInt();
	}

	public void readString(String whatToSay) throws IOException
	{
		dIn.readUTF();
	}

	public Object readObj(String whatToSay) throws IOException, ClassNotFoundException
	{
		return dInObj.readObject();
	}

	public void readChar(String whatToSay) throws IOException
	{
		dIn.readChar();
	}

	public boolean readBool(String whatToSay) throws IOException
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
		sock.close();
	}
}
