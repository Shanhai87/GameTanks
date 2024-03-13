package edu.school21.game;

import javafx.scene.image.Image;
import javafx.scene.layout.*;

public class Shell {
    private final Pane pane;

    public Shell(double x, double y, Image image) {
        this.pane = new Pane();
        pane.setPrefWidth(5);
        pane.setPrefHeight(5);
        pane.setLayoutX(x);
        pane.setLayoutY(y);
        pane.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
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
        pane.setLayoutX(x);
    }

    public void setY(double y) {
        pane.setLayoutY(y);
    }

    @Override
    public String toString() {
        return "Shell{" +
                ", pane=" + pane.getLayoutX() +
                " " + pane.getLayoutY() + '}';
    }
}
