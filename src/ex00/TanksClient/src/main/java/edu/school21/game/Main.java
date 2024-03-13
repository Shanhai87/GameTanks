package edu.school21.game;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        View view = new View(stage);
        Controller controller = new Controller(view);
        view.init(controller);
    }
}
