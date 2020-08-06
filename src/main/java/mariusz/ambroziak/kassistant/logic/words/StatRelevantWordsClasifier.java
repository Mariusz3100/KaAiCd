package mariusz.ambroziak.kassistant.logic.words;

import mariusz.ambroziak.kassistant.hibernate.model.Word;
import mariusz.ambroziak.kassistant.hibernate.repository.WordRepository;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.words.StatsWordType;
import mariusz.ambroziak.kassistant.pojos.words.WordStatData;
import mariusz.ambroziak.kassistant.pojos.words.WordStatParsingResult;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webclients.usda.SingleResult;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StatRelevantWordsClasifier extends WordClasifier {

    @Autowired
    WordRepository wordRepository;

    public WordStatParsingResult calculateWordStatData(){
        List<WordStatData> productParsedList=new ArrayList<>();
        List<WordStatData> ingredientParsedList=new ArrayList<>();

        Iterable<Word> all = wordRepository.findAll();

        for(Word w:all){
            WordStatData parsed=new WordStatData(w.getText(),w.getLemma());
            parsed.setIngredientCount(w.getIngredientWordOccurenceList().size());
            parsed.setProductCount(w.getProductWordOccurences().size());
            productParsedList.add(parsed);
            ingredientParsedList.add(parsed);


            StatsWordType statsWordType = calculateWordType(w);

            parsed.setCalculatedType(statsWordType==null?"":statsWordType.toString());

        }

        productParsedList.sort((o1, o2) -> o2.getProductCount()-o1.getProductCount());
        ingredientParsedList.sort((o1, o2) -> o2.getIngredientCount()-o1.getIngredientCount());


        WordStatParsingResult retValue=new WordStatParsingResult(ingredientParsedList,productParsedList);

        return  retValue;

    }

    private StatsWordType calculateWordType(Word w) {

        ArrayList<WordsApiResult> wordResults=new ArrayList<>();
        wordResults.addAll(wordsApiClient.searchFor(w.getText()));
        ifEmptyUpdateForLemma(w.getLemma(), wordResults);

        WordsApiResult wordsApiResult = checkProductTypesForWordObject(wordResults);

        if(wordsApiResult!=null){
            return StatsWordType.BaseProductElement;
        }else{
            List<String> types=new ArrayList<>();
            types.add("Survey (FNDDS)");

            UsdaResponse inApi = usdaApiClient.findInApi(w.getLemma(),10,types);
            for (SingleResult sp : inApi.getFoods()) {
                String desc = sp.getDescription();

                TokenizationResults parsedDesc = tokenizator.parse(desc);


                boolean originalPresent=parsedDesc.getTokens().stream().anyMatch(t->t.getText().equalsIgnoreCase(w.getLemma()));
                boolean freshKeywordPresent=parsedDesc.getTokens().stream().anyMatch(t->freshFoodKeywords.contains(t.getLemma().toLowerCase()));
                List<Token> collect = parsedDesc.getTokens().stream()
                        .filter(t -> !t.getLemma().equalsIgnoreCase(w.getLemma()))
                        .filter(t -> !Pattern.matches(punctuationRegex, t.getText()))
                        .filter(t -> !freshFoodKeywords.contains(t.getLemma()))
                        .collect(Collectors.toList());

                    if(freshKeywordPresent&&originalPresent&&collect.isEmpty()){
                        return  StatsWordType.BaseProductElement;
                    }



            }

        }
        return StatsWordType.Unknown;
    }


}
