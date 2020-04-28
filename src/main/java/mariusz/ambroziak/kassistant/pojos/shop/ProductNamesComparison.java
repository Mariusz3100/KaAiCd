package mariusz.ambroziak.kassistant.pojos.shop;

import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordComparisonResult;

import java.util.List;

public class ProductNamesComparison {
    List<WordComparisonResult> detailsNameResults;
    List<WordComparisonResult> searchNameResults;
    List<WordComparisonResult> ingredientsNameResults;

    public List<WordComparisonResult> getDetailsNameResults() {
        return detailsNameResults;
    }

    public void setDetailsNameResults(List<WordComparisonResult> detailsNameResults) {
        this.detailsNameResults = detailsNameResults;
    }

    public List<WordComparisonResult> getSearchNameResults() {
        return searchNameResults;
    }

    public void setSearchNameResults(List<WordComparisonResult> searchNameResults) {
        this.searchNameResults = searchNameResults;
    }

    public List<WordComparisonResult> getIngredientsNameResults() {
        return ingredientsNameResults;
    }

    public void setIngredientsNameResults(List<WordComparisonResult> ingredientsNameResults) {
        this.ingredientsNameResults = ingredientsNameResults;
    }
}
