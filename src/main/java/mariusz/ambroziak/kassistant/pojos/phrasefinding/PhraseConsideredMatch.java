package mariusz.ambroziak.kassistant.pojos.phrasefinding;

import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseConsidered;
import mariusz.ambroziak.kassistant.pojos.words.WordAssociacion;

import java.util.ArrayList;
import java.util.List;

public class PhraseConsideredMatch {
    PhraseConsidered match;
    List<PhraseConsidered> ingredientPhraseMatched;
    List<PhraseConsidered> productPhraseMatched;

    List<WordAssociacion> wordAssociacions;

    List<String> relatedUsdaPhrases;


    public List<String> getRelatedUsdaPhrases() {
        return relatedUsdaPhrases;
    }

    public void setRelatedUsdaPhrases(List<String> relatedUsdaPhrases) {
        this.relatedUsdaPhrases = relatedUsdaPhrases;
    }

    public PhraseConsidered getMatch() {
        return match;
    }

    public void setMatch(PhraseConsidered match) {
        this.match = match;
    }



    public List<WordAssociacion> getWordAssociacions() {
        if(wordAssociacions==null)
            wordAssociacions=new ArrayList<>();
        return wordAssociacions;
    }

    public void setWordAssociacions(List<WordAssociacion> wordAssociacions) {
        this.wordAssociacions = wordAssociacions;
    }

    public List<PhraseConsidered> getIngredientPhraseMatched() {
        if(ingredientPhraseMatched ==null)
            ingredientPhraseMatched =new ArrayList<>();

        return ingredientPhraseMatched;
    }

    public void setIngredientPhraseMatched(List<PhraseConsidered> ingredientPhraseMatched) {
        this.ingredientPhraseMatched = ingredientPhraseMatched;
    }
    public void addIngredientPhraseMatched(PhraseConsidered ingredientFound) {
        this.getIngredientPhraseMatched().add(ingredientFound);
    }

    public void addIngredientPhraseMatchedIfNew(PhraseConsidered phrase) {
        if(!this.getIngredientPhraseMatched().stream().anyMatch(phraseConsidered -> phraseConsidered.equals(phrase))){
            this.getIngredientPhraseMatched().add(phrase);
        }


    }

    public List<PhraseConsidered> getProductPhraseMatched() {
        if(productPhraseMatched ==null)
            productPhraseMatched =new ArrayList<>();
        return productPhraseMatched;
    }

    public void setProductPhraseMatched(List<PhraseConsidered> productPhraseMatched) {
        this.productPhraseMatched = productPhraseMatched;
    }
    public void addProductPhraseMatched(PhraseConsidered productFound) {
        this.getProductPhraseMatched().add(productFound);
    }


    public void addProductPhraseMatchedIfNew(PhraseConsidered phrase) {
        if(!this.getProductPhraseMatched().stream().anyMatch(phraseConsidered -> phraseConsidered.equals(phrase))){
            this.getProductPhraseMatched().add(phrase);
        }


    }


    public PhraseConsideredMatch(PhraseConsidered match, List<PhraseConsidered> ingredientPhraseMatched, List<PhraseConsidered> productPhraseMatched) {
        this.match = match;
        this.ingredientPhraseMatched = ingredientPhraseMatched;
        this.productPhraseMatched = productPhraseMatched;
    }

    public PhraseConsideredMatch() {
    }
}
