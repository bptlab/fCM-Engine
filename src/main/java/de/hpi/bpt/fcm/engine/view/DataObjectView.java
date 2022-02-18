package de.hpi.bpt.fcm.engine.view;

import de.hpi.bpt.fcm.engine.model.DataObject;

import javax.swing.*;
import java.awt.*;

public class DataObjectView extends JPanel {

    private final DataObject dataObject;
    private AttributeView[] attributeViews;

    public DataObjectView(DataObject dataObject) {
        super();
        this.dataObject = dataObject;
        this.setBackground(new Color(43, 43, 43));
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridLayout(dataObject.getClazz().getAttributes().length+1, 2));
        attributeViews = new AttributeView[dataObject.getClazz().getAttributes().length];
        this.add(new JLabel(dataObject.getClazz().getName() + " " + dataObject.getInstanceNo()));
        this.add(new JLabel());
        for (int i = 0; i < attributeViews.length; i++) {
            attributeViews[i] = new AttributeView(dataObject.getClazz().getAttributes()[i]);
            this.add(attributeViews[i].attributeLabel);
            this.add(attributeViews[i].attributeField);
        }
    }

    public void updateModel() {
        for (int i = 0; i < attributeViews.length; i++) {
            dataObject.setValue(i, attributeViews[i].attributeField.getText());
        }
    }

    private static class AttributeView {
        JTextField attributeField;
        JLabel attributeLabel;

        AttributeView(String name) {
            attributeLabel = new JLabel(name);
            attributeField = new JTextField();
        }
    }

}
