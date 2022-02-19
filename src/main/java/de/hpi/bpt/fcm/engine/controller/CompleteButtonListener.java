package de.hpi.bpt.fcm.engine.controller;

import de.hpi.bpt.fcm.engine.model.ColoredPetriNet;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CompleteButtonListener implements ActionListener {
    private final ColoredPetriNet cpnModel;
    private final JPanel formPanel;
    private final JLabel statusText;
    private int countActions = 0;

    public CompleteButtonListener(ColoredPetriNet cpnModel, JPanel formPanel, JLabel statusText) {
        super();
        this.cpnModel = cpnModel;
        this.formPanel = formPanel;
        this.statusText = statusText;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (cpnModel.executeCurrentBindingElement()) countActions++;
        formPanel.removeAll();
        formPanel.repaint();
        statusText.setText("#Executed Activitites: " + countActions);
    }
}
