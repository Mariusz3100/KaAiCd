package mariusz.ambroziak.kassistant.pojos.words;

import java.util.List;

public class WordStatData {
    private String text;
    private String lemma;
    private int productCount;
    private int ingredientCount;
    private String calculatedType;
    private List<String> phrasesAssociated;



    private List<WordAssociacion> wordsAssociated;



    public List<WordAssociacion> getWordsAssociated() {
        return wordsAssociated;
    }

    public void setWordsAssociated(List<WordAssociacion> wordsAssociated) {
        this.wordsAssociated = wordsAssociated;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getIngredientCount() {
        return ingredientCount;
    }

    public void setIngredientCount(int ingredientCount) {
        this.ingredientCount = ingredientCount;
    }

    public String getCalculatedType() {
        return calculatedType;
    }

    public void setCalculatedType(String calculatedType) {
        this.calculatedType = calculatedType;
    }

    public List<String> getPhrasesAssociated() {
        return phrasesAssociated;
    }

    public void setPhrasesAssociated(List<String> phrasesAssociated) {
        this.phrasesAssociated = phrasesAssociated;
    }

    public WordStatData(String text, String lemma) {
        this.text = text;
        this.lemma = lemma;
        this.calculatedType = StatsWordType.Unknown.toString();
    }
}
