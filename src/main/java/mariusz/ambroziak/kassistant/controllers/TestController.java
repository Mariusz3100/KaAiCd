package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.enums.AmountTypes;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientPhraseParsingResult;
import mariusz.ambroziak.kassistant.hibernate.model.ParsingBatch;
import mariusz.ambroziak.kassistant.hibernate.model.PhraseFound;
import mariusz.ambroziak.kassistant.hibernate.model.ProductParsingResult;
import mariusz.ambroziak.kassistant.hibernate.repository.*;
import mariusz.ambroziak.kassistant.logic.PhraseDependenciesComparator;
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
	CustomPhraseFoundRepository phraseFoundRepo;
	@Autowired
	UsdaApiClient usdaApiClient;

	@Autowired
	PhraseDependenciesComparator phraseDependenciesComparator;

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
		x.setParsingBatch(new ParsingBatch());
		this.ingredientParsingRepo.save(x);
		return "Done";
	}
	@RequestMapping("/testIngWithPhraseDb")
	public String testIngWithPhraseDb(@RequestParam(value="param", defaultValue="empty") String param){
		IngredientPhraseParsingResult x=new IngredientPhraseParsingResult("test",12, AmountTypes.pcs, ProductType.fresh,"test","Test",ProductType.unknown);
		x.setParsingBatch(new ParsingBatch());

		PhraseFound phrase=new PhraseFound("test", WordType.ProductElement,"test",x,null);

		this.ingredientParsingRepo.save(x);
		this.phraseFoundRepo.saveIfNew(phrase);
		return "Done";
	}

	@RequestMapping("/testFindingPhrases")
	public String testFindingPhrases(@RequestParam(value="param", defaultValue="empty") String param){
		List<PhraseFound> flax1 = this.phraseFoundRepo.findBySingleWordPhrase("flax");
		List<PhraseFound> flax2 = this.phraseFoundRepo.findByPhraseContaining("flax");


		return "Done";
	}



	@RequestMapping("/testProdDb")
	public String testProdDb(@RequestParam(value="param", defaultValue="empty") String param){
		Tesco_Product tesco_product = tescoProductRepository.findAll().get(0);
		ProductParsingResult x=new ProductParsingResult(tesco_product,"Test","test","Test",ProductType.unknown);
		x.setParsingBatch(new ParsingBatch());

		this.productParsingRepo.save(x);
		return "Done";
	}

	@RequestMapping("/testUsda")
	public String testUsda(@RequestParam(value="param", defaultValue="empty") String param){
		UsdaResponse inApi = this.usdaApiClient.findInApi(param, 10);

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



	@CrossOrigin
	@RequestMapping("/testPhraseDependenciesComparator")
	@ResponseBody
	public String testPhraseDependenciesComparator() throws IOException{
		String first1="heavy cream";
		String second1="heavy cream";

		String first2="fresh lime juice";
		String second2="lime juice";

		String first3="chopped onion";
		String second3="onion, chopped";

		String retValue="";

	//	retValue+=first1+":"+second1+":"+phraseDependenciesComparator.comparePhrases(first1,second1)+"<br>";
	//	retValue+=first2+":"+second2+":"+phraseDependenciesComparator.comparePhrases(first2,second2)+"<br>";
		retValue+=first3+":"+second3+":"+phraseDependenciesComparator.comparePhrases(first3,second3)+"<br>";

		return retValue;


	}



}
