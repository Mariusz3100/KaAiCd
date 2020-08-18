package mariusz.ambroziak.kassistant.pojos.words;

import mariusz.ambroziak.kassistant.enums.StatsWordType;

public class WordAssociacion {
    private String text;
    private int count;
    private StatsWordType calculatedType;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public StatsWordType getCalculatedType() {
        return calculatedType;
    }

    public void setCalculatedType(StatsWordType calculatedType) {
        this.calculatedType = calculatedType;
    }

    public WordAssociacion(String text, int count, StatsWordType calculatedType) {
        this.text = text;
        this.count = count;
        this.calculatedType = calculatedType;
    }

    public WordAssociacion() {
    }
}
