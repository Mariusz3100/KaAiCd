package mariusz.ambroziak.kassistant.ai.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import mariusz.ambroziak.kassistant.ai.edamam.nlp.EdamamNlpResponseData;
import mariusz.ambroziak.kassistant.ai.edamam.nlp.EdamanIngredientParsingService;

@RestController
public class EdamaNlpController {
    
	private EdamanIngredientParsingService service;

	@Autowired
	public EdamaNlpController(EdamanIngredientParsingService service) {
		super();
		this.service = service;
	}


	@RequestMapping("/edamanNlpParsing")
    public String edamanNlpParsing(@RequestParam(value="param", defaultValue="") String param){

		EdamamNlpResponseData retValue=this.service.find(param);
    	return retValue.toJsonString();
    	
    }
	
	@ResponseBody
	@RequestMapping("/retrieveEdamanParsingDataFromFileSequentially")
    public String edamanNlpParseAndSave() throws IOException{

		this.service.retrieveEdamanParsingDataFromFileSequentially();;
    	return "Done";
    	
    }

    
	
}
