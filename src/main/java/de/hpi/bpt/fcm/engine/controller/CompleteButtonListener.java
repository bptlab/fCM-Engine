package de.hpi.bpt.fcm.engine.controller;

import de.hpi.bpt.fcm.engine.model.ColoredPetriNet;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CompleteButtonListener implements ActionListener {
    private final ColoredPetriNet cpnModel;
    private final JPanel formPanel;

    public CompleteButtonListener(ColoredPetriNet cpnModel, JPanel formPanel) {
        super();
        this.cpnModel = cpnModel;
        this.formPanel = formPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cpnModel.executeCurrentBindingElement();
        formPanel.removeAll();
        formPanel.repaint();
    }
}
