package de.hpi.bpt.fcm.engine.controller;

import de.hpi.bpt.fcm.engine.model.CaseModel;
import de.hpi.bpt.fcm.engine.model.DomainModel;
import org.jdom2.JDOMException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class LoadUMLListener implements ActionListener {
    CaseModel caseModel;

    public LoadUMLListener(CaseModel caseModel) {
        super();
        this.caseModel = caseModel;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Domain Model", "uml");
        int returnVal = chooser.showOpenDialog(((JMenuItem)event.getSource()).getParent());
        if(returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File umlFile = chooser.getSelectedFile();
        try {
            caseModel.setDm(DomainModel.fromFile(umlFile));
        } catch (JDOMException | NullPointerException e) {
            System.err.println("Could not parse UML file");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not read UML file");
            e.printStackTrace();
        }
    }
}
