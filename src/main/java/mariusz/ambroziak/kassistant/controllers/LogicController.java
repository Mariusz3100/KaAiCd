package mariusz.ambroziak.kassistant.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import mariusz.ambroziak.kassistant.hibernate.cache.repositories.WebknoxResponseRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseConsidered;
import mariusz.ambroziak.kassistant.logic.matching.IngredientProductMatchingService;
import mariusz.ambroziak.kassistant.logic.matching.PhrasesCalculatingService;
import mariusz.ambroziak.kassistant.pojos.matching.MatchingProcessResult;
import mariusz.ambroziak.kassistant.pojos.matching.MatchingProcessResultList;
import mariusz.ambroziak.kassistant.pojos.phrasefinding.PhraseFindingResults;
import mariusz.ambroziak.kassistant.pojos.phrasefinding.PhraseFindingResultsOuter;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.*;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoFromFileService;
import mariusz.ambroziak.kassistant.webclients.webknox.RecipeSearchApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;

@RestController
public class LogicController {

	@Qualifier()
	private IngredientPhraseParser ingredientPhraseParser;
	private EdamanRecipeSearchService searchSevice;
	private EdamanIngredientParsingService edamanNlpService;
	@Autowired
	TescoFromFileService fileTescoService;
	@Autowired
	IngredientProductMatchingService ingredientProductMatchingService;
	@Autowired
	RecipeSearchApiClient recipeSearchApiClient;

	@Autowired
	WebknoxResponseRepository webknoxResponseRepository;

	@Autowired
	PhrasesCalculatingService phrasesCalculatingService;

	@Autowired
	public LogicController(IngredientPhraseParser ingredientPhraseParser,EdamanRecipeSearchService searchSevice, EdamanIngredientParsingService edamanNlpService) {
		super();
		this.ingredientPhraseParser = ingredientPhraseParser;
		this.searchSevice=searchSevice;
		this.edamanNlpService=edamanNlpService;

	}

	@CrossOrigin
	@RequestMapping("/parseRecipe")
	@ResponseBody
	public MatchingProcessResultList parseRecipe(@RequestParam(value="param", defaultValue="") String param) throws IOException{

		List<MatchingProcessResult> results = ingredientProductMatchingService.parseMatchAndJudgeResultsFromDbMatches(param);
		MatchingProcessResultList calculated=new MatchingProcessResultList(results);


		return calculated;
//		MatchingProcessResultList retValue=new MatchingProcessResultList(matchingService.parseMatchAndGetResultsFromDbAllCases(true));
//
//		return retValue;

	}


	@CrossOrigin
	@RequestMapping("/initializeLocalProducts")
	@ResponseBody
	public String initializeLocalProducts() throws IOException{
//		ParsingResultList parseFromFile = this.ingredientParser.parseFromFile();


        try {
			int size=fileTescoService.initializeProductMap();
			return "Products accessible: "+size;
        }catch (IOException e){
            e.printStackTrace();
			return "Excepton: "+e.getMessage();
        }


	}


	@CrossOrigin
	@ResponseBody
	@RequestMapping("/displayCurrentProductPhrasesConsidered")
	public Map<String, List<PhraseConsidered>>  displayCurrentProductPhrasesConsidered() throws IOException{
		Map<String, List<PhraseConsidered>> phrasesCalculated = this.phrasesCalculatingService.getProductPhrasesCalculated();

		return phrasesCalculated;


	}

	@CrossOrigin
	@ResponseBody
	@RequestMapping("/displayCurrentIngredientPhrasesConsidered")
	public Map<String, List<PhraseConsidered>>  displayCurrentIngredientPhrasesConsidered() throws IOException{
		Map<String, List<PhraseConsidered>> phrasesCalculated = this.phrasesCalculatingService.getIngredientPhrasesCalculated();

		return phrasesCalculated;


	}

	@CrossOrigin
	@ResponseBody
	@RequestMapping("/displayAllCurrentPhrasesConsidered")
	public PhraseFindingResultsOuter displayPhraseFindingResults() throws IOException{
		PhraseFindingResults results = this.phrasesCalculatingService.calculatePhraseFindingResults();

		return new PhraseFindingResultsOuter(results);


	}



}
