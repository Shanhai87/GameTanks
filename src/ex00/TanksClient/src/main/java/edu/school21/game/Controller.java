package edu.school21.game;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.json.JSONArray;
import org.json.JSONObject;

public class Controller {

    private final View view;
    private Socket socket;
    private BufferedReader inStream;
    private PrintWriter outStream;
    private String username;

    public Controller(View view) {
        this.view = view;
        this.socket = null;
    }

    public String getUsername() {
        return username;
    }

    boolean sign(String ip, int port, String username, String password, String sign) {
        boolean result = false;
        try {
            if (socket == null) {
                socket = new Socket(ip, port);
                inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outStream = new PrintWriter(socket.getOutputStream(), true);
            }
            sendMessageToServer(sign, username, password);
            String message = inStream.readLine();
            JSONObject json = new JSONObject(message);
            if (json.getString("stage").equals(sign)) {
                if (json.getString("status").equals("true")) {
                    this.username = username;
                    result = true;
                }
            }
        } catch (IOException ignore) {
        }
        return result;
    }

    private TimerTask getTimerTask(LinkedList<KeyCode> keyStack) {
        return new TimerTask() {
            @Override
            public void run() {
                if (!keyStack.isEmpty()) {
                    switch (keyStack.peek()) {
                        case LEFT:
                            sendCommandToServer("left");
                            break;
                        case RIGHT:
                            sendCommandToServer("right");
                            break;
                        case null, default:
                            break;
                    }
                }
            }
        };
    }

    void listenServer() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stage", "ready");
        outStream.println(jsonObject.toString().replaceAll("\"", "\\\""));
        new Thread(() -> {
            Timer timer = new Timer();
            label:
            while (!socket.isClosed()) {
                try {
                    String message = inStream.readLine();
                    if (message != null) {
                        JSONObject json = new JSONObject(message);
                        message = json.getString("stage");
                        switch (message) {
                            case "exit":
                                outStream.println(json.toString().replaceAll("\"", "\\\""));
                                System.exit(0);
                            case "startGame":
                                outStream.println(json.toString().replaceAll("\"", "\\\""));
                                final LinkedList<KeyCode> keyStack = getKeyCodes();
                                TimerTask task = getTimerTask(keyStack);
                                timer.scheduleAtFixedRate(task, 0L, 50);
                                break;
                            case "newGame":
                                getNewGame(json, timer);
                                break label;
                            case "inGame":
                                setGameParameters(json);
                                break;
                        }
                    }
                } catch (IOException ignore) {
                }
            }
        }).start();
    }

    private void getNewGame(JSONObject json, Timer timer) {
        timer.cancel();
        timer.purge();
        boolean isWin = json.getBoolean("isWin");
        int[] stats = new int[4];
        JSONArray array = json.getJSONArray("stats");
        for (int i = 0; i < 4; ++i) {
            stats[i] = array.getInt(i);
        }
        Platform.runLater(() -> view.gameScreen(view.getPlayer().getName(), false, isWin,
                stats));
        outStream.println(json.toString().replaceAll("\"", "\\\""));
    }

    private void setGameParameters(JSONObject json) {
        Platform.runLater(() -> {
            if (view.getEnemy().getLabelName().getText().isEmpty()) {
                view.getEnemy().getLabelName().setText(json.getString("enemyName"));
            }
            view.getPlayer().getPane().setLayoutX(json.getDouble("playerX"));
            view.getEnemy().getPane().setLayoutX(json.getDouble("enemyX"));
            var iterator = ((Pane) view.getStage().getScene().getRoot()).getChildren().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                iterator.next();
                if (++i > 8) {
                    iterator.remove();
                }
            }
            view.getPlayerShells().clear();
            drawShells((JSONArray) json.get("playerShells"), 1);
            drawShells((JSONArray) json.get("enemyShells"), 2);
            double playerHealth = json.getDouble("playerHealth");
            double enemyHealth = json.getDouble("enemyHealth");
            view.getPlayer().getBar().setPrefWidth(370 * playerHealth / 100);
            view.getEnemy().getBar().setPrefWidth(370 * enemyHealth / 100);
        });
    }

    private LinkedList<KeyCode> getKeyCodes() {
        Scene scene = view.getStage().getScene();
        AtomicBoolean shoot = new AtomicBoolean(true);
        final LinkedList<KeyCode> keyStack = new LinkedList<>();
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code.equals(KeyCode.RIGHT) || code.equals(KeyCode.LEFT)) {
                if (!keyStack.contains(code)) {
                    keyStack.push(code);
                }
            } else if (shoot.get() && code.equals(KeyCode.SPACE)) {
                shoot.set(false);
                sendCommandToServer("shoot");
            }
        });

        scene.setOnKeyReleased(event -> {
            keyStack.remove(event.getCode());
            if (event.getCode().equals(KeyCode.SPACE)) {
                shoot.set(true);
            }
        });
        return keyStack;
    }

    private void drawShells(JSONArray array, int flag) {
        Shell shell;
        double x, y;
        for (int i = 0; i < array.length(); ++i) {
            JSONObject jsonShell = array.getJSONObject(i);
            x = jsonShell.getLong("x") + 23;
            if (flag == 1) {
                y = 345 - jsonShell.getLong("y");
                shell = new Shell(x, y, View.image1);
            } else {
                y = jsonShell.getLong("y") + 95;
                shell = new Shell(x, y, View.image2);
            }
            Shell finalShell = shell;
            ((Pane) view.getStage().getScene().getRoot()).getChildren().add(finalShell.getPane());
            view.getPlayerShells().add(shell);
        }
    }

    void exit() {
        if (outStream != null) {
            JSONObject json = new JSONObject();
            json.put("stage", "exit");
            String message = json.toString().replaceAll("\"", "\\\"");
            outStream.println(message);
            System.exit(0);
        }
    }

    private void sendMessageToServer(String stage, String username, String password) {
        JSONObject json = new JSONObject();
        json.put("stage", stage);
        json.put("username", username);
        json.put("password", password);
        String message = json.toString().replaceAll("\"", "\\\"");
        outStream.println(message);
    }

    void sendCommandToServer(String command) {
        JSONObject json = new JSONObject();
        json.put("stage", "inGame");
        json.put("command", command);
        String message = json.toString().replaceAll("\"", "\\\"");
        outStream.println(message);
    }
}
