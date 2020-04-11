package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.utils.ProblemLogger;
import mariusz.ambroziak.kassistant.webclients.spacy.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntity;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductWordsClassifier extends WordClasifier {


    @Override
    public void classifyWord(AbstractParsingObject parsingAPhrase, int index) throws WordNotFoundException {
       ProductParsingProcessObject productObject=(ProductParsingProcessObject)parsingAPhrase;

       String brand=productObject.getProduct().getBrand();
        boolean found=false;
       if(brand!=null&&!brand.isEmpty()){
           if(brand.split(" ").length>1){
               ProblemLogger.logProblem("Not a one word brand");
           }else{
               TokenizationResults tokens=parsingAPhrase.getEntitylessTokenized();
               Token t=tokens.getTokens().get(index);
               String token=t.getText();

               if(token.equalsIgnoreCase(brand)){
                   found=true;
                   addResult(parsingAPhrase,index,new QualifiedToken(t,WordType.Ignored));
               }

           }
       }

       if(!found) {
           super.classifyWord(parsingAPhrase, index);
       }
    }



    public void calculateWordsType(ProductParsingProcessObject parsingAPhrase) {
        super.calculateWordsType(parsingAPhrase);

    }




}
