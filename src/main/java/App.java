import javafx.application.Application;
import javafx.stage.Stage;
import vista.TelLlevoApp;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        new TelLlevoApp(stage).mostrarLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
