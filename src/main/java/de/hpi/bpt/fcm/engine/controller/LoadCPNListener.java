package de.hpi.bpt.fcm.engine.controller;


import de.hpi.bpt.fcm.engine.model.ColoredPetriNet;
import org.cpntools.accesscpn.engine.EvaluationException;
import org.cpntools.accesscpn.model.importer.NetCheckException;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoadCPNListener implements ActionListener {
    ColoredPetriNet cpnModel;

    public LoadCPNListener(ColoredPetriNet cpnModel) {
        super();
        this.cpnModel = cpnModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Colored Petri Net", "cpn");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(((JMenuItem)e.getSource()).getParent());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                cpnModel.loadCPN(chooser.getSelectedFile());
            } catch (IOException ex) {
                System.err.println("Could not read file");
                ex.printStackTrace();
            } catch (EvaluationException ex) {
                System.err.println("Could not evaluate Petri net");
                ex.printStackTrace();
            } catch (NetCheckException ex) {
                System.err.println("Petri net check failed");
                ex.printStackTrace();
            } catch (ParserConfigurationException | SAXException ex) {
                System.err.println("CPN could not been parsed");
                ex.printStackTrace();
            }
            try {
                cpnModel.verifyCpn();
            } catch (Exception ex) {
                System.err.println("Petri net verification failed");
                ex.printStackTrace();
            }
            cpnModel.initialize();
        }

    }
}
