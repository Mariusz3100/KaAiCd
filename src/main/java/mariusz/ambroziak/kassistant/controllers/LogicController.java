package mariusz.ambroziak.kassistant.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.hibernate.cache.repositories.WebknoxResponseRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.edamam.recipes.*;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoFromFileService;
import mariusz.ambroziak.kassistant.webclients.webknox.RecipeSearchApiClient;
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
	RecipeSearchApiClient recipeSearchApiClient;

	@Autowired
	WebknoxResponseRepository webknoxResponseRepository;


	@Autowired
	public LogicController(IngredientPhraseParser ingredientParser,EdamanRecipeSearchService searchSevice, EdamanIngredientParsingService edamanNlpService) {
		super();
		this.ingredientParser = ingredientParser;
		this.searchSevice=searchSevice;
		this.edamanNlpService=edamanNlpService;

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


}
