package de.hpi.bpt.fcm.engine.controller;

import de.hpi.bpt.fcm.engine.model.CaseModel;
import de.hpi.bpt.fcm.engine.model.ColoredPetriNet;
import de.hpi.bpt.fcm.engine.model.DataObject;
import de.hpi.bpt.fcm.engine.model.ObjectModel;
import de.hpi.bpt.fcm.engine.view.DataObjectView;
import de.hpi.bpt.fcm.engine.view.MainForm;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectInputOutputListListener implements ListSelectionListener {
    private final CaseModel caseModel;
    private final JPanel objectPanel;
    private final Map<String, DataObjectView> dataObjectViews;

    public SelectInputOutputListListener(CaseModel caseModel, JPanel objectPanel, Map<String, DataObjectView> dataObjectViews) {
        this.caseModel = caseModel;
        this.objectPanel = objectPanel;
        this.dataObjectViews = dataObjectViews;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = ((JList)e.getSource()).getSelectionModel();
        if (lsm.isSelectionEmpty()) return;
        int index = lsm.getAnchorSelectionIndex();
        //if (e.getValueIsAdjusting()) return;
        caseModel.getCpn().selectBinding(index);
        updateObjectPanel(caseModel.getCpn().getTokens(e.getFirstIndex()));
    }

    private void updateObjectPanel(List<ColoredPetriNet.DataObjectToken> tokens) {
        objectPanel.removeAll();
        for (ColoredPetriNet.DataObjectToken token : tokens) {
            if (token.getName().equalsIgnoreCase("case")) continue;
            DataObject dataObject = caseModel.getOm().getObjectFor(token.getObjectId());
            DataObjectView view = dataObjectViews.computeIfAbsent(token.getObjectId(), key -> new DataObjectView(dataObject));
            objectPanel.add(view);
        }

        objectPanel.revalidate();
        objectPanel.repaint();
    }
}
