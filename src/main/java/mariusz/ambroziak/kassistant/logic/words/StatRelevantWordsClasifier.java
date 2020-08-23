package mariusz.ambroziak.kassistant.logic.words;

import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseFound;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.ProductParsingResult;
import mariusz.ambroziak.kassistant.hibernate.statistical.model.ProductWordOccurence;
import mariusz.ambroziak.kassistant.hibernate.statistical.model.Word;
import mariusz.ambroziak.kassistant.hibernate.statistical.repository.WordRepository;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.enums.StatsWordType;
import mariusz.ambroziak.kassistant.pojos.words.WordAssociacion;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StatRelevantWordsClasifier extends WordClasifier {

    @Autowired
    WordRepository wordRepository;

    public WordStatParsingResult calculateWordStatData(){
        List<WordStatData> productParsedList=new ArrayList<>();
        List<WordStatData> ingredientParsedList=new ArrayList<>();
        Map<String, WordStatData> resultsMap=new HashMap<>();

        Map<String, StatsWordType> typeFoundMap=new HashMap<>();


        Iterable<Word> all = wordRepository.findAll();

        for(Word w:all){
            WordStatData parsed=new WordStatData(w.getText(),w.getLemma());
            parsed.setIngredientCount(w.getIngredientWordOccurenceList().size());
            parsed.setProductCount(w.getProductWordOccurences().size());
            productParsedList.add(parsed);
            ingredientParsedList.add(parsed);
            resultsMap.put(w.getText(),parsed);

            StatsWordType statsWordType = getOrcalculateWordType(w);

            parsed.setCalculatedType(statsWordType==null?StatsWordType.Unknown:statsWordType);


            typeFoundMap.put(w.getText(),statsWordType);

            List<PhraseFound> byPhraseContaining = this.phraseFoundRepo.findByPhraseContaining(w.getText());
            byPhraseContaining.addAll(this.phraseFoundRepo.findByPhraseContaining(w.getLemma()));

            parsed.setPhrasesAssociated(byPhraseContaining.stream().map(phraseFound -> phraseFound.getPhrase()).collect(Collectors.toList()));
            System.out.print(w.getText()+" ");
        }

        for(Word w:all) {

                List<ProductParsingResult> collect = w.getProductWordOccurences().stream().map(pwo -> pwo.getProductParsingResult()).collect(Collectors.toList());
                Map<String, Integer> count = new HashMap<>();

                for (ProductParsingResult ppr : collect) {

                    countUpWordsOccuring(count, ppr);


                }
            WordStatData wordStatData = resultsMap.get(w.getText());
            List<WordAssociacion> result = new ArrayList<>();

                count.forEach(
                        (s, integer) ->
                        {
                            if (integer > 1
                                    && !s.equalsIgnoreCase(w.getText())
                                    && !s.equalsIgnoreCase(w.getLemma())
                                    && !wordStatData.getPhrasesAssociated().stream().anyMatch(phrase->phrase.contains(s)))
                                result.add(new WordAssociacion(s,integer,typeFoundMap.get(s)));
                        });
                result.sort((o1, o2) -> o2.getCount()-o1.getCount());
            wordStatData.setWordsAssociated(result);


        }



        productParsedList.sort((o1, o2) -> o2.getProductCount()-o1.getProductCount());
        ingredientParsedList.sort((o1, o2) -> o2.getIngredientCount()-o1.getIngredientCount());


        WordStatParsingResult retValue=new WordStatParsingResult(ingredientParsedList,productParsedList);

        return  retValue;

    }

    public static void countUpWordsOccuring(Map<String, Integer> count, ProductParsingResult ppr) {
        for(ProductWordOccurence word:ppr.getWordsOccuring()){
            String text = word.getWord().getText();
            Integer retr = count.get(text) == null ? 0 : count.get(text);

            count.put(text, ++retr);
        }
    }


    protected WordsApiResult checkForTypes(ArrayList<WordsApiResult> wordResults,String keywordForTypeconsidered, ArrayList<String> attributesForTypeConsidered) {
        ArrayList<String> keywordsForTypeconsidered=new ArrayList<>();//Arrays.asList(new String[]{keywordForTypeconsidered});
        keywordsForTypeconsidered.add(keywordForTypeconsidered);
        return checkForTypes(wordResults, keywordsForTypeconsidered, attributesForTypeConsidered);
    }

    public StatsWordType getOrcalculateWordType(Word w) {
        if (w.getStatsWordType() != null||StatsWordType.Unknown.equals(w.getStatsWordType())) {
            return w.getStatsWordType();
        } else {
            StatsWordType typeCalculated=StatsWordType.Unknown;

            ArrayList<WordsApiResult> wordResults = new ArrayList<>();
            wordResults.addAll(wordsApiClient.searchFor(w.getText()));
            ifEmptyUpdateForLemma(w.getLemma(), wordResults);

            //      WordsApiResult wordsApiResult = checkProductTypesForWordObject(wordResults);


            for(StatsWordType typeConsidered:StatsWordType.values()){
                WordsApiResult wordsApiResult = checkForTypes(wordResults, typeConsidered.toString().toLowerCase(), new ArrayList<>());
                if(wordsApiResult!=null){
                    w.setStatsWordType(typeConsidered);
                    wordRepository.save(w);
                    return typeConsidered;

                }

            }

            WordsApiResult war = checkForTypes(wordResults, productTypeKeywords, new ArrayList<>());


            if (war != null) {
                typeCalculated= StatsWordType.BaseProductElement;
            } else {
                List<String> types = new ArrayList<>();
                types.add("Survey (FNDDS)");

                UsdaResponse inApi = usdaApiClient.findInApi(w.getLemma(), 10, types);
                for (SingleResult sp : inApi.getFoods()) {
                    String desc = sp.getDescription();

                    TokenizationResults parsedDesc = tokenizator.parse(desc);


                    boolean originalPresent = parsedDesc.getTokens().stream().anyMatch(t -> t.getLemma().equalsIgnoreCase(w.getLemma()));
                    boolean freshKeywordPresent = parsedDesc.getTokens().stream().anyMatch(t -> freshFoodKeywords.contains(t.getLemma().toLowerCase()));
                    List<Token> collect = parsedDesc.getTokens().stream()
                            .filter(t -> !t.getLemma().equalsIgnoreCase(w.getLemma()))
                            .filter(t -> !Pattern.matches(punctuationRegex, t.getText()))
                            .filter(t -> !freshFoodKeywords.contains(t.getLemma()))
                            .collect(Collectors.toList());

                    if (freshKeywordPresent && originalPresent && collect.isEmpty()) {
                        typeCalculated= StatsWordType.BaseProductElement;
                    }


                }

            }

            w.setStatsWordType(typeCalculated);
            wordRepository.save(w);
            return typeCalculated;
        }
    }


}
