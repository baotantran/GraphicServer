package com.controller;

import com.message.Message;
import com.server.Server;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer.*;
import javafx.scene.media.MediaView;
import javafx.scene.control.Button;
import java.net.URL;
import javafx.util.Duration;

import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;



public class Controller {

    @FXML
    private TextArea message;
    @FXML
    private TextField userIn;
    @FXML
    private MediaView mediaView;
    @FXML
    private Button playButton;
    @FXML
    private Slider timeSlider;
    private Duration duration;
    private Duration current;
    private boolean serverExist = false;

    private static Controller controllerInstance;
    private static MediaPlayer player;

    public Controller() {
        controllerInstance = this;

    }

    @FXML
    private void initialize() {
        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
            }
        });


    }

    public static Controller getInstance() {
        return controllerInstance;
    }



    // Show message sent from server
    public void showOutMessage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText("SERVER - " + userIn.getText() + "\n");
                userIn.setText("");
            }
        });
    }

    public void playMedia() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                URL mediaURL = getClass().getResource("video.mp4");
                String mediaString = mediaURL.toExternalForm();
                Media media = new Media(mediaString);
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                player = mediaPlayer;
                player.setOnReady(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.setStringMessage("Server player is ready");
                        sendMessage(message);
                        duration = player.getMedia().getDuration();
                    }
                });
                player.currentTimeProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        updateTime();
                    }
                });
                mediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.setAutoPlay(false);
                showNotification("Opened Video");
            }
        });
    }

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
                    }
                }
            });
        }
    }

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
            m.setStringMessage(userIn.getText());
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
    public void startServer() {
        if(!serverExist) {
            serverExist = true;
            Controller controller = getInstance();
            Server server = new Server(5678, controller);
            Thread t = new Thread(server);
            t.start();
        }
    }

}
