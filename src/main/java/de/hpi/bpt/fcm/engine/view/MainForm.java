package de.hpi.bpt.fcm.engine.view;

import de.hpi.bpt.fcm.engine.controller.*;
import de.hpi.bpt.fcm.engine.model.CaseModel;
import de.hpi.bpt.fcm.engine.model.ColoredPetriNet;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class MainForm extends JFrame {
    public JSplitPane panel;
    public JMenuBar menuBar = new JMenuBar();
    public JMenu fileMenu = new JMenu("File");
    public JMenuItem loadCPNItem = new JMenuItem("Load CPN");
    public JMenuItem loadUMLItem = new JMenuItem("Load UML");
    public JMenu aboutMenu = new JMenu("About");
    public JMenuItem exitOption = new JMenuItem("Exit");
    public JList<ColoredPetriNet.ElementWithRecommendation> workItemList;
    public JList<ColoredPetriNet.ElementWithRecommendation> inputOutputList;
    public JPanel statusPanel = new JPanel();
    public JButton completeButton = new JButton("Complete");
    public JLabel statusText = new JLabel("");
    public JPanel formPanel = new JPanel();
    private final Map<String, DataObjectView> dataObjectViews = new HashMap<>();

    public MainForm(String title, CaseModel caseModel) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // List
        workItemList = new JList<>(caseModel.getCpn().getWorkItemModel());
        workItemList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellhasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellhasFocus);
                if (value instanceof ColoredPetriNet.ElementWithRecommendation) {
                    switch (((ColoredPetriNet.ElementWithRecommendation)value).getRecommendation()) {
                        case COMPLIANT :
                        case BOTH:
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                            //c.setForeground(new Color(73, 156, 84));
                            break;
                            //c.setForeground(new Color(246, 191, 105));
                            //break;
                        case VIOLATING:
                            Font newFont = c.getFont();
                            Map attributes = newFont.getAttributes();
                            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                            c.setFont(newFont.deriveFont(attributes));
                            //c.setForeground(new Color(199, 84, 80   ));
                            break;
                    }
                }
                return c;
            }
        });
        workItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workItemList.addListSelectionListener(new SelectWorkItemListener(caseModel.getCpn()));
        inputOutputList = new JList<>(caseModel.getCpn().getInputOutputModel());
        inputOutputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inputOutputList.addListSelectionListener(new SelectInputOutputListListener(caseModel, formPanel, dataObjectViews));
        inputOutputList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellhasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellhasFocus);
                if (value instanceof ColoredPetriNet.ElementWithRecommendation) {
                    switch (((ColoredPetriNet.ElementWithRecommendation)value).getRecommendation()) {
                        case COMPLIANT :
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                            //c.setForeground(new Color(73, 156, 84));
                            break;
                        //c.setForeground(new Color(246, 191, 105));
                        //break;
                        case VIOLATING:
                            Font newFont = c.getFont();
                            Map attributes = newFont.getAttributes();
                            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                            c.setFont(newFont.deriveFont(attributes));
                            //c.setForeground(new Color(199, 84, 80   ));
                            break;
                    }
                }
                return c;
            }
        });
        // Menu
        exitOption.addActionListener(event -> System.exit(0));
        loadCPNItem.addActionListener(new LoadCPNListener(caseModel.getCpn()));
        loadUMLItem.addActionListener(new LoadUMLListener(caseModel));
        fileMenu.add(loadCPNItem);
        fileMenu.add(loadUMLItem);
        fileMenu.add(exitOption);
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        setJMenuBar(menuBar);
        // Content Pane
        //panel.setLayout(new BorderLayout());
        //panel.add(new JScrollPane(workItemList), BorderLayout.WEST);
        //panel.add(new JScrollPane(inputOutputList), BorderLayout.NORTH);
        statusPanel.setLayout(new BorderLayout());
        formPanel.setBackground(new Color(40, 40, 40));
        formPanel.setLayout(new FlowLayout());
        completeButton.addActionListener(new CompleteButtonListener(caseModel.getCpn(), formPanel, statusText));
        statusPanel.add(completeButton, BorderLayout.EAST);
        statusPanel.add(statusText, BorderLayout.CENTER);
        JSplitPane workArea = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(workItemList), new JScrollPane(formPanel));
        JPanel bigArea = new JPanel();
        bigArea.setLayout(new BorderLayout());
        bigArea.add(statusPanel, BorderLayout.SOUTH);
        bigArea.add(new JScrollPane(workArea), BorderLayout.CENTER);
        panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(inputOutputList), bigArea);
        this.add(panel);
    }

    public static void main(String[] args)
    {
        JFrame mainFrame = new MainForm("fCM Engine", new CaseModel(false));
        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);
    }
}
