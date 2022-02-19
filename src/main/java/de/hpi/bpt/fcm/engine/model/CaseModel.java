package de.hpi.bpt.fcm.engine.model;

import java.io.File;
import java.io.InputStream;

public class CaseModel {
    private ColoredPetriNet cpn;
    private DomainModel dm;
    private ObjectModel om;

    public CaseModel(boolean recommendations) {
        this.cpn = new ColoredPetriNet(recommendations);
        InputStream cpnFile = getClass().getClassLoader().getResourceAsStream("jiis_example/colored_petri_net(Experiments).cpn");
        try {
            cpn.loadCPN(cpnFile, "cpn_experiments");
            cpn.verifyCpn();
            cpn.initialize();
        } catch (Exception e) {
            System.err.println("Could not load CPN");
            e.printStackTrace();
        }
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
