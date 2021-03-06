package net.relinc.processor.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.relinc.libraries.staticClasses.SPOperations;

import java.io.File;
import java.util.*;

enum TestType {
    COMPRESSION,
    TENSION,
    COMPRESSION_TRAPPED;

    @Override
    public String toString() {
        switch(this) {
            case COMPRESSION: return "Compression";
            case TENSION: return "Tension";
            case COMPRESSION_TRAPPED: return "Compression Trapped";
            default: throw new IllegalArgumentException();
        }
    }
}

public class HowToSHPBController {

    public Stage stage;

    @FXML
    public ChoiceBox<TestType> selectTestChoiceBox;

    @FXML
    public ImageView imageView;

    @FXML
    public Pane imageViewPane;

    private int compressionIndex = 0;
    private static int compressionLastIndex = 12;
    private int tensionIndex = 0;
    private static int tensionLastIndex = 12;
    private int compressionTrappedIndex = 0;
    private static int compressionTrappedLastIndex = 13;



    @FXML
    public void initialize() {
        imageView.fitWidthProperty().bind(imageViewPane.widthProperty());
        imageView.fitHeightProperty().bind(imageViewPane.heightProperty());
        imageView.setImage(new Image(getClass().getResourceAsStream(SPOperations.folderImageLocation)));


        selectTestChoiceBox.getItems().addAll(EnumSet.allOf(TestType.class));

        selectTestChoiceBox.getSelectionModel().select(TestType.COMPRESSION);

        selectTestChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                render();
            }
        });

        render();
    }

    public void addListeners() {
        EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.RIGHT) {
                    forwardButtonClicked();
                } else if( event.getCode() == KeyCode.LEFT) {
                    backButtonClicked();
                }
                event.consume();
            }
        };

        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyListener);
    }

    @FXML
    public void backButtonClicked() {
        switch(selectTestChoiceBox.getSelectionModel().selectedItemProperty().get()) {
            case COMPRESSION:
                if(compressionIndex > 0) {
                    compressionIndex--;
                }
                break;
            case TENSION:
                if(tensionIndex > 0) {
                    tensionIndex--;
                }
                break;
            case COMPRESSION_TRAPPED:
                if(compressionTrappedIndex > 0) {
                    compressionTrappedIndex--;
                }
                break;
            default:
                throw new RuntimeException("Test type not recognized!");

        }
        render();
    }

    @FXML
    public void forwardButtonClicked() {
        switch(selectTestChoiceBox.getSelectionModel().selectedItemProperty().get()) {
            case COMPRESSION:
                if(compressionIndex < compressionLastIndex) {
                    compressionIndex++;
                }
                break;
            case TENSION:
                if(tensionIndex < tensionLastIndex) {
                    tensionIndex++;
                }
                break;
            case COMPRESSION_TRAPPED:
                if(compressionTrappedIndex < compressionTrappedLastIndex) {
                    compressionTrappedIndex++;
                }
                break;
            default:
                throw new RuntimeException("Test type not recognized!");

        }
        render();
    }

    public static String padLeft(String s, char c, int n) {
        return String.format("%" + n + "s", s).replace(' ', c);
    }

    public void render() {
        switch(selectTestChoiceBox.getSelectionModel().selectedItemProperty().get()) {
            case COMPRESSION:
                imageView.setImage(new Image(getClass().getResourceAsStream(
                        "/net/relinc/processor/images/HowToSHPB/Compression/" + padLeft(Integer.toString(compressionIndex), '0', 2) + ".jpg"))
                );
                break;
            case TENSION:
                imageView.setImage(new Image(getClass().getResourceAsStream(
                        "/net/relinc/processor/images/HowToSHPB/Tension/" + padLeft(Integer.toString(tensionIndex), '0', 2) + ".jpg"))
                );
                break;
            case COMPRESSION_TRAPPED:
                imageView.setImage(new Image(getClass().getResourceAsStream(
                        "/net/relinc/processor/images/HowToSHPB/CompressionTrapped/" + padLeft(Integer.toString(compressionTrappedIndex), '0', 2) + ".jpg"))
                );
                break;
            default:
                throw new RuntimeException("Test type not recognized!");

        }
    }


}
