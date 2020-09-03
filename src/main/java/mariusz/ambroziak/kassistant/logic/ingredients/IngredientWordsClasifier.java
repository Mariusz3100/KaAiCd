package mariusz.ambroziak.kassistant.logic.ingredients;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import org.springframework.stereotype.Service;

@Service
public class IngredientWordsClasifier extends WordClasifier {

    @Override
    protected void calculateProductType(AbstractParsingObject parsingAPhrase) {
        super.calculateProductType(parsingAPhrase);

        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {

            calculateReasoningsBaseOnQuantityTokens(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }

        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {

            calculateReasoningsBaseOnClassifiedPhrases(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }


    }

    private void calculateReasoningsBaseOnQuantityTokens(AbstractParsingObject parsingAPhrase) {

        for(String keyword:processedPackagingKeywords){
            if(parsingAPhrase.getFinalResults().stream()
                    .anyMatch(qualifiedToken -> WordType.QuantityElement.equals(qualifiedToken.getWordType())&&qualifiedToken.getText().equals(keyword))){
                parsingAPhrase.getProductTypeReasoning().put("Processed keyword: "+keyword+"]", ProductType.processed);

            }
        }



    }


}
