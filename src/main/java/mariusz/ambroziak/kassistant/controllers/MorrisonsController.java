package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.enums.AmountTypes;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.*;
import mariusz.ambroziak.kassistant.hibernate.repository.*;
import mariusz.ambroziak.kassistant.logic.IngredientPhraseTokenizerTest;
import mariusz.ambroziak.kassistant.logic.PhraseDependenciesComparator;
import mariusz.ambroziak.kassistant.webclients.morrisons.MorrisonsClientService;
import mariusz.ambroziak.kassistant.webclients.morrisons.Morrisons_Product;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NerResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaApiClient;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class MorrisonsController {

	TokenizationClientService tokenizationService;
	NamedEntityRecognitionClientService nerService;

	ProductParsingResultRepository productParsingRepo;
	IngredientPhraseParsingResultRepository ingredientParsingRepo;
	IngredientPhraseTokenizerTest testTokenizerService;
	TescoProductRepository tescoProductRepository;




	@Autowired
	MorrisonsClientService morrisonsClientService;




	public MorrisonsController(TokenizationClientService tokenizationService, NamedEntityRecognitionClientService nerService,
                               ProductParsingResultRepository productParsingRepo, IngredientPhraseParsingResultRepository ingredientParsingRepo,
                               IngredientPhraseTokenizerTest testTokenizerService, TescoProductRepository tescoProductRepository) {
		this.tokenizationService = tokenizationService;
		this.nerService = nerService;
		this.productParsingRepo = productParsingRepo;
		this.ingredientParsingRepo = ingredientParsingRepo;
		this.testTokenizerService = testTokenizerService;
		this.tescoProductRepository = tescoProductRepository;
	}



	@RequestMapping("/saveMorrisonsProductsFor")
	public String saveMorrisonsProductsFor(@RequestParam(value="param", defaultValue="empty") String param){
		List<Morrisons_Product>  products = this.morrisonsClientService.searchInDbAndApiFor(param);

		return ""+products.size();
	}

	@RequestMapping("/saveAllProxiedProducts")
	public String saveAllProxiedProducts(){
		List<Morrisons_Product>  products = this.morrisonsClientService.saveInDbAllCachedProducts();

		return ""+products.size();
	}



}
