package mariusz.ambroziak.kassistant.pojos.words;

import java.util.List;

public class WordStatParsingResult {
    private List<WordStatData> ingredientResults;
    private List<WordStatData> productResults;


    public List<WordStatData> getIngredientResults() {
        return ingredientResults;
    }

    public void setIngredientResults(List<WordStatData> ingredientResults) {
        this.ingredientResults = ingredientResults;
    }

    public List<WordStatData> getProductResults() {
        return productResults;
    }

    public void setProductResults(List<WordStatData> productResults) {
        this.productResults = productResults;
    }

    public WordStatParsingResult(List<WordStatData> ingredientResults, List<WordStatData> productResults) {
        this.ingredientResults = ingredientResults;
        this.productResults = productResults;
    }
}
