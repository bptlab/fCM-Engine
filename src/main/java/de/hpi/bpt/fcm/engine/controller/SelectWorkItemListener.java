package de.hpi.bpt.fcm.engine.controller;

import de.hpi.bpt.fcm.engine.model.ColoredPetriNet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SelectWorkItemListener implements ListSelectionListener {
    private ColoredPetriNet cpnModel;

    public SelectWorkItemListener(ColoredPetriNet cpnModel) {
        this.cpnModel = cpnModel;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        //if (e.getValueIsAdjusting()) {
        //    return;
        //}
        ListSelectionModel lsm = ((JList)e.getSource()).getSelectionModel();
        if (lsm.isSelectionEmpty()) return;
        int index = lsm.getAnchorSelectionIndex();
        try {
            cpnModel.updateInputOutputOptions(index);
        } catch (Exception ex) {
            System.err.println("Could not load bindings");
            ex.printStackTrace();
        }
    }
}
