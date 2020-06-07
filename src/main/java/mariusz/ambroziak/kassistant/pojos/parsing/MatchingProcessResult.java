package mariusz.ambroziak.kassistant.pojos.parsing;

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
        return productsConsideredParsingResults;
    }

    public void setProductsConsideredParsingResults(List<ProductMatchingResult> productsConsideredParsingResults) {
        this.productsConsideredParsingResults = productsConsideredParsingResults;
    }


    public void addProductsConsideredParsingResults(ProductMatchingResult result) {
        if(productsConsideredParsingResults==null)
            productsConsideredParsingResults=new ArrayList<ProductMatchingResult>();

        this.productsConsideredParsingResults.add(result);
    }

}