package mariusz.ambroziak.kassistant.pojos.matching;

import mariusz.ambroziak.kassistant.pojos.parsing.CalculatedResults;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResult;

import java.util.List;

public class ProductMatchingResult {
     ParsingResult baseResult;

    private  boolean calculatedVerdict;
    private  boolean expectedVerdict;

    private CalculatedResults wordsMatching;

    public boolean isCalculatedVerdict() {
        return calculatedVerdict;
    }

    public void setCalculatedVerdict(boolean calculatedVerdict) {
        this.calculatedVerdict = calculatedVerdict;
    }

    public boolean isExpectedVerdict() {
        return expectedVerdict;
    }

    public void setExpectedVerdict(boolean expectedVerdict) {
        this.expectedVerdict = expectedVerdict;
    }

    public CalculatedResults getWordsMatching() {
        return wordsMatching;
    }

    public void setWordsMatching(CalculatedResults wordsMatching) {
        this.wordsMatching = wordsMatching;
    }

    public ParsingResult getBaseResult() {
        return baseResult;
    }

    public void setBaseResult(ParsingResult baseResult) {
        this.baseResult = baseResult;
    }

    public ProductMatchingResult(ParsingResult baseResult) {
        this.baseResult = baseResult;
    }
}
