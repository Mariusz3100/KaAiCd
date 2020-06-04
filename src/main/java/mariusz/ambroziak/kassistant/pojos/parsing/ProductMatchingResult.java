package mariusz.ambroziak.kassistant.pojos.parsing;

import java.util.List;

public class ProductMatchingResult {
     ParsingResult baseResult;

    private  boolean verdict;

    private CalculatedResults wordsMatching;


    public boolean isVerdict() {
        return verdict;
    }

    public void setVerdict(boolean verdict) {
        this.verdict = verdict;
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
