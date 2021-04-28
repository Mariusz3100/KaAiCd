package mariusz.ambroziak.kassistant.logic.usda;

import mariusz.ambroziak.kassistant.hibernate.statistical.repository.WordRepository;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.usda.Classification;
import mariusz.ambroziak.kassistant.pojos.usda.ParsingFromUsdaResult;
import mariusz.ambroziak.kassistant.pojos.usda.UsdaElementParsed;
import mariusz.ambroziak.kassistant.pojos.usda.UsdaLineParsed;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
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
//    private Resource surveyFoodFileResource;
    private Resource legacyFoodFileResource;
    //private Resource outputFileResource;
    @Autowired
    protected WordsApiClient wordsApiClient;


    public static String alphanumericPattern="([a-zA-Z]*)";


    @Autowired
    public UsdaWordsClasifierService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
//        this.surveyFoodFileResource=this.resourceLoader.getResource("classpath:/teachingResources/usdaInputs/surveyFood.csv");
        this.legacyFoodFileResource=this.resourceLoader.getResource("classpath:/teachingResources/usdaInputs/legacy_food_sorted.csv");

    }


    public void parseUsdaData() throws IOException {
        ArrayList<Map<String,Integer>> resultsMapList=new ArrayList<>();


        InputStream inputStream = legacyFoodFileResource.getInputStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
        String line=br.readLine();

        while(line!=null) {
            if(line.startsWith("#"))
                line=br.readLine();

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


    public List<List<String>> getUsdaLegacyData() throws IOException {
        List<List<String>> retValue=new ArrayList<>();
        InputStream inputStream = legacyFoodFileResource.getInputStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
        String line=br.readLine();

        while(line!=null) {
            if(line.startsWith("#"))
                line=br.readLine();

            if(line.startsWith("\""))
                line=line.substring(1);

            if(line.endsWith("\""))
                line=line.substring(0,line.length()-1);

            String[] split = line.split(",");
            retValue.add(Arrays.asList(split));
            line=br.readLine();
        }

        return retValue;

    }


    public ParsingFromUsdaResult getUsdaLegacyDataWithTypes() throws IOException {
        List<List<String>> usdaData = getUsdaLegacyData();
        ParsingFromUsdaResult retValue=new ParsingFromUsdaResult();
        TreeMap<String,UsdaElementParsed> productWordsFound=new TreeMap<>();



        for(List<String> line:usdaData){

            if(line.size()==1&&Pattern.matches(alphanumericPattern,line.get(0))){
                String word = line.get(0);
                UsdaElementParsed element=new UsdaElementParsed(word, Classification.FOOD);
                UsdaLineParsed usdaLineParsed=new UsdaLineParsed(word,element);
                retValue.getLines().add(usdaLineParsed);
                productWordsFound.put(word,element);

            }

        }
        retValue.setProductWords(productWordsFound.values().stream().collect(Collectors.toList()));
        return retValue;
    }




}
