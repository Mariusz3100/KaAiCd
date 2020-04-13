package mariusz.ambroziak.kassistant.logic.shops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.inputs.TescoDetailsTestCases;
import mariusz.ambroziak.kassistant.pojos.*;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.webclients.edamamnlp.LearningTuple;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoApiClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.webclients.edamamnlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NerResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;

import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;


@Service
public class ShopProductParser {
	@Autowired
	private TokenizationClientService tokenizator;
	@Autowired
	private NamedEntityRecognitionClientService nerRecognizer;
	@Autowired
	private TescoApiClientService tescoApiClientService;
	@Autowired
	private TescoDetailsTestCases tescoTestCases;
	@Autowired
	private EdamanIngredientParsingService edamanNlpParsingService;

	@Autowired
	private ProductWordsClassifier shopWordClacifier;
	
	
	
	
	private String spacelessRegex="(\\d+)(\\w+)";
	
	








	public ParsingResultList categorizeProducts(String phrase) throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<Tesco_Product> inputs= this.tescoApiClientService.getProduktsFor(phrase);
		for(int i=0;i<inputs.size()&&i<5;i++) {
			Tesco_Product product=inputs.get(i);
			ProductParsingProcessObject parsingAPhrase=new ProductParsingProcessObject(product);
			NerResults entitiesFound = this.nerRecognizer.find(product.getName());
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());

			this.shopWordClacifier.calculateWordTypesForWholePhrase(parsingAPhrase);


			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;

	}




	private ParsingResult createResultObject(ProductParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getProduct().getName());
		String fused=parsingAPhrase.getEntities().getEntities().stream().map(s->s.getText()).collect( Collectors.joining("<br>") );

		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());
		object.setTokens(parsingAPhrase.getFinalResults());
		String expected=parsingAPhrase.getExpectedWords().stream().collect(Collectors.joining(" "));
		LearningTuple lp=new LearningTuple(parsingAPhrase.getOriginalPhrase(),0,"empty",expected,parsingAPhrase.getExpectedType());
		object.setExpectedResult(lp);
		object.setProductTypeFound(parsingAPhrase.getFoodTypeClassified().toString());
		object.setRestrictivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getExpectedWords(),parsingAPhrase.getFinalResults()));
		object.setPermisivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));

        object.setRestrictivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getAllExpectedWords(),parsingAPhrase.getFinalResults()));
        object.setPermisivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getAllExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));



		return object;
	}


	private CalculatedResults calculateWordsFound(List<String> expected,List<QualifiedToken> finalResults) {
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

//	private CalculatedResults calculateWordsFound(ProductParsingProcessObject parsingAPhrase) {
//		List<String> found=new ArrayList<String>();
//		List<String> mistakenlyFound=new ArrayList<String>();
//		String expected=parsingAPhrase.getExpectedWords().stream().collect(Collectors.joining(" ")).toLowerCase();
//		for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
//			if(qt.getWordType()==WordType.ProductElement) {
//				if(expected.contains(qt.getText().toLowerCase())) {
//					found.add(qt.getText());
//					expected=expected.replaceAll(qt.getText().toLowerCase(), "").replaceAll("  ", " ");
//				}else {
//					mistakenlyFound.add(qt.getText());
//				}
//			}
//		}
//
//		List<String> notFound=Arrays.asList(expected.split(" "));
//		List<String> wordsMarked=parsingAPhrase.getFinalResults().stream().filter(qualifiedToken -> qualifiedToken.getWordType()==WordType.ProductElement).map(qualifiedToken -> qualifiedToken.getText()).collect(Collectors.toList());
//
//		return new CalculatedResults(notFound,found,mistakenlyFound,wordsMarked);
//
//	}










	

	public TokenizationResults tokenizeSingleWord(String phrase) throws WordNotFoundException {
		TokenizationResults parse = this.tokenizator.parse(phrase);

		if(parse.getTokens()==null||parse.getTokens().size()<1) {
			return new TokenizationResults();
		}else {
			return parse;
		}

	}




	public void tescoGetResults(String param) {
		List<Tesco_Product> inputs= tescoApiClientService.getProduktsFor(param);
		for(Tesco_Product tp:inputs) {
			System.out.println(tp.getName()+";"+tp.getUrl());
		}
		
	}




	public ParsingResultList parseFromFile() throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<ProductParsingProcessObject> inputs= tescoTestCases.getProduktsFromFile();
		for(ProductParsingProcessObject parsingAPhrase:inputs) {
			String originalPhrase= parsingAPhrase.getOriginalPhrase();

			NerResults entitiesFound = this.nerRecognizer.find(originalPhrase);
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());




            this.shopWordClacifier.calculateProductType(parsingAPhrase);
			this.shopWordClacifier.calculateWordTypesForWholePhrase(parsingAPhrase);

			improperlyCorrectErrorsInFinalResults(parsingAPhrase);
			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;
	}



    private void improperlyCorrectErrorsInFinalResults(ProductParsingProcessObject parsingAPhrase) {
		for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
				if(qt.getText().toLowerCase().equals("tomatoes")) {
					qt.setLemma("tomato");
				}

		}
		for(QualifiedToken qt:parsingAPhrase.getPermissiveFinalResults()) {
			if(qt.getText().toLowerCase().equals("tomatoes")) {
				qt.setLemma("tomato");
			}

		}
	}


}
