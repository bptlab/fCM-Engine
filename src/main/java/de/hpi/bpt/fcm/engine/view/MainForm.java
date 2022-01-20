package de.hpi.bpt.fcm.engine.view;

import de.hpi.bpt.fcm.engine.controller.*;
import de.hpi.bpt.fcm.engine.model.CaseModel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainForm extends JFrame {
    public JPanel panel = new JPanel();
    public JMenuBar menuBar = new JMenuBar();
    public JMenu fileMenu = new JMenu("File");
    public JMenuItem loadCPNItem = new JMenuItem("Load CPN");
    public JMenuItem loadUMLItem = new JMenuItem("Load UML");
    public JMenu aboutMenu = new JMenu("About");
    public JMenuItem exitOption = new JMenuItem("Exit");
    public JList workItemList;
    public JList inputOutputList;
    public JPanel statusPanel = new JPanel();
    public JButton completeButton = new JButton("Complete");
    public JLabel statusText = new JLabel("");
    public JPanel formPanel = new JPanel();
    private final Map<String, DataObjectView> dataObjectViews = new HashMap<>();

    public MainForm(String title, CaseModel caseModel) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // List
        workItemList = new JList(caseModel.getCpn().getWorkItemModel());
        workItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workItemList.addListSelectionListener(new SelectWorkItemListener(caseModel.getCpn()));
        inputOutputList = new JList(caseModel.getCpn().getInputOutputModel());
        inputOutputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inputOutputList.addListSelectionListener(new SelectInputOutputListListener(caseModel, formPanel, dataObjectViews));
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
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(workItemList), BorderLayout.WEST);
        panel.add(new JScrollPane(inputOutputList), BorderLayout.NORTH);
        statusPanel.setLayout(new BorderLayout());
        completeButton.addActionListener(new CompleteButtonListener(caseModel.getCpn(), formPanel));
        statusPanel.add(completeButton, BorderLayout.EAST);
        statusPanel.add(statusText, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);
        panel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        this.add(panel);
    }

    public static void main(String[] args)
    {
        JFrame mainFrame = new MainForm("fCM Engine", new CaseModel());
        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);
    }
}
