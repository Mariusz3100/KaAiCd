package mariusz.ambroziak.kassistant.ai.controllers;

import org.springframework.web.bind.annotation.RestController;

import mariusz.ambroziak.kassistant.ai.logic.IngredientPhraseTokenizerTest;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class TestController {
    
	TokenizationClientService tokenizationService;
	NamedEntityRecognitionClientService nerService;
	
	IngredientPhraseTokenizerTest testTokenizerService;


	@Autowired
	public TestController(TokenizationClientService tokenizationService, NamedEntityRecognitionClientService nerService,
			IngredientPhraseTokenizerTest testTokenizerService) {
		super();
		this.tokenizationService = tokenizationService;
		this.nerService = nerService;
		this.testTokenizerService = testTokenizerService;
	}
	
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
