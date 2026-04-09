package application;
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import view.LoginView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        LoginView loginView = new LoginView();
        new LoginController(loginView);
        loginView.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
