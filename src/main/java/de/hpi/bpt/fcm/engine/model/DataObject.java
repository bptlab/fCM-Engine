package de.hpi.bpt.fcm.engine.model;


public class DataObject {

    protected DataClazz clazz;
    protected int instanceNo;
    protected Object[] values;
    protected String state;

    public DataObject(DataClazz clazz) {
        this.clazz = clazz;
        values = new Object[clazz.attributes.length];
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

    public void setState(String state) {
        this.state = state;
    }



    public DataClazz getClazz() {
        return clazz;
    }
}
