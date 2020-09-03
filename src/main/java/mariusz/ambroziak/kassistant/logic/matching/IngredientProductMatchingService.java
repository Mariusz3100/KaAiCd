package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.*;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.*;
import mariusz.ambroziak.kassistant.logic.AbstractParser;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.logic.shops.ShopProductParser;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.matching.InputCases;
import mariusz.ambroziak.kassistant.pojos.matching.MatchingProcessResult;
import mariusz.ambroziak.kassistant.pojos.matching.MatchingProcessResultList;
import mariusz.ambroziak.kassistant.pojos.matching.ProductMatchingResult;
import mariusz.ambroziak.kassistant.pojos.parsing.*;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.morrisons.MorrisonsClientService;
import mariusz.ambroziak.kassistant.webclients.morrisons.Morrisons_Product;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoFromFileService;
import mariusz.ambroziak.kassistant.webclients.webknox.RecipeSearchApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class IngredientProductMatchingService extends AbstractParser {

	@Autowired
	IngredientPhraseParser ingredientPhraseParser;
	@Autowired
	ShopProductParser shopProductParser;

	@Autowired
    ParsingBatchRepository parsingBatchRepository;

    @Autowired
    ProductParsingResultRepository productParsingResultRepository;


    @Autowired
    TescoProductRepository tescoProductRepository;

    @Autowired
    private TescoFromFileService tescoFromFileService;

    @Autowired
    IngredientPhraseLearningCaseRepository ingredientPhraseLearningCaseRepository;
    @Autowired
    MorrisonsClientService morrisonsClientService;

    @Autowired
    MorrisonProductRepository morrisonProductRepository;

    @Autowired
    MatchFoundRepository matchFoundRepository;


    @Autowired
    MatchExpectedRepository matchExpectedRepository;

    @Autowired
    RecipeSearchApiClient recipeSearchApiClient;

//    public List<MatchingProcessResult> parseMatchListAndJudgeResults(List<IngredientLearningCase> ingredientLearningCasesFromDb,boolean saveInDb) {
//
//
//
//    }
    public List<MatchingProcessResult> parseMatchAndJudgeResultsFromDbMatches(String recipeId) {
        List<IngredientLearningCase> andSaveIngredientsForRecipe = this.recipeSearchApiClient.getAndSaveIngredientsForRecipe(recipeId);

        if(andSaveIngredientsForRecipe==null||andSaveIngredientsForRecipe.isEmpty()){
            return new ArrayList<>();
        }else{
            List<MatchingProcessResult> matchingProcessResults = parseMatchList(andSaveIngredientsForRecipe, true);
            return matchingProcessResults;
        }

    }


    public List<MatchingProcessResult> parseMatchAndJudgeResultsFromDbMatches(boolean saveInDb) {
        Iterable<MatchExpected> allExpected = this.matchExpectedRepository.findAll();
        Set<String> setOfExpected=new HashSet<>();
        allExpected.forEach(e->setOfExpected.add(e.getIngredient()));
        List<IngredientLearningCase> ingredientLearningCases=new ArrayList<>();

        for(String ingredientLine:setOfExpected){
            List<IngredientLearningCase> byOriginalPhrase = this.ingredientPhraseLearningCaseRepository.findByOriginalPhrase(ingredientLine);

            if(byOriginalPhrase==null||byOriginalPhrase.isEmpty()){
                System.err.println("Ingredient learning case not in db");

            }else{
                ingredientLearningCases.add(byOriginalPhrase.get(0));
            }

        }

        List<MatchingProcessResult> matchingProcessResults = parseMatchList(ingredientLearningCases,saveInDb);

       // List<MatchingProcessResult> matchingProcessResults = parseMatchAndGetResultsFromDbAllCases(saveInDb);
        for(MatchingProcessResult mpr:matchingProcessResults){
            ParsingResult ingredientParsingDetails = mpr.getIngredientParsingDetails();
            String ingredientPhrase=ingredientParsingDetails.getOriginalPhrase();

            List<MatchExpected> expectedForIngredient = this.matchExpectedRepository.findByIngredient(ingredientPhrase);

            if(expectedForIngredient==null||expectedForIngredient.isEmpty()){
                System.err.println("Ingredient not in db");
            }else{
                for (ProductMatchingResult pmr : mpr.getProductsConsideredParsingResults()) {
                    String productName = pmr.getBaseResult().getOriginalPhrase().trim();

                    List<MatchExpected> collect = expectedForIngredient.stream().filter(me -> me.getProduct().trim().equals(productName)).collect(Collectors.toList());

                    if(collect.stream().filter(me->!me.isExpectedVerdict()&&me.getProduct().isEmpty()).count()>0){
                        pmr.setExpectedVerdict(false);
                    }else {
                        if (collect.size() > 0) {
                            if (collect.get(0).isExpectedVerdict()) {
                                pmr.setExpectedVerdict(true);
                                expectedForIngredient = expectedForIngredient.stream().filter(me -> !me.getProduct().equals(productName)).collect(Collectors.toList());

                            }
                        } else {
                            pmr.setExpectedVerdict(false);
                        }
                    }
                }

          //      mpr.setProductsNotFound(missing);

            }
            List<String> missing = expectedForIngredient.stream().map(me -> me.getProduct()).filter(s -> !s.equals("")).collect(Collectors.toList());

            if(missing.isEmpty())
                missing.add("test");

            mpr.setProductNamesNotFound(missing);
            mpr.setIncorrectProductsConsideredParsingResults(mpr.getProductsConsideredParsingResults().stream().filter(result->result.isCalculatedVerdict()!=result.isExpectedVerdict()).collect(Collectors.toList()));
        }

        return matchingProcessResults;
    }

    public List<MatchingProcessResult> parseMatchAndGetResultsFromDbAllCases(boolean saveInDb) {
        List<IngredientLearningCase> ingredientLearningCasesFromDb = this.ingredientPhraseParser.getIngredientLearningCasesFromDb();
        List<MatchingProcessResult> parseMatchAndGetResultsFromDb = parseMatchList(ingredientLearningCasesFromDb,saveInDb);

        return parseMatchAndGetResultsFromDb;

    }

    public List<MatchingProcessResult> parseMatchAndGetResultsFromDbForSingleCase(long ingredientLEarningCaseID,boolean saveInDb) {
        List<IngredientLearningCase> ingredientLearningCasesFromDb = new ArrayList<>();

        ingredientLearningCasesFromDb.add(this.ingredientPhraseLearningCaseRepository.findById(ingredientLEarningCaseID));
        List<MatchingProcessResult> parseMatchAndGetResultsFromDb = parseMatchList(ingredientLearningCasesFromDb,saveInDb);

        return parseMatchAndGetResultsFromDb;

    }



    public List<MatchingProcessResult> parseMatchList(List<IngredientLearningCase> ingredientLearningCasesFromDb,boolean saveInDb) {
        List<MatchingProcessResult> retValue = new ArrayList<>();
        ParsingBatch batchObject = null;
        for (IngredientLearningCase er : ingredientLearningCasesFromDb) {
            try {
                IngredientPhraseParsingProcessObject parsingAPhrase = this.ingredientPhraseParser.processSingleCase(er);
                ParsingResult ingredientResult = this.ingredientPhraseParser.createResultObject(parsingAPhrase);
                IngredientPhraseParsingResult ingredientPhraseParsingResult=null;

                MatchingProcessResult match = new MatchingProcessResult();
                match.setIngredientParsingDetails(ingredientResult);
                List<ProductParsingResult> parsingResultList = retrieveProductCandidates(parsingAPhrase);
                parsingResultList.sort((o1, o2) -> {return o1.getOriginalName().compareTo(o2.getOriginalName());});
                for (ProductParsingResult pr : parsingResultList) {
                    ProductData product = getProductFromDb(pr);
                    if (product != null) {
                        ProductParsingProcessObject pppo = new ProductParsingProcessObject(product, new ProductLearningCase());
                        this.shopProductParser.parseProductParsingObjectWithNamesComparison(pppo);
                        ParsingResult ppr = this.shopProductParser.createResultObject(pppo);
                        ProductMatchingResult pmr = calculateMatchingResult(parsingAPhrase, pppo, ppr);
                        match.addProductsConsideredParsingResults(pmr);

                        if(saveInDb) {
                            if (batchObject == null) {
                                batchObject = new ParsingBatch();
                                parsingBatchRepository.save(batchObject);
                            }
                            if (ingredientPhraseParsingResult == null)
                                ingredientPhraseParsingResult = this.ingredientPhraseParser.saveResultAndPhrasesInDb(parsingAPhrase, batchObject);
                            saveProductAndMatchInDb(batchObject, ingredientPhraseParsingResult, pppo, pmr);
                        }
                    }
                }
                retValue.add(match);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return retValue;
    }

    private void saveProductAndMatchInDb(ParsingBatch batchObject, IngredientPhraseParsingResult ingredientPhraseParsingResult, ProductParsingProcessObject pppo, ProductMatchingResult pmr) {
        try {
            ProductParsingResult productParsingResult = this.shopProductParser.saveResultInDb(pppo, batchObject);
            MatchFound mf = new MatchFound();
            mf.setProduct(productParsingResult);
            mf.setBatch(batchObject);
            mf.setIngredient(ingredientPhraseParsingResult);
            mf.setVerdict(pmr.isCalculatedVerdict());

            this.matchFoundRepository.save(mf);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<ProductParsingResult> retrieveProductCandidates(IngredientPhraseParsingProcessObject parsingAPhrase) {
        String markedWords = parsingAPhrase.getFinalResults().stream().filter(s -> s.getWordType() == WordType.ProductElement).map(s -> s.getLemma()).collect(Collectors.joining(" "));//ingredientResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));
        ProductType foodTypeClassified = parsingAPhrase.getFoodTypeClassified();
        List<ProductParsingResult> parsingResultList = searchForProductResults(markedWords,foodTypeClassified);
   //     parsingResultList = parsingResultList.subList(0, Math.min(20, parsingResultList.size()));
        return parsingResultList;
    }



    private ProductMatchingResult calculateMatchingResult(IngredientPhraseParsingProcessObject parsingAPhrase, ProductParsingProcessObject pppo, ParsingResult ppr) {
        ProductMatchingResult pmr = new ProductMatchingResult(ppr);


        List<QualifiedToken> ingredientWordsMarked = parsingAPhrase.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
        List<QualifiedToken> productWordsMarked = pppo.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
        Set<String> matched = new HashSet<>();
        Set<String> ingredientSurplus = new HashSet<>();
        List<String> productSurplus = new ArrayList<>();


        for (QualifiedToken ingredientQt : ingredientWordsMarked) {
            if (productWordsMarked.stream().filter(productQt -> productQt.compareWithToken(ingredientQt)).findAny().isPresent()) {
                matched.add(ingredientQt.getLemma());
            } else {
                ingredientSurplus.add(ingredientQt.getLemma());
            }

        }
        productSurplus = productWordsMarked.stream().filter(qt -> !matched.contains(qt.getLemma())).map(t -> t.getLemma()).distinct().collect(Collectors.toList());

        List<String> ingSurplusList=new ArrayList<>();
        ingSurplusList.addAll(ingredientSurplus);

        List<String> matchedList=new ArrayList<>();
        matchedList.addAll(matched);

        CalculatedResults cr = new CalculatedResults(ingSurplusList, matchedList, productSurplus, matchedList);
        pmr.setWordsMatching(cr);
        boolean namesMatch=ingredientSurplus.isEmpty() && productSurplus.isEmpty();
        boolean typesMatch=checkTypesCompatibility(parsingAPhrase.getFoodTypeClassified(),pppo.getFoodTypeClassified());


        pmr.setCalculatedVerdict(namesMatch&&typesMatch);
        return pmr;
    }

    private ProductData getProductFromDb(ProductParsingResult pr) {
       // List<Tesco_Product> byName = this.tescoProductRepository.findByName(pr.getOriginalName());
        List<Morrisons_Product> morrisons_products = this.morrisonProductRepository.findByName(pr.getOriginalName());
        if (morrisons_products.size() > 0)
            return morrisons_products.get(0);
        else
            return null;

    }


    private List<ProductParsingResult> searchForProductResults(String markedWords, ProductType typeSearched) {
        List<ProductParsingResult> retValue = new ArrayList<>();
        //return this.productParser.tescoSearchForParsings(markedWords);
        if(markedWords!=null&&!markedWords.isEmpty()) {
            List<String> wordsToFind = Arrays.asList(markedWords.split(" "));
//		Set<String> names = tescoFromFileService.nameToProducts.keySet();

            Iterable<ProductParsingResult> allProdResults = productParsingResultRepository.findAll();

            for (ProductParsingResult productParsingResult : allProdResults) {
                String nameLowercase = productParsingResult.getMinimalResultsCalculated().toLowerCase();
                if (wordsToFind.stream().filter(wordToFind -> !nameLowercase.contains(wordToFind)).count() == 0
                        && checkTypesCompatibility(typeSearched, productParsingResult.getTypeCalculated())
                ) {

                    if (retValue.stream().filter(ppr -> ppr.getOriginalName().equals(productParsingResult.getOriginalName())).count() < 1)
                        retValue.add(productParsingResult);
                }
            }
        }

        return retValue;
    }

    private boolean checkTypesCompatibility(ProductType typeSearched, ProductType productParsingResult) {
        return productParsingResult.equals(typeSearched)
                ||ProductType.unknown.equals(typeSearched)
                ||ProductType.unknown.equals(productParsingResult);
    }


    public InputCases retrieveAllIngredientsProductsAndMatchesExpectedConsidered(){
        InputCases retValue=new InputCases();

        List<String> ingredients=new ArrayList<>();

        this.ingredientPhraseLearningCaseRepository.findAll().forEach(ilc->ingredients.add(ilc.getOriginalPhrase()));

        List<String> products=new ArrayList<>();

        this.morrisonProductRepository.findAll().forEach(p->products.add(p.getName()));


        List<MatchExpected> matches=new ArrayList<>();

        this.matchExpectedRepository.findAll().forEach(me->matches.add(me));

        retValue.setIngredientLines(ingredients);
        retValue.setProductNames(products);
        retValue.setMatchesExpected(matches);

        return retValue;
    }

}
