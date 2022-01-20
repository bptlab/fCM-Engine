package de.hpi.bpt.fcm.engine.model;

public class CaseModel {
    private ColoredPetriNet cpn;
    private DomainModel dm;
    private ObjectModel om;

    public CaseModel() {
        this.cpn = new ColoredPetriNet();
    }

    public ColoredPetriNet getCpn() {
        return cpn;
    }

    public void setCpn(ColoredPetriNet cpn) {
        this.cpn = cpn;
    }

    public DomainModel getDm() {
        return dm;
    }

    public void setDm(DomainModel dm) {
        this.dm = dm;
        this.om = new ObjectModel(dm);
    }

    public ObjectModel getOm() {
        return om;
    }

}
