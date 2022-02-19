import de.hpi.bpt.fcm.engine.model.CaseModel;
import de.hpi.bpt.fcm.engine.view.MainForm;
import org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new RadianceGraphiteLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            RecommendationDialog dialog = new RecommendationDialog(420, 180);
            boolean recommendations = dialog.showDialog();
            JFrame mainFrame = new MainForm("fCM Engine", new CaseModel(recommendations));
            mainFrame.setSize(500, 500);
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }

    private static class RecommendationDialog extends JDialog {
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");
        JTextPane infoText = new JTextPane();
        boolean recommendations = false;

        RecommendationDialog(int width, int height) {
            setSize(width, height);
            setLocationRelativeTo(null);
            infoText.setText("This version of the fCM engine is setup for a user study on providing decision support for knowledge workers. It automatically loads an example case and must not be used with other case models. Do you want to receive recommendations?");
            infoText.setEnabled(false);
            infoText.setDisabledTextColor(infoText.getForeground());

            JDialog dialog = this;

            yesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    recommendations = true;
                    return;
                }
            });

            noButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });

            this.setLayout(new BorderLayout());
            this.add(infoText, BorderLayout.CENTER);
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            yesButton.setForeground(new Color(87, 155, 247));
            buttonPanel.add(noButton);
            buttonPanel.add(yesButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setUndecorated(true);
            dialog.setModal(true);
        }

        public boolean showDialog() {
            this.requestFocus();
            this.setVisible(true);
            return recommendations;
        }
    }

}
