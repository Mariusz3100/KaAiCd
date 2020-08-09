package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.hibernate.parsing.repository.IngredientPhraseParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.ProductParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.TescoProductRepository;
import mariusz.ambroziak.kassistant.logic.IngredientPhraseTokenizerTest;
import mariusz.ambroziak.kassistant.webclients.morrisons.MorrisonsClientService;
import mariusz.ambroziak.kassistant.webclients.morrisons.Morrisons_Product;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
