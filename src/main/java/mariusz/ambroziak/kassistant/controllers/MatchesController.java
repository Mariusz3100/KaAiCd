package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.logic.matching.IngredientProductMatchingService;
import mariusz.ambroziak.kassistant.pojos.parsing.MatchingProcessResult;
import mariusz.ambroziak.kassistant.pojos.parsing.MatchingProcessResultList;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResultList;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamamNlpIngredientOuter;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamamNlpResponseData;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamamNlpSingleIngredientInner;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.EdamanRecipeSearchService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.Ingredient;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.RecipeHitInner;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.RecipeSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MatchesController {
	@Autowired
	IngredientProductMatchingService matchingService;

	@CrossOrigin
	@RequestMapping("/findMatchesForIngredients")
	@ResponseBody
	public MatchingProcessResultList findMatchesForIngredients() throws IOException{

		MatchingProcessResultList retValue=new MatchingProcessResultList(matchingService.parseMatchAndSaveToDb());

		return retValue;

	}







}
