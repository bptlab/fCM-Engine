import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ObjectModel {
    protected DomainModel domainModel;
    protected Map<String, DataObject> dataObjects;

    public ObjectModel(DomainModel domainModel) {
        this.domainModel = domainModel;
        this.dataObjects = new HashMap<>();
    }

    public DataObject getObjectFor(String id) {
        int instanceNo = Integer.parseInt(id.replaceFirst(".+\\(", "").replace(")", ""));
        if (!dataObjects.containsKey(id)) {
            Optional<DataClazz> clazz = Arrays.stream(domainModel.classes)
                    .filter(s -> id.toLowerCase()
                            .contains(s.name.toLowerCase())).findFirst();
            clazz.ifPresent(dataClazz -> dataObjects.put(id, dataClazz.instantiate(instanceNo)));
        }
        return dataObjects.get(id);
    }
}
