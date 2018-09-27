package com.server;

import com.controller.Controller;
import com.message.Message;
import com.message.Type;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Server implements Runnable{
   //private static DataInputStream inMessage;
   private static DataOutputStream outMessage;
   private static ServerSocket server;
   private static Socket connection;
   private static Controller controller;
   public static String serverName = "Server";
   public static Map<String, ObjectOutputStream> outStreams;
   public static Map<String, ObjectInputStream> inStreams;

   // setup the server
   public Server (int port, Controller controller) {
      this.controller = controller;
      outStreams = new HashMap<String, ObjectOutputStream>();
      inStreams = new HashMap<String, ObjectInputStream>();
      try {
         controller.showNotification("Setting up server...");
         server = new ServerSocket(port);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
    public void run() {
       try{
           waitConnection();
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           closeConnection();
       }
   }

   public static void setServerName(String name) {
       serverName = name;
   }

   public void waitConnection() throws IOException {
       while(true) {
           connection = server.accept();
           SocketThreads socket = new SocketThreads(connection, controller);
           Thread thread = new Thread(socket);
           thread.start();
       }
   }

   // Close server
   public static void closeConnection(){
       try {
           server.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    // Send message to all clients
    public static void sendServerMessage(Message message) {
        outStreams.forEach((k, v) -> {
           try {
               v.writeObject(message);
           } catch (IOException e) {
               e.printStackTrace();
           }
        });
    }


   private static class SocketThreads implements Runnable{
       private Controller controller;
       private Socket connection;
       private ObjectInputStream inMessage;
       private ObjectOutputStream outMessage;
       private static Message message;
       private String clientName;

       public SocketThreads (Socket socket, Controller controller) {
           this.connection = socket;
           this.controller = controller;
       }

       @Override
       public void run() {
           try {
               inMessage = new ObjectInputStream(connection.getInputStream());
               outMessage = new ObjectOutputStream(connection.getOutputStream());
               readInitialMessage(inMessage);
               controller.showNotification(clientName + " has connected to server");
               while (!connection.isClosed()) {
                   try {
                       message = (Message) inMessage.readObject();
                       if(message.getStringMessage().equalsIgnoreCase(clientName + ": end")) {
                           sendTerminate(outMessage);
                           closeConnection();
                           break;
                       }
                       controller.showInMessage(message.getStringMessage());
                       sendServerMessage(message); // Send message from client to other clients
                   } catch (IOException e) {
                       e.printStackTrace();
                       closeConnection();
                   } catch (ClassNotFoundException e) {
                       e.printStackTrace();
                       closeConnection();
                   }
               }
           } catch (IOException e) {
               e.printStackTrace();
           } catch (ClassNotFoundException e) {
               e.printStackTrace();
           }
       }

       // Send end message to signal client to terminate
       private void sendTerminate(ObjectOutputStream output) throws IOException{
           Message terminate = new Message();
           terminate.setStringMessage("end");
           output.writeObject(terminate);
       }


       // Read initial message from client to set client name
       // put the client name and stream into map
       private void readInitialMessage(ObjectInputStream input) throws ClassNotFoundException, IOException {
           Message initial = (Message) inMessage.readObject();
           clientName = initial.getName();
           if(initial.getType() == Type.FIRST) {
               outStreams.put(clientName, outMessage); // add stream to stream database along with client name
               inStreams.put(clientName, inMessage);
           }
       }

       private void closeConnection() {
           try {
               inStreams.remove(inMessage);
               outStreams.remove(outMessage);
               inMessage.close();
               outMessage.close();
               connection.close();
               controller.showNotification("A client ended connection");
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
}
