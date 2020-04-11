package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.spacy.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntity;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductWordsClassifier extends WordClasifier {


    @Override
    public void classifyWord(AbstractParsingObject parsingAPhrase, int index) throws WordNotFoundException {
        super.classifyWord(parsingAPhrase, index);
    }



    public void calculateWordsType(ProductParsingProcessObject parsingAPhrase) {
        super.calculateWordsType(parsingAPhrase);

    }




}
