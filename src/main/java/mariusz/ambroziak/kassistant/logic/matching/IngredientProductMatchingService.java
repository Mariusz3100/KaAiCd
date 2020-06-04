package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.constants.NlpConstants;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientPhraseParsingResult;
import mariusz.ambroziak.kassistant.hibernate.model.ParsingBatch;
import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseLearningCaseRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ParsingBatchRepository;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientWordsClasifier;
import mariusz.ambroziak.kassistant.logic.shops.ShopProductParser;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.CalculatedResults;
import mariusz.ambroziak.kassistant.pojos.parsing.MatchingProcessResult;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResult;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResultList;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntity;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NerResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.*;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

			ParsingResult singleResult = createResultObject(parsingAPhrase);

			MatchingProcessResult match=new MatchingProcessResult();


			match.setIngredientParsingDetails(singleResult);
			retValue.add(match);

		}
		return retValue;
	}


	private ParsingResult createResultObject(IngredientPhraseParsingProcessObject parsingAPhrase) {
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



}
