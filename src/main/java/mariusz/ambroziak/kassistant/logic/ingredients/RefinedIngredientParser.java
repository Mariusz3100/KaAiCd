package mariusz.ambroziak.kassistant.logic.ingredients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefinedIngredientParser extends IngredientPhraseParser{



    @Autowired
    public RefinedIngredientParser(RefinedIngredientWordClasifier refinedWordClasifier) {
        super(refinedWordClasifier);

    }



}
