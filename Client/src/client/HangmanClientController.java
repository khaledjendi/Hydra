package client;

import controller.ScreensFramework;
import controller.ScreensController;
import controller.IScreensController;
import service.Server;
import com.jfoenix.controls.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import service.ServerConnection;

/**
 * Created by kal on 3/1/17.
 */
public class HangmanClientController implements Initializable, IScreensController {
    ScreensController myController;
    private static Server srv = null;
    private static boolean ongoingGame = false;
    ServerConnection srvConn;

    @FXML
    private TreeView<String> srvTree;

    @FXML
    private Label word;

    @FXML
    private Label selecredSrv;

    @FXML
    private Label remainingAttempts;

    @FXML
    private Label score;

    @FXML
    private Label info;

    @FXML
    private Label gameStatus;

    @FXML
    private JFXTextField wholeWord;

    @FXML
    private ImageView hang;

    @FXML
    private ImageView hangItems;

    private TreeItem<String> root = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.root = new TreeItem<>("Hangman Servers");
        this.root.setExpanded(true);
        this.srvTree.setRoot(root);
        this.srvTree.setShowRoot(false);

        srvTree.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (!ongoingGame) {
                    if (event.getClickCount() == 2) { //unselect the server with double clicks
                        TreeItem<String> selectedSrv = srvTree.getSelectionModel().getSelectedItem();
                        if (selectedSrv != null) {
                            if (selectedSrv.isLeaf()) { // get parent name and compare it with server name
                                if (selectedSrv.getParent().getValue().equals(srv.getSrvName())) {
                                    srv = null;
                                    unselectAllNodes();
                                }
                            } else // get item name and compare it with server name
                            {
                                if (selectedSrv.getValue().equals(srv.getSrvName())) {
                                    srv = null;
                                    unselectAllNodes();
                                }
                            }
                        }

                    } else if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                        if (srv == null) {
                            TreeItem<String> selectedSrv = srvTree.getSelectionModel().getSelectedItem();
                            if (selectedSrv != null) {
                                if (selectedSrv.getParent() != null) { // not root item -> node0
                                    if (selectedSrv.getChildren() != null && selectedSrv.getChildren().size() > 0) { // server name -> node 1
                                        srv = new Server(selectedSrv.getValue(), selectedSrv.getChildren().get(0).getValue(), selectedSrv.getChildren().get(1).getValue());
                                        selecredSrv.setText(srv.getSrvName() + " Selected");
                                        System.out.printf("%s [%s: %s]%n", srv.getSrvName(), srv.getSrvIP(), srv.getPort());
                                    } else { // one of children is selected
                                        TreeItem<String> parent = selectedSrv.getParent();
                                        srv = new Server(parent.getValue(), parent.getChildren().get(0).getValue(), parent.getChildren().get(1).getValue());
                                        selecredSrv.setText(srv.getSrvName() + " Selected");
                                        System.out.printf("%s [%s: %s]%n", srv.getSrvName(), srv.getSrvIP(), srv.getPort());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        loadServers();
    }

    public void setScreenParent(ScreensController screenParent) {
        this.myController = screenParent;
    }

    @FXML
    void playGameAction(ActionEvent event) {
        if (!ongoingGame) { // this is a new game, sever must be selected!
            if (srv == null) { // server is not selected !
                showAlert(Alert.AlertType.ERROR, "System Alert", "New Game Error", "Please select a server to play Hangman!");
                return;
            }
            initializeGameVariables(true);
            ConnectService cs = new ConnectService();
            cs.start();
            ongoingGame = true;
        } else {
            try {
                initializeGameVariables(false);
                srvConn.writeToServer("new_game");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    @FXML
    void resetAction(ActionEvent event) throws IOException {
        if (ongoingGame) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Add New Server");
            alert.setContentText("Are you sure want to stop the game?\nNote that the ongoing game will be stopped and canceled!");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                //srvConn.writeToServer("stop_game");
                srvConn.getClientSocket().close();
                srvConn = null;
                srv = null;
                ongoingGame = false;
                initializeGameVariables(true);
                unselectAllNodes();
                info.setText("The game has been stopped and reseted successfully");
            }
        }
    }

    @FXML
    void sendLetter(ActionEvent event) {
        if (ongoingGame) {
            try {
                if (Integer.parseInt(remainingAttempts.getText()) > 0 && word.getText().contains("-")) {
                    String msg = ((JFXButton) event.getSource()).getText();
                    if (msg != null && !msg.isEmpty()) {
                        srvConn.writeToServer(msg);
                        System.out.println(msg);
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "System Information", "New Game Required", "Your either do not have remaining attempts or won the game! Please play a new game!\nGood Luck :)");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    void sendWholeWordAction(ActionEvent event) {
        if (ongoingGame) {
            try {
                if (Integer.parseInt(remainingAttempts.getText()) > 0 && word.getText().contains("-")) {
                    String msg = wholeWord.getText();
                    if (msg != null && !msg.isEmpty()) {
                        srvConn.writeToServer(msg);
                        System.out.println(msg);
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "System Information", "New Game Required", "Your either do not have remaining attempts or won the game! Please play a new game!\nGood Luck :)");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        wholeWord.setText("");
    }

    @FXML
    void addSrvAction(ActionEvent event) {
        if (ongoingGame) {
            showAlert(Alert.AlertType.WARNING, "System Warning", "Add New Server", "You cannot add a new server while you play!\nPlease end-up your game first");
            return;
        }
        System.out.println("client.HangmanClientController.addSrvAction()");
        this.myController.setScreen(ScreensFramework.insertingServerScreenID);
    }

    private void loadServers() {
        File f = new File("servers.xml");
        if (f.exists() && !f.isDirectory()) {
            try {
                srvTree.setShowRoot(true);

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse("servers.xml");
                NodeList serversList = doc.getElementsByTagName("server");

                for (int i = 0; i < serversList.getLength(); i++) {
                    Element srvEl = (Element) serversList.item(i);
                    Server server = new Server(srvEl.getElementsByTagName("srvName").item(0).getTextContent(), srvEl.getElementsByTagName("srvIP").item(0).getTextContent(), srvEl.getElementsByTagName("port").item(0).getTextContent());
                    addToTreeView(server);
                }
            } catch (ParserConfigurationException ex) {
                ex.printStackTrace();
            } catch (SAXException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        srv = null;
    }

    private void addToTreeView(Server server) {
        TreeItem<String> parent;
        if (server.getSrvName() == null || server.getSrvName().isEmpty()) {
            parent = makeBranch("Untitled Server", root, true);
        } else {
            parent = makeBranch("Server: " + server.getSrvName(), root, true);
        }

        makeBranch("IP: " + server.getSrvIP(), parent, true);
        makeBranch("Port: " + server.getPort(), parent, true);
    }

    private TreeItem<String> makeBranch(String title, TreeItem<String> parent, boolean setExpand) {
        TreeItem<String> item = new TreeItem<>(title);
        item.setExpanded(setExpand);
        parent.getChildren().add(item);
        return item;
    }

    private void unselectAllNodes() {
        srvTree.getSelectionModel().select(null);
        selecredSrv.setText("No Server Selected");
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void initializeGameVariables(boolean initScore) {
        word.setText("-");
        remainingAttempts.setText("0");
        if (initScore) {
            score.setText("0");
        }
        info.setText("");
        gameStatus.setText("");

        hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang.png")));
        hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item1.png")));

    }

    private class ConnectService extends Service<String> {

        private ConnectService() {
            setOnSucceeded((WorkerStateEvent event) -> {
                String result = getValue();
                if (result.equals("done")) {
                    info.setText("Connection establised with: " + srv.getSrvName() + " [" + srv.getSrvIP() + ":" + srv.getPort() + "]");
                    showAlert(Alert.AlertType.INFORMATION, "System Information", "Connection Established", "Connection to: " + srv.getSrvName() + " has been established successfully");
                    srvConn.writeToServer("new_game");
                    ReceiveService rs = new ReceiveService();
                    rs.start();
                } else {
                    info.setText("Unable to connect to: " + srv.getSrvName());
                    srvConn = null;
                    srv = null;
                    ongoingGame = false;
                    initializeGameVariables(true);
                    unselectAllNodes();
                    showAlert(Alert.AlertType.ERROR, "System Alert", "Connection Error", "Unable to connect to server\nError is: " + result);
                }
            });
        }

        @Override
        protected Task<String> createTask() {
            return new Task<String>() {
                @Override
                protected String call() {
                    srvConn = new ServerConnection();
                    if (srvConn.getClientSocket() == null || srvConn.getClientSocket().isClosed()) {  // if socket is not connected to the server
                        return srvConn.connect(srv.getSrvIP().substring(4, srv.getSrvIP().length()), srv.getPort().substring(6, srv.getPort().length()));
                    }
                    return "done"; //means the connection is already established and no need to establish it again
                }
            };
        }

        @Override
        public boolean cancel() { // not used !!!
            try {
                srvConn.getClientSocket().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return super.cancel();
        }

    }

    private class ReceiveService extends Service<Void> {

        private ReceiveService() {
            setOnSucceeded((WorkerStateEvent event) -> {
                // We can update GUI here as well instead of using Platform.runLater
            });

            setOnFailed((WorkerStateEvent event) -> {
                // We can update GUI here as well instead of using Platform.runLater
            });
        }

        @Override
        protected Task<Void> createTask() {
            return new Task() {
                @Override
                protected Object call() throws IOException {
                    srvConn.readFromServer(word, info, remainingAttempts, score, gameStatus, hang, hangItems);
                    return null;
                }
            };
        }

    }
}
