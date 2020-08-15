package mariusz.ambroziak.kassistant.logic;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.constants.NlpConstants;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseFound;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.PhraseFoundProductType;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.CustomPhraseFoundRepository;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.spacy.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;

import mariusz.ambroziak.kassistant.enums.MergeType;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.pojos.quantity.QuantityTranslation;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.ProductData;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NerResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.*;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.webclients.usda.SingleResult;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaApiClient;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntity;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.webclients.wikipedia.WikipediaApiClient;
import mariusz.ambroziak.kassistant.webclients.convert.ConvertApiClient;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiClient;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiResult;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordsApiResultImpostor;

@Service
public class WordClasifier {
	private static String wikipediaCheckRegex=".*[a-zA-Z].*";
	private static String convertApiCheckRegex=".*[a-zA-Z].*";
	public static String punctuationRegex="[\\.,\\-\\*\\(\\)]*";


	@Autowired
	protected WordsApiClient wordsApiClient;

	@Autowired
	protected UsdaApiClient usdaApiClient;

	@Autowired
	private WikipediaApiClient wikipediaClient;
	@Autowired
	private ConvertApiClient convertClient;
	@Autowired
    protected TokenizationClientService tokenizator;
	@Autowired
	private NamedEntityRecognitionClientService nerRecognizer;
	@Autowired
	PhraseDependenciesComparator dependenciesComparator;

	@Autowired
	protected  CustomPhraseFoundRepository phraseFoundRepo;

	public static ArrayList<String> productTypeKeywords;
	public static ArrayList<String> freshTypeKeywords;

	public static ArrayList<String> irrelevanceKeywords;
	public static ArrayList<String> quantityTypeKeywords;
	public static ArrayList<String> quantityAttributeKeywords;
	public static ArrayList<String> punctationTypeKeywords;

	public static ArrayList<String> freshFoodKeywords;
    public static ArrayList<String> foodFlavouredKeywords;

	public static ArrayList<String> pureeFoodKeywords;
	public static ArrayList<String> juiceKeywords;
    public static ArrayList<String> usdaIgnoreKeywords;

    public static ArrayList<String> impromperProductPropertyKeywords;
    public static ArrayList<String> impromperQuantityKeywords;

    static {



//		productTypeKeywords.add("flavouring");
//	//	productTypeKeywords.add("herb");
//
//		productTypeKeywords.add("seasoning");
//		productTypeKeywords.add("dairy");
//		productTypeKeywords.add("meat");
//		productTypeKeywords.add("food");
//		productTypeKeywords.add("sweetener");
//		productTypeKeywords.add("cheese");
//		productTypeKeywords.add("citrous fruit");
//		productTypeKeywords.add("dish");
//		productTypeKeywords.add("victuals");
//		productTypeKeywords.add("alcohol");
//		productTypeKeywords.add("edible fat");

        productTypeKeywords=new ArrayList<String>();

//        productTypeKeywords.add("vegetable");
        productTypeKeywords.add("dairy");
//        productTypeKeywords.add("fruit");
        productTypeKeywords.add("food");


        freshTypeKeywords=new ArrayList<String>();
		freshTypeKeywords.add("vegetable");
		freshTypeKeywords.add("fruit");
        productTypeKeywords.addAll(freshTypeKeywords);




		irrelevanceKeywords=new ArrayList<String>();
		irrelevanceKeywords.add("activity");
		irrelevanceKeywords.add("love");


		quantityTypeKeywords=new ArrayList<String>();
		quantityTypeKeywords.add("containerful");
		quantityTypeKeywords.add("small indefinite quantity");
		quantityTypeKeywords.add("weight unit");
		quantityTypeKeywords.add("capacity unit");
		quantityTypeKeywords.add("avoirdupois unit");
		quantityTypeKeywords.add("package");


		//presumably too specific ones:
	//	productTypeKeywords.add("dressing");

		quantityAttributeKeywords=new ArrayList<String>();
		quantityAttributeKeywords.add("size");

		freshFoodKeywords=new ArrayList<String>();
		freshFoodKeywords.add("fresh");
        freshFoodKeywords.add("raw");


		pureeFoodKeywords=new ArrayList<>();
		pureeFoodKeywords.add("puree");
		pureeFoodKeywords.add("passata");
		pureeFoodKeywords.add("paste");
        pureeFoodKeywords.add("sieved");
   //     pureeFoodKeywords.add("concentrate");

		juiceKeywords=new ArrayList<>();
		juiceKeywords.add("juice");

        impromperProductPropertyKeywords=new ArrayList<>();
        impromperProductPropertyKeywords.add("organic");

        impromperQuantityKeywords=new ArrayList<>();
        impromperQuantityKeywords.add("medium");

        usdaIgnoreKeywords=new ArrayList<>();
        usdaIgnoreKeywords.add("slices");

        foodFlavouredKeywords=new ArrayList<>();
        foodFlavouredKeywords.add("flavoured");
        foodFlavouredKeywords.add("scented");
        foodFlavouredKeywords.add("infused");




    }


	public void calculateWordTypesForWholePhrase(AbstractParsingObject parsingAPhrase) {
		handleBracketsAndSetBracketLess(parsingAPhrase);
	//	handleEntities(parsingAPhrase);




        initializePrimaryTokensAndConnotations(parsingAPhrase);
        initialCategorization(parsingAPhrase);
        fillQuanAndProdPhrases(parsingAPhrase);
        initializeProductPhraseTokensAndConnotations(parsingAPhrase);
        //if(parsingAPhrase.getFinalResults().stream().filter(t->t.getWordType()==null||t.getWordType()==WordType.Unknown).findAny().isPresent()) {

        if(checkifAreUnclassifiedTokesLeft(parsingAPhrase)) {
            emptyProductTokensAndPhrases(parsingAPhrase);
            //lookForWholePhrasesInDb(parsingAPhrase);

            categorizationFromConnotations(parsingAPhrase);

            if (checkifAreUnclassifiedTokesLeft(parsingAPhrase)) {
                categorizeSingleTokens(parsingAPhrase);
            }
        }
        recategorize(parsingAPhrase);
        //}
        calculateProductType(parsingAPhrase);

        categorizeAllElseAsProducts(parsingAPhrase);

    }

    private void categorizeSingleTokens(AbstractParsingObject parsingAPhrase) {

        for (int i = 0; i < parsingAPhrase.getFinalResults().size(); i++) {

            try {
                QualifiedToken qt = parsingAPhrase.getFinalResults().get(i);
                if(qt.getWordType()==null) {
                    searchDbAndApis(parsingAPhrase, i, qt);
                }
            } catch (WordNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
    private void emptyProductTokensAndPhrases(AbstractParsingObject parsingAPhrase) {
        parsingAPhrase.getFinalResults().stream().filter(qt -> qt.getWordType() == WordType.ProductElement).forEach(qt -> qt.setWordType(null));
        parsingAPhrase.setPhrasesFound(new ArrayList<>());
    }

    private void handleEntities(AbstractParsingObject parsingAPhrase) {
        NerResults entitiesFound = this.nerRecognizer.find(parsingAPhrase.getBracketLessPhrase());
        parsingAPhrase.setEntities(entitiesFound);

        String entitylessString = parsingAPhrase.getEntitylessString();

        TokenizationResults tokens = this.tokenizator.parse(entitylessString);
        parsingAPhrase.setEntitylessTokenized(tokens);
    }

    private void initializeProductPhraseTokensAndConnotations(AbstractParsingObject parsingAPhrase) {

        TokenizationResults tokenized = this.tokenizator.parse(parsingAPhrase.getQuantitylessPhrase());
        parsingAPhrase.setQuantitylessTokenized(tokenized);
        DependencyTreeNode dependencyTreeRoot = tokenized.getDependencyTree();
        List<ConnectionEntry> quantitylessConnotations = tokenized.getAllTwoWordDependencies();
        Token foundToken = tokenized.findToken(tokenized.getTokens(), dependencyTreeRoot == null ? "" : dependencyTreeRoot.getText());
        quantitylessConnotations.add(new ConnectionEntry(new Token("ROOT", "", ""), foundToken));

        parsingAPhrase.setQuantitylessConnotations(quantitylessConnotations);

    }

    protected void calculateProductType(AbstractParsingObject parsingAPhrase) {
        extractAndMarkProductPropertyWords(parsingAPhrase);
        calculateTypeFromReasonings(parsingAPhrase);
    }

    protected boolean calculateTypeFromReasonings(AbstractParsingObject parsingAPhrase) {
        List<ProductType> foundTypes=parsingAPhrase.getProductTypeReasoning().values().stream().filter(pt->!pt.equals(ProductType.unknown)).distinct().collect(Collectors.toList());
        if(foundTypes.size()==1){
            parsingAPhrase.setFoodTypeClassified(foundTypes.get(0));
            return true;
        }else if(foundTypes.size()>1){
            parsingAPhrase.getProductTypeReasoning().put("[conflicting types found]", ProductType.unknown);
            return true;
        }else {
            return false;
        }
    }
    protected void calculateBasedOnPhrasesOrUpdatePhrasesWithTypes(AbstractParsingObject parsingAPhrase) {
        if(parsingAPhrase.getFoodTypeClassified()!=null&&!parsingAPhrase.getFoodTypeClassified().equals(ProductType.unknown)) {
            //add productType to phrases
            parsingAPhrase.getPhrasesFound().stream()
                    //   .filter(phraseFound -> phraseFound.getPhraseFoundProductType().isEmpty()) as of now, it doesn't have to be empty
                    .forEach(pf->pf.addPhraseFoundProductType(new PhraseFoundProductType(parsingAPhrase.getFoodTypeClassified(),null,pf.getRelatedProductResult(),pf)));

        }else{
            calculateReasoningsBaseOnClassifiedPhrases(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);

        }
    }

    protected void calculateReasoningsBaseOnClassifiedPhrases(AbstractParsingObject parsingAPhrase) {
        List<PhraseFound> phrasesFound = parsingAPhrase.getPhrasesFound();


        for(PhraseFound pf:phrasesFound){
            if(pf.getPf_id()!=null&&pf.getTypesFoundForPhraseAndBase()!=null&&!pf.getTypesFoundForPhraseAndBase().equals(ProductType.unknown)){
                parsingAPhrase.getProductTypeReasoning().put("[DB from a phrase: "+pf.getPhrase()+"]", pf.getWeightedLeadingProductType());
            }
        }

    }

    protected void extractAndMarkProductPropertyWords(AbstractParsingObject parsingAPhrase) {
        for (QualifiedToken qt : parsingAPhrase.getFinalResults()) {
            for (String keyword : freshFoodKeywords) {
                if (keyword.equals(qt.getText())) {
                    //qt.setWordType(WordType.ProductPropertyElement);
                    //parsingAPhrase.setFoodTypeClassified(ProductType.fresh);
                    parsingAPhrase.getProductTypeReasoning().put("keyword: " + keyword, ProductType.fresh);
                }
            }

            for (String keyword : juiceKeywords) {
                if (keyword.equals(qt.getText())) {
                    //qt.setWordType(WordType.ProductPropertyElement);
                   // parsingAPhrase.setFoodTypeClassified(ProductType.juice);
                    parsingAPhrase.getProductTypeReasoning().put("keyword: " + keyword, ProductType.juice);

                }
            }

            for (String keyword : pureeFoodKeywords) {
                if (keyword.equals(qt.getText())) {
                   // qt.setWordType(WordType.ProductPropertyElement);
                    //parsingAPhrase.setFoodTypeClassified(ProductType.puree);
                    parsingAPhrase.getProductTypeReasoning().put("keyword: " + keyword, ProductType.puree);

                }
            }


            for (String keyword : foodFlavouredKeywords) {
                if (keyword.equals(qt.getText())) {
                    // qt.setWordType(WordType.ProductPropertyElement);
                    //parsingAPhrase.setFoodTypeClassified(ProductType.puree);
                    parsingAPhrase.getProductTypeReasoning().put("keyword: " + keyword, ProductType.flavoured);

                }
            }
        }

    }
    private void categorizationFromConnotations(AbstractParsingObject parsingAPhrase) {
        checkAllTokensForAdjacencyPhrases(parsingAPhrase);
        if(checkifAreUnclassifiedTokesLeft(parsingAPhrase)) {
            checkAllTokensInUsdaApiWithRespectForDependencies(parsingAPhrase);
        }
        if(checkifAreUnclassifiedTokesLeft(parsingAPhrase)){
            List<ConnectionEntry> quantitylessDependenciesConnotations = parsingAPhrase.getQuantitylessConnotations().stream().filter(s -> !s.getHead().getText().equals("ROOT")).collect(Collectors.toList());
            List<ConnectionEntry> filteredQuantitylessDependenciesConnotations = quantitylessDependenciesConnotations.stream()
                    .filter(t -> !NlpConstants.of_Word.equals(t.getHead().getText()) && !NlpConstants.of_Word.equals(t.getChild().getText()))
                    .filter(t -> !t.getHead().getText().trim().isEmpty() && !t.getChild().getText().trim().isEmpty())
                    .filter(ce -> !Pattern.matches(punctuationRegex, ce.getChild().getText()) && !Pattern.matches(punctuationRegex, ce.getHead().getText()))
                    .collect(Collectors.toList());

            if (filteredQuantitylessDependenciesConnotations != null && filteredQuantitylessDependenciesConnotations.isEmpty()) {
                for (ConnectionEntry entry : filteredQuantitylessDependenciesConnotations) {
                    checkWordsApi(parsingAPhrase, entry);
                }
            }
            if (checkifAreUnclassifiedTokesLeft(parsingAPhrase)) {
                for (ConnectionEntry ce : filteredQuantitylessDependenciesConnotations) {
                    checkUsdaApiForSingleDependency(parsingAPhrase, ce);
                }
            }

        }
    }

    private void checkAllTokensForAdjacencyPhrases(AbstractParsingObject parsingAPhrase) {
        Map<String, Integer> adjacentyConotations = calculateAdjacencies(parsingAPhrase);

        if (adjacentyConotations != null) {
            for (String twoWordEntry : adjacentyConotations.keySet()) {
                String[] splitted = twoWordEntry.split(" ");
                if (!Pattern.matches(punctuationRegex, splitted[0]) && !Pattern.matches(punctuationRegex, splitted[1])) {
                    boolean found=checkWithPhrasesInDb(parsingAPhrase, adjacentyConotations.get(twoWordEntry), twoWordEntry);
                    if (!found) {
                        found = checkWordsApi(parsingAPhrase, adjacentyConotations, twoWordEntry);
                    }
                    if (!found) {
                        found = checkUsdaApiForAdjacencyEntry(parsingAPhrase, adjacentyConotations.get(twoWordEntry), twoWordEntry);
                    }
                }
            }
        }
    }

    private boolean checkifAreUnclassifiedTokesLeft(AbstractParsingObject parsingAPhrase) {
        return parsingAPhrase.getFinalResults().stream().filter(qt->qt.getWordType()==null).count()>0;
    }

    private boolean checkUsdaApiForSingleDependency(AbstractParsingObject parsingAPhrase, ConnectionEntry quantitylessDependenciesConnotation) {
        //TODO right now we do not consider shorter search phrases, to be thought about
        String quantitylessTokens = quantitylessDependenciesConnotation.getHead().getText() + " " + quantitylessDependenciesConnotation.getChild().getText();
        String quantitylessTokensWithPluses = "+" + quantitylessDependenciesConnotation.getHead().getText() + " +" + quantitylessDependenciesConnotation.getChild().getText();
        UsdaResponse inApi = findInUsdaApi(quantitylessTokensWithPluses, 20);
        for (SingleResult sp : inApi.getFoods()) {
            String desc = sp.getDescription();
            boolean isSame = this.dependenciesComparator.comparePhrases(quantitylessTokens, desc);

            if (isSame) {
                return markFoundDependencyResults(parsingAPhrase, sp);
            }
        }

        return false;
    }

    private UsdaResponse findInUsdaApi(String quantitylessTokensWithPluses, int i) {
        return this.usdaApiClient.findInApi(quantitylessTokensWithPluses, i);
    }

    private boolean checkAllTokensInUsdaApiWithRespectForDependencies(AbstractParsingObject parsingAPhrase) {
        //TODO right now we do not consider shorter search phrases, to be thought about
        String quantitylessTokens = parsingAPhrase.getQuantitylessTokenized().getTokens().stream().map(t -> t.getText().toLowerCase()).collect(Collectors.joining(" "));
        String quantitylessTokensWithPluses = parsingAPhrase.getQuantitylessTokenized().getTokens().stream().filter(t -> !t.getText().equals(NlpConstants.of_Word)).map(t -> "+" + t.getText().toLowerCase()).collect(Collectors.joining(" "));
        UsdaResponse inApi = findInUsdaApi(quantitylessTokensWithPluses, 20);
        for (SingleResult sp : inApi.getFoods()) {
            String desc = sp.getDescription();
            boolean isSame = this.dependenciesComparator.comparePhrases(quantitylessTokens, desc);
            //		System.out.println(desc+" : "+quantitylessTokens+" : "+isSame);

            if (isSame) {
                return markFoundDependencyResults(parsingAPhrase, sp);
            }
        }

        return false;
    }

    private boolean markFoundDependencyResults(AbstractParsingObject parsingAPhrase, SingleResult sp) {
        if (sp.getDescription() == null || sp.getDescription().isEmpty())
            return false;

        TokenizationResults descTokenized = this.tokenizator.parse(sp.getDescription().toLowerCase());

        parsingAPhrase.addPhraseFound(new PhraseFound(sp.getDescription().toLowerCase(), WordType.ProductElement, "[usda api: " + sp.getFdcId() + "]"));
        List<Token> descTokensLeft = descTokenized.getTokens();
        for (int i = 0; i < descTokenized.getTokens().size(); i++) {
            Token t = descTokenized.getTokens().get(i);

            Optional<QualifiedToken> found = parsingAPhrase.getFinalResults().stream()
                    .filter(token -> !Pattern.matches(punctuationRegex,t.getText()))
                    .filter(token -> token.compareWithToken(t)).findFirst();

            if (found.isPresent()) {
                found.get().setWordType(WordType.ProductElement);
                found.get().setReasoning("[usda api: " + sp.getFdcId() + "]");
            } else {
                if (!Pattern.matches(WordClasifier.punctuationRegex, t.getText())) {
                    System.err.println(t.getText() + " from " + sp.getFdcId() + " has no matching tokens in '" + parsingAPhrase.getOriginalPhrase() + "'");
                }
            }

        }


        return false;
    }

    private boolean checkUsdaApiForAdjacencyEntry(AbstractParsingObject parsingAPhrase, int index, String entry) {
        UsdaResponse inApi = findInUsdaApi(entry, 10);

        for (SingleResult sp : inApi.getFoods()) {
            String desc = sp.getDescription();
            if (desc.toLowerCase().equals(entry.toLowerCase())) {
                return markFoundAdjacencyResults(parsingAPhrase, index, sp);
            }
        }
        return false;
    }


//	private boolean checkUsdaApi(AbstractParsingObject parsingAPhrase, ConnectionEntry dependencyConnotation) {
//		String entry=dependencyConnotation.getHead()+" "+dependencyConnotation.getChild();
//		UsdaResponse inApi = this.usdaApiClient.findInApi(entry, 10);
//		if(inApi!=null){
//
//
//			for(SingleResult sp:inApi.getFoods()){
//				String desc=sp.getDescription();
//				boolean isSame=this.dependenciesComparator.comparePhrases(desc,entry);
//				System.out.println(desc+" : "+entry+" : "+isSame);
//
//				if(isSame){
//					return markFoundAdjacencyResults(parsingAPhrase, sp);
//				}else{
//					return false;
//				}
//			}
//
//
//		}else{
//			return false;
//		}
//		return false;
//	}

//
//	private boolean checkUsdaApi(AbstractParsingObject parsingAPhrase,int index, String entry) {
//		UsdaResponse inApi = this.usdaApiClient.findInApi(entry, 10);
//
//		for(SingleResult sp:inApi.getFoods()){
//			String desc=sp.getDescription();
//			if(desc.toLowerCase().contains(entry.toLowerCase())){
//				return markFoundAdjacencyResults(parsingAPhrase, index, sp);
//			}else{
//				boolean isSame=this.dependenciesComparator.comparePhrases(desc,entry);
//				System.out.println(desc+" : "+entry+" : "+isSame);
//
//				if(isSame){
//					return markFoundAdjacencyResults(parsingAPhrase, index, sp);
//				}
//			}
//		}
//		return false;
//	}

    private boolean markFoundAdjacencyResults(AbstractParsingObject parsingAPhrase, int index, SingleResult sp) {
        addFoundLongPhrase(parsingAPhrase, sp.getDescription().toLowerCase(), WordType.ProductElement, "[usda api: " + sp.getFdcId() + "]");

        QualifiedToken qualifiedToken1 = parsingAPhrase.getFinalResults().get(index);
        addProductResult(parsingAPhrase, index, qualifiedToken1, "[usda api: " + sp.getFdcId() + "]");

        QualifiedToken qualifiedToken2 = parsingAPhrase.getFinalResults().get(index + 1);
        addProductResult(parsingAPhrase, index + 1, qualifiedToken2, "[usda api: " + sp.getFdcId() + "]");
        return true;
    }


    private boolean checkWordsApi(AbstractParsingObject parsingAPhrase, Map<String, Integer> adjacentyConotations, String entry) {
        ArrayList<WordsApiResult> wordsApiResults = wordsApiClient.searchFor(entry);
        WordsApiResult wordsApiResult = checkProductTypesForWordObject(wordsApiResults);
        if (wordsApiResult != null) {
            checkForProductTypeProperties(parsingAPhrase, wordsApiResult);

            int index = adjacentyConotations.get(entry);
            String[] x = entry.split(" ");
            if (!(parsingAPhrase.getFinalResults().get(index).getText().equals(x[0]) || parsingAPhrase.getFinalResults().get(index + 1).getText().equals(x[1]))) {
                System.out.println("Problem, connotations not match");
            } else {
                markFoundAdjacencyResults(parsingAPhrase, wordsApiResult, index, "[Double, " + wordsApiResult.getReasoningForFound() + "]");
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    private void checkForProductTypeProperties(AbstractParsingObject parsingAPhrase, WordsApiResult wordsApiResult) {
        ArrayList<String> typeOf = wordsApiResult.getTypeOf();

        for (String keyword : freshTypeKeywords) {
            for (String type : typeOf) {
                if (type.indexOf(keyword) > 0) {
                    parsingAPhrase.getProductTypeReasoning().put("api keyword for fresh found: " + keyword, ProductType.fresh);
      //              parsingAPhrase.setFoodTypeClassified(ProductType.fresh);
                }
            }

        }

    }

    private void markFoundAdjacencyResults(AbstractParsingObject parsingAPhrase, WordsApiResult wordsApiResult, int index, String reasoning) {
        addFoundLongPhrase(parsingAPhrase, wordsApiResult.getBaseWord(), WordType.ProductElement, wordsApiResult.getReasoningForFound());

        addProductResult(parsingAPhrase, index, parsingAPhrase.getFinalResults().get(index), reasoning);
        addProductResult(parsingAPhrase, index + 1, parsingAPhrase.getFinalResults().get(index + 1), reasoning);
    }

    private boolean checkWordsApi(AbstractParsingObject parsingAPhrase, ConnectionEntry dependencyConnotation) {
        String entry = dependencyConnotation.getHead().getText() + " " + dependencyConnotation.getChild().getText();
        ArrayList<WordsApiResult> wordsApiResults = wordsApiClient.searchFor(entry);
        WordsApiResult wordsApiResult = checkProductTypesForWordObject(wordsApiResults);
        if (wordsApiResult != null) {
            markFoundDependencyConnotation(parsingAPhrase, dependencyConnotation, entry, wordsApiResult);
        } else {
            return false;
        }
        return false;
    }

    private void markFoundDependencyConnotation(AbstractParsingObject parsingAPhrase, ConnectionEntry dependencyConnotation, String entry, WordsApiResult wordsApiResult) {
        boolean foundHead = false, foundChild = false;
        addFoundLongPhrase(parsingAPhrase, entry, WordType.ProductElement, wordsApiResult.getReasoningForFound());
        for (int i = 0; i < parsingAPhrase.getFinalResults().size(); i++) {
            QualifiedToken qt = parsingAPhrase.getFinalResults().get(i);

            if (dependencyConnotation.getHead().getText() == qt.getText()) {
                foundHead = true;
                addProductResult(parsingAPhrase, i, qt, "[Double, " + wordsApiResult.getReasoningForFound() + "]");

            }
            if (dependencyConnotation.getChild().getText() == qt.getText() && qt.getHead().equals(dependencyConnotation.getHead())) {
                foundChild = true;
                addProductResult(parsingAPhrase, i, qt, "[Double, " + wordsApiResult.getReasoningForFound() + "]");

            }
        }
        if (!foundChild || !foundHead) {
            System.err.println("Entry '" + entry + "' found in words api, but not in tokens");
        }
    }

//	private void markFoundConnotation(AbstractParsingObject parsingAPhrase, ConnectionEntry dependencyConnotation, String entry, SingleResult usdaResult) {
//		boolean foundHead=false,foundChild=false;
//		for(int i=0;i<parsingAPhrase.getFinalResults().size();i++){
//			QualifiedToken qt=parsingAPhrase.getFinalResults().get(i);
//
//			if(dependencyConnotation.getHead().getText()==qt.getText()){
//				foundHead=true;
//				addProductResult(parsingAPhrase, i, qt, "[Double usda, "+usdaResult.getFdcId()+"]");
//
//			}
//			if(dependencyConnotation.getChild().getText()==qt.getText()&&qt.getHead().equals(dependencyConnotation.getHead())){
//				foundChild=true;
//				addProductResult(parsingAPhrase, i, qt, "[Double usda, "+usdaResult.getFdcId()+"]");
//
//			}
//		}
//		if(!foundChild||!foundHead){
//			System.err.println("Entry '"+entry+"' found in words api, but not in tokens");
//		}
//	}

    private Map<String, Integer> calculateAdjacencies(AbstractParsingObject parsingAPhrase) {
        Map<String, Integer> retValue = new HashMap<>();


        for (int i = 0; i < parsingAPhrase.getFinalResults().size() - 1; i++) {
            QualifiedToken qt1 = parsingAPhrase.getFinalResults().get(i);
            QualifiedToken qt2 = parsingAPhrase.getFinalResults().get(i + 1);

            if (WordType.QuantityElement != qt1.getWordType() && WordType.QuantityElement != qt2.getWordType()) {
                retValue.put(qt1.getText() + " " + qt2.getText(), i);
            }

        }


        return retValue;


    }


    protected void categorizeAllElseAsProducts(AbstractParsingObject parsingAPhrase) {
        List<QualifiedToken> permissiveList = new ArrayList<QualifiedToken>();
        for (QualifiedToken qt : parsingAPhrase.getFinalResults()) {
            WordType type = qt.getWordType() == null || qt.getWordType() == WordType.Unknown ? WordType.ProductElement : qt.getWordType();
            permissiveList.add(new QualifiedToken(qt.getText(), qt.getLemma(), qt.getTag(), type));



        }
        parsingAPhrase.setPermissiveFinalResults(permissiveList);
    }

    protected void recategorize(AbstractParsingObject parsingAPhrase) {

        TokenizationResults correctedPhraseParsed = this.tokenizator.parse(parsingAPhrase.createCorrectedPhrase());

        parsingAPhrase.setCorrectedToknized(correctedPhraseParsed);

        TokenizationResults productPhraseparsed = this.tokenizator.parse(parsingAPhrase.getQuantitylessPhrase());

        parsingAPhrase.setQuantitylessTokenized(productPhraseparsed);


    }
    private void initializePrimaryTokensAndConnotations(AbstractParsingObject parsingAPhrase) {
        String phrase = parsingAPhrase.getBracketLessPhrase();

        TokenizationResults tokenized = this.tokenizator.parse(phrase);
        parsingAPhrase.setBracketlessTokenized(tokenized);
        DependencyTreeNode dependencyTreeRoot = tokenized.getDependencyTree();
        List<ConnectionEntry> originalPhraseConotations = tokenized.getAllTwoWordDependencies();
        Token foundToken = tokenized.findToken(tokenized.getTokens(), dependencyTreeRoot == null ? "" : dependencyTreeRoot.getText());
        originalPhraseConotations.add(new ConnectionEntry(new Token("ROOT", "", ""), foundToken));


        parsingAPhrase.setFromEntityLessConotations(originalPhraseConotations);


    }

    private void fillQuanAndProdPhrases(AbstractParsingObject parsingAPhrase) {
        String quantityPhrase = "", productPhrase = "";
        for (int i = 0; i < parsingAPhrase.getFinalResults().size(); i++) {
            QualifiedToken qt = parsingAPhrase.getFinalResults().get(i);
            if (WordType.QuantityElement == qt.getWordType()) {//&&productPhrase.equals("")) {
                quantityPhrase += qt.getText() + " ";
            } else if (WordType.PunctuationElement == qt.getWordType()) {
                //ignore
            } else {
                productPhrase += qt.getText() + " ";
            }

        }
        productPhrase = productPhrase.trim();
        quantityPhrase = quantityPhrase.trim();

        parsingAPhrase.setQuantityPhrase(quantityPhrase);
        parsingAPhrase.setQuantitylessPhrase(productPhrase);
    }

    protected void initialCategorization(AbstractParsingObject parsingAPhrase) {
        for (int i = 0; i < parsingAPhrase.getEntitylessTokenized().getTokens().size(); i++) {
            Token t = parsingAPhrase.getEntitylessTokenized().getTokens().get(i);

            if (PythonSpacyLabels.tokenisationCardinalLabel.equals(t.getTag()) || PythonSpacyLabels.listItemMarker.equals(t.getTag())) {
                addQuantityResult(parsingAPhrase, i, t, "[spacy tag:" + t.getTag() + "]");
                List<NamedEntity> cardinalEntities = parsingAPhrase.getCardinalEntities();

                for (NamedEntity cardinalEntity : cardinalEntities) {
                    if (cardinalEntity.getText().contains(t.getText())) {
                        if (!PythonSpacyLabels.entitiesCardinalLabel.equals(cardinalEntity.getLabel())) {
                            System.err.println("Tokenization and Ner labels do not match");
                        }
                    }
                }
            } else {
                try {
                    classifySingleWord(parsingAPhrase, i);
                } catch (WordNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }


    }

    public void classifySingleWord(AbstractParsingObject parsingAPhrase, int index) throws WordNotFoundException {
        TokenizationResults tokens = parsingAPhrase.getEntitylessTokenized();
        Token t = tokens.getTokens().get(index);
        String token = t.getText();
        WordType improperlyFoundType = improperlyFindType(parsingAPhrase, index, parsingAPhrase.getFutureTokens());
        if (improperlyFoundType != null) {
            QualifiedToken qt = new QualifiedToken(t, improperlyFoundType);
            qt.setReasoning("[improperly found, for now]");
            parsingAPhrase.addResult(index, qt);

            return;
        }
        if (Pattern.matches(punctuationRegex, token)) {
            parsingAPhrase.addResult(index, new QualifiedToken(t, WordType.PunctuationElement));

            return;
        }
		searchDbAndApis(parsingAPhrase, index, t);

	}

	private void searchDbAndApis(AbstractParsingObject parsingAPhrase, int index, Token t) throws WordNotFoundException {
		boolean found = checkWithPhrasesInDb(parsingAPhrase, index, t);
		if (!found) {
			found = checkWithResultsFromWordsApi(parsingAPhrase, index, t);

			if (!found) {
				found = checkWithResultsFromUsdaApi(parsingAPhrase, index, t);
			}

			if (!found) {
				parsingAPhrase.addResult(index, new QualifiedToken(t, null));
			}
		}

	}

	private boolean checkWithPhrasesInDb(AbstractParsingObject parsingAPhrase, int index, Token t) {
        List<PhraseFound> dbResults = searchDbForTextAndLemma(t);

        if (dbResults != null && !dbResults.isEmpty()) {
            PhraseFound phraseFound = dbResults.get(0);
            addExistingInDbPhrase(parsingAPhrase, phraseFound);



            QualifiedToken qualifiedToken = new QualifiedToken(t,phraseFound.getWordType());
            qualifiedToken.setReasoning("[DB: " + phraseFound.getReasoning() + "]");
            addResultBasedOnDb(parsingAPhrase, index, qualifiedToken, phraseFound);


            return true;
        }
        return false;

    }

    private boolean checkWithPhrasesInDb(AbstractParsingObject parsingAPhrase, int index, String twoWordEntry) {
        List<PhraseFound> dbResults = this.phraseFoundRepo.findByPhrase(twoWordEntry);
        dbResults=reduceList(dbResults);

        if (dbResults != null && !dbResults.isEmpty()) {
            PhraseFound phraseFound = dbResults.get(0);
            addExistingInDbPhrase(parsingAPhrase,phraseFound);
            String[] splitted=twoWordEntry.split(" ");
            for(int i=index;i<index+splitted.length;i++){
                QualifiedToken qualifiedToken = parsingAPhrase.getFinalResults().get(i);
                qualifiedToken.setReasoning("[DB: "+phraseFound.getReasoning()+"]");
                addResultBasedOnDb(parsingAPhrase, i, qualifiedToken, phraseFound);
            }


            return true;
        }
        return false;

    }

    private List<PhraseFound> searchDbForTextAndLemma(Token t) {
        List<PhraseFound> dbResults = this.phraseFoundRepo.findBySingleWordPhrase(t.getText());

        if(dbResults==null||dbResults.isEmpty()){
            dbResults = this.phraseFoundRepo.findBySingleWordPhrase(t.getLemma());
        }

        return reduceList(dbResults);

    }

    private List<PhraseFound> reduceList(List<PhraseFound> dbResults) {
        if(dbResults==null||dbResults.isEmpty())
            return new ArrayList<>();

	    Optional<PhraseFound> reduce = dbResults.stream().reduce((phraseFound, phraseFound2) -> pickOneWithBiggerProductTypesList(phraseFound, phraseFound2));

        if(reduce.isPresent()){
            List<PhraseFound> retValue=new ArrayList<>();
            retValue.add(reduce.get());
            return  retValue;
        }else{
            System.err.println("List of Phrases reduced to null, returning original");
            return dbResults;
        }
    }

    PhraseFound pickOneWithBiggerProductTypesList(PhraseFound p1, PhraseFound p2){
	    if(p1==null||p1.getTypesFoundForPhraseAndBase()==null){
	        return p2;
        }
        if(p2==null||p2.getTypesFoundForPhraseAndBase()==null){
            return p1;
        }


        if(p1.getTypesFoundForPhraseAndBase().size()>=p2.getTypesFoundForPhraseAndBase().size()){
            return p1;
        }else{
            return p2;
        }

    }

    private boolean checkWithResultsFromUsdaApi(AbstractParsingObject parsingAPhrase, int index, Token t) {
        UsdaResponse inApi = findInUsdaApi(t.getText(), 10);

        for (SingleResult sp : inApi.getFoods()) {
            String desc = sp.getDescription();
            if (desc.toLowerCase().equals(t.getText().toLowerCase())) {
                addProductResult(parsingAPhrase, index, t, "[usda api: " + sp.getFdcId() + "]");
                addFoundSingleWordPhrase(parsingAPhrase, parsingAPhrase.getFinalResults().get(index));

                return true;
            }
        }
        return false;
    }

    private boolean checkWithResultsFromWordsApi(AbstractParsingObject parsingAPhrase, int index, Token t)
            throws WordNotFoundException {
        ArrayList<WordsApiResult> wordResults = new ArrayList<WordsApiResult>();
        boolean found = searchForAllPossibleMeaningsInWordsApi(parsingAPhrase, wordResults, index, t);
        if (!found) {
            if (wordResults != null && !wordResults.isEmpty()) {
                WordsApiResult quantityTypeRecognized = checkQuantityTypesForWordObject(wordResults);
                if (quantityTypeRecognized != null) {
                    addQuantityResult(parsingAPhrase, index, t, quantityTypeRecognized);
                    return true;
                } else {
                    WordsApiResult productTypeRecognized = checkProductTypesForWordObject(wordResults);
                    if (productTypeRecognized != null) {
                        //checkOtherTokens(parsingAPhrase,index ,productTypeRecognized);
                        //change the approach.
                        //	parsingAPhrase.addResult(index, new QualifiedToken(t, WordType.ProductElement));

                        addProductResult(parsingAPhrase, index, t, productTypeRecognized);
                        addFoundSingleWordPhrase(parsingAPhrase, parsingAPhrase.getFinalResults().get(index));
                        return true;
                    } else {
                        //	parsingAPhrase.addResult(index, new QualifiedToken(t,null));
                        return false;
                    }
                }
            } else {
                parsingAPhrase.addResult(index, new QualifiedToken(t, WordType.Unknown));
                return false;
            }

        }

        if (found)
            return true;
        else
            return false;
    }

    private WordType improperlyFindType(AbstractParsingObject parsingAPhrase, int index,
                                        Map<Integer, QualifiedToken> map) {
        //TODO this should be deleted in the end
        TokenizationResults tokens = parsingAPhrase.getEntitylessTokenized();
        Token t = tokens.getTokens().get(index);

        if (impromperQuantityKeywords.contains(t.getText().toLowerCase()))
            return WordType.QuantityElement;

        if (impromperProductPropertyKeywords.contains(t.getText().toLowerCase()))
            return WordType.ProductPropertyElement;

        return null;
    }

    private void handleBracketsAndSetBracketLess(AbstractParsingObject parsingAPhrase) {
        String x = parsingAPhrase.getOriginalPhrase();
        if (x == null)
            parsingAPhrase.setBracketLessPhrase("");
        else {
            x = x.replaceAll("\\(.*\\)", "");
            parsingAPhrase.setBracketLessPhrase(x);
        }



    }

    private boolean searchForAllPossibleMeaningsInWordsApi(AbstractParsingObject parsingAPhrase,
                                                           ArrayList<WordsApiResult> wordResults, int index, Token t) throws WordNotFoundException {

        if (t == null || t.getText() == null || t.getText().replaceAll(" ", "").equals("")) {
            return false;
        } else {

            String token = t.getText();
            String lemma = t.getLemma();

            wordResults.addAll(wordsApiClient.searchFor(token));
            ifEmptyUpdateForLemma(lemma, wordResults);
            if (canWeFindQuantityInConvertApi(parsingAPhrase, index, t, token, lemma, wordResults))
                return true;

            ifEmptyUpdateForWikipediaBaseWord(wordResults, token);
            return false;
        }
    }

    private void ifEmptyUpdateForWikipediaBaseWord(ArrayList<WordsApiResult> wordResults, String token)
            throws WordNotFoundException {
        if (wordResults == null || wordResults.isEmpty()) {

            String baseWord = null;

            if (Pattern.matches(wikipediaCheckRegex, token)) {
                baseWord = wikipediaClient.getRedirectIfAny(token);
            }

            if ((baseWord == null || baseWord.isEmpty()) && Pattern.matches(convertApiCheckRegex, token)) {
                QuantityTranslation checkForTranslation = convertClient.checkForTranslation(token);
                if (checkForTranslation != null) {
                    WordsApiResult war = new WordsApiResultImpostor(checkForTranslation);
                    wordResults.add(war);
                }
            }
			if(baseWord!=null&&!baseWord.isEmpty())
			{
                wordResults.addAll(wordsApiClient.searchFor(baseWord));
            }
        }
    }

    private boolean canWeFindQuantityInConvertApi(AbstractParsingObject parsingAPhrase, int index, Token t, String token,
                                                  String lemma, ArrayList<WordsApiResult> wordResults) throws WordNotFoundException {
        if (wordResults == null || wordResults.isEmpty()) {

			if((lemma!=null&&!lemma.isEmpty())&&Pattern.matches(convertApiCheckRegex, lemma))
			{
                QuantityTranslation checkForTranslation = convertClient.checkForTranslation(token);
                if (checkForTranslation != null) {
                    addQuantityResult(parsingAPhrase, index, t, " [convert api:" + checkForTranslation.getMultiplier() + " " + checkForTranslation.getTargetAmountType() + "]");
                    addFoundSingleWordPhrase(parsingAPhrase, parsingAPhrase.getFinalResults().get(index));
                    return true;
                }
            }
        }

        return false;
    }

    protected void ifEmptyUpdateForLemma(String lemma, ArrayList<WordsApiResult> wordResults) {
        if (wordResults == null || wordResults.isEmpty()) {

			if(lemma!=null&&!lemma.isEmpty()&&!lemma.equals("O"))
			{
                wordResults.addAll(wordsApiClient.searchFor(lemma));

            }
        }
    }

    protected void addQuantityResult(AbstractParsingObject parsingAPhrase, int index, Token t, WordsApiResult quantityTypeRecognized) {
        QualifiedToken result = new QualifiedToken(t, WordType.QuantityElement);
        result.setReasoning(quantityTypeRecognized == null || quantityTypeRecognized.getDefinition() == null ? "" : quantityTypeRecognized.getDefinition());
        parsingAPhrase.addResult(index, result);
    }

    private void addFoundSingleWordPhrase(AbstractParsingObject parsingAPhrase, QualifiedToken result) {
        PhraseFound pf = new PhraseFound(result.getLemma(), result.getWordType(), result.getReasoning());
        parsingAPhrase.addPhraseFound(pf);
        //	this.phrasesRepo.save(pf);
    }

    private void addFoundLongPhrase(AbstractParsingObject parsingAPhrase, String phrase, WordType wordType, String reasoning) {
        PhraseFound pf = new PhraseFound(phrase, wordType, reasoning);
        parsingAPhrase.addPhraseFound(pf);
        //	this.phrasesRepo.save(pf);
    }

    protected void addQuantityResult(AbstractParsingObject parsingAPhrase, int index, Token t, String reasoning) {
        QualifiedToken result = new QualifiedToken(t, WordType.QuantityElement);
        result.setReasoning(reasoning);
        parsingAPhrase.addResult(index, result);
    }

    protected void addProductResult(AbstractParsingObject parsingAPhrase, int index, Token t, WordsApiResult productTypeRecognized) {
        QualifiedToken result = new QualifiedToken(t, WordType.ProductElement);
        result.setReasoning(productTypeRecognized == null || productTypeRecognized.getReasoningForFound() == null ? "" : productTypeRecognized.getReasoningForFound());
        parsingAPhrase.addResult(index, result);
    }

    protected void addProductResult(AbstractParsingObject parsingAPhrase, int index, Token t, String reasoning) {
        QualifiedToken result = new QualifiedToken(t, WordType.ProductElement);
        result.setReasoning(reasoning);
        parsingAPhrase.addResult(index, result);
    }
    protected void addResultBasedOnDb(AbstractParsingObject parsingAPhrase, int index, Token t, PhraseFound singleWordPhrase) {
        QualifiedToken result = new QualifiedToken(t, singleWordPhrase.getWordType());
        result.setReasoning("[DB: " + singleWordPhrase.getReasoning() + "]");
        parsingAPhrase.addResult(index, result);
    }

    private void addExistingInDbPhrase(AbstractParsingObject parsingAPhrase, PhraseFound pf) {
    //    PhraseFound pf = new PhraseFound(phrase, wordType, reasoning);
        parsingAPhrase.addPhraseFound(pf);
        //	this.phrasesRepo.save(pf);
    }

    private void checkOtherTokens(AbstractParsingObject parsingAPhrase, int index, WordsApiResult productTypeRecognized) {
        if (parsingAPhrase.getFutureTokens().containsKey(index)) {
            return;
        }

        List<String> setOfRelevantWords = getSetOfAllRelevantWords(productTypeRecognized);

        TokenizationResults tokens = parsingAPhrase.getEntitylessTokenized();
        boolean extendedWordFound = false;

        for (int i = 0; i < setOfRelevantWords.size() && !extendedWordFound; i++) {
            //check if not longer than 2 words (for now)
            String expandedWordFromApi = setOfRelevantWords.get(i);
            extendedWordFound = wasExtendedWordFound(parsingAPhrase, index, tokens, expandedWordFromApi);
        }

        if (!extendedWordFound) {
            Token t = parsingAPhrase.getEntitylessTokenized().getTokens().get(index);
            parsingAPhrase.addResult(index, new QualifiedToken(t, WordType.ProductElement));
        }
//		}

    }

    private boolean wasExtendedWordFound(AbstractParsingObject parsingAPhrase, int index, TokenizationResults tokens,
                                         String expandedWordFromApi) {
        boolean extendedWordFound = false;
        int expandedWordFromApiLength = expandedWordFromApi.split(" ").length;
		if(expandedWordFromApiLength<3)
		{
            List<Token> actualTokens = tokens.getTokens();
            extendedWordFound = wereAnyTokensMarkedBesideCurrentDueToAdjacency(parsingAPhrase, index, expandedWordFromApi,
                    expandedWordFromApiLength, actualTokens);
            if (!extendedWordFound) {
                extendedWordFound = wereAnyTokensReplacedDueToDependencyTree(parsingAPhrase, expandedWordFromApi);
            }
        }
        return extendedWordFound;
    }

    private List<String> getSetOfAllRelevantWords(WordsApiResult productTypeRecognized) {
        List<String> setOfRelevantWords = new ArrayList<String>();
        //we take them from the longest to the shortest
        setOfRelevantWords.addAll(productTypeRecognized.getChildTypes());
        setOfRelevantWords.addAll(productTypeRecognized.getSynonyms());
        setOfRelevantWords.sort(Collections.reverseOrder());
        return setOfRelevantWords;
    }

    private boolean wereAnyTokensReplacedDueToDependencyTree(AbstractParsingObject parsingAPhrase,
                                                             String expandedWordFromApi) {
        List<ConnectionEntry> connotations = parsingAPhrase.getFromEntityLessConotations();

        TokenizationResults extendedFromApiTokenized = this.tokenizator.parse(expandedWordFromApi);
        List<ConnectionEntry> dependenciesFromExtendedWord = extendedFromApiTokenized.getAllTwoWordDependencies();

        for (ConnectionEntry connotationFromExendedPhrase : dependenciesFromExtendedWord) {
            for (ConnectionEntry connotationFromPhrase : connotations) {
                if (areThoseConnectionsBetweenTheSameWords(connotationFromExendedPhrase, connotationFromPhrase)) {
                    //				System.out.println("found");

                    goThroughTokensAnMarkConnected(parsingAPhrase, connotationFromExendedPhrase);

                    return true;
                }

            }
        }
        return false;
    }

    private void goThroughTokensAnMarkConnected(AbstractParsingObject parsingAPhrase,
                                                ConnectionEntry connotationFromExendedPhrase) {
        boolean headFound = false, childFound = false;
        //check current token
        if (parsingAPhrase.getFinalResults().size() < parsingAPhrase.getEntitylessTokenized().getTokens().size() && (!headFound || !childFound)) {
            Token currentToken = parsingAPhrase.getEntitylessTokenized().getTokens().get(parsingAPhrase.getFinalResults().size());
            headFound = checkForHead(parsingAPhrase, connotationFromExendedPhrase, headFound, currentToken);
            childFound = checkForChild(parsingAPhrase, connotationFromExendedPhrase, childFound, currentToken);
        }
        //checking past tokens
        for (int i = 0; i < parsingAPhrase.getFinalResults().size() && (!headFound || !childFound); i++) {
            if ((connotationFromExendedPhrase.getHead().getText().equals(parsingAPhrase.getFinalResults().get(i).getText()))
                    || (connotationFromExendedPhrase.getHead().getLemma().equals(parsingAPhrase.getFinalResults().get(i).getLemma()))) {
                headFound = checkForHeadInPast(parsingAPhrase, connotationFromExendedPhrase, headFound, i);
            } else if ((connotationFromExendedPhrase.getChild().getText().equals(parsingAPhrase.getFinalResults().get(i).getText()))
                    || (connotationFromExendedPhrase.getChild().getLemma().equals(parsingAPhrase.getFinalResults().get(i).getLemma()))) {
                childFound = checkForChildInPast(parsingAPhrase, connotationFromExendedPhrase, i);
            }
        }
        //if still not both found, check future tokens as well
        if (parsingAPhrase.getEntitylessTokenized().getTokens().size() > parsingAPhrase.getFinalResults().size() && (!headFound || !childFound)) {

            Token currentToken = parsingAPhrase.getEntitylessTokenized().getTokens().get(parsingAPhrase.getFinalResults().size());
            headFound = checkForHeadInFuture(parsingAPhrase, connotationFromExendedPhrase, headFound, currentToken);
            childFound = checkForChildInFuture(parsingAPhrase, connotationFromExendedPhrase, childFound, currentToken);
        }

    }

    private boolean checkForChildInFuture(AbstractParsingObject parsingAPhrase,
                                          ConnectionEntry connotationFromExendedPhrase, boolean childFound, Token currentToken) {
        if (!childFound) {
            if (currentToken.getText().equals(connotationFromExendedPhrase.getChild().getText())
                    || currentToken.getLemma().equals(connotationFromExendedPhrase.getChild().getLemma())) {
                QualifiedToken result = new QualifiedToken(connotationFromExendedPhrase.getChild().getText(),
                        connotationFromExendedPhrase.getChild().getLemma(), connotationFromExendedPhrase.getChild().getTag(), WordType.ProductElement);
                parsingAPhrase.getFinalResults().add(result);
                childFound = true;
            }
        }
        return childFound;
    }

    private boolean checkForHeadInFuture(AbstractParsingObject parsingAPhrase,
                                         ConnectionEntry connotationFromExendedPhrase, boolean headFound, Token currentToken) {
        if (!headFound) {
            if (currentToken.getText().equals(connotationFromExendedPhrase.getHead().getText())
                    || currentToken.getLemma().equals(connotationFromExendedPhrase.getHead().getLemma())) {
                QualifiedToken result = new QualifiedToken(connotationFromExendedPhrase.getHead().getText(),
                        connotationFromExendedPhrase.getHead().getLemma(), connotationFromExendedPhrase.getHead().getTag(), WordType.ProductElement);
                parsingAPhrase.getFinalResults().add(result);
                parsingAPhrase.getDependencyConotationsFound().add(connotationFromExendedPhrase);

                headFound = true;
            }
        }
        return headFound;
    }

    private boolean checkForChildInPast(AbstractParsingObject parsingAPhrase,
                                        ConnectionEntry connotationFromExendedPhrase, int i) {
        boolean childFound;
        if (parsingAPhrase.getFinalResults().get(i).getWordType() != null) {
            System.out.println("already classified word classified again due to dependency: " + i);
        } else {

            QualifiedToken result = new QualifiedToken(connotationFromExendedPhrase.getChild().getText(),
                    parsingAPhrase.getFinalResults().get(i).getLemma(), parsingAPhrase.getFinalResults().get(i).getTag(), WordType.ProductElement);
            parsingAPhrase.getFinalResults().set(i, result);
        }
        childFound = true;
        return childFound;
    }

    private boolean checkForHeadInPast(AbstractParsingObject parsingAPhrase,
                                       ConnectionEntry connotationFromExendedPhrase, boolean headFound, int i) {
        if (parsingAPhrase.getFinalResults().get(i).getWordType() != null) {
            System.out.println("already classified word classified again due to dependency: " + i);
        } else {

            QualifiedToken result = new QualifiedToken(connotationFromExendedPhrase.getHead().getText(),
                    parsingAPhrase.getFinalResults().get(i).getLemma(), parsingAPhrase.getFinalResults().get(i).getTag(), WordType.ProductElement);
            parsingAPhrase.getFinalResults().set(i, result);
            parsingAPhrase.getDependencyConotationsFound().add(connotationFromExendedPhrase);

            headFound = true;
        }
        return headFound;
    }

    private boolean checkForChild(AbstractParsingObject parsingAPhrase, ConnectionEntry connotationFromExendedPhrase,
                                  boolean childFound, Token currentToken) {
        if (!childFound) {
            if (currentToken.getText().equals(connotationFromExendedPhrase.getChild().getText())
                    || currentToken.getLemma().equals(connotationFromExendedPhrase.getChild().getLemma())) {
                parsingAPhrase.getFinalResults().add(new QualifiedToken(connotationFromExendedPhrase.getChild(), WordType.ProductElement));
                childFound = true;
            }
        }
        return childFound;
    }

    private boolean checkForHead(AbstractParsingObject parsingAPhrase, ConnectionEntry connotationFromExendedPhrase,
                                 boolean headFound, Token currentToken) {
        if (!headFound) {
            if (currentToken.getText().equals(connotationFromExendedPhrase.getHead().getText())
                    || currentToken.getLemma().equals(connotationFromExendedPhrase.getHead().getLemma())) {
                parsingAPhrase.getFinalResults().add(new QualifiedToken(connotationFromExendedPhrase.getHead(), WordType.ProductElement));
                parsingAPhrase.getDependencyConotationsFound().add(connotationFromExendedPhrase);

                headFound = true;
            }
        }
        return headFound;
    }



    private boolean areThoseConnectionsBetweenTheSameWords(ConnectionEntry connotationFromExendedPhrase,
                                                           ConnectionEntry connotationFromPhrase) {
        if (connotationFromPhrase.getHead().getText().equals(connotationFromExendedPhrase.getHead().getText())
                || connotationFromPhrase.getHead().getLemma().equals(connotationFromExendedPhrase.getHead().getLemma())) {
            if (connotationFromPhrase.getChild().getText().equals(connotationFromExendedPhrase.getChild().getText())
                    || connotationFromPhrase.getChild().getLemma().equals(connotationFromExendedPhrase.getChild().getLemma())) {
                return true;
            }
        }
        return false;


    }

    private boolean wereAnyTokensMarkedBesideCurrentDueToAdjacency(AbstractParsingObject parsingAPhrase, int index,
                                                                   String expandedWordFromApi, int expandedWordFromApiLength,
                                                                   List<Token> actualTokens) {
        if (parsingAPhrase.getEntitylessString().indexOf(expandedWordFromApi) >= 0) {

            if (expandedWordFromApiLength > 1) {
                if (index - expandedWordFromApiLength >= 0) {
                    //start at first wor
                    wereAnyTokensMarkedBeforeCurrentOne(parsingAPhrase, index, expandedWordFromApi,
                            expandedWordFromApiLength, actualTokens);
                    return true;

                    //does it end after current index?
                } else if (expandedWordFromApiLength + index <= actualTokens.size()) {
                    if (wereAnyTokensMarkedAfterCurrent(parsingAPhrase, index, expandedWordFromApi,
                            expandedWordFromApiLength, actualTokens)) {
                        return true;
                    }
                } else {
                    System.err.println("well, we got some word in the middle of sentence case in word api");
                }
            }
        }
        return false;
    }

    private boolean wereAnyTokensMarkedBeforeCurrentOne(AbstractParsingObject parsingAPhrase, int index,
                                                        String expandedWordFromApi, int expandedWordFromApiLength, List<Token> actualTokens) {
        List<Token> subList = actualTokens.subList(index - expandedWordFromApiLength + 1, index + 1);



        String fused = subList.stream().map(s -> s.getText()).collect(Collectors.joining(" "));
        if (fused.indexOf(expandedWordFromApi) >= 0) {
            List<String> conotation = new ArrayList<String>();
            //		QualifiedToken result=new QualifiedToken(fused, "fused", "fused", WordType.ProductElement);
            for (int i = index - expandedWordFromApiLength + 1; i <= index; i++) {
                QualifiedToken resultQt = new QualifiedToken(actualTokens.get(i), WordType.ProductElement);
                resultQt.setMergeType(MergeType.ADJACENCY);
                parsingAPhrase.getFinalResults().set(i, resultQt);
                parsingAPhrase.addResult(index, resultQt);
                conotation.add(actualTokens.get(i).getText());
            }
            parsingAPhrase.getAdjacentyConotationsFound().add(conotation);

            return true;
        } else {
            return false;
        }
    }

    private boolean wereAnyTokensMarkedAfterCurrent(AbstractParsingObject parsingAPhrase, int index,
                                                    String expandedWordFromApi, int expandedWordFromApiLength,
                                                    List<Token> actualTokens) {
        List<Token> subList = actualTokens.subList(index, expandedWordFromApiLength + index);
        String fused = subList.stream().map(s -> s.getText()).collect(Collectors.joining(" "));
        if (fused.indexOf(expandedWordFromApi) >= 0) {
            //			QualifiedToken result=QualifiedToken.createMerged(fused,WordType.ProductElement);
            //
            //			addOtherWordsFromExpandedTofututreTokens(index, parsingAPhrase,expandedWordFromApi);
            for (int i = index; i < expandedWordFromApiLength + index; i++) {
                QualifiedToken resultQt = new QualifiedToken(subList.get(i), WordType.ProductElement);
                resultQt.setMergeType(MergeType.ADJACENCY);


                parsingAPhrase.addResult(i, resultQt);
                parsingAPhrase.addFutureToken(i, resultQt);

            }
            return true;
        } else {
            return false;
        }

    }

    private void addOtherWordsFromExpandedTofututreTokens(int index, AbstractParsingObject parsingAPhrase,
                                                          String expandedWordFromApi) {

        String[] split = expandedWordFromApi.split(" ");
        for (int i = index; i < split.length + index; i++) {

            parsingAPhrase.addFutureToken(i, new QualifiedToken(parsingAPhrase.getEntitylessTokenized().getTokens().get(i), WordType.ProductElement));
        }
    }

    protected  WordsApiResult checkProductTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
        WordsApiResult war = checkForTypes(wordResults, productTypeKeywords, new ArrayList<>());
        if (war != null)
            return war;

        WordsApiResult war1 = checkWordsTypesWithPhrasesInDb(wordResults,WordType.ProductElement);
        if (war1 != null)
            return war1;

        return null;
    }


    private  WordsApiResult checkQuantityTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
        WordsApiResult war = checkForTypes(wordResults, quantityTypeKeywords, quantityAttributeKeywords);
        if (war != null)
            return war;

        WordsApiResult war1 = checkWordsTypesWithPhrasesInDb(wordResults,WordType.QuantityElement);
        if (war1 != null)
            return war1;

        return null;
    }

    private WordsApiResult checkWordsTypesWithPhrasesInDb(ArrayList<WordsApiResult> wordResults, WordType WordType) {
        for (WordsApiResult war1 : wordResults) {

            for (String typeOf : war1.getTypeOf()) {
                List<PhraseFound> byPhrase = this.phraseFoundRepo.findByPhrase(typeOf);

                if (byPhrase != null && !byPhrase.isEmpty()) {
                    for (PhraseFound pf : byPhrase) {
                        if (pf.getWordType().equals(WordType)) {
                            war1.setReasoningForFound("[WordsApi transitive, subtype of:'" + pf.getPhrase() + "']");

                            return war1;
                        }
                    }
                }


            }
        }
        return null;
    }

    protected WordsApiResult checkForTypes(ArrayList<WordsApiResult> wordResults, ArrayList<String> keywordsForTypeconsidered, ArrayList<String> attributesForTypeConsidered) {
        for (WordsApiResult war : wordResults) {
            if (war instanceof WordsApiResultImpostor) {
                return war;
            }
            String typeOfTagRecognized = checkIfPropertiesFromWordsApiContainKeywords(war.getOriginalWord(), war.getTypeOf(), keywordsForTypeconsidered);

            if (typeOfTagRecognized != null && !typeOfTagRecognized.isEmpty()) {
                war.setReasoningForFound("[WordsApi: " + war.getDefinition() + " (" + typeOfTagRecognized + ")]");
                return war;
            }
            String attributeTagRecognized = checkIfPropertiesFromWordsApiContainKeywords(war.getOriginalWord(), war.getAttribute(), attributesForTypeConsidered);

            if (attributeTagRecognized != null && !attributeTagRecognized.isEmpty()) {
                war.setReasoningForFound("[WordsApi: " + war.getDefinition() + " (" + attributeTagRecognized + ")]");

                return war;
            }





        }
        return null;
    }


    private static String checkIfPropertiesFromWordsApiContainKeywords(String productName, ArrayList<String> typeResults, ArrayList<String> keywords) {
        for (String typeToBeChecked : typeResults) {
            for (String typeConsidered : keywords) {
                if (typeToBeChecked.equalsIgnoreCase(typeConsidered)||typeToBeChecked.contains(" "+typeConsidered)||typeToBeChecked.contains(typeConsidered+" ")) {
//					System.out.println(productName+" -> "+typeToBeChecked+" : "+typeConsidered);

                    return typeToBeChecked;
                }
            }
        }
        return null;
    }



    public void checkDepartmentForKeywords(ProductParsingProcessObject parsingAPhrase) {
        ProductData product = parsingAPhrase.getProduct();
        String department = product.getDepartment();
        for (String keyword : freshFoodKeywords) {
            if (department != null && department.toLowerCase().contains(keyword)) {
                parsingAPhrase.getProductTypeReasoning().put("department keyword: " + keyword, ProductType.fresh);

              //  return ProductType.fresh;
            }

            if (product instanceof Tesco_Product) {
                String superdepartment = ((Tesco_Product) product).getSuperdepartment();
                if (superdepartment != null && superdepartment.toLowerCase().contains(keyword)) {
                    parsingAPhrase.getProductTypeReasoning().put("department keyword: " + keyword, ProductType.fresh);

                //    return ProductType.fresh;
                }

            }

        }

    }

    public void checkQuantities(ProductParsingProcessObject parsingAPhrase) {
        ProductData product = parsingAPhrase.getProduct();
        if (product.getTotalQuantity() != null && product.getTotalQuantity().equals(product.getQuantity())) {
            parsingAPhrase.getProductTypeReasoning().put("quantity difference", ProductType.processed);



        }
    }
}
