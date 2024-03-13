package edu.school21.game;

import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class View {
    private final Stage stage;
    private Controller controller;
    private final Label labelMessage;
    private final List<Shell> shells;
    private volatile Player player;
    private volatile Player enemy;
    static final Image image1 = new Image(Objects.requireNonNull(View.class.getResourceAsStream("/images/playerBullet" +
            ".png")), 7D, 7D, false, false);
    static final Image image2 = new Image(Objects.requireNonNull(View.class.getResourceAsStream("/images/enemyBullet" +
            ".png")), 7D, 7D, false, false);

    public View(Stage stage) {
        this.stage = stage;
        this.labelMessage = new Label("");
        labelMessage.setFont(new Font(20));
        shells = new ArrayList<>();
        player = null;
        enemy = null;
    }

    public Stage getStage() {
        return stage;
    }

    public List<Shell> getPlayerShells() {
        return shells;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getEnemy() {
        return enemy;
    }

    public void init(Controller controller) {
        this.controller = controller;
        stage.setResizable(false);
        stage.setWidth(640);
        stage.setHeight(480);
        stage.setTitle("Tanks");
        stage.setOnCloseRequest(we -> controller.exit());
        InputStream iconStream = getClass().getResourceAsStream("/images/player.png");
        Image image;
        if (iconStream != null) {
            image = new Image(iconStream);
            stage.getIcons().add(image);
        }
        startScreen();
    }

    private Pane createPane(double height, BackgroundFill background) {
        Pane pane = new Pane();
        pane.setPrefWidth(640);
        pane.setPrefHeight(height);
        pane.setBackground(new Background(background));
        return pane;
    }

    private TextField createTextField(double layoutX, double layoutY, double width, String text, String prompt,
                                      String pattern) {
        TextField field = new TextField(text);
        field.setLayoutX(layoutX);
        field.setLayoutY(layoutY);
        field.setFont(new Font(30));
        field.setAlignment(Pos.CENTER);
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        if (pattern != null) {
            field.setTextFormatter(new TextFormatter<>(change -> {
                if (change.getControlNewText().matches(pattern)) {
                    return change;
                }
                return null;
            }));
        }
        return field;
    }

    private Button creareButton(double layoutX, double layoutY, String text) {
        Button button = new Button(text);
        button.setLayoutX(layoutX);
        button.setLayoutY(layoutY);
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER);
        button.setFont(new Font(30));
        return button;
    }

    private void updateLabel(double layoutX, String text, boolean result) {
        if (result) {
            labelMessage.setTextFill(Color.color(0, 1, 0));
        } else {
            labelMessage.setTextFill(Color.color(1, 0, 0));
        }
        labelMessage.setText(text);
        labelMessage.setLayoutX(layoutX);
        labelMessage.setLayoutY(350);
    }

    private void startScreen() {
        BackgroundFill startScreen1 = new BackgroundFill(Color.DARKVIOLET, null, null);
        BackgroundFill startScreen2 = new BackgroundFill(Color.MOCCASIN, new CornerRadii(40), new Insets(40));
        Pane pane1 = createPane(480, startScreen1);
        Pane pane2 = createPane(440, startScreen2);
        TextField fieldName = createTextField(100, 80, 440, null, "Enter UserName", null);
        TextField fieldPassword = createTextField(100, 150, 440, null, "Enter Password", null);
        TextField fieldIp = createTextField(100, 220, 300, "127.0.0.1", "Enter Ip Address", "[0-9\\.]{0,15}");
        TextField fieldPort = createTextField(410, 220, 130, "8081", "Port", "\\d{0,5}");
        Button buttonSignIn = creareButton(100, 290, "SignIn");

        buttonSignIn.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            createButtonEvent(new TextField[]{fieldName, fieldPassword, fieldIp, fieldPort}, 1);
        });
        Button buttonSignUp = creareButton(340, 290, "SignUp");
        buttonSignUp.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            createButtonEvent(new TextField[]{fieldName, fieldPassword, fieldIp, fieldPort}, 2);
        });
        pane2.getChildren().addAll(fieldName, fieldPassword, fieldIp, fieldPort, buttonSignIn, buttonSignUp,
                labelMessage);
        pane1.getChildren().add(pane2);
        stage.setScene(new Scene(pane1));
        stage.show();
    }

    private void createButtonEvent(TextField[] fields, int flag) {
        String username = fields[0].getText();
        String password = fields[1].getText();
        if (username == null || password == null) {
            return;
        }
        username = username.trim();
        password = password.trim();
        if (username.isEmpty() || password.isEmpty()) {
            return;
        }
        if (flag == 1) {
            if (controller.sign(fields[2].getText(), Integer.parseInt(fields[3].getText()), username, password,
                    "signIn")) {
                updateLabel(100, "Success to SignIn", true);
                gameScreen(username, true, true, null);
            } else {
                updateLabel(100, "Fail to SignIn", false);
            }
        } else {
            if (controller.sign(fields[2].getText(), Integer.parseInt(fields[3].getText()), username, password,
                    "signUp")) {
                updateLabel(340, "Success to SignUp", true);
            } else {
                updateLabel(340, "Fail to SignUp", false);
            }
        }
    }

    public void gameScreen(String username, boolean firstGame, boolean isWin, int[] stats) {
        if (!firstGame) {
            createModalWindow(isWin, stats);
        }
        BackgroundImage image = new BackgroundImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                "/images/field.png")), 640D, 480D, true, true), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        Pane field = new Pane();
        player = new Player(username, 295, 350, "/images/player.png", 400);
        field.setBackground(new Background(image));
        Button button = creareButton(230, 180, "Start");
        button.setFocusTraversable(false);
        field.getChildren().addAll(player.getPane(), button);
        Scene scene = new Scene(field);
        stage.setScene(scene);
        AnimationTimer shellAnimation = getShellAnimation(field);
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
                Shell shell = new Shell(player.getX() + 23, player.getY() - 5, image1);
                shells.add(shell);
                field.getChildren().addAll(shell.getPane());
            }
        });
        scene.setOnKeyReleased(event -> {
            keyStack.remove(event.getCode());
            if (event.getCode().equals(KeyCode.SPACE)) {
                shoot.set(true);
            }
        });
        final AnimationTimer playerAnimation = getPlayerAnimation(keyStack, shoot);
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            field.getChildren().remove(button);
            shellAnimation.stop();
            playerAnimation.stop();
            startGame();
        });
    }

    private void createModalWindow(boolean isWin, int[] stats) {
        Stage modalStage = new Stage();
        Pane modalRoot = new Pane();
        String image;
        Label[] labels = new Label[5];
        labels[0] = new Label("Shoots:\t" + (stats[0] + stats[1]));
        if (stats[0] == 0 && stats[1] == 0) {
            labels[1] = new Label("Hits:\t0\t(0 %)");
            labels[2] = new Label("Misses:\t0\t(0 %)");
        } else {
            labels[1] = new Label("Hits:\t" + stats[0] + "\t(" + String.format("%.2f",
                    stats[0] * 100.0 / (stats[0] + stats[1])) + " %)");
            labels[2] = new Label("Misses:\t" + stats[1] + "\t(" + String.format("%.2f",
                    stats[1] * 100.0 / (stats[0] + stats[1])) + " %)");
        }
        if (stats[2] == 0 && stats[3] == 0) {
            labels[3] = new Label("All Hits:\t0\t(0 %)");
            labels[4] = new Label("All Misses:\t0\t(0 %)");
        } else {
            labels[3] = new Label("All Hits:\t" + String.format("%.2f",
                    stats[2] * 100.0 / (stats[2] + stats[3])) + " %");
            labels[4] = new Label("All Misses:\t" + String.format("%.2f",
                    stats[3] * 100.0 / (stats[2] + stats[3])) + " %");
        }
        int i = 40;
        for (Label label : labels) {
            label.setLayoutX(50);
            label.setLayoutY(i);
            i += 40;
            label.setFont(new Font(30));
            label.setTextFill(Color.DARKVIOLET);
            label.setBackground(Background.fill(Color.color(0.8, 0.8, 0.8, 0.8)));
        }
        modalRoot.getChildren().addAll(labels);
        if (isWin) {
            image = "/images/victory.png";
            modalStage.setTitle("Victory");
        } else {
            image = "/images/fail.png";
            modalStage.setTitle("Defeat");
        }
        BackgroundImage backgroundImage =
                new BackgroundImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        image)), 400D, 300D, false, false), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                        BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        modalRoot.setBackground(new Background(backgroundImage));
        modalStage.setScene(new Scene(modalRoot, 400, 300));
        modalStage.setResizable(false);
        modalStage.initOwner(stage);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.showAndWait();
    }

    private AnimationTimer getShellAnimation(Pane field) {
        AnimationTimer shellAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (int i = 0; i < shells.size(); ++i) {
                    Shell shell = shells.get(i);
                    shell.setY(shell.getY() - 2);
                    if (shell.getY() < 0) {
                        field.getChildren().remove(shell.getPane());
                        shells.remove(shell);
                    }
                }
            }
        };
        shellAnimation.start();
        return shellAnimation;
    }

    private AnimationTimer getPlayerAnimation(LinkedList<KeyCode> keyStack, AtomicBoolean shoot) {
        final LongProperty lastUpdateTime = new SimpleLongProperty();
        final AnimationTimer playerAnimation = new AnimationTimer() {
            @Override
            public void handle(long timestamp) {
                if (!keyStack.isEmpty() && lastUpdateTime.get() > 0) {
                    final double elapsedSeconds = (timestamp - lastUpdateTime.get()) / 100_000_000.0;
                    switch (keyStack.peek()) {
                        case LEFT:
                            player.setX(player.getX() - 10 * elapsedSeconds);
                            break;
                        case RIGHT:
                            player.setX(player.getX() + 10 * elapsedSeconds);
                            break;
                        case SPACE:
                            shoot.set(true);
                        case null, default:
                            break;
                    }
                }
                lastUpdateTime.set(timestamp);
            }
        };
        playerAnimation.start();
        return playerAnimation;
    }

    void startGame() {
        BackgroundImage image = new BackgroundImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                "/images/field.png")), 640D, 480D, true, true), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        Pane field = new Pane();
        field.setBackground(new Background(image));
        player = new Player(controller.getUsername(), 295, 350, "/images/player.png", 410);
        enemy = new Player("", 295, 50, "/images/enemy.png", 10);

        field.getChildren().addAll(player.getPane(), player.getBar(), player.getBorder(), player.getLabelName(),
                enemy.getPane(), enemy.getBar(), enemy.getBorder(), enemy.getLabelName());
        Scene scene = new Scene(field);
        stage.setScene(scene);
        controller.listenServer();
    }
}
