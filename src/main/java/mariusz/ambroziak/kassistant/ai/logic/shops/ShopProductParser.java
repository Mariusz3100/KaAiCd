package mariusz.ambroziak.kassistant.ai.logic.shops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.webapis.edamamnlp.LearningTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.ai.logic.CalculatedResults;
import mariusz.ambroziak.kassistant.webapis.edamamnlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResult;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.ProductsWordsClasifier;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;
import mariusz.ambroziak.kassistant.ai.logic.WordClasifier;
import mariusz.ambroziak.kassistant.webapis.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webapis.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.webapis.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webapis.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webapis.tesco.TescoApiClientService;
import mariusz.ambroziak.kassistant.webapis.tesco.TescoDetailsApiClientService;
import mariusz.ambroziak.kassistant.webapis.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.webapis.wordsapi.WordNotFoundException;


@Service
public class ShopProductParser {
	@Autowired
	private TokenizationClientService tokenizator;
	@Autowired
	private NamedEntityRecognitionClientService nerRecognizer;
	@Autowired
	private TescoApiClientService tescoService;
	@Autowired
	private TescoDetailsApiClientService tescoDetailsService;
	@Autowired
	private EdamanIngredientParsingService edamanNlpParsingService;
	@Autowired
	private WordClasifier wordClasifier;
	@Autowired
	private ProductsWordsClasifier shopWordClacifier;
	
	
	
	
	private String spacelessRegex="(\\d+)(\\w+)";
	
	








	public ParsingResultList categorizeProducts(String phrase) throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<Tesco_Product> inputs= this.tescoService.getProduktsFor(phrase);
		for(int i=0;i<inputs.size()&&i<5;i++) {
			Tesco_Product product=inputs.get(i);
			ProductParsingProcessObject parsingAPhrase=new ProductParsingProcessObject(product);
			NerResults entitiesFound = this.nerRecognizer.find(product.getName());
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());

			this.shopWordClacifier.calculateWordsType(parsingAPhrase);


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
			if(qt.getWordType()==WordType.ProductElement) {
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
		List<Tesco_Product> inputs= tescoService.getProduktsFor(param);
		for(Tesco_Product tp:inputs) {
			System.out.println(tp.getName()+";"+tp.getDetailsUrl());
		}
		
	}




	public ParsingResultList parseFromFile() throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<ProductParsingProcessObject> inputs= tescoDetailsService.getProduktsFromFile();
		for(ProductParsingProcessObject parsingAPhrase:inputs) {
			//ProductParsingProcessObject parsingAPhrase=new ProductParsingProcessObject(product);
			//parsingAPhrase.setProduct(product);

			String originalPhrase= parsingAPhrase.getOriginalPhrase();


			NerResults entitiesFound = this.nerRecognizer.find(originalPhrase);
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());


            this.wordClasifier.calculateProductType(parsingAPhrase);
			this.wordClasifier.calculateWordsType(parsingAPhrase);

			improperlyCorrectErrors(parsingAPhrase);
			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;
	}



    private void improperlyCorrectErrors(ProductParsingProcessObject parsingAPhrase) {
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
