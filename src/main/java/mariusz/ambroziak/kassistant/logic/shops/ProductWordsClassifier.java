package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProductWordsClassifier extends WordClasifier {


    @Override
    public void classifySingleWord(AbstractParsingObject parsingAPhrase, int index) throws WordNotFoundException {


        super.classifySingleWord(parsingAPhrase, index);

    }


    @Override
    public void calculateWordTypesForWholePhrase(AbstractParsingObject parsingAPhrase) {


        super.calculateWordTypesForWholePhrase(parsingAPhrase);


//        initialCategorization(parsingAPhrase);
//
//        recategorize(parsingAPhrase);
//        calculateProductType(parsingAPhrase);
//
//        categorizeAllElseAsProducts(parsingAPhrase);

    }



    public void calculateProductType(AbstractParsingObject parsingAPhrase) {
        extractAndMarkProductProperties(parsingAPhrase);
    }

    protected void extractAndMarkProductProperties(AbstractParsingObject parsingAPhrase) {
        for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
            for(String keyword:freshFoodKeywords) {
                if(keyword.equals(qt.getText())) {
                    qt.setWordType(WordType.ProductPropertyElement);
                    parsingAPhrase.setFoodTypeClassified(ProductType.fresh);
                }
            }
        }

    }

    public void calculateProductType(ProductParsingProcessObject parsingAPhrase) {
        ProductType result=checkDepartmentForKeywords(parsingAPhrase);

        if(result==null||result.equals(ProductType.unknown)){
            result=checkQuantities(parsingAPhrase);
        }

        parsingAPhrase.setFoodTypeClassified(result);
    }


}
