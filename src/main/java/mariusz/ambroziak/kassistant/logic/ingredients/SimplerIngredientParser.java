package mariusz.ambroziak.kassistant.logic.ingredients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimplerIngredientParser extends IngredientPhraseParser{



    @Autowired
    public SimplerIngredientParser(SimplerIngredientWordClasifier simplerIngredientWordClasifier) {
        super(simplerIngredientWordClasifier);

    }



}
