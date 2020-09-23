package mariusz.ambroziak.kassistant.pojos.matching;

import java.util.ArrayList;
import java.util.List;

public class MatchingProcessResultList {
    private List<MatchingProcessResult> results;

    private int ingredientsCovered;
    private int ingredientsCorrectlyGuessedAsEmpty;
    private int ingredientsTotal;
    private int productsFound;
    private int improperProductsFound;
    private int productsTotal;

    public List<MatchingProcessResult> getResults() {
        return results;
    }

    public void setResults(List<MatchingProcessResult> results) {
        this.results = results;
    }

    public void addResult(MatchingProcessResult result) {
        if(results==null)
            results=new ArrayList<MatchingProcessResult>();

        this.results.add(result);
    }

    public MatchingProcessResultList(List<MatchingProcessResult> results) {
        this.results = results;
    }

    public int getIngredientsCovered() {
        return ingredientsCovered;
    }

    public void setIngredientsCovered(int ingredientsCovered) {
        this.ingredientsCovered = ingredientsCovered;
    }

    public int getProductsFound() {
        return productsFound;
    }

    public void setProductsFound(int productsFound) {
        this.productsFound = productsFound;
    }

    public int getIngredientsTotal() {
        return ingredientsTotal;
    }

    public void setIngredientsTotal(int ingredientsTotal) {
        this.ingredientsTotal = ingredientsTotal;
    }

    public int getProductsTotal() {
        return productsTotal;
    }

    public void setProductsTotal(int productsTotal) {
        this.productsTotal = productsTotal;
    }

    public int getImproperProductsFound() {
        return improperProductsFound;
    }

    public void setImproperProductsFound(int improperProductsFound) {
        this.improperProductsFound = improperProductsFound;
    }

    public int getIngredientsCorrectlyGuessedAsEmpty() {
        return ingredientsCorrectlyGuessedAsEmpty;
    }

    public void setIngredientsCorrectlyGuessedAsEmpty(int ingredientsCorrectlyGuessedAsEmpty) {
        this.ingredientsCorrectlyGuessedAsEmpty = ingredientsCorrectlyGuessedAsEmpty;
    }
}
