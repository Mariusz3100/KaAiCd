package mariusz.ambroziak.kassistant.pojos.usda;

public class UsdaElementParsed {
    private String text;
    //private Classification classification;
    private Classification classificationCalculated;
    private Classification classificationExpected;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Classification getClassificationCalculated() {
        return classificationCalculated;
    }

    public void setClassificationCalculated(Classification classificationCalculated) {
        this.classificationCalculated = classificationCalculated;
    }

    public Classification getClassificationExpected() {
        return classificationExpected;
    }

    public void setClassificationExpected(Classification classificationExpected) {
        this.classificationExpected = classificationExpected;
    }


    public UsdaElementParsed(String text) {
        this.text = text;
        this.classificationCalculated = Classification.NONE;
        this.classificationExpected = Classification.NONE;

    }


    public UsdaElementParsed(String text, Classification classificationCalculated, Classification classificationExpected) {
        this.text = text;
        this.classificationCalculated = classificationCalculated;
        this.classificationExpected = classificationExpected;
    }


}
