import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        App.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // load Default Scene
        Parent root = FXMLLoader.load(getClass().getResource("scenes/main_view.fxml"));
        Scene scene = new Scene(root, 1080, 720);
        primaryStage.setTitle("FXML Welcome");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
