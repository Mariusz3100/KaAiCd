package mariusz.ambroziak.kassistant.ai.controllers;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;

@RestController
public class LogicController {

	private IngredientPhraseParser ingredientParser;


	@Autowired
	public LogicController(IngredientPhraseParser ingredientParser) {
		super();
		this.ingredientParser = ingredientParser;
	}


	@CrossOrigin
	@RequestMapping("/parseIngredients")
	@ResponseBody
	public ParsingResultList phrasesParsing() throws IOException{
		ParsingResultList parseFromFile = this.ingredientParser.parseFromFile();
		
		System.out.println();

		return parseFromFile;

	}
}
