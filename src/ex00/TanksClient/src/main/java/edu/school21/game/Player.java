package edu.school21.game;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.Objects;

public class Player {
    private final String name;
    private final Pane pane;
    private final Pane bar;
    private final Pane border;
    private final Label labelName;

    public Player(String name, Integer x, Integer y, String image, Integer barY) {
        this.name = name;
        this.pane = new Pane();
        pane.setPrefWidth(50);
        pane.setPrefHeight(50);
        pane.setLayoutX(x);
        pane.setLayoutY(y);
        pane.setBackground(new Background(new BackgroundImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                image)), 50D, 50D, false, false), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        bar = new Pane();
        bar.setPrefWidth(370);
        bar.setPrefHeight(20);
        bar.setLayoutX(65);
        bar.setLayoutY(barY + 5);
        bar.setBackground(new Background(new BackgroundImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                "/images/life.png")), 400D, 30D, false, false), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        border = new Pane();
        border.setPrefWidth(400);
        border.setPrefHeight(30);
        border.setLayoutX(50);
        border.setLayoutY(barY);
        border.setBackground(new Background(new BackgroundImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                "/images/border.png")), 400D, 30D, false, false), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        labelName = new Label(name);
        labelName.setLayoutX(500);
        labelName.setLayoutY(barY);
        labelName.setFont(new Font(30));
    }

    public Pane getBar() {
        return bar;
    }

    public Pane getBorder() {
        return border;
    }

    public Label getLabelName() {
        return labelName;
    }

    public String getName() {
        return name;
    }

    public Pane getPane() {
        return pane;
    }

    public double getX() {
        return pane.getLayoutX();
    }

    public double getY() {
        return pane.getLayoutY();
    }

    public void setX(double x) {
        if (x > 590) {
            x = 590;
        } else if (x < 0) {
            x = 0;
        }
        this.pane.setLayoutX(x);
    }
}
