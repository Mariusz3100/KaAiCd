package mariusz.ambroziak.kassistant.logic.ingredients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientPhraseParsingResult;
import mariusz.ambroziak.kassistant.hibernate.model.ParsingBatch;
import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseLearningCaseRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ParsingBatchRepository;
import mariusz.ambroziak.kassistant.pojos.parsing.CalculatedResults;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResult;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResultList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.constants.NlpConstants;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientLearningCase;

import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntity;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NerResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.DependencyTreeNode;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;


@Service
public class IngredientPhraseParser {
	private TokenizationClientService tokenizator;
	@Autowired
	private NamedEntityRecognitionClientService nerRecognizer;
	private ResourceLoader resourceLoader;

	private EdamanIngredientParsingService edamanNlpParsingService;
	@Autowired
	private IngredientWordsClasifier wordClasifier;

	@Autowired
	private IngredientPhraseLearningCaseRepository ingredientPhraseLearningCaseRepository;

	@Autowired
	private IngredientPhraseParsingResultRepository ingredientParsingRepo;

	@Autowired
	private ParsingBatchRepository parsingBatchRepository;

	private final String csvSeparator=";";
	private final String wordSeparator=",";



	private String spacelessRegex="(\\d+)(\\w+)";





	public IngredientPhraseParser(TokenizationClientService tokenizator,
			NamedEntityRecognitionClientService nerRecognizer, ResourceLoader resourceLoader,
			EdamanIngredientParsingService edamanNlpParsingService,
			IngredientWordsClasifier wordClasifier) {
		super();
		this.tokenizator = tokenizator;
		this.nerRecognizer = nerRecognizer;
		this.resourceLoader = resourceLoader;
		this.edamanNlpParsingService = edamanNlpParsingService;
		this.wordClasifier = wordClasifier;
	}


	public ParsingResultList parseIngredientLines(List<String> lines) throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<IngredientLearningCase> inputLines= edamanNlpParsingService.retrieveDataFromFile();
		for(IngredientLearningCase er:inputLines) {
			String line=correctErrors(er.getOriginalPhrase());
			er.setOriginalPhrase(line);
			IngredientPhraseParsingProcessObject parsingAPhrase=new IngredientPhraseParsingProcessObject(er);



			this.wordClasifier.calculateWordTypesForWholePhrase(parsingAPhrase);
			initializeCorrectedConnotations(parsingAPhrase);
//			initializeProductPhraseConnotations(parsingAPhrase);

			ParsingResult singleResult = createResultObject(parsingAPhrase);

			retValue.addResult(singleResult);


		}

		printResults(retValue);

		return retValue;

	}


	public ParsingResultList parseFromDb() throws IOException {
		ParsingResultList retValue=new ParsingResultList();
		List<IngredientLearningCase> inputLines = getIngredientLearningCasesFromDb();
		ParsingBatch batchObject=new ParsingBatch();
		parsingBatchRepository.save(batchObject);



		for(IngredientLearningCase er:inputLines) {
			IngredientPhraseParsingProcessObject parsingAPhrase = processSingleCase(er);

			ParsingResult singleResult = createResultObject(parsingAPhrase);
			saveResultInDb(parsingAPhrase,batchObject);
			retValue.addResult(singleResult);


		}

	//	printResults(retValue);

		return retValue;

	}

	public  List<IngredientLearningCase> getIngredientLearningCasesFromDb() {
		List<IngredientLearningCase> inputLines= new ArrayList<>();//edamanNlpParsingService.retrieveDataFromFile();
		this.ingredientPhraseLearningCaseRepository.findAll().forEach(e->inputLines.add(e));
		return inputLines;
	}

	public ParsingResultList parseFromFile() throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		ParsingBatch batchObject=new ParsingBatch();
		parsingBatchRepository.save(batchObject);
		List<IngredientLearningCase> inputLines= edamanNlpParsingService.retrieveDataFromFile();
		for(IngredientLearningCase er:inputLines) {
			IngredientPhraseParsingProcessObject parsingAPhrase = processSingleCase(er);

			ParsingResult singleResult = createResultObject(parsingAPhrase);
			saveResultInDb(parsingAPhrase,batchObject);
			retValue.addResult(singleResult);


		}

		printResults(retValue);

		return retValue;

	}

	public IngredientPhraseParsingProcessObject processSingleCase(IngredientLearningCase er) {
		String line=correctErrors(er.getOriginalPhrase());
		er.setOriginalPhrase(line);
		IngredientPhraseParsingProcessObject parsingAPhrase=new IngredientPhraseParsingProcessObject(er);
		handleBracketsAndSetBracketLess(parsingAPhrase);

		NerResults entitiesFound = this.nerRecognizer.find(line);
		parsingAPhrase.setEntities(entitiesFound);

		String entitylessString=parsingAPhrase.calculateEntitylessString(parsingAPhrase.getBracketLessPhrase());

		TokenizationResults tokens = this.tokenizator.parse(entitylessString);
		parsingAPhrase.setEntitylessTokenized(tokens);

	//	initializePrimaryConnotations(parsingAPhrase);


		this.wordClasifier.calculateWordTypesForWholePhrase(parsingAPhrase);
	//	initializeCorrectedConnotations(parsingAPhrase);
	//	initializeProductPhraseConnotations(parsingAPhrase);
		return parsingAPhrase;
	}

	private void printResults(ParsingResultList retValue) {
		for (ParsingResult result : retValue.getResults()) {
			String line = result.getOriginalPhrase() + csvSeparator +
					result.getExpectedResult().getFoodMatch() + csvSeparator;

			String restrictive = "";
			for (String x : result.getRestrictivelyCalculatedResult().getMarkedWords()) {
				if (!restrictive.isEmpty())
					restrictive += wordSeparator;
				restrictive += x;
			}

			String permissive = "";
			for (String x : result.getRestrictivelyCalculatedResult().getMarkedWords()) {
				if (!permissive.isEmpty())
					permissive += wordSeparator;
				permissive += x;
			}
			line += permissive + csvSeparator + restrictive;
			System.out.println(line);
		}
	}

	public IngredientPhraseParsingResult saveResultInDb(IngredientPhraseParsingProcessObject parsingAPhrase, ParsingBatch batchObject) {

		IngredientPhraseParsingResult toSave=new IngredientPhraseParsingResult();
		toSave.setOriginalName(parsingAPhrase.getLearningTuple().getOriginalPhrase());
		toSave.setExtendedResultsCalculated(parsingAPhrase.getPermissiveFinalResultsString());
		toSave.setMinimalResultsCalculated(parsingAPhrase.getFinalResultsString());
		toSave.setTypeCalculated(parsingAPhrase.getFoodTypeClassified());
		toSave.setedamamAmount(parsingAPhrase.getLearningTuple().getAmount());
		toSave.setedamamAmountType(parsingAPhrase.getLearningTuple().getAmountType());
		toSave.setedamamProductType(parsingAPhrase.getLearningTuple().getFoodTypeCategory());
		toSave.setMinimalResultsCalculated(parsingAPhrase.getFinalResultsString());
		toSave.setExtendedResultsCalculated(parsingAPhrase.getPermissiveFinalResultsString());

		toSave.setParsingBatch(batchObject);

		this.ingredientParsingRepo.save(toSave);
		return toSave;
	}


	private void handleBracketsAndSetBracketLess(IngredientPhraseParsingProcessObject parsingAPhrase) {
		String x=parsingAPhrase.getLearningTuple().getOriginalPhrase();
		if(x==null)
			parsingAPhrase.setBracketLessPhrase("");
		else {
			x=x.replaceAll("\\(.*\\)","");
			parsingAPhrase.setBracketLessPhrase(x);
		}



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




	private void initializeCorrectedConnotations(IngredientPhraseParsingProcessObject parsingAPhrase) {
		
		TokenizationResults tokenized = parsingAPhrase.getCorrectedToknized();
		DependencyTreeNode dependencyTreeRoot = tokenized.getDependencyTree();
		List<ConnectionEntry> correctedConotations = tokenized.getAllTwoWordDependencies();
		Token foundToken = tokenized.findToken(tokenized.getTokens(),dependencyTreeRoot==null?"":dependencyTreeRoot.getText());
		correctedConotations.add(new ConnectionEntry(new Token("ROOT","",""),foundToken));

		
		parsingAPhrase.setCorrectedConotations(correctedConotations);


	}

	private void initializeProductPhraseConnotations(IngredientPhraseParsingProcessObject parsingAPhrase) {

		TokenizationResults tokenized = parsingAPhrase.getQuantitylessTokenized();
		DependencyTreeNode dependencyTreeRoot = tokenized.getDependencyTree();
		List<ConnectionEntry> quantitylessConnotations = tokenized.getAllTwoWordDependencies();
		Token foundToken = tokenized.findToken(tokenized.getTokens(),dependencyTreeRoot==null?"":dependencyTreeRoot.getText());
		quantitylessConnotations.add(new ConnectionEntry(new Token("ROOT","",""),foundToken));

		parsingAPhrase.setQuantitylessConnotations(quantitylessConnotations);


	}



	private List<ConnectionEntry> extractCorrectedConnotations(IngredientPhraseParsingProcessObject parsingAPhrase) {
		
		return parsingAPhrase.getCorrectedToknized().getAllTwoWordDependencies();
		
	}

	private List<List<Token>> extractPrimarilyFoundConnotations(List<QualifiedToken> primaryResults) {
		List<List<Token>> originalConotations=new ArrayList<List<Token>>();

		for(int i=0;i<primaryResults.size();i++) {
			List<Token> entryFromPrimary=new ArrayList<Token>();
			QualifiedToken qt=primaryResults.get(i);
			if(qt.getWordType()!=WordType.QuantityElement) {
				entryFromPrimary.add(primaryResults.get(i));

				QualifiedToken tokenFromFinal=(QualifiedToken) findHeadInTokens(primaryResults, primaryResults.get(i));
				if(tokenFromFinal!=null&&tokenFromFinal.getWordType()!=WordType.QuantityElement)
					entryFromPrimary.add(tokenFromFinal);

				if(entryFromPrimary.size()>=2) {
					originalConotations.add(entryFromPrimary);
				}

			}


		}
		return originalConotations;
	}




	private List<List<Token>> findConnotationsInCorrected(List<Token> correctedTokens) {
		List<List<Token>> correctedConotations=new ArrayList<List<Token>>();
		boolean ofPassed=false;

		for(Token tokenFromCorrected:correctedTokens) {

			if("of".equals(tokenFromCorrected.getText())) {
				ofPassed=true;
			}else if(ofPassed){

				List<Token> correctedEntry=new ArrayList<Token>();
				correctedEntry.add(tokenFromCorrected);
				Token found=findHeadInTokens(correctedTokens, tokenFromCorrected);
				if(found!=null)
					correctedEntry.add(found);

				if(correctedEntry.size()>=2) {
					correctedConotations.add(correctedEntry);
				}
			}
		}
		return correctedConotations;
	}




	private Token findHeadInTokens(List<? extends Token> correctedTokens, Token getHeadFotThis) {
		for(Token t:correctedTokens) {
			if(t.getText().equals(getHeadFotThis.getHead())&&!t.getText().equals(getHeadFotThis.getText())) {
				if(NlpConstants.connectiveTag.equals(t.getTag())
						&&NlpConstants.connectivePos.equals(t.getPos())){
					return findHeadInTokens(correctedTokens,t);
				}
				
				return t;
			}
			
			
		}
		return null;
	}




	private CalculatedResults calculateWordsFound(IngredientPhraseParsingProcessObject parsingAPhrase) {
		String expected=parsingAPhrase.getLearningTuple().getFoodMatch();

		List<String> found=new ArrayList<String>();
		List<String> mistakenlyFound=new ArrayList<String>();

		for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
			if(qt.getWordType()==WordType.ProductElement) {
				if(expected.contains(qt.getText())) {
					found.add(qt.getText());
					expected=expected.replaceAll(qt.getText(), "").replaceAll("  ", " ");
				}else {
					mistakenlyFound.add(qt.getText());
				}
			}
		}

		List<String> notFound=Arrays.asList(expected.split(" "));

		List<String> wordsMarked=parsingAPhrase.getPermissiveFinalResults().stream().filter(qualifiedToken -> qualifiedToken.getWordType()==WordType.ProductElement).map(qualifiedToken -> qualifiedToken.getText()).collect(Collectors.toList());

		return new CalculatedResults(notFound,found,mistakenlyFound,wordsMarked);

	}




	private String createEntitiesString(NerResults entitiesFound) {
		String entitiesString="";

		if(entitiesFound!=null&&!entitiesFound.getEntities().isEmpty()) {
			for(NamedEntity ner:entitiesFound.getEntities()) {
				entitiesString+=ner.getText()+" ["+ner.getLabel()+"]";
			}
		}
		return entitiesString;
	}






	private String correctErrors(String phrase) {

		phrase=phrase.replaceFirst("½", "1/2");
		phrase=phrase.replaceFirst("¼", "1/4");
		phrase=phrase.replaceAll("é", "e");

		String replacedString=phrase.replaceAll(spacelessRegex, "$1 $2");
		if(!phrase.equals(replacedString)) {
			phrase=replacedString;
		}

		if(phrase.substring(0, phrase.length()<10?phrase.length():10).indexOf(" c ")>0) {
			phrase=phrase.replaceFirst(" c ", " cup ");
		}

		if(phrase.substring(0, phrase.length()<10?phrase.length():10).indexOf(" & ")>0) {
			phrase=phrase.replaceFirst(" & ", " and ");
		}

		return phrase;
	}



	public TokenizationResults tokenizeSingleWord(String phrase) throws WordNotFoundException {
		TokenizationResults parse = this.tokenizator.parse(phrase);

		if(parse.getTokens()==null||parse.getTokens().size()<1) {
			return new TokenizationResults();
		}else {
			return parse;
		}

	}



}
