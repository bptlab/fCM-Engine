import de.hpi.bpt.fcm.engine.model.CaseModel;
import de.hpi.bpt.fcm.engine.view.MainForm;
import org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteLookAndFeel;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new RadianceGraphiteLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JFrame mainFrame = new MainForm("fCM Engine", new CaseModel());
            mainFrame.setSize(500, 500);
            mainFrame.setVisible(true);
        });
    }

}
