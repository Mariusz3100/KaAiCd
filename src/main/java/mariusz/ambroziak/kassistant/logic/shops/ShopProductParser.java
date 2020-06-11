package mariusz.ambroziak.kassistant.logic.shops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.*;
import mariusz.ambroziak.kassistant.hibernate.repository.CustomPhraseFoundRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ParsingBatchRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ProductParsingResultRepository;
import mariusz.ambroziak.kassistant.inputs.TescoDetailsTestCases;
import mariusz.ambroziak.kassistant.logic.AbstractParser;
import mariusz.ambroziak.kassistant.pojos.*;
import mariusz.ambroziak.kassistant.pojos.parsing.CalculatedResults;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResult;
import mariusz.ambroziak.kassistant.pojos.parsing.ParsingResultList;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.shop.ProductNamesComparison;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordComparisonResult;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoDetailsApiClientService;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoApiClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.webclients.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;

import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;


@Service
public class ShopProductParser  extends AbstractParser {
	@Autowired
	private TokenizationClientService tokenizator;
	@Autowired
	private NamedEntityRecognitionClientService nerRecognizer;
	@Autowired
	private TescoApiClientService tescoApiClientService;

	@Autowired
	private TescoDetailsApiClientService tescoDetailsApiClientService;
	@Autowired
	private TescoDetailsTestCases tescoTestCases;
	@Autowired
	private EdamanIngredientParsingService edamanNlpParsingService;

	@Autowired
	private ProductWordsClassifier shopWordClacifier;

	@Autowired
	private ProductParsingResultRepository productParsingResultRepository;

	@Autowired
	private ParsingBatchRepository parsingBatchRepository;


	@Autowired
	CustomPhraseFoundRepository phraseFoundRepo;

	private String spacelessRegex="(\\d+)(\\w+)";




	public ParsingResultList tescoSearchForAndSaveResults(String phrase) {
		List<Tesco_Product> inputs= this.tescoApiClientService.getProduktsFor(phrase,5);

		List<ProductParsingProcessObject> parsingProcessObjects=inputs.stream()
				.map(s->new ProductParsingProcessObject(tescoDetailsApiClientService.getFullDataFromDbOrApi(s.getUrl()),new ProductLearningCase())).collect(Collectors.toList());

		ParsingResultList retValue = parseListOfCases(parsingProcessObjects);
		ParsingBatch batchObject=new ParsingBatch();
		parsingBatchRepository.save(batchObject);
		parsingProcessObjects.forEach(parsingProcessObject->saveResultInDb(parsingProcessObject,batchObject));


		retValue.getResults().forEach(pr->pr.setExpectedResult(null));
		return retValue;

	}



	public ParsingResultList tescoSearchForResults(String phrase) {
		List<Tesco_Product> inputs= this.tescoApiClientService.getProduktsFor(phrase,5);

		List<ProductParsingProcessObject> parsingProcessObjects=inputs.stream()
				.map(s->new ProductParsingProcessObject(tescoDetailsApiClientService.getFullDataFromDbOrApi(s.getUrl()),new ProductLearningCase())).collect(Collectors.toList());

		ParsingResultList retValue = parseListOfCases(parsingProcessObjects);

		retValue.getResults().forEach(pr->pr.setExpectedResult(null));
		return retValue;

	}

	public List<ProductParsingProcessObject> tescoSearchForParsings(String phrase) {
		List<Tesco_Product> inputs= this.tescoApiClientService.getProduktsFor(phrase,2);

		List<ProductParsingProcessObject> parsingProcessObjects=inputs.stream()
				.map(s->new ProductParsingProcessObject(tescoDetailsApiClientService.getFullDataFromDbOrApi(s.getUrl()),new ProductLearningCase())).collect(Collectors.toList());
		ParsingResultList retValue = parseListOfCases(parsingProcessObjects);

		retValue.getResults().forEach(pr->pr.setExpectedResult(null));
		return parsingProcessObjects;

	}

	private void saveFoundPhrasesInDb(ProductParsingProcessObject parsingAPhrase, ProductParsingResult productParsingResult) {
		List<PhraseFound> phrasesFound = parsingAPhrase.getPhrasesFound();

		phrasesFound.forEach(pf->pf.setRelatedProductResult(productParsingResult));

		phraseFoundRepo.saveAllIfNew(phrasesFound);

	}
	public ParsingResult createResultObject(ProductParsingProcessObject parsingAPhrase) {
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

	private ProductNamesComparison createNamesComparison(ProductParsingProcessObject parsingAPhrase) {
		String name=parsingAPhrase.getProduct().getName();
		List<String> nameWordsList= Arrays.asList(name.split(" "));

		String searchApiName="";
		String ingredients="";
		if(parsingAPhrase.getProduct() instanceof Tesco_Product) {



			Tesco_Product tp=(Tesco_Product)(parsingAPhrase.getProduct());
			searchApiName = tp.getSearchApiName()==null?"":tp.getSearchApiName();
			ingredients = tp.getIngredients()==null?"":tp.getIngredients();

			ProductNamesComparison productNamesComparison =ParseCompareProductNames.parseTwoPhrases(name,searchApiName);




			if(ingredients==null||ingredients.isEmpty()) {
				productNamesComparison.setIngredientsNameResults(Arrays.asList(new WordComparisonResult[]{new WordComparisonResult("", false)}));
			}else{
				List<WordComparisonResult> ingredientListResult = Arrays.asList(ingredients.split(" ")).stream().map(s -> new WordComparisonResult(s, true)).collect(Collectors.toList());
				productNamesComparison.setIngredientsNameResults(ingredientListResult);

			}

			return productNamesComparison;

		}else{
			ProductNamesComparison p=new ProductNamesComparison();
			p.setDetailsNameResults(nameWordsList.stream().map(s->new WordComparisonResult(s,true))
					.collect(Collectors.toList()));
			return p;
		}



	}



	private ProductNamesComparison calculatewordLists(List<String> longerList, List<String> shorterList){
		List<WordComparisonResult> nameListResults=new ArrayList<>();
		List<WordComparisonResult> searchNameListResults=new ArrayList<>();


		for(int i=0;i<longerList.size();i++){

			boolean equals=i<shorterList.size()&&longerList.get(i).equalsIgnoreCase(shorterList.get(i));

			if(equals) {
				nameListResults.add(new WordComparisonResult(i >= longerList.size() ? "" : longerList.get(i), equals));
				searchNameListResults.add(new WordComparisonResult(i >= shorterList.size() ? "" : shorterList.get(i), equals));
			}else {

			}

		}


		ProductNamesComparison p=new ProductNamesComparison();

		return  p;
	}




	

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




	public ParsingResultList parseAllTestCases() throws IOException {

		List<ProductParsingProcessObject> inputs= getTestCases();

		ParsingResultList retValue = parseListOfCases(inputs);

		return retValue;
	}


	public ParsingResultList parseAllTestCasesAndSaveResults() throws IOException {

		List<ProductParsingProcessObject> inputs= getTestCases();

		ParsingResultList retValue = parseListOfCases(inputs);

		ParsingBatch batchObject=new ParsingBatch();
		parsingBatchRepository.save(batchObject);

		inputs.forEach(input->saveResultInDb(input,batchObject));

		return retValue;
	}

	private ParsingResultList parseListOfCases(List<ProductParsingProcessObject> inputs) {
		ParsingResultList retValue=new ParsingResultList();

		for(ProductParsingProcessObject parsingAPhrase:inputs) {
			 parseProductParsingObjectWithNamesComparison(parsingAPhrase);
			ParsingResult singleResult=createResultObject(parsingAPhrase);


			retValue.addResult(singleResult);


		}
		return retValue;
	}

	public void parseProductParsingObjectWithNamesComparison(ProductParsingProcessObject parsingAPhrase) {
		parseAProductParsingObject(parsingAPhrase);

		Tesco_Product tp=(Tesco_Product)parsingAPhrase.getProduct();

		String secondName=tp.getSearchApiName();
		ProductNamesComparison productNamesComparisonOfFinalTokens=null;
		String detailsName = parsingAPhrase.getFinalResults().stream()
				.filter(t -> t.getWordType()==null||!t.getWordType().equals(WordType.QuantityElement))
				.map(t -> t.getText()).collect(Collectors.joining(" "));
		if(secondName==null||secondName.isEmpty()) {
			productNamesComparisonOfFinalTokens = ParseCompareProductNames.parseTwoPhrases(detailsName, "");
		}else{

			Tesco_Product tempTp = tp.clone();
			tempTp.setName(secondName);
			tempTp.setSearchApiName(tp.getName());

			ProductParsingProcessObject tempParsingObject = new ProductParsingProcessObject(tempTp, parsingAPhrase.getTestCase());
			parseAProductParsingObject(tempParsingObject);

			detailsName = parsingAPhrase.getFinalResults().stream()
					.filter(t -> t.getWordType()==null||!t.getWordType().equals(WordType.QuantityElement))
					.map(t -> t.getText()).collect(Collectors.joining(" "));


			String searchApiName = tempParsingObject.getFinalResults().stream()
					.filter(t -> t.getWordType()==null||!t.getWordType().equals(WordType.QuantityElement))
					.map(t -> t.getText()).collect(Collectors.joining(" "));


			productNamesComparisonOfFinalTokens = ParseCompareProductNames.parseTwoPhrases(detailsName, searchApiName);
		}
		parsingAPhrase.setFinalNames(productNamesComparisonOfFinalTokens);

	}

	private void parseAProductParsingObject(ProductParsingProcessObject parsingAPhrase) {


		String resultOfComparison=compareNames(parsingAPhrase).toLowerCase();

		String brandlessPhrase= calculateBrandlessPhrase(resultOfComparison,parsingAPhrase.getProduct().getBrand());
		parsingAPhrase.setBrandlessPhrase(brandlessPhrase);

		parsingAPhrase.setEntitylessString(brandlessPhrase);
		TokenizationResults tokens = this.tokenizator.parse(brandlessPhrase);
		parsingAPhrase.setEntitylessTokenized(tokens);


		this.shopWordClacifier.calculateProductType(parsingAPhrase);
		this.shopWordClacifier.calculateWordTypesForWholePhrase(parsingAPhrase);

	}

	private String compareNames(ProductParsingProcessObject parsingAPhrase) {
		ProductNamesComparison comparison=createNamesComparison(parsingAPhrase);
		parsingAPhrase.setInitialNames(comparison);

		return comparison.getResultPhrase();


	}

	private List<ProductParsingProcessObject> getTestCases() throws IOException {
		//return tescoTestCases.getProduktsFromFile();
		return tescoTestCases.getParsingObjectsFromDb();
	}

	private String calculateBrandlessPhrase(String originalPhrase, String brand) {

		Pattern p=Pattern.compile(brand,Pattern.CASE_INSENSITIVE);
		Matcher m=p.matcher(originalPhrase);
		String result=m.replaceAll("");
		result=result.replaceAll("  "," ").trim();
		return result;

	}






	public ProductParsingResult saveResultInDb(ProductParsingProcessObject parsingAPhrase, ParsingBatch pb) {
		ProductParsingResult toSave = saveProductParsingResult(parsingAPhrase, pb);
		saveFoundPhrasesInDb(parsingAPhrase,toSave);

		return toSave;
	}

	private ProductParsingResult saveProductParsingResult(ProductParsingProcessObject parsingAPhrase, ParsingBatch pb) {
		ProductParsingResult toSave=new ProductParsingResult();
		toSave.setOriginalName(parsingAPhrase.getOriginalPhrase());
		toSave.setProduct(parsingAPhrase.getProduct());
		toSave.setExtendedResultsCalculated(parsingAPhrase.getPermissiveFinalResultsString());
		toSave.setMinimalResultsCalculated(parsingAPhrase.getFinalResultsString());
		toSave.setTypeCalculated(parsingAPhrase.getFoodTypeClassified());
		toSave.setParsingBatch(pb);

		this.productParsingResultRepository.save(toSave);
		return toSave;
	}

}
