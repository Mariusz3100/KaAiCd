package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.hibernate.cache.repositories.WebknoxResponseRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.logic.ingredients.RefinedIngredientParser;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResultList;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.EdamanRecipeSearchService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.Ingredient;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.RecipeHitInner;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.RecipeSearchResponse;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoFromFileService;
import mariusz.ambroziak.kassistant.webclients.webknox.RecipeSearchApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IngredientController {
	@Autowired
	private IngredientPhraseParser ingredientPhraseParser;
	@Autowired
	private EdamanRecipeSearchService searchSevice;
	@Autowired
	private EdamanIngredientParsingService edamanNlpService;
	@Autowired
	TescoFromFileService fileTescoService;

	@Autowired
	RecipeSearchApiClient recipeSearchApiClient;


	@Autowired
	private RefinedIngredientParser refinedIngredientParser;


	@CrossOrigin
	@RequestMapping("/parseIngredients")
	@ResponseBody
	public ParsingResultList phrasesParsing() throws IOException{
//		ParsingResultList parseFromFile = this.ingredientParser.parseFromFile();

		ParsingResultList parseFromFile = this.ingredientPhraseParser.parseFromDbAndSaveAllToDb();


		return parseFromFile;

	}

	@CrossOrigin
	@RequestMapping("/ingredientRefinedParsing")
	@ResponseBody
	public ParsingResultList phrasesRefinedParsing() throws IOException{
//		ParsingResultList parseFromFile = this.ingredientParser.parseFromFile();

		ParsingResultList parseFromFile = this.refinedIngredientParser.parseFromDbAndSaveAllToDb();


		return parseFromFile;

	}


	@RequestMapping("/searchIngredientsFor")
	public String searchIngredientsFor(@RequestParam(value="param", defaultValue="") String param){

		RecipeSearchResponse searchRecipeResponse=this.searchSevice.findInApi(param);
		List<String> results=new ArrayList<>();
		int allCount=0;
		int count=0;
		List<String> ings=new ArrayList<>();
		for(int i=0;i<searchRecipeResponse.getHits().size();i++){

			RecipeHitInner recipe = searchRecipeResponse.getHits().get(i).getRecipe();
			for(int j = 0; j< recipe.getIngredients().size();j++){
				allCount++;
				Ingredient ingredient = recipe.getIngredients().get(j);
				if( ingredient.getText().toLowerCase().contains(param)){
					count++;

							ings.add(ingredient.getText());


				}
			}
		}

		ings.stream().sorted(Comparator.comparingInt(String::length)).forEach(t->System.out.println(t));
		return searchRecipeResponse.toJsonString();

	}


	@RequestMapping("/searchAndSaveWebknoxRecipesfor")
	public String searchAndSaveWebknoxRecipesfor(@RequestParam(value="param") String param) {

		if (param == null || param.isEmpty()) {
			return "Param required";
		} else {
			List<IngredientLearningCase> ingredientLearningCases = this.recipeSearchApiClient.getandSaveIngredientsFor(param);

			String retValue = ingredientLearningCases.stream().map(ilc -> ilc.getOriginalPhrase()).collect(Collectors.joining("<br>"));

			return retValue;
		}
	}



	@RequestMapping("/searchAndSaveAllProxiedIngredients")
	public String searchAndSaveAllProxiedIngredients() {
		List<IngredientLearningCase> ingredientLearningCases = this.recipeSearchApiClient.saveInDbAllCachedIngredients();
		String retValue=ingredientLearningCases.stream().map(IngredientLearningCase::getOriginalPhrase).collect(Collectors.joining("<br>"));
		return retValue;
	}


	@RequestMapping("/searchAndSaveAllProxiedIngredientsFor")
	public String searchAndSaveAllProxiedIngredientsFor(@RequestParam(value="param") String param) {
		List<IngredientLearningCase> ingredientLearningCases = this.recipeSearchApiClient.saveInDbAllCachedIngredientsFor(param);




		String retValue=ingredientLearningCases.stream().map(IngredientLearningCase::getOriginalPhrase).collect(Collectors.joining("<br>"));
		return retValue;
	}

}
