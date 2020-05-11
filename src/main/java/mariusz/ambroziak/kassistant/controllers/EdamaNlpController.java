package mariusz.ambroziak.kassistant.controllers;

import java.io.IOException;

import mariusz.ambroziak.kassistant.webclients.edamam.recipes.EdamanRecipeSearchService;
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

		RecipeSearchResponse retValue=this.searchSevice.findInApi(param);
		return retValue.toJsonString();

	}


}
