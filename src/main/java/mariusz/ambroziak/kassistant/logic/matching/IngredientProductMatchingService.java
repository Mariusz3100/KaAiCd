package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientLearningCase;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class IngredientProductMatchingService {

	@Autowired
	IngredientPhraseParser ingredientParser;
	@Autowired
	ShopProductParser productParser;



	public List<MatchingProcessResult> parseAndMatch(){
		List<IngredientLearningCase> ingredientLearningCasesFromDb = this.ingredientParser.getIngredientLearningCasesFromDb();
		List<MatchingProcessResult> retValue=new ArrayList<>();


		for(IngredientLearningCase er:ingredientLearningCasesFromDb) {
			IngredientPhraseParsingProcessObject parsingAPhrase = this.ingredientParser.processSingleCase(er);

			ParsingResult singleResult = createIngredientResultObject(parsingAPhrase);

			MatchingProcessResult match=new MatchingProcessResult();


			match.setIngredientParsingDetails(singleResult);
			String markedWords=singleResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));
			List<ProductParsingProcessObject> parsingResultList = this.productParser.tescoSearchForParsings(markedWords);

			for(ProductParsingProcessObject pr:parsingResultList){
				this.productParser.parseProductParsingObjectWithNamesComparison(pr);
				ParsingResult ppr=createProductResultObject(pr);
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
			}

			retValue.add(match);

		}
		return retValue;
	}

	private ParsingResult createProductResultObject(ProductParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getProduct().getName());
		String fused=parsingAPhrase.getEntities()==null||parsingAPhrase.getEntities().getEntities()==null?"":parsingAPhrase.getEntities().getEntities().stream().map(s->s.getText()).collect( Collectors.joining("<br>") );

		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());
		object.setTokens(parsingAPhrase.getFinalResults());
		String expected=parsingAPhrase.getMinimalExpectedWords().stream().collect(Collectors.joining(" "));
		IngredientLearningCase lp=new IngredientLearningCase(parsingAPhrase.getOriginalPhrase(),0,"empty",expected,parsingAPhrase.getExpectedType());
		object.setExpectedResult(lp);
		object.setProductTypeFound(parsingAPhrase.getFoodTypeClassified().toString());
		object.setRestrictivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getMinimalExpectedWords(),parsingAPhrase.getFinalResults()));
		object.setPermisivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getMinimalExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));

		object.setRestrictivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getExtendedExpectedWords(),parsingAPhrase.getFinalResults()));
		object.setPermisivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getExtendedExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));

		object.setBrand(parsingAPhrase.getProduct().getBrand());
		object.setBrandless(parsingAPhrase.getBrandlessPhrase());

		if(parsingAPhrase.getProduct() instanceof Tesco_Product){
			String secondName="";
			Tesco_Product product = (Tesco_Product) parsingAPhrase.getProduct();
			secondName= product.getSearchApiName();
			object.setAlternateName(secondName);

			object.setIngredientPhrase(product.getIngredients());
		}

		object.setDescriptionPhrase(parsingAPhrase.getProduct().getDescription());

		object.setInitialNames(parsingAPhrase.getInitialNames());

		object.setQuantitylessPhrase(parsingAPhrase.getQuantitylessPhrase());
		object.setFinalNames(parsingAPhrase.getFinalNames());

		return object;
	}

	private ParsingResult createIngredientResultObject(IngredientPhraseParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getLearningTuple().getOriginalPhrase());
		List<QualifiedToken> primaryResults = parsingAPhrase.getFinalResults();
		object.setTokens(primaryResults);

		String fused=parsingAPhrase.getCardinalEntities().stream().map(s->s.getText()).collect( Collectors.joining(" ") );


		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());
		object.setCorrectedPhrase(parsingAPhrase.createCorrectedPhrase());
		//	object.setCorrectedTokens(parsingAPhrase.getCorrectedtokens());
		object.setExpectedResult(parsingAPhrase.getLearningTuple());
		object.setRestrictivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getLearningTuple().getFoodMatch(),parsingAPhrase.getFinalResults()));
		object.setPermisivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getLearningTuple().getFoodMatch(),parsingAPhrase.getPermissiveFinalResults()));
		object.setProductTypeFound(parsingAPhrase.getFoodTypeClassified()==null? ProductType.unknown.name():parsingAPhrase.getFoodTypeClassified().name());
		object.setCorrectedConnotations(parsingAPhrase.getCorrectedConotations());
		object.setOriginalConnotations(parsingAPhrase.getFromEntityLessConotations());
		object.setAdjacentyConotationsFound(parsingAPhrase.getAdjacentyConotationsFound());
		object.setDependencyConotationsFound(parsingAPhrase.getDependencyConotationsFound());


		object.setQuantitylessPhrase(parsingAPhrase.getQuantitylessPhrase());


		return object;
	}


	private CalculatedResults calculateWordsFound(String expected, List<QualifiedToken> finalResults) {
		List<String> found=new ArrayList<String>();
		List<String> mistakenlyFound=new ArrayList<String>();

		for(QualifiedToken qt:finalResults) {
			if(qt.getWordType()== WordType.ProductElement) {
				if(expected.contains(qt.getText())) {
					found.add(qt.getText());
					expected=expected.replaceAll(qt.getText(), "").replaceAll("  ", " ");
				}else {
					mistakenlyFound.add(qt.getText());
				}
			}
		}

		List<String> notFound=Arrays.asList(expected.split(" "));
		List<String> wordsMarked=finalResults.stream().filter(qualifiedToken -> qualifiedToken.getWordType()==WordType.ProductElement).map(qualifiedToken -> qualifiedToken.getText()).collect(Collectors.toList());

		return new CalculatedResults(notFound,found,mistakenlyFound,wordsMarked);
	}

	private CalculatedResults calculateWordsFound(List<String> expected, List<QualifiedToken> finalResults) {
		List<String> found=new ArrayList<String>();
		List<String> mistakenlyFound=new ArrayList<String>();

		for(QualifiedToken qt:finalResults) {
			String qtText = qt.getText().toLowerCase();
			String qtLemma=qt.getLemma();
			if(qt.getWordType()== WordType.ProductElement) {
				if(expected.contains(qtText)||expected.contains(qtLemma)) {
					if(expected.contains(qtText)){
						found.add(qtText);
						expected=expected.stream().filter(s->!s.equals(qtText)).collect(Collectors.toList());
					}else {
						found.add(qtLemma);
						expected = expected.stream().filter(s -> !s.equals(qtLemma)).collect(Collectors.toList());
					}
				}else {
					mistakenlyFound.add(qtText);
				}
			}
		}

		List<String> wordsMarked=finalResults.stream().filter(qualifiedToken -> qualifiedToken.getWordType()==WordType.ProductElement).map(qualifiedToken -> qualifiedToken.getText()).collect(Collectors.toList());

		return new CalculatedResults(expected,found,mistakenlyFound,wordsMarked);
	}

}
