import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Acts as an interface to the main server
 */
@SuppressWarnings("unused") public class Com
{
	private OutputStream out;
	private InputStream in;
	private DataOutputStream dOut;
	private DataInputStream dIn;
	private ObjectOutputStream dOutObj;
	private ObjectInputStream dInObj;
	private Socket sock;

	/**
	 * Method to set up connection with the main server
	 * @param ip The ip of the main server
	 * @param port The port to connect to it with
	 * @throws IOException
	 */
	public Com(String ip, int port) throws IOException
	{
		sock = new Socket(ip, port);
		in = sock.getInputStream();
		dIn = new DataInputStream(in);
		dInObj = new ObjectInputStream(dIn);
		out = sock.getOutputStream();
		dOut = new DataOutputStream(out);
		dOutObj = new ObjectOutputStream(dOut);

	}

	/**
	 * Method used to send an object to the server, resets previous objects sent.
	 * @param object The object we wish to send
	 * @param whatToSay Description of the object that is being sent (Used for debugging)
	 * @throws IOException
	 */
	public void sendObject(Object object, String whatToSay) throws IOException
	{
		dOutObj.reset();
		dOutObj.writeObject(object);
		dOutObj.flush();
	}

	/**
	 * Method used to send an object to the server, resets previous objects sent.
	 * @param object The String we wish to send
	 * @param whatToSay Description of the String that is being sent (Used for debugging)
	 * @throws IOException
	 */
	public void sendString(String object, String whatToSay) throws IOException
	{
		dOut.writeUTF(object);
		dOut.flush();
	}

	/**
	 * Method used to send an object to the server, resets previous objects sent.
	 * @param value The int we wish to send
	 * @param whatToSay Description of the int that is being sent (Used for debugging)
	 * @throws IOException
	 */
	public void sendInt(int value, String whatToSay) throws IOException
	{
		dOut.writeInt(value);
		dOut.flush();
	}

	/**
	 * Method used to send an object to the server, resets previous objects sent.
	 * @param value The char we wish to send
	 * @param whatToSay Description of the char that is being sent (Used for debugging)
	 * @throws IOException
	 */
	public void sendChar(char value, String whatToSay) throws IOException
	{
		dOut.writeChar(value);
		dOut.flush();
	}

	/**
	 * Method used to send an object to the server, resets previous objects sent.
	 * @param value The Boolean we wish to send
	 * @param whatToSay Description of the Boolean that is being sent (Used for debugging)
	 * @throws IOException
	 */
	public void sendBool(boolean value, String whatToSay) throws IOException
	{
		dOut.writeBoolean(value);
		dOut.flush();
	}

	/**
	 * Method used to read the next int sent from the server
	 * @param whatToSay Description of the Int we wish to receive (Used for debugging)
	 * @return The int we wished to read
	 * @throws IOException
	 */
	public int readInt(String whatToSay) throws IOException
	{
		return dIn.readInt();
	}

	/**
	 * Method used to read the next String sent from the server
	 * @param whatToSay Description of the String we wish to receive (Used for debugging)
	 * @return The String we wished to read
	 * @throws IOException
	 */
	public String readString(String whatToSay) throws IOException
	{
		return dIn.readUTF();
	}

	/**
	 * Method used to read the next Object sent from the server
	 * @param whatToSay Description of the Object we wish to receive (Used for debugging)
	 * @return The Object we wished to read
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object readObj(String whatToSay) throws IOException, ClassNotFoundException
	{
		return dInObj.readObject();
	}

	/**
	 * Method used to read the next boolean sent from the server
	 * @param whatToSay Description of the Boolean we wish to receive (Used for debugging)
	 * @return The Boolean we wished to read
	 * @throws IOException
	 */
    public boolean readBool(String whatToSay) throws IOException
	{
		return dIn.readBoolean();
	}

	/**
	 * Method used to clean up the connections with the server.
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
		sock.close();
	}
}
