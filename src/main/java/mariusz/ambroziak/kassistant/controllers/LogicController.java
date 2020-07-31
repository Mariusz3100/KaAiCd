package mariusz.ambroziak.kassistant.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamamNlpIngredientOuter;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamamNlpResponseData;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamamNlpSingleIngredientInner;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.*;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoFromFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResultList;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;

@RestController
public class LogicController {

	private IngredientPhraseParser ingredientParser;
	private EdamanRecipeSearchService searchSevice;
	private EdamanIngredientParsingService edamanNlpService;
	@Autowired
	TescoFromFileService fileTescoService;


	@Autowired
	public LogicController(IngredientPhraseParser ingredientParser,EdamanRecipeSearchService searchSevice, EdamanIngredientParsingService edamanNlpService) {
		super();
		this.ingredientParser = ingredientParser;
		this.searchSevice=searchSevice;
		this.edamanNlpService=edamanNlpService;

	}


	@CrossOrigin
	@RequestMapping("/parseIngredients")
	@ResponseBody
	public ParsingResultList phrasesParsing() throws IOException{
//		ParsingResultList parseFromFile = this.ingredientParser.parseFromFile();

		ParsingResultList parseFromFile = this.ingredientParser.parseFromDbAndSaveAllToDb();


		return parseFromFile;

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
//					EdamamNlpResponseData edamamNlpResponseData = edamanNlpService.find(ingredient.getText());
//					edamamNlpResponseData.getIngredients();
//					if(edamamNlpResponseData.getIngredients().isEmpty()||!ingredient.getFood().equals(edamamNlpResponseData.getIngredients().get(0).getParsed().get(0).getFoodMatch())){
//						System.out.println("not always match");
//					}
//
//					for(int k = 0; k< edamamNlpResponseData.getIngredients().size();k++){
//						EdamamNlpIngredientOuter outer = edamamNlpResponseData.getIngredients().get(k);
//						String original=outer.getText();
//
//						for(EdamamNlpSingleIngredientInner inner:outer.getParsed()) {
//							String lineOut=original+EdamanIngredientParsingService.csvSeparator+inner.getFoodMatch()+EdamanIngredientParsingService.csvSeparator
//									+inner.getQuantity()+EdamanIngredientParsingService.csvSeparator+inner.getMeasure();
//							System.out.println(lineOut);
//						}
//					}
							ings.add(ingredient.getText());


				}
			}
		}
	//	System.out.println("All amount: "+allCount);
	//	System.out.println("Counted amount: "+count);
		ings.stream().sorted(Comparator.comparingInt(String::length)).forEach(t->System.out.println(t));
		return searchRecipeResponse.toJsonString();

	}




}
