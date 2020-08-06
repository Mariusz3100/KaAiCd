package mariusz.ambroziak.kassistant.pojos.words;

public class WordStatData {
    private String text;
    private String lemma;
    private int productCount;
    private int ingredientCount;


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

    public WordStatData(String text, String lemma) {
        this.text = text;
        this.lemma = lemma;
    }
}
