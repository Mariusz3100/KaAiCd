package mariusz.ambroziak.kassistant.pojos.words;

import java.util.List;

public class WordStatParsingResultOuter {
    private WordStatParsingResult inner;

    public WordStatParsingResultOuter(WordStatParsingResult inner) {
        this.inner = inner;
    }


    public WordStatParsingResult getInner() {
        return inner;
    }

    public void setInner(WordStatParsingResult inner) {
        this.inner = inner;
    }
}
