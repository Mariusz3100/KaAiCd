package mariusz.ambroziak.kassistant.pojos.matching;

import mariusz.ambroziak.kassistant.pojos.matching.ProductMatchingResult;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResult;

import java.util.ArrayList;
import java.util.List;

public class MatchingProcessResult {
    ParsingResult ingredientParsingDetails;

    List<ProductMatchingResult> productsConsideredParsingResults;


    public ParsingResult getIngredientParsingDetails() {
        return ingredientParsingDetails;
    }

    public void setIngredientParsingDetails(ParsingResult ingredientParsingDetails) {
        this.ingredientParsingDetails = ingredientParsingDetails;
    }

    public List<ProductMatchingResult> getProductsConsideredParsingResults() {
        if(productsConsideredParsingResults==null)
        productsConsideredParsingResults=new ArrayList<ProductMatchingResult>();

        return productsConsideredParsingResults;
    }

    public void setProductsConsideredParsingResults(List<ProductMatchingResult> productsConsideredParsingResults) {
        this.productsConsideredParsingResults = productsConsideredParsingResults;
    }


    public void addProductsConsideredParsingResults(ProductMatchingResult result) {
        getProductsConsideredParsingResults().add(result);
    }

}
