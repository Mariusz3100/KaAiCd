package mariusz.ambroziak.kassistant.logic.ingredients;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import org.springframework.stereotype.Service;

@Service
public class IngredientWordsClasifier extends WordClasifier {

    @Override
    protected void calculateProductType(AbstractParsingObject parsingAPhrase) {
        super.calculateProductType(parsingAPhrase);

        IngredientPhraseParsingProcessObject parsing= (IngredientPhraseParsingProcessObject) parsingAPhrase;


        for(QualifiedToken qt:parsing.getFinalResults()){
            for(String keyword:freshFoodKeywords){
                if(qt.getText().equals(keyword)){
           //         qt.setWordType(WordType.ProductPropertyElement);
                    parsing.setFoodTypeClassified(ProductType.fresh);
                }
            }
        }



    }
}
