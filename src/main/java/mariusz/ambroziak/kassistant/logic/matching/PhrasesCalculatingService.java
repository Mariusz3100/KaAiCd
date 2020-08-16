package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.*;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.CustomPhraseConsideredRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.IngredientPhraseLearningCaseRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.MorrisonProductRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.PhraseConsideredRepository;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.phrasefinding.PhraseConsideredMatch;
import mariusz.ambroziak.kassistant.pojos.phrasefinding.PhraseFindingResults;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Null;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PhrasesCalculatingService {


	private Map<ProductParsingResult,List<PhraseConsidered>> productPhrasesConsidered;
	private Map<IngredientPhraseParsingResult,List<PhraseConsidered>> ingredientPhrasesConsidered;


	@Autowired
	IngredientPhraseLearningCaseRepository ingredientPhraseLearningCaseRepository;

	@Autowired
	MorrisonProductRepository morrisonProductRepository;

	@Autowired
	TokenizationClientService tokenizationClientService;

	@Autowired
	CustomPhraseConsideredRepository phraseConsideredRepository;



	public static String doesContainPunctuationRegex=".*[\\.,—\\-\\*\\(\\)]+.*";


	public static final String csvSeparator=";";



//	public Map<String, List<PhraseConsidered>> getProductPhrasesCalculated(){
//
//
//		Map<String, List<PhraseConsidered>> collect =
//				getProductPhrasesConsidered().entrySet().stream()
//						.collect(Collectors.toMap(entry -> entry.getKey().getOriginalName(), entry -> entry.getValue()));
//
//		return collect;
//	}
//
//
//	public Map<String, List<PhraseConsidered>> getIngredientPhrasesCalculated(){
//
//
//		Map<String, List<PhraseConsidered>> collect =
//				getIngredientPhrasesConsidered().entrySet().stream()
//						.collect(Collectors.toMap(entry -> entry.getKey().getOriginalName(), entry -> entry.getValue()));
//
//		return collect;
//	}



//	private Map<ProductParsingResult, List<PhraseConsidered>> getProductPhrasesConsidered() {
//		if(productPhrasesConsidered==null)
//			productPhrasesConsidered=new HashMap<>();
//
//		return this.productPhrasesConsidered;
//	}
//
//	public Map<IngredientPhraseParsingResult, List<PhraseConsidered>> getIngredientPhrasesConsidered() {
//		if(ingredientPhrasesConsidered==null)
//			ingredientPhrasesConsidered=new HashMap<>();
//
//		return this.ingredientPhrasesConsidered;
//
//	}

	public List<PhraseConsidered>  addProductPhraseConsidered(ProductParsingResult productParsingResult, ProductParsingProcessObject processObject){
		List<PhraseConsidered> phrases=new ArrayList<>();

		try {
			phrases=calculateAllAdjacencyAndDependencyPhrases(processObject);

			phrases.stream().forEach(phraseConsidered -> phraseConsidered.setProductParsingResult(productParsingResult));
			this.phraseConsideredRepository.saveAllPhrases(phrases);

		}catch (IllegalStateException e){
			e.printStackTrace();
		}
		return phrases;

	}


	public List<PhraseConsidered>  addIngredientPhrasesConsidered(IngredientPhraseParsingResult ingredientPhraseParsingResult, IngredientPhraseParsingProcessObject processObject){
		List<PhraseConsidered> phrases=new ArrayList<>();

		try {
			phrases=calculateAllAdjacencyAndDependencyPhrases(processObject);

			phrases.stream().forEach(phraseConsidered -> phraseConsidered.setIngredientPhraseParsingResult(ingredientPhraseParsingResult));
			this.phraseConsideredRepository.saveAllPhrases(phrases);

		}catch (IllegalStateException e){
			e.printStackTrace();
		}
		return phrases;

	}

	private List<PhraseConsidered> calculateAllAdjacencyAndDependencyPhrases(AbstractParsingObject processObject) {
		List<PhraseConsidered> phrases=new ArrayList<>();

		for(int i=0;i<processObject.getFinalResults().size()-1;i++){
			QualifiedToken qt1 = processObject.getFinalResults().get(i);
			QualifiedToken qt2 = processObject.getFinalResults().get(i+1);
			if(qt1.getWordType()!= WordType.QuantityElement&&qt2.getWordType()!=WordType.QuantityElement){
				AdjacencyPhraseConsidered adjacencyPc=new AdjacencyPhraseConsidered();

				adjacencyPc.setPhrase(qt1.getText()+" "+qt2.getText());
				phrases.add(adjacencyPc);
			}


			List<QualifiedToken> collect = processObject.getFinalResults().stream()
					.filter(t -> t.getText().equals(qt1.getText()) && t.getHead().equals(qt1.getHead()))
					.filter(t -> t.getWordType()!=WordType.QuantityElement&&t.getWordType()!=WordType.PunctuationElement)
					.collect(Collectors.toList());


			if(collect.size()>1){
				System.err.println("two matching tokens");
			}else if(collect.size()==1){
				QualifiedToken qtChild =collect.get(0);
				QualifiedToken qtFather = processObject.getFinalResults().stream()
						.filter(t -> t.getText().equals(qt1.getHead()))
						.reduce((qualifiedToken, qualifiedToken2) -> pickOneWithTypeNotFilled(qt1, qt2)).orElse(null);

				if (qtFather != null) {
					DependencyPhraseConsidered dependencyPc = new DependencyPhraseConsidered();
					dependencyPc.setHead(new SavedToken(qtFather));
					dependencyPc.setChild(new SavedToken(qtChild));
					phrases.add(dependencyPc);
				}
			}



		}


		return phrases;
	}


	private QualifiedToken pickOneWithTypeNotFilled(QualifiedToken qt1,QualifiedToken qt2){
		try {
			if (qt1.getWordType() == null) {
				if (qt2.getWordType()!=null&&!qt2.getWordType().equals(WordType.QuantityElement) && !qt2.getWordType().equals(WordType.PunctuationElement))
					return qt2;
			}
			if (qt2.getWordType() == null) {
				if (qt1.getWordType()!=null&&!qt1.getWordType().equals(WordType.QuantityElement) && !qt2.getWordType().equals(WordType.PunctuationElement))
					return qt1;
			}
		}catch (NullPointerException e){
			e.printStackTrace();
		}
		return  qt1;
	}


	public PhraseFindingResults calculatePhraseFindingResults() {
		Iterable<PhraseConsidered> allPhrases = this.phraseConsideredRepository.findAllPhrases();
		Map<String, List<PhraseConsidered>> ingredientPhrasesCalculated =new HashMap<>();
		Map<String, List<PhraseConsidered>> productPhrasesCalculated = new HashMap<>();


		allPhrases.forEach(phraseConsidered -> {
			if(checkValidity(phraseConsidered)) {

				if (phraseConsidered.getProductParsingResult() != null) {
					if (productPhrasesCalculated.get(phraseConsidered.getProductParsingResult().getOriginalName()) == null)
						productPhrasesCalculated.put(phraseConsidered.getProductParsingResult().getOriginalName(), new ArrayList<>());

					productPhrasesCalculated.get(phraseConsidered.getProductParsingResult().getOriginalName()).add(phraseConsidered);


				}

				if (phraseConsidered.getIngredientPhraseParsingResult() != null) {
					if (ingredientPhrasesCalculated.get(phraseConsidered.getIngredientPhraseParsingResult().getOriginalName()) == null)
						ingredientPhrasesCalculated.put(phraseConsidered.getIngredientPhraseParsingResult().getOriginalName(), new ArrayList<>());

					ingredientPhrasesCalculated.get(phraseConsidered.getIngredientPhraseParsingResult().getOriginalName()).add(phraseConsidered);

				}
			}
		});








//		Map<String, List<PhraseConsidered>> ingredientPhrasesCalculated = getIngredientPhrasesCalculated();
//		Map<String, List<String>> frontEndIngredientMap = ingredientPhrasesCalculated.entrySet().stream()
//				.collect(Collectors.toMap(stringListEntry -> stringListEntry.getKey(), stringListEntry -> stringListEntry.getValue().stream().map(phrase -> phrase.toString()).collect(Collectors.toList())));
//
//		Map<String, List<PhraseConsidered>> productPhrasesCalculated = getProductPhrasesCalculated();
//		Map<String, List<String>> frontEndProductMap = ingredientPhrasesCalculated.entrySet().stream()
//				.collect(Collectors.toMap(stringListEntry -> stringListEntry.getKey(), stringListEntry -> stringListEntry.getValue().stream().map(phrase -> phrase.toString()).collect(Collectors.toList())));

		List<PhraseConsidered> ingredientTotal=new ArrayList<>();

		ingredientPhrasesCalculated.forEach((s, phraseConsidereds) -> ingredientTotal.addAll(phraseConsidereds));
		ingredientTotal.sort(Comparator.comparingInt(PhraseConsidered::hashCode));

		List<PhraseConsidered> productsTotal=new ArrayList<>();

		productPhrasesCalculated.forEach((s, phraseConsidereds) -> productsTotal.addAll(phraseConsidereds));
		productsTotal.sort(Comparator.comparingInt(PhraseConsidered::hashCode));

		List<PhraseConsidered> collectedProduct = productsTotal.stream().collect(Collectors.toList());//.filter(phraseConsidered -> ((DependencyPhraseConsidered) phraseConsidered).getHead().getText().equals("tomatoes") && ((DependencyPhraseConsidered) phraseConsidered).getChild().getText().equals("italian")).collect(Collectors.toList());
		List<PhraseConsidered> collectedIngredient = ingredientTotal.stream().collect(Collectors.toList());//.filter(phraseConsidered -> ((DependencyPhraseConsidered)phraseConsidered).getChild().getText().toLowerCase().equals("italian")).collect(Collectors.toList());

//		List<PhraseConsidered> matches = collectedProduct.stream()
//				.filter(ingredientPhrase -> collectedIngredient.stream().anyMatch(productPhrase -> productPhrase.equals(ingredientPhrase)))
//				.collect(Collectors.toList());
//
//
//		List<PhraseConsidered> ingredientSurplus = collectedIngredient.stream()
//				.filter(ingredientPhrase -> !matches.stream().anyMatch(matchedPhrase -> matchedPhrase.equals(ingredientPhrase))).collect(Collectors.toList());
//
//		List<PhraseConsidered> productSurplus = productsTotal.stream()
//				.filter(productPhrase -> !matches.stream().anyMatch(matchedPhrase -> matchedPhrase.equals(productPhrase))).collect(Collectors.toList());

		List<PhraseConsideredMatch> matches=new ArrayList<>();
		List<PhraseConsidered> ingredientSurplus=new ArrayList<>();
		List<PhraseConsidered> productSurplus =productsTotal.stream().collect(Collectors.toList());



		for(PhraseConsidered consideredNow:ingredientTotal){
			List<PhraseConsidered> collect = productsTotal.stream().filter(phraseConsidered -> phraseConsidered.equals(consideredNow)).collect(Collectors.toList());

			if(collect.size()>0) {
				PhraseConsideredMatch pcm=new PhraseConsideredMatch();
				pcm.setMatch(consideredNow);
				pcm.setIngredientFound(consideredNow);
				pcm.setProductFound(collect.get(0));

				if(!matches.stream().anyMatch(phraseConsideredMatch -> phraseConsideredMatch.getMatch().equals(pcm.getMatch()))){
					matches.add(pcm);
				}



				productSurplus=productSurplus.stream().filter(phraseConsidered -> !phraseConsidered.equals(consideredNow)).collect(Collectors.toList());
			}else {
				ingredientSurplus.add(consideredNow);
			}


		}
		List<PhraseConsideredMatch> matchesCutDown=new ArrayList<>();

		Map<PhraseConsidered, PhraseConsideredMatch> mat = matches.stream().collect(Collectors.toMap(phraseConsideredMatch -> phraseConsideredMatch.getMatch(), phraseConsideredMatch -> phraseConsideredMatch));
		matchesCutDown.addAll(mat.values());

		PhraseFindingResults retValue=new  PhraseFindingResults();

		retValue.setIngredientPhrases(collectedIngredient);
		retValue.setProductPhrases(collectedProduct);
		retValue.setMatchedPhrases(matchesCutDown);
		retValue.setExtraIngredientPhrases(ingredientSurplus);
		retValue.setExtraProductPhrases(productSurplus);


		return  retValue;
	}

	private boolean checkValidity(PhraseConsidered phraseConsidered) {
		if(phraseConsidered!=null){
			if(phraseConsidered instanceof AdjacencyPhraseConsidered){
				AdjacencyPhraseConsidered phraseConsidered1 = (AdjacencyPhraseConsidered) phraseConsidered;
				if(phraseConsidered1.getPhrase()==null
						||phraseConsidered1.getPhrase().isEmpty()
						||Pattern.matches(doesContainPunctuationRegex, phraseConsidered1.getPhrase())){
					return false;
				}else {
					return true;
				}
			}

			if(phraseConsidered instanceof DependencyPhraseConsidered){
				DependencyPhraseConsidered phraseConsidered1 = (DependencyPhraseConsidered) phraseConsidered;
				if(phraseConsidered1.getHead()==null||phraseConsidered1.getHead().getText()==null
						||phraseConsidered1.getHead().getText().isEmpty()
						||Pattern.matches(doesContainPunctuationRegex, phraseConsidered1.getHead().getText())
						||phraseConsidered1.getChild()==null||phraseConsidered1.getChild().getText()==null||phraseConsidered1.getChild().getText().isEmpty()

						||Pattern.matches(doesContainPunctuationRegex, phraseConsidered1.getChild().getText())){
					return false;
				}else {
					return true;
				}
			}
		}

		return  false;

	}


	public Map<String, List<PhraseConsidered>> getProductPhrasesCalculated() {
		Iterable<PhraseConsidered> allPhrases = this.phraseConsideredRepository.findAllPhrases();
		Map<String, List<PhraseConsidered>> productPhrasesCalculated = new HashMap<>();


		allPhrases.forEach(phraseConsidered -> {
			if (phraseConsidered.getProductParsingResult() != null) {
				if (productPhrasesCalculated.get(phraseConsidered.getProductParsingResult().getOriginalName()) == null)
					productPhrasesCalculated.put(phraseConsidered.getProductParsingResult().getOriginalName(), new ArrayList<>());
				productPhrasesCalculated.get(phraseConsidered.getProductParsingResult().getOriginalName()).add(phraseConsidered);
			}
		});

		return productPhrasesCalculated;


	}


	public Map<String, List<PhraseConsidered>> getIngredientPhrasesCalculated() {
		Iterable<PhraseConsidered> allPhrases = this.phraseConsideredRepository.findAllPhrases();
		Map<String, List<PhraseConsidered>> ingredientPhrasesCalculated = new HashMap<>();


		allPhrases.forEach(phraseConsidered -> {
			if (phraseConsidered.getIngredientPhraseParsingResult() != null) {
				if (ingredientPhrasesCalculated.get(phraseConsidered.getIngredientPhraseParsingResult().getOriginalName()) == null)
					ingredientPhrasesCalculated.put(phraseConsidered.getIngredientPhraseParsingResult().getOriginalName(), new ArrayList<>());
				ingredientPhrasesCalculated.get(phraseConsidered.getIngredientPhraseParsingResult().getOriginalName()).add(phraseConsidered);
			}
		});

		return ingredientPhrasesCalculated;


	}

}
