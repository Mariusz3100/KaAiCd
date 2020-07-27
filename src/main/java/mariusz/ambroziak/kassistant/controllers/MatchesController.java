package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.hibernate.repository.MatchExpectedRepository;
import mariusz.ambroziak.kassistant.logic.matching.ExpectedMatchesService;
import mariusz.ambroziak.kassistant.logic.matching.IngredientProductMatchingService;
import mariusz.ambroziak.kassistant.pojos.matching.InputCases;
import mariusz.ambroziak.kassistant.pojos.matching.MatchingProcessResultList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class MatchesController {
	@Autowired
	IngredientProductMatchingService matchingService;

	@Autowired
	ExpectedMatchesService expectedMatchesService;

	@Autowired
	MatchExpectedRepository matchExpectedRepository;


	@CrossOrigin
	@RequestMapping("/findMatchesForIngredients")
	@ResponseBody
	public MatchingProcessResultList findMatchesForIngredients() throws IOException{

		MatchingProcessResultList retValue=new MatchingProcessResultList(matchingService.parseMatchAndGetResultsFromDbAllCases(true));

		return retValue;

	}


	@CrossOrigin
	@RequestMapping("/checkMatchesFound")
	@ResponseBody
	public MatchingProcessResultList checkMatchesFound() throws IOException{

		MatchingProcessResultList calculated=new MatchingProcessResultList(matchingService.parseMatchAndJudgeResultsFromDbMatches(true));


		return calculated;
//		MatchingProcessResultList retValue=new MatchingProcessResultList(matchingService.parseMatchAndGetResultsFromDbAllCases(true));
//
//		return retValue;

	}

	@ResponseBody
	@RequestMapping("/retrieveMatchesExpectedDataFromFileSequentially")
	public String retrieveMatchesExpectedDataFromFileSequentially() throws IOException{

		this.expectedMatchesService.retrieveMatchesExpectedDataFromFileSequentially();;
		return "Done";

	}

	@ResponseBody
	@RequestMapping("/retrieveAllMatchExpectedAndIngredientData")
	public String retrieveAllMatchExpectedAndIngredientData() throws IOException{

		this.expectedMatchesService.retrieveAllMatchExpectedAndIngredientData();;
		return "Done";

	}

	@CrossOrigin
	@ResponseBody
	@RequestMapping("/retrieveInputCases")
	public InputCases retrieveInputCases() throws IOException{
		InputCases inputCases = this.matchingService.retrieveAllIngredientsProductsAndMatchesExpectedConsidered();

		return inputCases;


	}

}
