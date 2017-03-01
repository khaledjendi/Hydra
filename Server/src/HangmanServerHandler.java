/**
 * Created by kal on 3/1/17.
 */

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class HangmanServerHandler implements Runnable {
    private final static short _TatalAttempts = 7;

    private Socket clientSocket;
    private ArrayList<String> words;

    private String currentWord;
    private String attempedGuess;

    private short remainingAttempts;
    private short score;

    HangmanServerHandler(ArrayList<String> words, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.words = words;
        this.score = 0;
    }

    @Override
    public void run() {
        BufferedReader br = null;
        PrintWriter pw = null;

        try {
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String line;
            String msg;

            while ((line = br.readLine()) != null) {
                if (line.trim().equals("new_game")) {
                    currentWord = getRandomWord();
                    remainingAttempts = _TatalAttempts;
                    attempedGuess = "";
                    msg = String.format("start;%s;%s;%s", getWordWithDashes(), remainingAttempts, score);
                    pw.println(msg);
                    pw.flush();
                } else if (line.trim().equals(currentWord)) {
                    score++;
                    msg = String.format("win;%s;%s;%s", currentWord, remainingAttempts, score);
                    pw.println(msg);
                    pw.flush();
                } else if (line.trim().length() == 1) {

                    String oldWordWithDashes = getWordWithDashes();
                    attempedGuess += line;
                    String newWordWithDashes = getWordWithDashes();

                    if (newWordWithDashes.equals(currentWord)) {
                        score++;
                        msg = String.format("big_win;%s;%s;%s", currentWord, remainingAttempts, score);
                        pw.println(msg);
                        pw.flush();
                    } else if (oldWordWithDashes.equals(newWordWithDashes)) {
                        if (--remainingAttempts == 0) {
                            score--;
                            msg = String.format("lose;%s;%s;%s", currentWord, remainingAttempts, score);
                            pw.println(msg);
                            pw.flush();
                        } else {
                            msg = String.format("wrong;%s;%s;%s", newWordWithDashes, remainingAttempts, score);
                            pw.println(msg);
                            pw.flush();
                        }
                    } else {
                        msg = String.format("correct;%s;%s;%s", newWordWithDashes, remainingAttempts, score);
                        pw.println(msg);
                        pw.flush();
                    }
                } else if (--remainingAttempts == 0) {
                    score--;
                    String message = String.format("lose;%s;%s;%s", currentWord, remainingAttempts, score);
                    pw.println(message);
                    pw.flush();
                } else {
                    String message = String.format("wrong;%s;%s;%s", getWordWithDashes(), remainingAttempts, score);
                    pw.println(message);
                    pw.flush();
                }
            }
            clientSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            br.close();
            pw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getRandomWord() {
        int randIndex = (int) (Math.random() * words.size());
        String word = words.get(randIndex);
        System.out.println("=== Word === " + word);
        return word;
    }

    private String getWordWithDashes() {
        if (attempedGuess.length() > 0) {
            return currentWord.replaceAll("[^" + attempedGuess + "]", "-");
        } else {
            return getAllWordToDashes(new StringBuilder(currentWord), 0);
        }
    }

    private String getAllWordToDashes(StringBuilder word, int charIndex) {
        if (charIndex >= word.length()) {
            return word.toString();
        } else {
            word.setCharAt(charIndex, '-');
            return getAllWordToDashes(word, ++charIndex);
        }
    }
}
