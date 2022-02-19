package de.hpi.bpt.fcm.engine.model;

import org.cpntools.accesscpn.engine.DaemonSimulator;
import org.cpntools.accesscpn.engine.EvaluationException;
import org.cpntools.accesscpn.engine.Simulator;
import org.cpntools.accesscpn.engine.highlevel.HighLevelSimulator;
import org.cpntools.accesscpn.engine.highlevel.checker.Checker;
import org.cpntools.accesscpn.engine.highlevel.instance.Binding;
import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.engine.highlevel.instance.Marking;
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
import java.io.InputStream;
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
    private DefaultListModel<ElementWithRecommendation> workItemModel = new DefaultListModel<>();
    private DefaultListModel<ElementWithRecommendation> inputOutputModel = new DefaultListModel<>();
    private Map<String,List<Instance<Transition>>> enabledActivities = new HashMap<>();
    private List<Binding> currentBindings = new ArrayList<>();
    private Binding selectedBinding = null;
    private boolean reassessed = false;
    private boolean recommendations = false;

    public ColoredPetriNet(boolean recommendations) {
        this.recommendations = recommendations;
    }

    public DefaultListModel<ElementWithRecommendation> getWorkItemModel() {
        return workItemModel;
    }

    public void setWorkItemModel(DefaultListModel<ElementWithRecommendation> workItemModel) {
        this.workItemModel = workItemModel;
    }

    public DefaultListModel<ElementWithRecommendation> getInputOutputModel() {
        return inputOutputModel;
    }

    public void setInputOutputModel(DefaultListModel<ElementWithRecommendation> inputOutputModel) {
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
        enabledActivities.clear();
        inputOutputModel.clear();
        List<Instance<Transition>> transitionInstances = simulator.getAllTransitionInstances();
        for (Instance<Transition> ti : transitionInstances) {
            if (simulator.isEnabled(ti)) {
                String transitionName = ti.getNode().getName().getText();
                String activityName = transitionName.replaceFirst("_\\d+$", "").replaceAll("\\n", " ");
                if (!enabledActivities.containsKey(activityName)) {
                    enabledActivities.put(activityName, new ArrayList<>());
                    Recommendation recommendation = recommendationForActivity(activityName);
                    ElementWithRecommendation activity = new ElementWithRecommendation(activityName, recommendation);
                    workItemModel.addElement(activity);
                }
                enabledActivities.get(activityName).add(ti);
            }
        }
    }

    private Recommendation recommendationForActivity(String activityName) {
        if (!recommendations) return Recommendation.NEUTRAL;
        if (activityName.contains("request") ||
                activityName.contains("create") ||
                activityName.contains("review") ||
                activityName.contains("reassess_risk")) return Recommendation.COMPLIANT;
        if (activityName.contains("revise") || activityName.contains("decide")) return Recommendation.BOTH;
        if (activityName.contains("reassess_claim")) {
            if (reassessed) return Recommendation.VIOLATING;
            try {
                if (!reassessed) {
                    List<Marking> markings = simulator.getMarking().getAllMarkings();
                    Marking markingAssessment = markings.stream().filter(m -> m.getPlaceInstance().getNode().getName().getText().contains("assessment__APPROVED")).findFirst().get();
                    Marking risk = markings.stream().filter(m -> m.getPlaceInstance().getNode().getName().getText().contains("risk__LOW")).findFirst().get();
                    if (markingAssessment.getTokenCount() > 3 && risk.getTokenCount() > 0) {
                        return Recommendation.BOTH;
                    } else {
                        return Recommendation.VIOLATING;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Recommendation.NEUTRAL;
    }

    public void updateInputOutputOptions(int index) throws Exception {
        if (workItemModel.size() <= index) return;
        updateBindings(workItemModel.get(index));
    }

    private void updateBindings(ElementWithRecommendation activity) throws Exception {
        currentBindings.clear();
        if (null == activity || null == activity.name) return;
        List<Instance<Transition>> transitionInstances = enabledActivities.get(activity.name);
        for (Instance<Transition> transitionInstance : transitionInstances) {
            if (simulator.isEnabled(transitionInstance))
                currentBindings.addAll(simulator.getBindings(transitionInstance));
        }
        updateBindingListView(activity);
    }

    private void updateBindingListView(ElementWithRecommendation activity) {
        inputOutputModel.clear();
        currentBindings.stream()
                .map(this::formatBinding)
                .map(label -> new ElementWithRecommendation(label, recommendationForInputOutputAndActivity(activity, label)))
                .forEachOrdered(inputOutputModel::addElement);
    }

    private Recommendation recommendationForInputOutputAndActivity(ElementWithRecommendation activity, String label) {
        if (!recommendations) return Recommendation.NEUTRAL;
        if (activity.recommendation==Recommendation.BOTH) {
            if (activity.name.contains("revise")) {
                return label.contains("--> rejected") ? Recommendation.VIOLATING : Recommendation.COMPLIANT;
            } else if (activity.name.contains("reassess_claim")) {
                return label.contains("approve") ? Recommendation.COMPLIANT : Recommendation.VIOLATING;
            } else if (activity.name.contains("decide")) {
                return label.contains("--> in_question") ? Recommendation.COMPLIANT : Recommendation.VIOLATING;
            }
        }
        return activity.recommendation;
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
                if (!token.isNewObject) {
                    token.state = targetArcs.stream()
                            .map(a -> a.getSource().getName().getText())
                            .filter(label -> label.toLowerCase().contains(token.name.toLowerCase() + "__"))
                            .map(label -> label.toLowerCase().substring(token.name.length() + 2))
                            .findFirst()
                            .orElse("");
                }
                token.state = (token.state == null ? "" : token.state + " --> ") + sourceArcs.stream()
                        .map(a -> a.getTarget().getName().getText())
                        .filter(label -> label.toLowerCase().contains(token.name.toLowerCase() + "__"))
                        .map(label -> label.toLowerCase().substring(token.name.length() + 2))
                        .findFirst()
                        .orElse("");
                token.count = token.isNewObject ? value : value.replaceAll("(\\(|\\)|\"|case|[A-Za-z]+,)", "");
                tokens.add(token);
            }
        }
        return tokens;
    }

    public boolean executeCurrentBindingElement() {
        if (selectedBinding == null) return false;
        try {
            simulator.execute(selectedBinding);
            reassessed = selectedBinding.getTransitionInstance().getNode().getName().getText().contains("reassess_claim");
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not execute selected binding");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not update enabled bindings");
            return false;
        }
        selectedBinding = null;
        return true;
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

    public void loadCPN(InputStream cpnStream, String fileName) throws NetCheckException, IOException, ParserConfigurationException, SAXException, EvaluationException {
        petriNet = DOMParser.parse(cpnStream, fileName);
        // Create a simulator object
        simulator = HighLevelSimulator.getHighLevelSimulator(new Simulator(new DaemonSimulator(InetAddress.getLocalHost(), 23456, new File("cpn.ml"))));
        // set initial state
        simulator.initialState();
        // configure simulator for petriNet
        simulator.setTarget((Notifier) petriNet);
    }

    public class ElementWithRecommendation {
        private String name;
        private Recommendation recommendation;

        public ElementWithRecommendation(String label, Recommendation recommendation) {
            this.name = label;
            this.recommendation = recommendation;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public Recommendation getRecommendation() {
            return recommendation;
        }
    }

    public static enum Recommendation {
        COMPLIANT,
        NEUTRAL,
        BOTH,
        VIOLATING;
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
