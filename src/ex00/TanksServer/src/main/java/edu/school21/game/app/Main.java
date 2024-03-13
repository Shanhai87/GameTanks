package edu.school21.game.app;

import edu.school21.game.server.Server;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        new Server().start(8081);
    }
}
