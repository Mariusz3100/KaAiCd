package mariusz.ambroziak.kassistant.controllers;

import java.io.IOException;
import java.util.ArrayList;

import mariusz.ambroziak.kassistant.inputs.TescoDetailsTestCases;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import mariusz.ambroziak.kassistant.pojos.ParsingResultList;
import mariusz.ambroziak.kassistant.logic.shops.ShopProductParser;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoApiClientService;

@RestController
public class TescoController {

	
	@Autowired
	TescoApiClientService tescoService;
	@Autowired
	ShopProductParser productParserService;
	
	@Autowired
	TescoDetailsTestCases testCasesManager;






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


	@CrossOrigin
	@ResponseBody
	@RequestMapping("/saveTestCases")
	public String saveTestCases(@RequestParam(value="param", defaultValue="empty") String param) throws IOException{

		this.testCasesManager.copyTestCasesFromFileToDb();
		return "done";

	}
}
