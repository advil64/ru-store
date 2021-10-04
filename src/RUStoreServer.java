package com.RUStore;

/* any necessary Java packages here */
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.lang.Thread;
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

    // vars for put memstore
		int messageLength;
		String message;
		byte[] value;
		String key;
    boolean connected;

    //vars for put file
    String fileName;
    int fileLength;
    byte[] fileBytes;
		FileOutputStream fout;
		File directory;

		//vars for get file
		Path filePath;

		socket = new ServerSocket(port);

		while (true){
			// Open a socket to start listening to activity on the given port
			clientSocket = socket.accept();

			// Read and output any messages given on the socket
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			connected = true;

			// loop to recieve continuous messages
			while (connected) {

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
            break;
          case "PUT_FILE":

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

								fileName = in.readUTF();
                fileLength = in.readInt();
								fileBytes = new byte[fileLength];
								in.readFully(fileBytes, 0, fileLength);

								memStore.put(key, fileName.getBytes());
								System.out.println(memStore);
							} catch(Exception e){

								out.writeUTF("ERROR UNABLE TO STORE");
								out.flush();
								e.printStackTrace();
								continue;
							}

              //write bytes to file (first create directory is not there)
							directory = new File("./serverfiles"); 
							
							if (directory.exists()) { 
								System.out.println("Directory already exists..."); 
							} else { 
								System.out.println("Directory does not exist, creating now");
								if (directory.mkdir()) { 
									System.out.printf("Created new directory : serverfiles"); 
								} else { 
									System.out.printf("Could not create new directory: serverfiles"); 
									break;
								} 
							}

							//next create and write to file
							fout = new FileOutputStream("./serverfiles/" + key);
							fout.write(fileBytes);
							fout.close();
							out.writeUTF("Success");
							out.flush();
						}
            break;
					case "GET_FILE":
						// read the key
					  key = in.readUTF();

            // check and retrieve the value associated with the key
            if (!memStore.containsKey(key)){
              out.writeUTF("Invalid Key");
              out.flush();
            } else {
              try{

                out.writeUTF("Success");
								filePath = Paths.get("./serverfiles/"+key);
								fileBytes = Files.readAllBytes(filePath);
                out.writeInt(fileBytes.length);
                out.write(fileBytes);
                out.flush();
              } catch (Exception e){
                e.printStackTrace();
              }
            }
						break;
					case "REMOVE":
						//read the key
						key = in.readUTF();

						if (memStore.containsKey(key)){
							memStore.remove(key);
							out.writeUTF("Success");
						} else{
							out.writeUTF("Invalid Key");
						}

						out.flush();
						break;
					case "LIST":

						//send the number of keys
						out.writeInt(memStore.keySet().size());
						out.flush();

						//write keys
						for (String k : memStore.keySet()){
							out.writeUTF(k);
							out.flush();
						}
						break;
					case "EXIT":
            connected = false;
						break;
					default:
						break;
				}

        while(in.available() == 0 && connected){
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
