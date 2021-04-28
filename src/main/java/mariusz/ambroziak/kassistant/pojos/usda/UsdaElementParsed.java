package mariusz.ambroziak.kassistant.pojos.usda;

public class UsdaElementParsed {
    private String text;
    private Classification classification;



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public UsdaElementParsed(String text) {
        this.text = text;
        this.classification = Classification.NONE;
    }

    public UsdaElementParsed(String text, Classification classification) {
        this.text = text;
        this.classification = classification;
    }
}
