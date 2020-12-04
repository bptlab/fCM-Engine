import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class DataObject {
    protected DataClazz clazz;
    protected int instanceNo;
    protected Object[] values;
    protected String state;
    protected GridPane pane;
    protected TextField[] textFields;

    public DataObject(DataClazz clazz) {
        this.clazz = clazz;
        values = new Object[clazz.attributes.length];
        textFields = new TextField[clazz.attributes.length];
    }

    public void setValue(String attributeName, String value) {
        for (int i = 0; i < values.length; i++) {
            if (clazz.attributes[i].equals(attributeName)) {
                setValue(i, value);
                break;
            }
        }
    }

    public void setValue(int idx, String value) {
        values[idx] = parseValue(clazz.types[idx], value);
    }

    public String getValue(String attributeName) {
        for (int i = 0; i < values.length; i++) {
            if (clazz.attributes[i].equals(attributeName)) {
                return getValue(i).toString();
            }
        }
        return "";
    }

    public Object getValue(int idx) {
        return values[idx];
    }

    private Object parseValue(String type, String value) {
        if (value == null || value.equals("")) return null;
        switch (type) {
            case "string":
                return value;
            case "boolean":
                return Boolean.parseBoolean(value);
            case "int":
                return Integer.parseInt(value);
            default:

                return value;
        }
    }

    public int getInstanceNo() {
        return instanceNo;
    }

    public void setInstanceNo(int instanceNo) {
        this.instanceNo = instanceNo;
    }

    public GridPane getPane() {
        if (null == pane) {
            pane = new GridPane();
            pane.add(new Label(clazz.name + " " + this.instanceNo), 0, 0);
            for (int i = 0; i < values.length; i++) {
                pane.add(new Label(clazz.attributes[i]), 0, i + 1);
                textFields[i] = new JFXTextField(values[i] == null ? "" : values[i].toString());
                pane.add(textFields[i], 1, i + 1 );
            }
        }
        return pane;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void update() {
        if (null == pane) return;
        for (int i = 0; i < textFields.length; i++) {
            values[i] = parseValue(clazz.types[i], textFields[i].getText());
        }
    }
}
