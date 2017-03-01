/**
 * Created by kal on 3/1/17.
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HangmanServer {
    private static ArrayList<String> words = null;
    private static boolean listening = true;

    private static final int availableThreads = Runtime.getRuntime().availableProcessors();
    private static ServerSocket serverSocket = null;
    private static ExecutorService executorService = Executors.newFixedThreadPool(availableThreads);

    public static void main(String[] args) {
        readWords();
        listen(args);
    }

    private static void listen(String[] args) {
        try {
            serverSocket = new ServerSocket(setPort(args));
            System.out.printf("Available threads: [%s]%n", availableThreads);
            System.out.printf("Awaiting connections from port [%s]...%n%n", serverSocket.getLocalPort());
            while (listening) {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(new HangmanServerHandler(words, clientSocket));
                //(new HangmanServerHandler(words, clientSocket)).start();
                System.out.printf("Client: %s [%s] is connected%n", clientSocket.getLocalAddress().getHostName(), clientSocket.getLocalAddress().getHostAddress());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static int setPort(String[] args) {
        int port = 5151;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = 5151;
            }
        }
        return port;
    }

    private static void readWords() {
        words = new ArrayList<String>();
        String line;
        try {
            BufferedReader bReader = new BufferedReader(new FileReader("Server/words.txt"));

            while ((line = bReader.readLine()) != null) {
                words.add(line.toLowerCase());
            }

            bReader.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void stopServer() {
        //Stop the executor service.
        listening = false;
        executorService.shutdownNow();
        try {
            //Stop accepting requests.
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error in server shutdown");
            e.printStackTrace();
        }
        System.exit(0);
    }
}
