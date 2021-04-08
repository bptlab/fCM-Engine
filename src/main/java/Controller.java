import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.cpntools.accesscpn.engine.DaemonSimulator;
import org.cpntools.accesscpn.engine.OSValidator;
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
import org.eclipse.emf.common.notify.Notifier;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Controller {
    public static final String DATA_OBJECT_TYPE = "DATA_OBJECT";
    public static final String OUTPUT_CHECK = "check_output_file.txt";

    @FXML private ListView<String> transitionList;
    @FXML private ListView<String> bindingList;
    @FXML private VBox objectBox;
    @FXML private TextField statusText;

    private static final ObservableList<String> transitionListData = FXCollections.observableArrayList();
    private static final ObservableList<String> bindingListData = FXCollections.observableArrayList();

    private File cpnFile;
    private DomainModel domainModel;

    private PetriNet petriNet;
    private HighLevelSimulator simulator;

    private List<Instance<Transition>> currentTransitions = new ArrayList<>();
    private List<Binding> currentBindings = new ArrayList<>();
    private Map<String,List<Instance<Transition>>> enabledActivities = new HashMap<>();
    private Map<String, ObjectModel> objectModels = new HashMap<>();
    private List<DataObject> currentObjects = new ArrayList<>();

    @FXML public void initialize() {
        // Register collection to lists
        bindingList.setItems(bindingListData);
        bindingList.getSelectionModel().selectedIndexProperty().addListener((observable, oldIdx, newIdx) -> {
            objectBox.getChildren().clear();
            if (domainModel == null || newIdx.intValue() < 0) return;
            renderObjectsFor(currentBindings.get(newIdx.intValue()));
        });
        transitionList.setItems(transitionListData);
        transitionList.getSelectionModel().selectedItemProperty().addListener((observable, previousSelection, selectedActivityName) -> {
            bindingListData.clear();
            try {
                updateBindings(selectedActivityName);
            } catch (Exception e) {
                setStatus(Status.ERROR, "Could not load bindings");
            }
        });
    }

    @FXML protected void handleLoadCPNAction(ActionEvent event) throws Exception {
        // Choose a CPN File
        cpnFile = getFile("cpn File","*.cpn");
        // Load & Parse CPN
        setStatus(Status.INFO, "Loading Petri Net");
        petriNet = DOMParser.parse(new FileInputStream(cpnFile), cpnFile.getName());
        // Create a simulator object
        simulator = HighLevelSimulator.getHighLevelSimulator(new Simulator(new DaemonSimulator(InetAddress.getLocalHost(), 23456, new File("cpn.ml"))));
        // set initial state
        simulator.initialState();
        // configure simulator for petriNet
        simulator.setTarget((Notifier) petriNet);
        // check Petri net
        checkPetriNet(petriNet, simulator);
        clearStatus();
        System.out.println(simulator.evaluate("use \"" + getModelDirectory() + "simpledfs.sml\""));
        System.out.println(simulator.evaluate("CPNToolsModel.getInitialStates()"));
        System.out.println(simulator.evaluate("let val (s, storage) = dfs dead (CPNToolsModel.getInitialStates())" +
                " in (s, HashTable.numItems storage) " +
                "end"));
        try {
            setBasicParameters();
            // start execution
            updateTransitions();
        } catch (Exception e) {
            setStatus(Status.ERROR, "Failed to load Petri net");
            simulator.destroy();
        }
    }

    private void renderObjectsFor(Binding binding) {
        // Identify case
        String caseId = binding.getAllAssignments().stream()
                .filter(v -> v.getName().equals("caseId"))
                .map(ValueAssignment::getValue)
                .map(v -> v.replaceAll("\"", ""))
                .findFirst().orElse(null);
        if (caseId == null) {
            caseId = binding.getAllAssignments().stream()
                    .map(ValueAssignment::getValue)
                    .filter(v -> v.matches("case\\d+"))
                    .map(v -> {
                        Pattern pattern = Pattern.compile("case\\d+");
                        Matcher matcher = pattern.matcher(v);
                        return matcher.find() ? matcher.group(0) : null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        if (caseId == null)
            caseId = "case" + binding.getAllAssignments().stream()
                    .filter(v -> v.getName().equals("count"))
                    .map(ValueAssignment::getValue)
                    .findAny()
                    .orElse("0");
        // Get or create object model
        ObjectModel objectModel = objectModels.computeIfAbsent(caseId, s -> new ObjectModel(domainModel));
        // Parse tokens
        List<DataObjectToken> tokens = getTokens(binding);
        for (DataObjectToken token : tokens) {
            DataObject object = objectModel.getObjectFor(token.getObjectId());
            if (object == null) continue;
            object.setState(token.state);
            currentObjects.add(object);
            objectBox.getChildren().addAll(object.getPane(), new Separator());
        }
    }

    @FXML protected void handleLoadUMLAction(ActionEvent event) {
        // Choose a UML File
        File umlFile = getFile("Domain Model (UML)", "*.uml");
        try {
            domainModel = DomainModel.fromFile(umlFile);
        } catch (JDOMException | NullPointerException e) {
            e.printStackTrace();
            setStatus(Status.ERROR, "Could not parse UML file");
        } catch (IOException e) {
            e.printStackTrace();
            setStatus(Status.ERROR, "Could not read UML file");
        }
    }

    private void updateTransitions() throws Exception {
        List<Instance<Transition>> transitionInstances = simulator.getAllTransitionInstances();
        currentTransitions.clear();
        for (Instance<Transition> ti : transitionInstances) {
            if (simulator.isEnabled(ti)) {
                currentTransitions.add(ti);
            }
        }
        updateTransitionListView();
    }

    private void setStatus(Status status, String text) {
        String style = status == Status.ERROR ? "-fx-text-fill: red" : "-fx-text-fill: black";
        statusText.setStyle(style);
        statusText.setText(text);
    }

    private void clearStatus() {
        statusText.clear();
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
        bindingListData.clear();
        currentBindings.stream()
                .map(this::formatBinding)
                .forEachOrdered(bindingListData::add);
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

    private void updateTransitionListView() {
        transitionListData.clear();
        enabledActivities.clear();
        for (Instance<Transition> ti : currentTransitions) {
            String transitionName = ti.getNode().getName().getText();
            String activityName = transitionName.replaceFirst("_\\d+$", "").replaceAll("\\n", " ");
            if (!enabledActivities.containsKey(activityName)) {
                enabledActivities.put(activityName, new ArrayList<>());
                transitionListData.add(activityName);
            }
            enabledActivities.get(activityName).add(ti);
        }
    }

    @FXML protected void handleCloseAction(ActionEvent event) {
        System.exit(0);
    }

    @FXML protected void handleAboutClick(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("scenes/about_popup.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML protected void handleCompleteAction(ActionEvent event) {
        int idx = bindingList.getSelectionModel().getSelectedIndex();
        try {
            simulator.execute(currentBindings.get(idx));
            updateTransitions();
            currentObjects.forEach(DataObject::update);
        } catch (IOException e) {
            setStatus(Status.ERROR, "Could not execute selected binding");
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.ERROR, "Could not update enabled bindings");
        }
    }

    private void setBasicParameters() throws Exception {
        String modelName = petriNet.getName().getText();
        try {
            simulator.setModelNameModelDirOutputDir(modelName, getModelDirectory(), getModelDirectory());
        } catch (NoSuchElementException e) {
            System.err.println("Execution failed! Did you initialize/check the model?");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private String getModelDirectory() throws MalformedURLException {
        if (OSValidator.isWindows()) {
            return cpnFile.getParentFile().toURI().toURL().toExternalForm().replace("file", "/cygdrive").replace(":", "");
        } else {
            setStatus(Status.ERROR,"Access/CPN requires Windows to run properly");
            return cpnFile.getParentFile().toString();
        }
    }

    private File getFile(String description, String extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(description, extensions));
        fileChooser.setTitle("Choose " + description);
        return fileChooser.showOpenDialog(null);
    }

    private void checkPetriNet(PetriNet petriNet, HighLevelSimulator simulator) throws Exception {
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

    private enum Status {
        INFO,
        ERROR;
    }

    private class DataObjectToken {
        private String name;
        private String count;
        private String state;
        private boolean isNewObject;

        private String getObjectId() {
            return name + "(" + count + ")";
        }

        @Override
        public String toString() {
            return getObjectId() + (null == state ? "" : "[" + state + "]");
        }
    }
}