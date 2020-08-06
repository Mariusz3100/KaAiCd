package mariusz.ambroziak.kassistant.logic.words;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.model.Word;
import mariusz.ambroziak.kassistant.hibernate.repository.WordRepository;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.words.WordStatData;
import mariusz.ambroziak.kassistant.pojos.words.WordStatParsingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        }

        productParsedList.sort((o1, o2) -> o2.getProductCount()-o1.getProductCount());
        ingredientParsedList.sort((o1, o2) -> o2.getIngredientCount()-o1.getIngredientCount());


        WordStatParsingResult retValue=new WordStatParsingResult(ingredientParsedList,productParsedList);

        return  retValue;

    }


}
