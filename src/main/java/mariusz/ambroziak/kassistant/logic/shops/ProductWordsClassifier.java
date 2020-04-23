package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.utils.ProblemLogger;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductWordsClassifier extends WordClasifier {


    @Override
    public void classifySingleWord(AbstractParsingObject parsingAPhrase, int index) throws WordNotFoundException {


        super.classifySingleWord(parsingAPhrase, index);

    }


    @Override
    public void calculateWordTypesForWholePhrase(AbstractParsingObject parsingAPhrase) {





        super.calculateWordTypesForWholePhrase(parsingAPhrase);

    }




}
