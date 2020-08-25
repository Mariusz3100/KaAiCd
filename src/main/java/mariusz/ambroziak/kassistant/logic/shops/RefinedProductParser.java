package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.logic.ingredients.RefinedIngredientWordClasifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefinedProductParser extends ShopProductParser{

    @Autowired
    RefinedIngredientWordClasifier refinedWordClasifier;





}
