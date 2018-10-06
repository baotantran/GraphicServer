package com.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.message.Message;
import com.message.Type;
import com.server.Server;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer.*;
import javafx.scene.media.MediaView;
import javafx.scene.control.Button;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer;

import static com.server.Server.sendServerMessage;

public class Controller {

    // FXML variable
    @FXML
    private JFXTextArea message;
    @FXML
    private JFXTextField userIn;
    @FXML
    private MediaView mediaView;
    @FXML
    private JFXButton playButton;
    @FXML
    private JFXSlider timeSlider;
    @FXML
    private ListView listView;
    @FXML
    private JFXTextField linkField;

    // Server variable
    public static boolean serverExist = false;
    private static String serverName = "Server";
    private Stage stage;
    private ExecutorService es;

    // Media variable
    private static Duration duration;
    private Duration current;
    private static Controller controllerInstance;
    public static MediaPlayer player;
    public static Status status;
    public static boolean playerExist = false;
    public static final String SAMPLE =  "http://www.html5videoplayer.net/videos/toystory.mp4";
    public static String currentLink;

    // Make a reference of controller instance
    // Get a reference of stage instance
    public Controller() {
        controllerInstance = this;
        stage = loginController.getInstance();
    }

    // Return controller instance called by server
    public static Controller getInstance() {
        return controllerInstance;
    }

    // Set server name pass in by login controller
    public void setServerName(String name) {
        serverName = name;
    }

    @FXML
    private void initialize() {
        // Set stage on close to turn off server
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(serverExist) {
                    es.shutdownNow();
                }
            }
        });

        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.getItems().add(SAMPLE);
    }

    // ----------------------------------------------------- //
    // -------------------Media Setup----------------------- //
    // ----------------------------------------------------- //
    // Add the link to list view
    public void addToList() {
        listView.getItems().add(linkField.getText());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                linkField.setText("");
            }
        });
    }

    public void addClientLink(final String link) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                listView.getItems().add(link);
            }
        });
    }

    // Load media to media player
    // Link required video format in the link
    // Load the SAMPLE link
    public void playMedia() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String link = (String) listView.getSelectionModel().getSelectedItem();
                //URL mediaURL = getClass().getResource("video.mp4");
                //String mediaString = mediaURL.toExternalForm();
                Media media;
                if(link != null) {
                    media = setMedia(link);
                } else {
                    media = setMedia();
                }
                if(playerExist) player.dispose();
                player = new MediaPlayer(media);
                playButton.setText(">");
                playerExist = true;
                setup(player);
                mediaView.setMediaPlayer(player);
                player.setAutoPlay(false);
                showNotification("Opened Video");
            }
        });
    }

    // Add SAMPLE link to media
    private Media setMedia() {
        currentLink = SAMPLE;
        return new Media(SAMPLE);
    }

    // Add custom link to media
    private Media setMedia(String mediaLink) {
        Media media = new Media(mediaLink);
        currentLink = mediaLink;
        return media;
    }

    // Initialized method for player
    // onPlaying, onPaused....
    public void setup(MediaPlayer player) {
        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                status = Status.READY;
                Server.sendPlayerStatus(status, currentLink);
            }
        });

        player.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateTime();
            }
        });

        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                updateTime();
            }
        });

        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                status = Status.READY;
                Server.sendPlayerStatus(status, currentLink);
                duration = player.getMedia().getDuration();
            }
        });
    }

    // Update slider on time change
    // Update time on slider change
    private void updateTime() {
        if(timeSlider != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    current = player.getCurrentTime();
                    if(!timeSlider.isDisable()
                            && duration.greaterThan(Duration.ZERO)
                            && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(current.divide(duration.toMillis()).toMillis() * 100);
                    } else if (!timeSlider.isDisable() &&
                                duration.greaterThan(Duration.ZERO) &&
                                timeSlider.isValueChanging()) {
                        double time = duration.toMillis() * timeSlider.getValue() / 100;
                        player.seek(new Duration(time));
                        Server.sendUpdateTime(time);
                    }
                }
            });
        }
    }

    public static void updateMediaTime(double time) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(duration.greaterThan(Duration.ZERO)) {
                    player.seek(new Duration(time));
                }
            }
        });
    }

    // Play media on button click
    public void pressPlayButton() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Status status = player.getStatus();
                if(status == Status.HALTED || status == Status.UNKNOWN) {
                    showNotification("Can't open video");
                    return;
                }
                if(status == Status.READY || status == Status.PAUSED || status == Status.STOPPED) {
                    player.play();
                    playButton.setText("II");
                    sendCommand("play");
                } else {
                    player.pause();
                    playButton.setText(">");
                    sendCommand("paused");
                }
            }
        });
    }

    // ------------------------------------------------------------------ //
    // ----------------------Message Setup------------------------------- //
    // ------------------------------------------------------------------ //

    private void sendCommand(String command) {
        if(serverExist) {
            Status status = player.getStatus();
            Message message = new Message();
            message.setType(Type.COMMAND);
            message.setStringMessage(command);
            sendServerMessage(message);
        }
    }

    // Show message from client to server
    public void showInMessage(String mess) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText(mess + "\n");
            }
        });
    }

    // Show message sent to client
    public void showOutMessage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText(serverName + ": " + userIn.getText() + "\n");
                userIn.setText("");
            }
        });
    }

    // Show general necessary message in text area
    public void showNotification(String mess) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText(mess + "\n");
            }
        });
    }

    // Send simple string message if serverExist is true
    public void sendStringMessage() {
        if(serverExist) {
            Message m = new Message();
            m.setType(Type.NORMAL);
            m.setStatus(status);
            m.setStringMessage(Server.serverName + ": " + userIn.getText());
            sendServerMessage(m);
            showOutMessage();
            //showNotification(current.toString());
        }
    }

    // Send object message if serverExist is true
    public void sendMessage(Message m) {
        if(serverExist) {
            sendServerMessage(m);
            showOutMessage();
        }
    }


    // Create strictly 1 server regulated by serverExist
    // Pass in scene controller, and server name
    public void startServer() {
        if(!serverExist) {
            serverExist = true;
            Controller controller = getInstance();
            Server server = new Server(5678, controller);
            server.setServerName(serverName);
            es = Executors.newFixedThreadPool(1);
            es.submit(server);
        }
    }
}
