package mariusz.ambroziak.kassistant.ai.controllers;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.shops.ShopProductParser;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.ai.tesco.TescoApiClientService;
import mariusz.ambroziak.kassistant.ai.tesco.Tesco_Product;

@RestController
public class TescoController {

	
	@Autowired
	TescoApiClientService tescoService;
	@Autowired
	ShopProductParser productParserService;
	
	




	public TescoController(TescoApiClientService tescoService, ShopProductParser productParserService) {
		super();
		this.tescoService = tescoService;
		this.productParserService = productParserService;
	}

	@RequestMapping("/testTesco")
    public String testTesco(@RequestParam(value="param", defaultValue="empty") String param){
    	ArrayList<Tesco_Product> retValue=this.tescoService.getProduktsFor(param);
    	return retValue.toString();
    	
    }
	
	@CrossOrigin
	@ResponseBody
	@RequestMapping("/tescoSearchAndParse")
    public ParsingResultList tescoSearchAndParse(@RequestParam(value="param", defaultValue="empty") String param) throws IOException{
    	ParsingResultList retValue=this.productParserService.categorizeProducts(param);
    	return retValue;
    	
    }
	@CrossOrigin
	@ResponseBody
	@RequestMapping("/tescoParseFromFile")
    public ParsingResultList tescoParseFromFile() throws IOException{
    	ParsingResultList retValue=this.productParserService.parseFromFile();
    	return retValue;
    	
	}
	@CrossOrigin
	@ResponseBody
	@RequestMapping("/tescoGetResults")
    public String tescoGetResults(@RequestParam(value="param", defaultValue="empty") String param) throws IOException{
    	
		this.productParserService.tescoGetResults(param);
		return "done";
    	
    }
}
