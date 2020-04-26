package mariusz.ambroziak.kassistant.pojos.shop;

import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordParsed;

import java.util.List;

public class Product {
    List<WordParsed> detailsNameResults;
    List<WordParsed> searchNameResults;
    List<WordParsed> ingredientsNameResults;

    public List<WordParsed> getDetailsNameResults() {
        return detailsNameResults;
    }

    public void setDetailsNameResults(List<WordParsed> detailsNameResults) {
        this.detailsNameResults = detailsNameResults;
    }

    public List<WordParsed> getSearchNameResults() {
        return searchNameResults;
    }

    public void setSearchNameResults(List<WordParsed> searchNameResults) {
        this.searchNameResults = searchNameResults;
    }

    public List<WordParsed> getIngredientsNameResults() {
        return ingredientsNameResults;
    }

    public void setIngredientsNameResults(List<WordParsed> ingredientsNameResults) {
        this.ingredientsNameResults = ingredientsNameResults;
    }
}
