package mariusz.ambroziak.kassistant.logic.ingredients;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import org.springframework.stereotype.Service;

@Service
public class IngredientWordsClasifier extends WordClasifier {

    @Override
    public void calculateProductType(AbstractParsingObject parsingAPhrase) {
        super.calculateProductType(parsingAPhrase);

        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {

            calculateReasoningsBaseOnQuantityTokens(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }
        considerCompoundProductType(parsingAPhrase);
        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {

            calculateReasoningsBaseOnClassifiedPhrases(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }


    }

    private void calculateReasoningsBaseOnQuantityTokens(AbstractParsingObject parsingAPhrase) {

        for(String keyword:processedPackagingKeywords){
            if(parsingAPhrase.getFinalResults().stream()
                    .anyMatch(qualifiedToken -> WordType.QuantityElement.equals(qualifiedToken.getWordType())&&qualifiedToken.getText().equals(keyword))){
                parsingAPhrase.getProductTypeReasoning().put("Processed (packaging) keyword: "+keyword+"]", ProductType.processed);

            }
        }



    }

    @Override
    protected UsdaResponse findInUsdaApiAllTypes(String phrases, int i) {
        return super.findInUsdaApi(phrases, i);

    }


    @Override
    protected UsdaResponse findInUsdaApiWithRespectForTypes(String quantitylessTokensWithPluses, int i) {
        return super.findInUsdaApiExceptBranded(quantitylessTokensWithPluses, i);
    }
}
