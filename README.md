## RU-Store Instruction Manual
### By Advith Chegu (Rutgers Distributed Systems Fall 2021)

Welcome to this little project! In this guide I'll take you through how my app works from both the server and client sides. It includes code snippets so you can follow along easily!

### Server Side Structure

Let's first go over the server side store operations!

**Put Operation**

After a connection has been established and we have recieved a `PUT` command the app accepts the key from the client and checks to make sure it's not a duplicate.

```java
key = in.readUTF();
if (memStore.containsKey(key)){
  //inform client of an invalid key
} else {
  //continue with put store process
}
```

Once the necessary check is complete we proceed to storing the *key/value* pair in the memory store hashmap. The app does this by indicating to the client that the server is ready to recieve the data.

```java
out.writeUTF("Ready");

messageLength = in.readInt();
value = new byte[messageLength];
in.readFully(value, 0, messageLength);

memStore.put(key, value);
```

Then the app proceeds to first recieving the length of the message and then the message itself as an array of byted.

**Get Operation**

After a connection has been established and we have recieved a `GET` command the app accepts the key from the client and checks to make sure it exists in the memstore.

```java
key = in.readUTF();
if (!memStore.containsKey(key)){
  //inform client of an invalid key
} else {
  //continue with get value process
}
```

Once the checks are complete, we simply do the opposite of what we did in the `PUT` command. We tell the client to get ready for the data, give the client the size of the value and then hand over the bytes!

**Put File Operation**

The `PUT_FILE` command is pretty much the same as the `PUT` command, however we now have the added step of writing the file to the server side. We first start off with creating the folder (if needed) then writing the file in the server files folder.

```java
directory = new File("./serverfiles"); 
							
if (directory.exists()) { 
  //don't do anything
} else { 
  //create the server files directory
}

//next create and write to file
fout = new FileOutputStream(filepath);
fout.write(fileBytes);
out.writeUTF("Success");
```

Once the file has been stored we let the client know our action suceeded.

**Get File Operation**

