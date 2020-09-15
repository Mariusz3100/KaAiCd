package mariusz.ambroziak.kassistant.logic;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.webclients.usda.SingleResult;

public class PhraseDependenciesComparatotionResult {
    private boolean comparisonResults;
    private ProductType keywordFound;
    SingleResult singleResult;

    public boolean isComparisonResults() {
        return comparisonResults;
    }

    public void setComparisonResults(boolean comparisonResults) {
        this.comparisonResults = comparisonResults;
    }

    public ProductType getKeywordFound() {
        return keywordFound;
    }

    public void setKeywordFound(ProductType keywordFound) {
        this.keywordFound = keywordFound;
    }

    public SingleResult getSingleResult() {
        return singleResult;
    }

    public void setSingleResult(SingleResult singleResult) {
        this.singleResult = singleResult;
    }

    public PhraseDependenciesComparatotionResult(boolean comparisonResults) {
        this.comparisonResults = comparisonResults;
    }

    public PhraseDependenciesComparatotionResult() {
    }
}
