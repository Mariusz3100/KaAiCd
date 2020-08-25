package mariusz.ambroziak.kassistant.logic.ingredients;

import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import org.springframework.stereotype.Service;

@Service
public class RefinedIngredientWordClasifier extends IngredientWordsClasifier {


    public UsdaResponse findInUsdaApi(String quantitylessTokensWithPluses, int i) {
        return this.usdaApiClient.findInApi(quantitylessTokensWithPluses, i);
    }






}
