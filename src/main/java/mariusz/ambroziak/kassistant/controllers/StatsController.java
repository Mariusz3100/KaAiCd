package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ProductParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.TescoProductRepository;
import mariusz.ambroziak.kassistant.logic.IngredientPhraseTokenizerTest;
import mariusz.ambroziak.kassistant.logic.words.StatRelevantWordsClasifier;
import mariusz.ambroziak.kassistant.pojos.words.WordStatParsingResult;
import mariusz.ambroziak.kassistant.pojos.words.WordStatParsingResultOuter;
import mariusz.ambroziak.kassistant.webclients.morrisons.MorrisonsClientService;
import mariusz.ambroziak.kassistant.webclients.morrisons.Morrisons_Product;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StatsController {





	@Autowired
	StatRelevantWordsClasifier statRelevantWordsClasifier;




	@CrossOrigin
	@RequestMapping("/getStatData")
	@ResponseBody
	public WordStatParsingResultOuter getStatData(){
		WordStatParsingResult wordStatParsingResult = this.statRelevantWordsClasifier.calculateWordStatData();

		return new WordStatParsingResultOuter(wordStatParsingResult);
	}



}
