package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.hibernate.parsing.repository.MatchExpectedRepository;
import mariusz.ambroziak.kassistant.logic.matching.ExpectedMatchesService;
import mariusz.ambroziak.kassistant.logic.matching.IngredientProductMatchingService;
import mariusz.ambroziak.kassistant.pojos.matching.InputCases;
import mariusz.ambroziak.kassistant.pojos.matching.MatchingProcessResult;
import mariusz.ambroziak.kassistant.pojos.matching.MatchingProcessResultList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MatchesController {
	@Autowired
	IngredientProductMatchingService matchingService;

	@Autowired
	ExpectedMatchesService expectedMatchesService;

	@Autowired
	MatchExpectedRepository matchExpectedRepository;

	@Autowired
	IngredientProductMatchingService ingredientProductMatchingService;


	@CrossOrigin
	@RequestMapping("/findMatchesForIngredients")
	@ResponseBody
	public MatchingProcessResultList findMatchesForIngredients() throws IOException{

		MatchingProcessResultList calculated=new MatchingProcessResultList(matchingService.parseMatchAndGetResultsFromDbAllCases(true));


		summUpResults(calculated);



		return calculated;
	}


	@CrossOrigin
	@RequestMapping("/checkMatchesFound")
	@ResponseBody
	public MatchingProcessResultList checkMatchesFound() throws IOException{

		MatchingProcessResultList calculated=new MatchingProcessResultList(matchingService.parseMatchAndJudgeResultsFromDbMatches(true));

		summUpResults(calculated);



		return calculated;

	}


	@CrossOrigin
	@RequestMapping("/parseRecipe")
	@ResponseBody
	public MatchingProcessResultList parseRecipe(@RequestParam(value="param", defaultValue="") String param) throws IOException{

		List<MatchingProcessResult> results = ingredientProductMatchingService.parseMatchAndJudgeResultsFromDbMatches(param);
		MatchingProcessResultList calculated=new MatchingProcessResultList(results);

		summUpResults(calculated);
		return calculated;

	}

	private void summUpResults(MatchingProcessResultList calculated) {
		int ingredientsCorrectlyMatched=0;
		int ingredientsCorrectlyGuessedAsEmpty=0;
		int productsTotal=0;
		int productsIncorrectlyMatched=0;
		int productsNotFound=0;
		int productsMatched=0;

		for(MatchingProcessResult mpr:calculated.getResults()){
			long matched=mpr.getProductsConsideredParsingResults().stream()
					.filter(productMatchingResult -> productMatchingResult.isCalculatedVerdict()==productMatchingResult.isExpectedVerdict())
					.count();
			long correctlyFound=mpr.getProductsConsideredParsingResults().stream()
					.filter(productMatchingResult -> productMatchingResult.isCalculatedVerdict()&&productMatchingResult.isExpectedVerdict())
					.count();

			if(correctlyFound>0&&mpr.getIncorrectProductsConsideredParsingResults().isEmpty()&&mpr.getProductNamesNotFound().isEmpty()){
				ingredientsCorrectlyMatched++;
			}
			if(correctlyFound==0&&mpr.getIncorrectProductsConsideredParsingResults().isEmpty()&&mpr.getProductNamesNotFound().isEmpty()){
				ingredientsCorrectlyGuessedAsEmpty++;
			}
			productsMatched+=correctlyFound;
			productsTotal+=mpr.getProductsConsideredParsingResults().size();
			productsIncorrectlyMatched+=mpr.getProductsConsideredParsingResults().stream()
					.filter(productMatchingResult -> productMatchingResult.isCalculatedVerdict()&&!productMatchingResult.isExpectedVerdict())
					.count();
			productsNotFound+=mpr.getProductsConsideredParsingResults().stream()
					.filter(productMatchingResult -> !productMatchingResult.isCalculatedVerdict()&&productMatchingResult.isExpectedVerdict())
					.count()
					+mpr.getProductNamesNotFound().size();
		}
		calculated.setIngredientsCovered(ingredientsCorrectlyMatched);
		calculated.setProductsFound(productsMatched);
		calculated.setIngredientsTotal(calculated.getResults().size());
		calculated.setProductsTotal(productsTotal);
		calculated.setIngredientsCorrectlyGuessedAsEmpty(ingredientsCorrectlyGuessedAsEmpty);
		calculated.setImproperProductsFound(productsIncorrectlyMatched);
		calculated.setProductsMissing(productsNotFound);
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
