package mariusz.ambroziak.kassistant.pojos.phrasefinding;

import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseConsidered;

import java.util.List;
import java.util.Map;

public class PhraseFindingResults {
    List<String> productPhrases;
    List<String> ingredientPhrases;
    List<String> extraProductPhrases;
    List<String> extraIngredientPhrases;
    List<String> matchedPhrases;


    public List<String> getProductPhrases() {
        return productPhrases;
    }

    public void setProductPhrases(List<String> productPhrases) {
        this.productPhrases = productPhrases;
    }

    public List<String> getIngredientPhrases() {
        return ingredientPhrases;
    }

    public void setIngredientPhrases(List<String> ingredientPhrases) {
        this.ingredientPhrases = ingredientPhrases;
    }

    public List<String> getExtraProductPhrases() {
        return extraProductPhrases;
    }

    public void setExtraProductPhrases(List<String> extraProductPhrases) {
        this.extraProductPhrases = extraProductPhrases;
    }

    public List<String> getExtraIngredientPhrases() {
        return extraIngredientPhrases;
    }

    public void setExtraIngredientPhrases(List<String> extraIngredientPhrases) {
        this.extraIngredientPhrases = extraIngredientPhrases;
    }

    public List<String> getMatchedPhrases() {
        return matchedPhrases;
    }

    public void setMatchedPhrases(List<String> matchedPhrases) {
        this.matchedPhrases = matchedPhrases;
    }
}
