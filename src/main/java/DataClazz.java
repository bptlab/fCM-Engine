public class DataClazz {
    protected String name;
    protected String[] attributes;
    protected String[] types;
    protected boolean[] areLists;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public DataObject instantiate(int instanceNo) {
        DataObject dataObject = new DataObject(this);
        dataObject.setInstanceNo(instanceNo);
        return dataObject;
    }
}
