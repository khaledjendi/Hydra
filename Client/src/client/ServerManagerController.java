package client;

import controller.ScreensFramework;
import controller.ScreensController;
import controller.IScreensController;
import com.jfoenix.controls.*;
import com.jfoenix.validation.*;

import java.net.URL;
import java.util.ResourceBundle;

import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.fontawesome.Icon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * Created by kal on 3/1/17.
 */
public class ServerManagerController implements Initializable, IScreensController {
    ScreensController myController;

    @FXML
    private JFXButton cancel;

    @FXML
    private JFXTextField srvIP;

    @FXML
    private JFXTextField port;

    @FXML
    private JFXButton saveSrv;

    @FXML
    private JFXTextField srvName;

    RequiredFieldValidator required;
    NumberValidator onlyNumber;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        required = new RequiredFieldValidator();
        onlyNumber = new NumberValidator();

        required.setMessage("Input must provided!");
        required.setIcon(new Icon(AwesomeIcon.WARNING, "1em", ";", "error"));

        onlyNumber.setMessage("Input must be only numbers");
        onlyNumber.setIcon(new Icon(AwesomeIcon.WARNING, "1em", ";", "error"));

        srvIP.getValidators().add(required);
        port.getValidators().add(onlyNumber);

        port.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                port.validate();
            }
        });

        srvIP.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                srvIP.validate();
            }
        });

    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
        this.myController = screenParent;
    }

    @FXML
    private void saveSrvAction(ActionEvent event) {
        if (srvIP.textProperty().getValue().length() > 0) {
            if (port.textProperty().getValue().length() <= 0 || !port.textProperty().getValue().matches("[0-9]+")) {
                showAlert(Alert.AlertType.ERROR, "System Alert", "Add Server Error!", "Server port must be provided.\nServer port must be numberic.");
                return;
            }
            String result = addServer(srvName.textProperty().getValue(), srvIP.textProperty().getValue(), port.textProperty().getValue());
            if (result.equals("done")) {
                showAlert(Alert.AlertType.INFORMATION, "System Alert", "Add Server Information", "Server has been added successfully");
                srvName.textProperty().setValue("");
                srvIP.textProperty().setValue("");
                port.textProperty().setValue("");
                this.myController.unloadScreen(ScreensFramework.hangmanClientID);
                this.myController.loadScreen(ScreensFramework.hangmanClientID, ScreensFramework.hangmanClientFile);
                this.myController.setScreen(ScreensFramework.hangmanClientID);
            } else {
                showAlert(Alert.AlertType.ERROR, "System Alert", "Add Server Error!", "Error: " + result);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "System Alert", "Add Server Error!", "Server address must be provided.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        this.myController.setScreen(ScreensFramework.hangmanClientID);
    }

    private String addServer(String srvName, String srvIP, String port) {
        try {
            //Will be changed later to SAX [Simple API XML] because it is light weight !
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = null;
            Element rootElement = null;
            File f = new File("servers.xml");
            if (f.exists() && !f.isDirectory()) {
                doc = docBuilder.parse("servers.xml");
                rootElement = doc.getDocumentElement();
            } else {
                doc = docBuilder.newDocument();
                rootElement = doc.createElement("servers");
                doc.appendChild(rootElement);
            }

            Element server = doc.createElement("server");
            rootElement.appendChild(server);

            Element serverName = doc.createElement("srvName");
            serverName.appendChild(doc.createTextNode(srvName));
            server.appendChild(serverName);

            Element serverIP = doc.createElement("srvIP");
            serverIP.appendChild(doc.createTextNode(srvIP));
            server.appendChild(serverIP);

            Element serverPort = doc.createElement("port");
            serverPort.appendChild(doc.createTextNode(port));
            server.appendChild(serverPort);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("servers.xml"));

            transformer.transform(source, result);
            return "done";

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            return pce.getMessage();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
            return tfe.getMessage();
        } catch (SAXException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        } catch (IOException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

}

