package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.logic.words.StatRelevantWordsClasifier;
import mariusz.ambroziak.kassistant.pojos.words.WordStatParsingResult;
import mariusz.ambroziak.kassistant.pojos.words.WordStatParsingResultOuter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
