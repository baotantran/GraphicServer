package com.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.message.Message;
import com.message.Type;
import com.server.Server;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer.*;
import javafx.scene.media.MediaView;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;



public class Controller {

    // FXML variable
    @FXML
    private TextArea message;
    @FXML
    private TextField userIn;
    @FXML
    private MediaView mediaView;
    @FXML
    private JFXButton playButton;
    @FXML
    private JFXSlider timeSlider;

    // Server variable
    private boolean serverExist = false;
    private static String serverName = "Server";
    private Stage stage;
    private ExecutorService es;

    // Media variable
    private Duration duration;
    private Duration current;
    private static Controller controllerInstance;
    public static MediaPlayer player;
    public static Status status;
    public static boolean playerExist = false;

    // Make a reference of controller instance
    // Get a reference of stage instance
    public Controller() {
        controllerInstance = this;
        stage = loginController.getInstance();
    }

    // Set server name pass in by login controller
    public static void setServerName(String name) {
        serverName = name;
    }

    @FXML
    private void initialize() {
        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
            }
        });

        // Set stage on close to turn off server
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(serverExist) {
                    es.shutdown();
                }
            }
        });
    }

    public static Controller getInstance() {
        return controllerInstance;
    }

    public void playMedia() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                playerExist = true;
                //URL mediaURL = getClass().getResource("video.mp4");
                //String mediaString = mediaURL.toExternalForm();
                Media media = new Media("http://www.html5videoplayer.net/videos/toystory.mp4");
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                player = mediaPlayer;
                setup(player);
                mediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.setAutoPlay(false);
                showNotification("Opened Video");
            }
        });
    }

    // Setup method for player
    public void setup(MediaPlayer player) {
        player.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                status = Status.PLAYING;
                Server.sendPlayerStatus(status);
            }
        });

        player.setOnHalted(new Runnable() {
            @Override
            public void run() {
                status = Status.HALTED;
                Server.sendPlayerStatus(status);
            }
        });

        player.setOnPaused(new Runnable() {
            @Override
            public void run() {
                status = Status.PAUSED;
                Server.sendPlayerStatus(status);
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
                Server.sendPlayerStatus(status);
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
                } else {
                    player.pause();
                    playButton.setText(">");
                }
            }
        });
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
            Server.sendServerMessage(m);
            showOutMessage();
            //showNotification(current.toString());
        }
    }

    // Send object message if serverExist is true
    public void sendMessage(Message m) {
        if(serverExist) {
            Server.sendServerMessage(m);
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
            //Thread t = new Thread(server);
            //t.start();
        }
    }
}
