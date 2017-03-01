package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by kal on 3/1/17.
 */
public class ServerConnection {

    private PrintWriter out = null;
    private Socket clientSocket = null;

    public Socket getClientSocket() {
        return clientSocket;
    }

    public ServerConnection() {

    }

    public String connect(String host, String portStr) {
        try {
            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ex) {
                return "Unable to connect!\nPort is not numeric value!";
            }
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(host, port), 1000);
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            return "done";
        } catch (UnknownHostException e) {
            clientSocket = null;
            return "Host: " + host + " is unknown or not available in the nework.";
        } catch (IOException e) {
            clientSocket = null;
            return "Couldn't get I/O for the connection to: " + host + ".";
        }
    }

    public void readFromServer(Label word, Label info, Label remainingAttempts, Label score, Label gameStatus, ImageView hang, ImageView hangItems) throws IOException {
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        while ((line = in.readLine()) != null) {
            if (line.equals("ok_stopped")) {
                break;
            }
            String[] msg = line.split(";");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    word.setText(msg[1].contains("-") ? msg[1].replaceAll(".(?!$)", "$0 ") : msg[1]);
                    remainingAttempts.setText(msg[2]);
                    score.setText(msg[3]);
                    switch (msg[0]) {
                        case "start":
                            info.setText("The new game has been started successfully :)");
                            break;
                        case "win":
                            info.setText("Good job! you won!");
                            gameStatus.setText("Bravo! You Won!");
                            break;
                        case "wrong":
                            info.setText("Sorry, Your guess was wrong!");
                            setImages(hang, hangItems, Integer.parseInt(msg[2]));
                            break;
                        case "correct":
                            info.setText("good guess! Continue moving on!");
                            break;
                        case "big_win":
                            info.setText("Wow! Great guess! You knew the entire word :)");
                            gameStatus.setText("Bravo! You Won!");
                            break;
                        case "lose":
                            setImages(hang, hangItems, Integer.parseInt(msg[2]));
                            info.setText("Sorry! Game over :( Please try again");
                            gameStatus.setText("Game Over!");
                            break;
                        default:
                            break;
                    }
                }

                private void setImages(ImageView hang, ImageView hangItems, int remainingAttempts) {
                    switch (remainingAttempts) {
                        case 6:
                            hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang1.png")));
                            hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item2.png")));
                            break;
                        case 5:
                            hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang2.png")));
                            hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item3.png")));
                            break;
                        case 4:
                            hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang3.png")));
                            hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item4.png")));
                            break;
                        case 3:
                            hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang4.png")));
                            hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item5.png")));
                            break;
                        case 2:
                            hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang5.png")));
                            hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item6.png")));
                            break;
                        case 1:
                            hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang6.png")));
                            hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item7.png")));
                            break;
                        case 0:
                            hang.setImage(new Image(getClass().getResourceAsStream("/hangs/hang7.png")));
                            hangItems.setImage(new Image(getClass().getResourceAsStream("/item/item8.png")));
                            break;
                    }
                }
            });
        }
        clientSocket.close();
    }

    public void writeToServer(String msg) {
        out.println(msg.toLowerCase());
        out.flush();
    }
}
