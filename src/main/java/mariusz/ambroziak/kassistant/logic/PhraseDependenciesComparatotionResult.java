package mariusz.ambroziak.kassistant.logic;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.webclients.usda.SingleResult;

import java.util.ArrayList;
import java.util.List;

public class PhraseDependenciesComparatotionResult {
    private boolean comparisonResults;
    private List<String> keywordsFound;
    private ProductType typeDeduced;
    private String resultingPhrase;

    SingleResult singleResult;

    public boolean isComparisonResults() {
        return comparisonResults;
    }

    public void setComparisonResults(boolean comparisonResults) {
        this.comparisonResults = comparisonResults;
    }

    public List<String> getKeywordsFound() {
        if(keywordsFound==null)
            keywordsFound=new ArrayList<>();

        return keywordsFound;
    }

    public void setKeywordsFound(List<String> keywordsFound) {
        this.keywordsFound = keywordsFound;
    }

    public ProductType getTypeDeduced() {
        return typeDeduced;
    }

    public void setTypeDeduced(ProductType typeDeduced) {
        this.typeDeduced = typeDeduced;
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

    public String getResultingPhrase() {
        return resultingPhrase;
    }

    public String getEffectiveResultingPhrase() {
        if(getResultingPhrase()!=null)
            return getResultingPhrase();
        else
            return getSingleResult().getDescription();
    }

    public void setResultingPhrase(String resultingPhrase) {
        this.resultingPhrase = resultingPhrase;
    }

    public PhraseDependenciesComparatotionResult() {
    }
}
