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

    public static final int productsToBeParsedCutout = 30;
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
            judgeResultsBasesOnExpectedMatches(matchingProcessResults);
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
        judgeResultsBasesOnExpectedMatches(matchingProcessResults);

        return matchingProcessResults;
    }

    private void judgeResultsBasesOnExpectedMatches(List<MatchingProcessResult> matchingProcessResults) {
        for(MatchingProcessResult mpr:matchingProcessResults){
            ParsingResult ingredientParsingDetails = mpr.getIngredientParsingDetails();
            String ingredientPhrase=ingredientParsingDetails.getOriginalPhrase();

            List<MatchExpected> expectedForIngredient = this.matchExpectedRepository.findByIngredient(ingredientPhrase);
            List<MatchExpected> missingForIngredient = expectedForIngredient.stream().collect(Collectors.toList());

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
                                missingForIngredient = missingForIngredient.stream().filter(me -> !me.getProduct().equals(productName)).collect(Collectors.toList());

                            }
                        } else {
                            pmr.setExpectedVerdict(false);
                        }
                    }
                }

          //      mpr.setProductsNotFound(missing);

            }
            List<String> missing = missingForIngredient.stream().map(me -> me.getProduct()).filter(s -> !s.equals("")).collect(Collectors.toList());

            mpr.setProductNamesNotFound(missing);
            mpr.setIncorrectProductsConsideredParsingResults(mpr.getProductsConsideredParsingResults().stream().filter(result->result.isCalculatedVerdict()!=result.isExpectedVerdict()).collect(Collectors.toList()));
        }
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
                List<ProductParsingResult> parsingResultList = retrieveCandidateProductResults(parsingAPhrase);

                if(saveInDb) {
                    if (batchObject == null) {
                        batchObject = new ParsingBatch();
                        parsingBatchRepository.save(batchObject);
                    }
                }

                if(parsingResultList!=null&&!parsingResultList.isEmpty()) {
                    parsingResultList.sort((o1, o2) -> {
                        return o1.getOriginalName().compareTo(o2.getOriginalName());
                    });
                    for (ProductParsingResult pr : parsingResultList) {
                        ProductData product = getProductFromDb(pr);
                        if (product != null) {
                            ingredientPhraseParsingResult = parseSingleProduct(saveInDb, batchObject, parsingAPhrase, ingredientPhraseParsingResult, match, product);
                        }
                    }
                }

                List<ProductData> productsConsidered = parsingResultList.stream().map(productParsingResult -> productParsingResult.getProduct()).collect(Collectors.toList());

                ifEmptyProductsUpdateForUnparsedProducts(saveInDb, batchObject, parsingAPhrase, ingredientPhraseParsingResult, match,productsConsidered);
                retValue.add(match);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return retValue;
    }

    private void ifEmptyProductsUpdateForUnparsedProducts(boolean saveInDb, ParsingBatch batchObject,
         IngredientPhraseParsingProcessObject parsingAPhrase, IngredientPhraseParsingResult ingredientPhraseParsingResult, MatchingProcessResult match,
        List<ProductData> productsAlreadyConsidered ) {

    //    while(!match.getProductsConsideredParsingResults().stream().anyMatch(productMatchingResult -> productMatchingResult.isCalculatedVerdict())){

            if(match.getProductsConsideredParsingResults().size()>=productsToBeParsedCutout) {
                throwOutNotMatched(match);
            }

            List<ProductData> curentCandidateProducts = retrieveNewCandidateProducts(parsingAPhrase,productsAlreadyConsidered);
            if(curentCandidateProducts.isEmpty()){
                return;
            }else {
                productsAlreadyConsidered.addAll(curentCandidateProducts);
                for (ProductData product : curentCandidateProducts) {
                    ingredientPhraseParsingResult = parseSingleProduct(saveInDb, batchObject, parsingAPhrase, ingredientPhraseParsingResult, match, product);

                }
            }
       // }
    }

    private void throwOutNotMatched(MatchingProcessResult match) {
        List<ProductMatchingResult> productsConsideredParsingResults = match.getProductsConsideredParsingResults();

        List<ProductMatchingResult> result=productsConsideredParsingResults.stream()
                .filter(productMatchingResult -> productMatchingResult.isCalculatedVerdict())
                .collect(Collectors.toList());

        if(result.size()<productsToBeParsedCutout){
            Set<ProductMatchingResult> collect = productsConsideredParsingResults
                    .stream().filter(productMatchingResult -> !productMatchingResult.isCalculatedVerdict())
                    .limit(productsToBeParsedCutout - result.size()).collect(Collectors.toSet());

            result.addAll(collect);
        }
        match.setProductsConsideredParsingResults(result);

    }

    private IngredientPhraseParsingResult parseSingleProduct(boolean saveInDb, ParsingBatch batchObject, IngredientPhraseParsingProcessObject parsingAPhrase, IngredientPhraseParsingResult ingredientPhraseParsingResult, MatchingProcessResult match, ProductData product) {
        ProductParsingProcessObject pppo = new ProductParsingProcessObject(product, new ProductLearningCase());
        this.shopProductParser.parseProductParsingObjectWithNamesComparison(pppo);
        ParsingResult ppr = this.shopProductParser.createResultObject(pppo);
        ProductMatchingResult pmr = calculateMatchingResult(parsingAPhrase, pppo, ppr);
        if(!ProductType.notFood.equals(pmr.getBaseResult().getProductTypeFound()))
            match.addProductsConsideredParsingResults(pmr);

        if(saveInDb) {
            if (ingredientPhraseParsingResult == null)
                ingredientPhraseParsingResult = this.ingredientPhraseParser.saveResultAndPhrasesInDb(parsingAPhrase, batchObject);
            saveProductAndMatchInDb(batchObject, ingredientPhraseParsingResult, pppo, pmr);
        }
        return ingredientPhraseParsingResult;
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

    private List<ProductParsingResult> retrieveCandidateProductResults(IngredientPhraseParsingProcessObject parsingAPhrase) {
        String markedWords = parsingAPhrase.getFinalResults().stream().filter(s -> s.getWordType() == WordType.ProductElement).map(s -> s.getLemma()).collect(Collectors.joining(" "));//ingredientResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));
        ProductType foodTypeClassified = parsingAPhrase.getFoodTypeClassified();
        List<ProductParsingResult> parsingResultList = searchForProductResults(markedWords,foodTypeClassified);
   //     parsingResultList = parsingResultList.subList(0, Math.min(20, parsingResultList.size()));
        return parsingResultList;
    }

    private List<ProductData> retrieveNewCandidateProducts(IngredientPhraseParsingProcessObject parsingAPhrase, List<ProductData> allCandidateProductsConsidered) {
        String markedWords = parsingAPhrase.getFinalResults().stream().filter(s -> s.getWordType() == WordType.ProductElement).map(s -> s.getLemma()).collect(Collectors.joining(" "));//ingredientResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));

        List<ProductData> retValue=new ArrayList<>();
        if(markedWords!=null&&!markedWords.isEmpty()){
            List<String> wordsToFind = Arrays.asList(markedWords.split(" "));

            List<Morrisons_Product> productsConsidered = this.morrisonProductRepository.findByNameContainingIgnoreCase(wordsToFind.get(0));
            productsConsidered=productsConsidered.stream()
                    .filter(morrisons_product ->
                        !allCandidateProductsConsidered.stream().anyMatch(allProduct->allProduct.getPd_id().equals(morrisons_product.getPd_id())))
                    .collect(Collectors.toList());


            productsConsidered= productsConsidered.stream()
                    .filter(morrisons_product ->wordsToFind.stream().allMatch(word->morrisons_product.getName().toLowerCase().contains(word.toLowerCase())))
                    .collect(Collectors.toList());



            if(productsConsidered.size()> productsToBeParsedCutout)
                productsConsidered=productsConsidered.subList(0,productsToBeParsedCutout);

            retValue.addAll(productsConsidered);
        }
        return retValue;
    }

    private ProductMatchingResult calculateMatchingResult(IngredientPhraseParsingProcessObject parsingAPhrase, ProductParsingProcessObject pppo, ParsingResult ppr) {
        ProductMatchingResult pmr = new ProductMatchingResult(ppr);


        List<QualifiedToken> ingredientWordsMarked = parsingAPhrase.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
        List<QualifiedToken> productWordsMarked = pppo.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
        Set<QualifiedToken> matched = new HashSet<>();
        Set<String> ingredientSurplus = new HashSet<>();
        List<String> productSurplus = new ArrayList<>();


        for (QualifiedToken ingredientQt : ingredientWordsMarked) {
            if (productWordsMarked.stream().filter(productQt -> productQt.compareWithToken(ingredientQt)).findAny().isPresent()) {
                matched.add(ingredientQt);
            } else {
                ingredientSurplus.add(ingredientQt.getLemma());
            }

        }
        productSurplus = collectMissingProducts(productWordsMarked, matched);

        List<String> ingSurplusList=new ArrayList<>();
        ingSurplusList.addAll(ingredientSurplus);

        List<String> matchedList=new ArrayList<>();
        matchedList.addAll(matched.stream().map(qualifiedToken -> qualifiedToken.getLemma()).collect(Collectors.toSet()));

        CalculatedResults cr = new CalculatedResults(ingSurplusList, matchedList, productSurplus, matchedList);
        pmr.setWordsMatching(cr);
        boolean namesMatch=ingredientSurplus.isEmpty() && productSurplus.isEmpty();
        boolean typesMatch=checkTypesCompatibility(parsingAPhrase.getFoodTypeClassified(),pppo.getFoodTypeClassified());


        pmr.setCalculatedVerdict(namesMatch&&typesMatch);
        return pmr;
    }

    private List<String> collectMissingProducts(List<QualifiedToken> productWordsMarked, Set<QualifiedToken> matched) {
        List<String> collect = productWordsMarked.stream().filter(productQt ->
                !matched.stream().anyMatch(matchQt -> productQt.getText().equals(matchQt.getText()) || productQt.getLemma().equals(matchQt.getLemma()))
        ).distinct().map(qualifiedToken -> qualifiedToken.getLemma()).collect(Collectors.toList());

        return  collect;
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

            Iterable<ProductParsingResult> allProdResults = productParsingResultRepository.findByMinimalResultsCalculatedContaining(wordsToFind.get(0));

            for (ProductParsingResult productParsingResult : allProdResults) {
                String minimalResults = productParsingResult.getMinimalResultsCalculated().toLowerCase();
                if (wordsToFind.stream().filter(wordToFind -> !minimalResults.contains(wordToFind)).count() == 0
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
