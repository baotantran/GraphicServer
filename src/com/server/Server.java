package com.server;

import com.controller.Controller;
import com.message.Message;
import com.message.Type;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class Server implements Runnable{
   //private static DataInputStream inMessage;
   private static DataOutputStream outMessage;
   private static ServerSocket server;
   private static Socket connection;
   private static Controller controller;
   public static String serverName = "Server";
   public static Map<String, ObjectOutputStream> outStreams;
   public static Map<String, ObjectInputStream> inStreams;
   private final AtomicBoolean running = new AtomicBoolean(true);

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
       while(running.get()) {
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
       if(controller.serverExist) {
           outStreams.forEach((k, v) -> {
               try {
                   v.writeObject(message);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           });
       }
    }

    public static void sendPlayerStatus(Status curr, String link) {
        Message message = new Message();
        message.setType(Type.STATUS);
        message.setStatus(curr);
        message.setStringMessage(link);
        sendServerMessage(message);
    }

    public static void sendUpdateTime(double time) {
       Message message = new Message();
       message.setStatus(controller.player.getStatus());
       message.setType(Type.TIME);
       message.setStringMessage("Server player current time");
       message.setTime(time);
       sendServerMessage(message);
    }


   private static class SocketThreads implements Runnable{
       private Controller controller;
       private Socket connection;
       private ObjectInputStream inMessage;
       private ObjectOutputStream outMessage;
       private static Message message;
       private String clientName;
       //private final AtomicBoolean running = new AtomicBoolean(true);
       private ScheduledExecutorService ses;

       public SocketThreads (Socket socket, Controller controller) {
           this.connection = socket;
           this.controller = controller;
           ses = Executors.newScheduledThreadPool(1);

       }

       @Override
       public void run() {
           while(!connection.isClosed()) {
               try {
                   inMessage = new ObjectInputStream(connection.getInputStream());
                   outMessage = new ObjectOutputStream(connection.getOutputStream());
                   readInitialMessage(inMessage, outMessage);
                   controller.showNotification(clientName + " has connected to server");
                   sendInitialMessage(inMessage, outMessage);
                   //setupClientPlayer(inMessage, outMessage);
                   //setupTimeSender();
                   while (!connection.isClosed()) {
                       try {
                           message = (Message) inMessage.readObject();
                           interpreter(inMessage, outMessage, message);
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
                   closeConnection();
               } catch (ClassNotFoundException e) {
                   e.printStackTrace();
                   closeConnection();
               }
           }
       }

       // Send end message to signal client to terminate
       private void sendTerminate(ObjectOutputStream output) throws IOException{
           Message terminate = new Message();
           terminate.setType(Type.TERMINATE);
           terminate.setStringMessage("end");
           output.writeObject(terminate);
       }


       // Read initial message from client to set client name
       // put the client name and stream into map
       private void readInitialMessage(ObjectInputStream input, ObjectOutputStream output) throws ClassNotFoundException, IOException {
           Message initial = (Message) inMessage.readObject();
           clientName = initial.getName();
           if(initial.getType() == Type.FIRST) {
               outStreams.put(clientName, output); // add stream to stream database along with client name
               inStreams.put(clientName, input);

           }
       }

       // Create a thread to periodically send time
       private void setupTimeSender() {
           ses.scheduleAtFixedRate(new Runnable() {
               @Override
               public void run() {
                   try {
                       while (!connection.isClosed() && controller.playerExist) {
                           System.out.println("send update time");
                           Message message = new Message();
                           message.setType(Type.TIME);
                           message.setStringMessage("Server player current time");
                           message.setTime(controller.player.getCurrentTime().toMillis());
                           outMessage.writeObject(message);
                       }
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }, 3000L, 10000L, TimeUnit.MILLISECONDS);
       }

       // Send the current status of server player on first connection of client
       // If player has been created
       private void sendInitialMessage(ObjectInputStream input, ObjectOutputStream output) throws ClassNotFoundException, IOException {
           if(controller.playerExist) {
               Message message = new Message();
               message.setType(Type.STATUS);
               message.setStatus(controller.player.getStatus());
               message.setStringMessage(controller.currentLink);
               output.writeObject(message);
           }
       }


       // Decide server respond based on Type of message
       private void interpreter(ObjectInputStream input, ObjectOutputStream output, Message message) throws IOException{
           Status status = message.getStatus();
           Type type = message.getType();
           switch (type) {
               case NORMAL:
                   controller.showInMessage(message.getStringMessage());
                   sendServerMessage(message);
                   break;
               case REQUEST:
                   if(controller.playerExist) {
                       sendUpdateTime(controller.player.getCurrentTime().toMillis());
                   }
                   break;
               case TIME:
                   //TODO
               case STATUS:
                   controller.showInMessage(message.getStringMessage());
                   break;
               case TERMINATE:
                   sendTerminate(outMessage);
                   closeConnection();
                   break;
               case LINK:
                   controller.addClientLink(message.getStringMessage());
                   break;
               default:
                   controller.showInMessage(message.getStringMessage());
                   break;
           }
       }

       /*private void setupClientPlayer(ObjectInputStream input, ObjectOutputStream output) throws ClassNotFoundException, IOException {
           Message message = new Message();
           Status curr = controller.status;
           if(curr != Status.UNKNOWN) {
               message.setStringMessage("Server player is ready!");
               //check status of the player send the status if ready or send time if playing or paused
               if(curr == Status.PLAYING) {
                   message.setStatus(curr);
                   message.setType(Type.STATUS);
                   output.writeObject(message);
                   Message setup = (Message) input.readObject();
                   while(setup.getStatus() != Status.PLAYING) {
                       output.writeObject(message);
                       setup = (Message) input.readObject();
                       controller.showNotification("in the media setup loop \n" + message.getStatus());
                   }
                   message.setTime(controller.player.getCurrentTime().toMillis());
                   message.setType(Type.TIME);
                   output.writeObject(message);
                   // Send the time and keep checking if the client is in sync
                   // and then release
               } else if (curr == Status.READY) {
                   message.setType(Type.STATUS);
                   output.writeObject(message);
               }
           } else {
               message.setType(Type.STATUS);
               message.setStatus(curr);
               message.setStringMessage("Server player is not ready!");
               output.writeObject(message);
           }
       }*/

       private void closeConnection() {
           try {
               inStreams.remove(clientName);
               outStreams.remove(clientName);
               inMessage.close();
               outMessage.close();
               connection.close();
               ses.shutdown();
               controller.showNotification(clientName + " ended connection!");
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
}
