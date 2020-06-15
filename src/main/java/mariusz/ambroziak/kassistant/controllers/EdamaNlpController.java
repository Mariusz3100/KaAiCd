package mariusz.ambroziak.kassistant.controllers;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.webclients.edamam.recipes.EdamanRecipeSearchService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.Ingredient;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.RecipeHitOuter;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.RecipeSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamamNlpResponseData;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;

@RestController
public class EdamaNlpController {
    
	private EdamanIngredientParsingService parsingService;
	private EdamanRecipeSearchService searchSevice;

	@Autowired
	public EdamaNlpController(EdamanIngredientParsingService parsingService, EdamanRecipeSearchService searchSevice) {
		super();
		this.parsingService = parsingService;
		this.searchSevice=searchSevice;
	}


	@RequestMapping("/edamanNlpParsing")
    public String edamanNlpParsing(@RequestParam(value="param", defaultValue="") String param){

		EdamamNlpResponseData retValue=this.parsingService.find(param);
    	return retValue.toJsonString();
    	
    }
	
	@ResponseBody
	@RequestMapping("/retrieveEdamanParsingDataFromFileSequentially")
    public String edamanNlpParseAndSave() throws IOException{

		this.parsingService.retrieveEdamanParsingDataFromFileSequentially();;
    	return "Done";
    	
    }


	@RequestMapping("/recipeSearch")
	public String recipeSearch(@RequestParam(value="param", defaultValue="") String param){

		RecipeSearchResponse retValue=this.searchSevice.findInApi(param,0,10);

		List<String> ingredient=new ArrayList<>();

		printMatching(param, retValue,ingredient);
		System.out.println();

		retValue=this.searchSevice.findInApi(param,10,30);

		printMatching(param, retValue,ingredient);

		retValue=this.searchSevice.findInApi(param,30,50);

		printMatching(param, retValue,ingredient);


		retValue=this.searchSevice.findInApi(param,70,90);

		printMatching(param, retValue,ingredient);

		ingredient.sort(Comparator.comparingInt(String::length));
		ingredient=ingredient.stream().distinct().collect(Collectors.toList());
		ingredient.forEach(x->System.out.println(x));
		return retValue.toJsonString();

	}

	private void printMatching(@RequestParam(value = "param", defaultValue = "") String param, RecipeSearchResponse retValue,List<String>  ingredient) {
		for(RecipeHitOuter outer:retValue.getHits()){
			List<Ingredient> ingredients = outer.getRecipe().getIngredients();

			for (Ingredient i:ingredients){
				if(i.getText().contains(param)){
					ingredient.add(i.getText());
				}
			}
		}
	}


}
