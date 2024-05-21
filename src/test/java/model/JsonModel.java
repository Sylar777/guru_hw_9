package model;

public class JsonModel {
    private String name;
    private InnerDataModel innerData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InnerDataModel getInnerData() {
        return innerData;
    }

    public void setInnerData(InnerDataModel innerData) {
        this.innerData = innerData;
    }
}
