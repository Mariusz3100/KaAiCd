package mariusz.ambroziak.kassistant.pojos.phrasefinding;

import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseConsidered;

public class PhraseConsideredMatch {
    PhraseConsidered match;
    PhraseConsidered ingredientFound;
    PhraseConsidered productFound;


    public PhraseConsidered getMatch() {
        return match;
    }

    public void setMatch(PhraseConsidered match) {
        this.match = match;
    }

    public PhraseConsidered getIngredientFound() {
        return ingredientFound;
    }

    public void setIngredientFound(PhraseConsidered ingredientFound) {
        this.ingredientFound = ingredientFound;
    }

    public PhraseConsidered getProductFound() {
        return productFound;
    }

    public void setProductFound(PhraseConsidered productFound) {
        this.productFound = productFound;
    }

    public PhraseConsideredMatch(PhraseConsidered match, PhraseConsidered ingredientFound, PhraseConsidered productFound) {
        this.match = match;
        this.ingredientFound = ingredientFound;
        this.productFound = productFound;
    }


    public PhraseConsideredMatch() {
    }
}
