package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.model.*;
import mariusz.ambroziak.kassistant.hibernate.repository.ParsingBatchRepository;
import mariusz.ambroziak.kassistant.logic.AbstractParser;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.logic.shops.ShopProductParser;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.*;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class IngredientProductMatchingService extends AbstractParser {

	@Autowired
	IngredientPhraseParser ingredientParser;
	@Autowired
	ShopProductParser productParser;

	@Autowired
	ParsingBatchRepository parsingBatchRepository;





	public List<MatchingProcessResult> parseMatchAndSaveToDb(){
		List<IngredientLearningCase> ingredientLearningCasesFromDb = this.ingredientParser.getIngredientLearningCasesFromDb();
		List<MatchingProcessResult> retValue=new ArrayList<>();
		ParsingBatch batchObject=new ParsingBatch();
		parsingBatchRepository.save(batchObject);

		for(IngredientLearningCase er:ingredientLearningCasesFromDb) {
			IngredientPhraseParsingProcessObject parsingAPhrase = this.ingredientParser.processSingleCase(er);

			ParsingResult singleResult = this.ingredientParser.createResultObject(parsingAPhrase);
			IngredientPhraseParsingResult ingredientPhraseParsingResult = this.ingredientParser.saveResultAndPhrasesInDb(parsingAPhrase, batchObject);

			MatchingProcessResult match=new MatchingProcessResult();


			match.setIngredientParsingDetails(singleResult);
			String markedWords=singleResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));
			List<ProductParsingProcessObject> parsingResultList = this.productParser.tescoSearchForParsings(markedWords);

			for(ProductParsingProcessObject pr:parsingResultList){
				this.productParser.parseProductParsingObjectWithNamesComparison(pr);
				ParsingResult ppr = this.productParser.createResultObject(pr);
				ProductMatchingResult pmr=new ProductMatchingResult(ppr);


				List<String> ingredientWordsMarked=singleResult.getRestrictivelyCalculatedResult().getMarkedWords();
				List<String> productWordsMarked=ppr.getRestrictivelyCalculatedResult().getMarkedWords();
				List<String> matched=new ArrayList<>();
				List<String> ingredientSurplus=new ArrayList<>();
				List<String> productSurplus=new ArrayList<>();



				for(String x:ingredientWordsMarked){
					if(productWordsMarked.stream().filter(word->word.equals(x)).findAny().isPresent()){
						matched.add(x);
					}else {
						ingredientSurplus.add(x);
					}

				}
				productSurplus=productWordsMarked.stream().filter(s->!matched.contains(s)).collect(Collectors.toList());


				CalculatedResults cr=new CalculatedResults(ingredientSurplus,matched,productSurplus,matched);
				pmr.setWordsMatching(cr);
				pmr.setVerdict(ingredientSurplus.isEmpty()&&productSurplus.isEmpty());

				match.addProductsConsideredParsingResults(pmr);

				ProductParsingResult productParsingResult = this.productParser.saveResultInDb(pr, batchObject);

			}

			retValue.add(match);

		}
		return retValue;
	}
//	private ParsingResult createProductResultObject(ProductParsingProcessObject parsingAPhrase) {
//		ParsingResult object=new ParsingResult();
//		object.setOriginalPhrase(parsingAPhrase.getProduct().getName());
//		String fused=parsingAPhrase.getEntities()==null||parsingAPhrase.getEntities().getEntities()==null?"":parsingAPhrase.getEntities().getEntities().stream().map(s->s.getText()).collect( Collectors.joining("<br>") );
//
//		object.setEntities(fused);
//		object.setEntityLess(parsingAPhrase.getEntitylessString());
//		object.setTokens(parsingAPhrase.getFinalResults());
//		String expected=parsingAPhrase.getMinimalExpectedWords().stream().collect(Collectors.joining(" "));
//		IngredientLearningCase lp=new IngredientLearningCase(parsingAPhrase.getOriginalPhrase(),0,"empty",expected,parsingAPhrase.getExpectedType());
//		object.setExpectedResult(lp);
//		object.setProductTypeFound(parsingAPhrase.getFoodTypeClassified().toString());
//		object.setRestrictivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getMinimalExpectedWords(),parsingAPhrase.getFinalResults()));
//		object.setPermisivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getMinimalExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));
//
//		object.setRestrictivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getExtendedExpectedWords(),parsingAPhrase.getFinalResults()));
//		object.setPermisivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getExtendedExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));
//
//		object.setBrand(parsingAPhrase.getProduct().getBrand());
//		object.setBrandless(parsingAPhrase.getBrandlessPhrase());
//
//		if(parsingAPhrase.getProduct() instanceof Tesco_Product){
//			String secondName="";
//			Tesco_Product product = (Tesco_Product) parsingAPhrase.getProduct();
//			secondName= product.getSearchApiName();
//			object.setAlternateName(secondName);
//
//			object.setIngredientPhrase(product.getIngredients());
//		}
//
//		object.setDescriptionPhrase(parsingAPhrase.getProduct().getDescription());
//
//		object.setInitialNames(parsingAPhrase.getInitialNames());
//
//		object.setQuantitylessPhrase(parsingAPhrase.getQuantitylessPhrase());
//		object.setFinalNames(parsingAPhrase.getFinalNames());
//
//		return object;
//	}

//	private ParsingResult createIngredientResultObject(IngredientPhraseParsingProcessObject parsingAPhrase) {
//		ParsingResult object=new ParsingResult();
//		object.setOriginalPhrase(parsingAPhrase.getLearningTuple().getOriginalPhrase());
//		List<QualifiedToken> primaryResults = parsingAPhrase.getFinalResults();
//		object.setTokens(primaryResults);
//
//		String fused=parsingAPhrase.getCardinalEntities().stream().map(s->s.getText()).collect( Collectors.joining(" ") );
//
//
//		object.setEntities(fused);
//		object.setEntityLess(parsingAPhrase.getEntitylessString());
//		object.setCorrectedPhrase(parsingAPhrase.createCorrectedPhrase());
//		//	object.setCorrectedTokens(parsingAPhrase.getCorrectedtokens());
//		object.setExpectedResult(parsingAPhrase.getLearningTuple());
//		object.setRestrictivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getLearningTuple().getFoodMatch(),parsingAPhrase.getFinalResults()));
//		object.setPermisivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getLearningTuple().getFoodMatch(),parsingAPhrase.getPermissiveFinalResults()));
//		object.setProductTypeFound(parsingAPhrase.getFoodTypeClassified()==null? ProductType.unknown.name():parsingAPhrase.getFoodTypeClassified().name());
//		object.setCorrectedConnotations(parsingAPhrase.getCorrectedConotations());
//		object.setOriginalConnotations(parsingAPhrase.getFromEntityLessConotations());
//		object.setAdjacentyConotationsFound(parsingAPhrase.getAdjacentyConotationsFound());
//		object.setDependencyConotationsFound(parsingAPhrase.getDependencyConotationsFound());
//
//
//		object.setQuantitylessPhrase(parsingAPhrase.getQuantitylessPhrase());
//
//
//		return object;
//	}



}
