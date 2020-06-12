package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.pojos.shop.ProductNamesComparison;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductNameComparatorSevice {

    @Autowired
    TokenizationClientService tokenizer;


    public ProductNamesComparison parseTwoPhrases(TokenizationResults firstPhrase, TokenizationResults secondPhrase) {
        ParseCompareProductNames thisObject=new ParseCompareProductNames(firstPhrase,secondPhrase);
        return thisObject.calculateResults();

    }

    public ProductNamesComparison parseTwoPhrases(String firstPhrase, String secondPhrase) {
        if(firstPhrase==null||secondPhrase==null){
            throw new IllegalArgumentException("empty arguments");
        }

        TokenizationResults firstTokenized = this.tokenizer.parse(firstPhrase.toLowerCase());
        TokenizationResults secondTokenized = this.tokenizer.parse(secondPhrase.toLowerCase());
        ParseCompareProductNames thisObject=new ParseCompareProductNames(firstTokenized,secondTokenized);
        return thisObject.calculateResults();

    }

}
