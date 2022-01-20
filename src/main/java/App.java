import de.hpi.bpt.fcm.engine.model.CaseModel;
import de.hpi.bpt.fcm.engine.view.MainForm;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        JFrame mainFrame = new MainForm("fCM Engine", new CaseModel());
        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);
    }

}
