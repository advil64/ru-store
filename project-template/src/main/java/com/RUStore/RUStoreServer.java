package com.RUStore;

/* any necessary Java packages here */
import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.util.Arrays;
import java.util.HashMap;

public class RUStoreServer {

	// Class variables to setup server
	private ServerSocket socket;
  private Socket clientSocket;
	private DataInputStream in;
	private DataOutputStream out;

	// Class variables to store data
	HashMap<String, byte[]> memStore = new HashMap<>();

	/**
	 * starts the RU Store server
	 * @param port port to start listening to connections on
	 */
	public void start(int port) throws IOException, InterruptedException{

		int messageLength;
		String message;
		byte[] value;
		String key;

		socket = new ServerSocket(port);

		while (true){
			// Open a socket to start listening to activity on the given port
			clientSocket = socket.accept();

			// Read and output any messages given on the socket
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());

			// loop to recieve continuous messages
			while (true) {

        message = in.readUTF();

				switch (message) {
					case "PUT":

						//first get the key
						key = in.readUTF();

						// check if key exists and respond accordingly
						if (memStore.containsKey(key)){

							out.writeUTF("Invalid Key");
							out.flush();
							continue;
						} else {

							// we're ready to accept the byte string
							out.writeUTF("Ready");
							out.flush();
							try{

								messageLength = in.readInt();
								value = new byte[messageLength];
								in.readFully(value, 0, messageLength);

								memStore.put(key, value);
								System.out.println(memStore);
							} catch(Exception e){

								out.writeUTF("ERROR UNABLE TO STORE");
								out.flush();
								e.printStackTrace();
								continue;
							}
							out.writeUTF("Success");
							out.flush();
						}
            break;
          case "GET":
            // read the key
					  key = in.readUTF();

            // check and retrieve the value associated with the key
            if (!memStore.containsKey(key)){
              out.writeUTF("Invalid Key");
              out.flush();
            } else {

              try{
                out.writeUTF("Success");
                out.writeInt(memStore.get(key).length);
                out.write(memStore.get(key));
                out.flush();
              } catch (Exception e){
                e.printStackTrace();
              }
            }

					default:
						break;
				}

        while(in.available() == 0){
          Thread.sleep(1000);
        }

			}
		}
  }

	/**
	 * RUObjectServer Main(). Note: Accepts one argument -> port number
	 */
	public static void main(String args[]){

		// Check if at least one argument that is potentially a port number
		if(args.length != 1) {
			System.out.println("Invalid number of arguments. You must provide a port number.");
			return;
		}

		// Try and parse port # from argument
		int port = Integer.parseInt(args[0]);

		// If we have a port number, start the server
		RUStoreServer server = new RUStoreServer();

		// Handle exception
		try{
      server.start(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
