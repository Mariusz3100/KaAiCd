package mariusz.ambroziak.kassistant.pojos.matching;

import mariusz.ambroziak.kassistant.hibernate.parsing.model.MatchExpected;

import java.util.List;

public class InputCases {
    private List<String> ingredientLines;

    private List<String> productNames;

    private List<MatchExpected> matchesExpected;


    public List<String> getIngredientLines() {
        return ingredientLines;
    }

    public void setIngredientLines(List<String> ingredientLines) {
        this.ingredientLines = ingredientLines;
    }

    public List<String> getProductNames() {
        return productNames;
    }

    public void setProductNames(List<String> productNames) {
        this.productNames = productNames;
    }

    public List<MatchExpected> getMatchesExpected() {
        return matchesExpected;
    }

    public void setMatchesExpected(List<MatchExpected> matchesExpected) {
        this.matchesExpected = matchesExpected;
    }
}
