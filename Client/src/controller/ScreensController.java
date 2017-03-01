package controller;

import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by kal on 3/1/17.
 */
public class ScreensController extends StackPane {

    private HashMap<String, Node> screens = new HashMap<String, Node>();
    private Stage primaryStage;

    public ScreensController() {
        super();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    //Add screen to the collection
    public void addScreen(String name, Node screen) {
        screens.put(name, screen);
    }

    //returns a node with appropriate name
    public Node getScreen(String name) {
        return screens.get(name);
    }

    //load the fxml file, add the screen to the screens collections and
    //finally inject the screenPane to the controller
    public boolean loadScreen(String name, String resource) {
        try {
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
            Parent loadScreen = (Parent) myLoader.load();
            IScreensController myScreenController = ((IScreensController) myLoader.getController());
            myScreenController.setScreenParent(this);
            addScreen(name, loadScreen);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean setScreen(final String name) {
        if (screens.get(name) != null) { //screen loader
            final DoubleProperty opacity = opacityProperty();

            if (!getChildren().isEmpty()) { // if there is more than one screen

                Timeline fade = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
                        new KeyFrame(new Duration(1000), new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                getChildren().remove(0); // remove the displayed screen
                                getChildren().add(0, screens.get(name)); //add the screen

                                fitNodeInParent(name);
                                setNodeTitle(name);

                                Timeline faceIn = new Timeline(
                                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                                        new KeyFrame(new Duration(800), new KeyValue(opacity, 1.0)));
                                faceIn.play();
                            }

                        }, new KeyValue(opacity, 0.0)));
                fade.play();
            } else {
                //setOpacity(0.0);
                getChildren().add(screens.get(name)); //if no one else has been displayed, then just show
                fitNodeInParent(name);
                setNodeTitle(name);
                /*Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                        new KeyFrame(new Duration(2500), new KeyValue(opacity, 1.0)));
                fadeIn.play();*/
            }
            return true;
        } else {
            System.out.println("Screen has not been loaded!\n");
            return false;
        }
    }

    public boolean unloadScreen(String name) {
        if (screens.remove(name) == null) {
            System.out.println("Screen did not exist");
            return false;
        } else {
            return true;
        }
    }

    private void setNodeTitle(String name) {
        switch (name.split(";")[0]) {
            case "main":
                primaryStage.setTitle("Hydra [Hangman Client Application]");
                break;
            case "serverManager":
                primaryStage.setTitle("Hydra ServerManager");
                break;
            default:
                primaryStage.setTitle("");
                break;
        }
    }

    private void fitNodeInParent(String name) {
        try {
            String[] nameArr = name.split(";");
            double w = Double.parseDouble(nameArr[1]);
            double h = Double.parseDouble(nameArr[2]);

            primaryStage.setMinWidth(w);
            primaryStage.setMinHeight(h);

            primaryStage.setWidth(w);
            primaryStage.setHeight(h);
            primaryStage.centerOnScreen();

            // (fit the node to the primary stage)
            // Note: I will change in the next release the Node to Region in the HashMap definision
            // as Region is a subclass of Node
            Region reg = (Region) screens.get(name);
            reg.prefWidthProperty().bind(primaryStage.widthProperty());
            reg.prefHeightProperty().bind(primaryStage.heightProperty());

        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
}
