package de.hpi.bpt.fcm.engine.model;

import org.cpntools.accesscpn.engine.DaemonSimulator;
import org.cpntools.accesscpn.engine.EvaluationException;
import org.cpntools.accesscpn.engine.Simulator;
import org.cpntools.accesscpn.engine.highlevel.HighLevelSimulator;
import org.cpntools.accesscpn.engine.highlevel.checker.Checker;
import org.cpntools.accesscpn.engine.highlevel.instance.Binding;
import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.engine.highlevel.instance.ValueAssignment;
import org.cpntools.accesscpn.model.Arc;
import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.accesscpn.model.importer.DOMParser;
import org.cpntools.accesscpn.model.importer.NetCheckException;
import org.eclipse.emf.common.notify.Notifier;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColoredPetriNet {
    public static final String OUTPUT_CHECK = "check_output_file.txt";
    private File cpnFile;
    private PetriNet petriNet;
    private HighLevelSimulator simulator;
    private DefaultListModel<String> workItemModel = new DefaultListModel<>();
    private DefaultListModel<String> inputOutputModel = new DefaultListModel<>();
    private Map<String,List<Instance<Transition>>> enabledActivities = new HashMap<>();
    private List<Binding> currentBindings = new ArrayList<>();
    private Binding selectedBinding = null;

    public DefaultListModel<String> getWorkItemModel() {
        return workItemModel;
    }

    public void setWorkItemModel(DefaultListModel<String> workItemModel) {
        this.workItemModel = workItemModel;
    }

    public DefaultListModel<String> getInputOutputModel() {
        return inputOutputModel;
    }

    public void setInputOutputModel(DefaultListModel<String> inputOutputModel) {
        this.inputOutputModel = inputOutputModel;
    }

    public File getCpnFile() {
        return cpnFile;
    }

    public void setCpnFile(File cpnFile) {
        this.cpnFile = cpnFile;
    }

    public PetriNet getPetriNet() {
        return petriNet;
    }

    public void setPetriNet(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    public HighLevelSimulator getSimulator() {
        return simulator;
    }

    public void setSimulator(HighLevelSimulator simulator) {
        this.simulator = simulator;
    }

    public void loadCPN(File cpnFile) throws IOException, EvaluationException, NetCheckException, ParserConfigurationException, SAXException {
        this.cpnFile = cpnFile;
        petriNet = DOMParser.parse(new FileInputStream(cpnFile), cpnFile.getName());
        // Create a simulator object
        simulator = HighLevelSimulator.getHighLevelSimulator(new Simulator(new DaemonSimulator(InetAddress.getLocalHost(), 23456, new File("cpn.ml"))));
        // set initial state
        simulator.initialState();
        // configure simulator for petriNet
        simulator.setTarget((Notifier) petriNet);
    }

    public void verifyCpn() throws Exception {
        Checker checker = new Checker(petriNet, new File(OUTPUT_CHECK), simulator);
        // The state space may be unbounded. Therefore do not use
        // checker.checkEntireModel();
        checker.localCheck();
        checker.checkInitializing("","");
        checker.checkDeclarations();
        checker.generateSerializers();
        checker.checkPages();
        checker.generatePlaceInstances();
        checker.checkMonitors();
        checker.generateNonPlaceInstances();
        checker.initialiseSimulationScheduler();
        checker.instantiateSMLInterface();
    }

    public void initialize() {
        try {
            determineWorkItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void determineWorkItems() throws Exception {
        workItemModel.clear();
        inputOutputModel.clear();
        List<Instance<Transition>> transitionInstances = simulator.getAllTransitionInstances();
        for (Instance<Transition> ti : transitionInstances) {
            if (simulator.isEnabled(ti)) {
                String transitionName = ti.getNode().getName().getText();
                String activityName = transitionName.replaceFirst("_\\d+$", "").replaceAll("\\n", " ");
                if (!enabledActivities.containsKey(activityName)) {
                    enabledActivities.put(activityName, new ArrayList<>());
                    workItemModel.addElement(activityName);
                }
                enabledActivities.get(activityName).add(ti);
            }
        }
    }

    public void updateInputOutputOptions(int index) throws Exception {
        if (workItemModel.size() <= index) return;
        updateBindings(workItemModel.get(index));
    }

    private void updateBindings(String activityName) throws Exception {
        currentBindings.clear();
        if (null == activityName) return;
        List<Instance<Transition>> transitionInstances = enabledActivities.get(activityName);
        for (Instance<Transition> transitionInstance : transitionInstances) {
            if (simulator.isEnabled(transitionInstance))
                currentBindings.addAll(simulator.getBindings(transitionInstance));
        }
        updateBindingListView();
    }

    private void updateBindingListView() {
        inputOutputModel.clear();
        currentBindings.stream()
                .map(this::formatBinding)
                .forEachOrdered(inputOutputModel::addElement);
    }

    private String formatBinding(Binding binding) {
        // Parse tokens
        List<DataObjectToken> tokens = getTokens(binding);
        // Format
        List<String> objectsRead = new ArrayList<>();
        List<String> objectsCreated = new ArrayList<>();
        tokens.forEach(t -> (t.isNewObject? objectsCreated : objectsRead).add(t.toString()));
        return "Provided Objects: {" +
                String.join(", ", objectsRead) +
                "}, New Objects: {" +
                String.join(", ", objectsCreated) +
                "}";
    }

    private List<DataObjectToken> getTokens(Binding bi) {
        List<ValueAssignment> assignments = bi.getAllAssignments();
        List<DataObjectToken> tokens = new ArrayList<>();
        List<Arc> targetArcs = bi.getTransitionInstance().getNode().getTargetArc();
        List<Arc> sourceArcs = bi.getTransitionInstance().getNode().getSourceArc();
        for (ValueAssignment assignment : assignments) {
            String variable = assignment.getName();
            if (!(variable.contains("Count") || variable.contains("Id") || variable.endsWith("_list"))) continue;
            String value = assignment.getValue();
            if (variable.endsWith("_list") && !variable.contains("Count") && !variable.contains("id")) {
                String name = variable.substring(0, variable.length() - "_list".length());
                Pattern pattern = Pattern.compile("\\{id=\\([a-z]+,(\\d+)\\),caseId=\"case\\d+\",state=([A-Z]+)}");
                Matcher matcher = pattern.matcher(value);
                while (matcher.find()) {
                    DataObjectToken token = new DataObjectToken();
                    token.name = name;
                    token.count = matcher.group(1);
                    token.state = matcher.group(2);
                    tokens.add(token);
                }
            } else {
                DataObjectToken token = new DataObjectToken();
                token.isNewObject = variable.contains("Count");
                token.name = variable.replaceFirst("Count|Id", "");
                token.state = (token.isNewObject ? sourceArcs : targetArcs).stream()
                        .map(a -> token.isNewObject ? a.getTarget().getName().getText() : a.getSource().getName().getText())
                        .filter(label -> label.toLowerCase().contains(token.name.toLowerCase() + "__"))
                        .map(label -> label.toLowerCase().substring(token.name.toLowerCase().length() + 2))
                        //.map(a -> a.getHlinscription().getText())
                        //.filter(insc -> insc.startsWith("{id = " + token.name + "Id"))
                        //.map(insc -> insc.replaceAll("(^.+, state =)|}", ""))
                        .findFirst()
                        .orElse(null);
                token.count = token.isNewObject ? value : value.replaceAll("(\\(|\\)|\"|case|[A-Za-z]+,)", "");
                tokens.add(token);
            }
        }
        return tokens;
    }

    public void executeCurrentBindingElement() {
        if (selectedBinding == null) return;
        try {
            simulator.execute(selectedBinding);
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not execute selected binding");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not update enabled bindings");
        }
        selectedBinding = null;
    }

    public void selectBinding(int index) {
        selectedBinding = currentBindings.get(index);
    }



    public List<DataObjectToken> getTokens(int idx) {
        if (idx >= currentBindings.size()) return new ArrayList<>(0);
        Binding bi = currentBindings.get(idx);
        List<ValueAssignment> assignments = bi.getAllAssignments();
        List<DataObjectToken> tokens = new ArrayList<>();
        List<Arc> targetArcs = bi.getTransitionInstance().getNode().getTargetArc();
        List<Arc> sourceArcs = bi.getTransitionInstance().getNode().getSourceArc();
        for (ValueAssignment assignment : assignments) {
            String variable = assignment.getName();
            if (!(variable.contains("Count") || variable.contains("Id") || variable.endsWith("_list"))) continue;
            String value = assignment.getValue();
            if (variable.endsWith("_list") && !variable.contains("Count") && !variable.contains("id")) {
                String name = variable.substring(0, variable.length() - "_list".length());
                Pattern pattern = Pattern.compile("\\{id=\\([a-z]+,(\\d+)\\),caseId=\"case\\d+\",state=([A-Z]+)}");
                Matcher matcher = pattern.matcher(value);
                while (matcher.find()) {
                    DataObjectToken token = new DataObjectToken();
                    token.name = name;
                    token.count = matcher.group(1);
                    token.state = matcher.group(2);
                    tokens.add(token);
                }
            } else {
                DataObjectToken token = new DataObjectToken();
                token.isNewObject = variable.contains("Count");
                token.name = variable.replaceFirst("Count|Id", "");
                token.state = (token.isNewObject ? sourceArcs : targetArcs).stream()
                        .map(a -> a.getHlinscription().getText())
                        .filter(insc -> insc.startsWith("{id = " + token.name + "Id"))
                        .map(insc -> insc.replaceAll("(^.+, state =)|}", ""))
                        .findFirst()
                        .orElse(null);
                token.count = token.isNewObject ? value : value.replaceAll("(\\(|\\)|\"|case|[A-Za-z]+,)", "");
                tokens.add(token);
            }
        }
        return tokens;
    }

    public class DataObjectToken {
        private String name;
        private String count;
        private String state;
        private boolean isNewObject;

        public String getObjectId() {
            return name + "(" + count + ")";
        }

        public String getName() {
            return name;
        }

        public String getCount() {
            return count;
        }

        public String getState() {
            return state;
        }

        public boolean isNewObject() {
            return isNewObject;
        }

        @Override
        public String toString() {
            return getObjectId() + (null == state ? "" : "[" + state + "]");
        }
    }
}
