package mariusz.ambroziak.kassistant.pojos.phrasefinding;

import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseConsidered;

import java.util.List;
import java.util.Map;

public class PhraseFindingResults {
    List<PhraseConsidered> productPhrases;
    List<PhraseConsidered> ingredientPhrases;
    List<PhraseConsidered> extraProductPhrases;
    List<PhraseConsidered> extraIngredientPhrases;
    List<PhraseConsideredMatch> matchedPhrases;






    public List<PhraseConsidered> getProductPhrases() {
        return productPhrases;
    }

    public void setProductPhrases(List<PhraseConsidered> productPhrases) {
        this.productPhrases = productPhrases;
    }

    public List<PhraseConsidered> getIngredientPhrases() {
        return ingredientPhrases;
    }

    public void setIngredientPhrases(List<PhraseConsidered> ingredientPhrases) {
        this.ingredientPhrases = ingredientPhrases;
    }

    public List<PhraseConsidered> getExtraProductPhrases() {
        return extraProductPhrases;
    }

    public void setExtraProductPhrases(List<PhraseConsidered> extraProductPhrases) {
        this.extraProductPhrases = extraProductPhrases;
    }

    public List<PhraseConsidered> getExtraIngredientPhrases() {
        return extraIngredientPhrases;
    }

    public void setExtraIngredientPhrases(List<PhraseConsidered> extraIngredientPhrases) {
        this.extraIngredientPhrases = extraIngredientPhrases;
    }


    public List<PhraseConsideredMatch> getMatchedPhrases() {
        return matchedPhrases;
    }

    public void setMatchedPhrases(List<PhraseConsideredMatch> matchedPhrases) {
        this.matchedPhrases = matchedPhrases;
    }
}
