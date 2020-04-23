package mariusz.ambroziak.kassistant.logic.shops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.inputs.TescoDetailsTestCases;
import mariusz.ambroziak.kassistant.pojos.*;
import mariusz.ambroziak.kassistant.utils.ProblemLogger;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordParsed;
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

			this.shopWordClacifier.calculateWordTypesForWholePhrase(parsingAPhrase);


			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;

	}




	private ParsingResult createResultObject(ProductParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getProduct().getName());
		String fused=parsingAPhrase.getEntities()==null||parsingAPhrase.getEntities().getEntities()==null?"":parsingAPhrase.getEntities().getEntities().stream().map(s->s.getText()).collect( Collectors.joining("<br>") );

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


		createProductForResults(parsingAPhrase,object);

		return object;
	}

	private void createProductForResults(ProductParsingProcessObject parsingAPhrase, ParsingResult object) {
		String name=parsingAPhrase.getProduct().getName();
		List<String> nameWordsList= Arrays.asList(name.split(" "));
		Product p=new Product();

		String searchApiName="";
		String ingredients="";
		if(parsingAPhrase.getProduct() instanceof Tesco_Product) {



			Tesco_Product tp=(Tesco_Product)(parsingAPhrase.getProduct());
			searchApiName = tp.getSearchApiName()==null?"":tp.getSearchApiName();
			List<String> searchApiNameWordsList = Arrays.asList(searchApiName.split(" "));
			ingredients = tp.getIngredients()==null?"":tp.getIngredients();
			List<String> ingredientsList = Arrays.asList(ingredients.split(" "));


			int max= Math.max(Math.max(nameWordsList.size(),searchApiNameWordsList.size()),ingredientsList.size());
			List<WordParsed> nameListResults=new ArrayList<>();
			List<WordParsed> searchNameListResults=new ArrayList<>();
			List<WordParsed> ingredientsListResults=new ArrayList<>();

			if(name==null||name.isEmpty()){
				searchNameListResults=searchApiNameWordsList.stream().map(s->new WordParsed(s,true)).collect(Collectors.toList());
			}
			if(searchApiName==null||searchApiName.isEmpty()){
				nameListResults=nameWordsList.stream().map(s->new WordParsed(s,true)).collect(Collectors.toList());;
			}
																																																																																																																																																																																																																																																																																					int detailsNameOffset=0;
			int searchApiNameOffset=0;
			int nameOffset=0;
			for(int i=0;i+nameOffset<max&&i+searchApiNameOffset<max;i++){
				int currentNameIndex=i+nameOffset;
				int currentSearchApiNameIndex=i+searchApiNameOffset;

				if(currentNameIndex>=nameWordsList.size()){
					nameListResults.add(new WordParsed("", false));
				} else if(currentSearchApiNameIndex>=searchApiNameWordsList.size()){
					searchNameListResults.add(new WordParsed("", false));
				}else{

					String detailsNameWord = nameWordsList.get(currentNameIndex);
					String searchApiNameWord = searchApiNameWordsList.get(currentSearchApiNameIndex);
					boolean equals = currentNameIndex < nameWordsList.size() && currentSearchApiNameIndex < searchApiNameWordsList.size() && detailsNameWord.equalsIgnoreCase(searchApiNameWord);

					if (equals) {
						nameListResults.add(new WordParsed(detailsNameWord, true));
						searchNameListResults.add(new WordParsed(searchApiNameWord, true));
					} else {


						Optional<String> foundInNameList = nameWordsList.subList(currentNameIndex,nameWordsList.size()).stream().filter(s -> s.equals(searchApiNameWord)).findFirst();
						Optional<String> foundInNSearchApiList = searchApiNameWordsList.subList(currentSearchApiNameIndex,searchApiNameWordsList.size()).stream().filter(s -> s.equals(detailsNameWord)).findFirst();

						if(currentNameIndex+1>=nameWordsList.size()){
							nameListResults.add(new WordParsed(detailsNameWord, false));
						}
						if(currentSearchApiNameIndex+1>=searchApiNameWordsList.size()){
							searchNameListResults.add(new WordParsed(searchApiNameWord, false));

						}
						if(foundInNameList.isPresent()&&foundInNSearchApiList.isPresent()){
							ProblemLogger.logProblem("Words on index "+currentNameIndex+" for name "+name+" do not match and are found for different indices");
						}else{
							if(!foundInNSearchApiList.isPresent()&&!foundInNameList.isPresent()){
								searchNameListResults.add(new WordParsed(searchApiNameWord, false));
								nameListResults.add(new WordParsed(detailsNameWord, false));

							}

							if(foundInNSearchApiList.isPresent()){
								searchNameListResults.add(new WordParsed(searchApiNameWord, false));

								if(nameListResults.size()<nameWordsList.size()) {
									nameOffset--;
									nameListResults.add(new WordParsed("", false));
								}
							}
							if(foundInNameList.isPresent()){
								nameListResults.add(new WordParsed(detailsNameWord, false));

								if(searchNameListResults.size()<searchApiNameWordsList.size()) {
									searchApiNameOffset--;
									searchNameListResults.add(new WordParsed("", false));
								}

							}
						}


					}
				}

			}

			p.setDetailsNameResults(nameListResults);
			p.setSearchNameResults(searchNameListResults);
			if(ingredients==null||ingredients.isEmpty()) {
				p.setIngredientsNameResults(Arrays.asList(new WordParsed[]{new WordParsed("", false)}));

			}else{

				p.setIngredientsNameResults(ingredientsList.stream().map(s -> new WordParsed(s, true))
						.collect(Collectors.toList()));
			}

		}else{
			p.setDetailsNameResults(nameWordsList.stream().map(s->new WordParsed(s,true))
					.collect(Collectors.toList()));
		}



		object.setProduct(p);
	}



	private Product calculatewordLists(List<String> longerList,List<String> shorterList){
		List<WordParsed> nameListResults=new ArrayList<>();
		List<WordParsed> searchNameListResults=new ArrayList<>();


		for(int i=0;i<longerList.size();i++){

			boolean equals=i<shorterList.size()&&longerList.get(i).equalsIgnoreCase(shorterList.get(i));

			if(equals) {
				nameListResults.add(new WordParsed(i >= longerList.size() ? "" : longerList.get(i), equals));
				searchNameListResults.add(new WordParsed(i >= shorterList.size() ? "" : shorterList.get(i), equals));
			}else {

			}

		}


		Product p=new Product();

		return  p;
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
			String brandlessPhrase= calculateBrandlessPhrase(parsingAPhrase);
			parsingAPhrase.setBrandlessPhrase(brandlessPhrase);
//			NerResults entitiesFound = this.nerRecognizer.find(brandlessPhrase);
//			parsingAPhrase.setEntities(entitiesFound);
//
//			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(brandlessPhrase);
			parsingAPhrase.setEntitylessTokenized(tokens);








            this.shopWordClacifier.calculateProductType(parsingAPhrase);
			this.shopWordClacifier.calculateWordTypesForWholePhrase(parsingAPhrase);

			improperlyCorrectErrorsInFinalResults(parsingAPhrase);
			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;
	}

	private String calculateBrandlessPhrase(ProductParsingProcessObject productParsingObject) {
		String productName=productParsingObject.getProduct().getName();
		String brand=productParsingObject.getProduct().getBrand();
	//	String result=productName.replaceAll(brand,"").replaceAll("  ","");

		Pattern p=Pattern.compile(brand,Pattern.CASE_INSENSITIVE);
		Matcher m=p.matcher(productName);
		String result=m.replaceAll("");
		result=result.replaceAll("  "," ").trim();
		return result;

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
