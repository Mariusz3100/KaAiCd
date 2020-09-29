package mariusz.ambroziak.kassistant.logic.usda;

import mariusz.ambroziak.kassistant.enums.StatsWordType;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseFound;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.ProductParsingResult;
import mariusz.ambroziak.kassistant.hibernate.statistical.model.ProductWordOccurence;
import mariusz.ambroziak.kassistant.hibernate.statistical.model.Word;
import mariusz.ambroziak.kassistant.hibernate.statistical.repository.WordRepository;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.words.WordAssociacion;
import mariusz.ambroziak.kassistant.pojos.words.WordStatData;
import mariusz.ambroziak.kassistant.pojos.words.WordStatParsingResult;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webclients.usda.SingleResult;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiClient;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsdaWordsClasifierService extends WordClasifier {

    @Autowired
    WordRepository wordRepository;

    @Autowired
    UsdaPhrasesClasifier usdaPhrasesClasifier;

    @Autowired
    private ResourceLoader resourceLoader;
    private Resource surveyFoodFileResource;
    private Resource legacyFoodFileResource;
    //private Resource outputFileResource;
    @Autowired
    protected WordsApiClient wordsApiClient;

    @Autowired
    public UsdaWordsClasifierService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.surveyFoodFileResource=this.resourceLoader.getResource("classpath:/teachingResources/surveyFood.csv");
        this.legacyFoodFileResource=this.resourceLoader.getResource("classpath:/teachingResources/sr_legacy_food.csv");

    }


    public void parseUsdaData() throws IOException {
        ArrayList<Map<String,Integer>> resultsMapList=new ArrayList<>();


        InputStream inputStream = surveyFoodFileResource.getInputStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
        String line=br.readLine();

        while(line!=null) {
            if(line.startsWith("\""))
                line=line.substring(1);

            if(line.endsWith("\""))
                line=line.substring(0,line.length()-1);


            String[] split = line.split("\",\"");

            String productName=split[2].replaceAll("\"","");
            String[] nameSplit=productName.split(",");
            for(int i=0;i<nameSplit.length;i++) {
                String productNamePart=nameSplit[i];

                if (productNamePart.contains(" ")) {
                    System.out.println(productNamePart);
                } else {

                    try {
                        ArrayList<String> typesOf = wordsApiClient.getTypesOf(productNamePart);





                    } catch (WordNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println();

            line=br.readLine();
        }


        }






}
