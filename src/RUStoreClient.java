package com.RUStore;

/* any necessary Java packages here */
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

public class RUStoreClient {

	// Class variables to setup client
	private int port;
	private String host;
	private Socket clientSocket;
	private DataOutputStream out;
	private DataInputStream in;

	/**
	 * RUStoreClient Constructor, initializes default values
	 * for class members
	 *
	 * @param host	host url
	 * @param port	port number
	 */
	public RUStoreClient(String host, int port) {

		// Set the object variables
		this.port = port;
		this.host = host;
	}

	/**
	 * Opens a socket and establish a connection to the object store server
	 * running on a given host and port.
	 *
	 * @return n/a, however throw an exception if any issues occur
	 */
	public void connect() throws UnknownHostException, IOException{

		this.clientSocket = new Socket(host, port);
		this.out = new DataOutputStream(this.clientSocket.getOutputStream());
		this.in = new DataInputStream(this.clientSocket.getInputStream());
	}

	/**
	 * Sends an arbitrary data object to the object store server. If an 
	 * object with the same key already exists, the object should NOT be 
	 * overwritten
	 * 
	 * @param key	key to be used as the unique identifier for the object
	 * @param data	byte array representing arbitrary data object
	 * 
	 * @return		0 upon success
	 *        		1 if key already exists
	 *        		Throw an exception otherwise
	 */
	public int put(String key, byte[] data) throws IOException {

		String response;

		try{

			// Let server know you want to store a key/value
			this.out.writeUTF("PUT");
			this.out.writeUTF(key);
			this.out.flush();

			//check if the key already exists on the server and react accordingly
			response = this.in.readUTF();

			if ("Invalid Key".equals(response)){
				return 1;
			} else if ("Ready".equals(response)){

				//write size of the data then write the data
				this.out.writeInt(data.length);
				this.out.write(data);
				this.out.flush();
				response = this.in.readUTF();

				if("ERROR UNABLE TO STORE".equals(response)){
					throw new IOException("Unable to write to server");
				} else {
					return 0;
				}
			} else {
				throw new IOException("Invalid server response");
			}

		} catch(Exception e){
			throw new IOException("Unable to write to socket");
		}
	}

	/**
	 * Sends an arbitrary data object to the object store server. If an 
	 * object with the same key already exists, the object should NOT 
	 * be overwritten.
	 * 
	 * @param key	key to be used as the unique identifier for the object
	 * @param file_path	path of file data to transfer
	 * 
	 * @return		0 upon success
	 *        		1 if key already exists
	 *        		Throw an exception otherwise
	 */
	public int put(String key, String file_path) throws IOException{

		String response;
		Path path = Paths.get(file_path);
		byte[] data = Files.readAllBytes(path);

		try{

			// Let server know you want to store a key/value
			this.out.writeUTF("PUT_FILE");
			this.out.writeUTF(key);
			this.out.flush();

			//check if the key already exists on the server and react accordingly
			response = this.in.readUTF();

			if ("Invalid Key".equals(response)){
				return 1;
			} else if ("Ready".equals(response)){

				//send the file over
				this.out.writeUTF(file_path);
				this.out.writeInt(data.length);
				this.out.write(data);
				this.out.flush();
				response = this.in.readUTF();

				if("ERROR UNABLE TO STORE".equals(response)){
					throw new IOException("Unable to write to server");
				} else {
					return 0;
				}
			} else {
				throw new IOException("Invalid server response");
			}

		} catch(Exception e){
			throw new IOException("Unable to write to socket");
		}
	}

	/**
	 * Downloads arbitrary data object associated with a given key
	 * from the object store server.
	 * 
	 * @param key	key associated with the object
	 * 
	 * @return		object data as a byte array, null if key doesn't exist.
	 *        		Throw an exception if any other issues occur.
	 */
	public byte[] get(String key) throws IOException{

		//method vars
		String response;
		int messLength;
		byte[] message;

		// Tell the server that you want to retrieve something
		this.out.writeUTF("GET");

		// Give the server the key you'd like to retrieve
		this.out.writeUTF(key);
		this.out.flush();
		response = this.in.readUTF();

		// Check if the key was good
		if ("Invalid Key".equals(response)){
			// key does not exist on the server
			return null;
		} else if ("Success".equals(response)){
			try{

				//read in the length of the message then read the message
				messLength = this.in.readInt();
				message = new byte[messLength];
				this.in.readFully(message, 0, messLength);
				return message;
			} catch (Exception e){

				e.printStackTrace();
				throw new IOException("Invalid Server Response");
			}
		} else {
			throw new IOException("Invalid Server Response");
		}
	}

	/**
	 * Downloads arbitrary data object associated with a given key
	 * from the object store server and places it in a file. 
	 * 
	 * @param key	key associated with the object
	 * @param	file_path	output file path
	 * 
	 * @return		0 upon success
	 *        		1 if key doesn't exist
	 *        		Throw an exception otherwise
	 */
	public int get(String key, String file_path) throws IOException{

		// method vars
		byte[] file_bytes;
		String response;
		int fileLength;
		FileOutputStream fout;

		try{
			// Tell the server that you want to retrieve a file
			this.out.writeUTF("GET_FILE");

			// Give the server the key you'd like to retrieve
			this.out.writeUTF(key);
			this.out.flush();
			response = this.in.readUTF();

			// Check if the key was good
			if ("Invalid Key".equals(response)){
				// key does not exist on the server
				return 1;
			} else if ("Success".equals(response)){

				try{

					fileLength = this.in.readInt();
					file_bytes = new byte[fileLength];
					this.in.readFully(file_bytes, 0, fileLength);
				} catch (Exception e){

					e.printStackTrace();
					throw new IOException("Invalid Server Response");
				}

				// write file to output files
				fout = new FileOutputStream(file_path);
				fout.write(file_bytes);
				fout.close();
				return 0;
			} else {
				throw new IOException("Invalid Server Response");
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new IOException("Unable to connect to server");
		}
	}

	/**
	 * Removes data object associated with a given key 
	 * from the object store server. Note: No need to download the data object, 
	 * simply invoke the object store server to remove object on server side
	 * 
	 * @param key	key associated with the object
	 * 
	 * @return		0 upon success
	 *        		1 if key doesn't exist
	 *        		Throw an exception otherwise
	 */
	public int remove(String key) throws IOException{

		this.out.writeUTF("REMOVE");

		//write the key to be removed
		this.out.writeUTF(key);

		//wait for response
		String response = this.in.readUTF();

		if("Success".equals(response)){
			return 0;
		} else if("Invalid Key".equals(response)){
			return 1;
		}
		throw new IOException("Server Error");
	}

	/**
	 * Retrieves of list of object keys from the object store server
	 * 
	 * @return		List of keys as string array, null if there are no keys.
	 *        		Throw an exception if any other issues occur.
	 */
	public String[] list() throws IOException{

		this.out.writeUTF("LIST");
		ArrayList<String> keys = new ArrayList<String>();
		int numKeys = this.in.readInt();

		for(int i = 0; i < numKeys; i++){
			keys.add(this.in.readUTF());
		}

		return keys.toArray(new String[0]);
	}

	/**
	 * Signals to server to close connection before closes 
	 * the client socket.
	 * 
	 * @return		n/a, however throw an exception if any issues occur
	 */
	public void disconnect() throws IOException{

		// Close the client socket
		this.out.writeUTF("EXIT");
		this.clientSocket.close();
		this.out.close();
	}

}
