package mariusz.ambroziak.kassistant.logic.usda;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientWordsClasifier;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.usda.Classification;
import mariusz.ambroziak.kassistant.pojos.usda.ParsingFromUsdaResult;
import mariusz.ambroziak.kassistant.pojos.usda.UsdaElementParsed;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsdaClasifierJudgeService {

    @Autowired
    UsdaWordsClasifierService usdaWordsClasifierService;

    @Autowired
    IngredientPhraseParser ingredientPhraseParser;

    @Autowired
    WordClasifier wordClasifier;


    public ParsingFromUsdaResult getUsdaAndJudgeLegacyDataWithTypes() throws IOException {
        ParsingFromUsdaResult usdaLegacyDataWithTypes = usdaWordsClasifierService.getUsdaLegacyDataWithTypes();

        for(UsdaElementParsed productWord:  usdaLegacyDataWithTypes.getProductWords()){
            IngredientLearningCase cas=new IngredientLearningCase();
            cas.setOriginalPhrase(productWord.getText());
            IngredientPhraseParsingProcessObject parseObj=ingredientPhraseParser.processSingleCase(cas);
            System.out.println(parseObj.getFoodTypeClassified());
            String productWordText=productWord.getText();
            for(int i=0;i<parseObj.getFinalResults().size();i++){
                if(parseObj.getFinalResults().get(i).getText().equals(productWordText)
                        &&WordType.ProductElement.equals(parseObj.getFinalResults().get(i).getWordType())){
                    productWord.setClassificationExpected(Classification.FOOD);
                }


            }


            ArrayList<WordsApiResult> wordsApiResults = wordClasifier.searchForAllPossibleMeaningsInWordsApi(productWord.getText());


            List<String> collect =
                    wordsApiResults.stream().flatMap(wordsApiResult -> wordsApiResult.getTypeOf().stream())
                            .collect(Collectors.toList());

            productWord.setTypeOfList(collect);

        }

        return usdaLegacyDataWithTypes;
    }
}
