package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.enums.AmountTypes;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientPhraseParsingResult;
import mariusz.ambroziak.kassistant.hibernate.model.ProductParsingResult;
import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ProductParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.TescoProductRepository;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaApiClient;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import org.springframework.web.bind.annotation.RestController;

import mariusz.ambroziak.kassistant.logic.IngredientPhraseTokenizerTest;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NerResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class TestController {
    
	TokenizationClientService tokenizationService;
	NamedEntityRecognitionClientService nerService;

	ProductParsingResultRepository productParsingRepo;
	IngredientPhraseParsingResultRepository ingredientParsingRepo;
	IngredientPhraseTokenizerTest testTokenizerService;
	TescoProductRepository tescoProductRepository;

	@Autowired
	UsdaApiClient usdaApiClient;

	public TestController(TokenizationClientService tokenizationService, NamedEntityRecognitionClientService nerService,
						  ProductParsingResultRepository productParsingRepo, IngredientPhraseParsingResultRepository ingredientParsingRepo,
						  IngredientPhraseTokenizerTest testTokenizerService, TescoProductRepository tescoProductRepository) {
		this.tokenizationService = tokenizationService;
		this.nerService = nerService;
		this.productParsingRepo = productParsingRepo;
		this.ingredientParsingRepo = ingredientParsingRepo;
		this.testTokenizerService = testTokenizerService;
		this.tescoProductRepository = tescoProductRepository;
	}


//	@Autowired
//	public TestController(TokenizationClientService tokenizationService, NamedEntityRecognitionClientService nerService,
//			IngredientPhraseTokenizerTest testTokenizerService) {
//		super();
//		this.tokenizationService = tokenizationService;
//		this.nerService = nerService;
//		this.testTokenizerService = testTokenizerService;
//	}
	
	@RequestMapping("/springTokenize")
    public String springTokenize(@RequestParam(value="param", defaultValue="empty") String param){
    	TokenizationResults retValue=this.tokenizationService.parse(param);
    	return retValue.toString();
    	
    }
	
	@RequestMapping("/springNer")
    public String springNer(@RequestParam(value="param", defaultValue="empty") String param){
    	NerResults retValue=this.nerService.find(param);
    	return retValue.toString();
    }

	@RequestMapping("/testIngDb")
	public String testIngDb(@RequestParam(value="param", defaultValue="empty") String param){
		IngredientPhraseParsingResult x=new IngredientPhraseParsingResult("test",12, AmountTypes.pcs, ProductType.fresh,"test","Test",ProductType.unknown);
		this.ingredientParsingRepo.save(x);
		return "Done";
	}

	@RequestMapping("/testProdDb")
	public String testProdDb(@RequestParam(value="param", defaultValue="empty") String param){
		Tesco_Product tesco_product = tescoProductRepository.findAll().get(0);
		ProductParsingResult x=new ProductParsingResult(tesco_product,"Test","test","Test",ProductType.unknown);
		this.productParsingRepo.save(x);
		return "Done";
	}

	@RequestMapping("/testUsda")
	public String testUsda(@RequestParam(value="param", defaultValue="empty") String param){
		UsdaResponse inApi = this.usdaApiClient.findInApi("tomato paste", 10);

		return inApi.toJsonString();
	}

	@CrossOrigin
	@RequestMapping("/springTokenizerFromFile")
	@ResponseBody
	public List<TokenizationResults> phrasesParsing() throws IOException{
		Map<String, TokenizationResults> parseFromFile = this.testTokenizerService.parseFromFile();
		
		System.out.println();
		ArrayList<TokenizationResults> retValue=new ArrayList<TokenizationResults>();
		retValue.addAll(parseFromFile.values());
		
		for(String x:parseFromFile.keySet()) {
			String resultLine=x+" : ";
			
			for(Token r:parseFromFile.get(x).getTokens()) {
				resultLine+=r.getText()+" | ";
			}
			
			System.out.println(resultLine);
		}
		return retValue;

	}
}
