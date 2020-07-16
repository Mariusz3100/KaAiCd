package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.*;
import mariusz.ambroziak.kassistant.hibernate.repository.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class IngredientProductMatchingService extends AbstractParser {

	@Autowired
	IngredientPhraseParser ingredientParser;
	@Autowired
	ShopProductParser productParser;

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







    public List<MatchingProcessResult> parseMatchAndGetResultsFromDbAllCases(boolean saveInDb) {
        List<IngredientLearningCase> ingredientLearningCasesFromDb = this.ingredientParser.getIngredientLearningCasesFromDb();
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
                IngredientPhraseParsingProcessObject parsingAPhrase = this.ingredientParser.processSingleCase(er);
                ParsingResult ingredientResult = this.ingredientParser.createResultObject(parsingAPhrase);
                IngredientPhraseParsingResult ingredientPhraseParsingResult=null;

                MatchingProcessResult match = new MatchingProcessResult();
                match.setIngredientParsingDetails(ingredientResult);
                List<ProductParsingResult> parsingResultList = retrieveProductCandidates(parsingAPhrase);
                for (ProductParsingResult pr : parsingResultList) {
                    ProductData product = getProductFromDb(pr);
                    if (product != null) {
                        ProductParsingProcessObject pppo = new ProductParsingProcessObject(product, new ProductLearningCase());
                        this.productParser.parseProductParsingObjectWithNamesComparison(pppo);
                        ParsingResult ppr = this.productParser.createResultObject(pppo);
                        ProductMatchingResult pmr = calculateMatchingResult(parsingAPhrase, pppo, ppr);
                        match.addProductsConsideredParsingResults(pmr);

                        if(saveInDb) {
                            if (batchObject == null) {
                                batchObject = new ParsingBatch();
                                parsingBatchRepository.save(batchObject);
                            }
                            if (ingredientPhraseParsingResult == null)
                                ingredientPhraseParsingResult = this.ingredientParser.saveResultAndPhrasesInDb(parsingAPhrase, batchObject);
                            saveProductAndMatchInDb(batchObject, ingredientPhraseParsingResult, pppo, pmr);
                        }
                    }
                }
                match.setProductsConsideredParsingResults(match.getProductsConsideredParsingResults().stream().sorted((o1, o2) -> o1.isCalculatedVerdict() ? 1 : (o2.isCalculatedVerdict() ? -1 : 0)).collect(Collectors.toList()));
                retValue.add(match);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return retValue;
    }

    private void saveProductAndMatchInDb(ParsingBatch batchObject, IngredientPhraseParsingResult ingredientPhraseParsingResult, ProductParsingProcessObject pppo, ProductMatchingResult pmr) {
        ProductParsingResult productParsingResult = this.productParser.saveResultInDb(pppo, batchObject);
        MatchFound mf=new MatchFound();
        mf.setProduct(productParsingResult);
        mf.setBatch(batchObject);
        mf.setIngredient(ingredientPhraseParsingResult);
        mf.setVerdict(pmr.isCalculatedVerdict());

        this.matchFoundRepository.save(mf);
    }

    private List<ProductParsingResult> retrieveProductCandidates(IngredientPhraseParsingProcessObject parsingAPhrase) {
        String markedWords = parsingAPhrase.getFinalResults().stream().filter(s -> s.getWordType() == WordType.ProductElement).map(s -> s.getLemma()).collect(Collectors.joining(" "));//ingredientResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));
        List<ProductParsingResult> parsingResultList = searchForProductResults(markedWords);
        parsingResultList = parsingResultList.subList(0, Math.min(20, parsingResultList.size()));
        return parsingResultList;
    }


    public List<MatchingProcessResult> parseMatchAndSaveResultsToDb(List<IngredientLearningCase> ingredientLearningCasesFromDb) {

        List<MatchingProcessResult> retValue = new ArrayList<>();
        ParsingBatch batchObject = new ParsingBatch();
        parsingBatchRepository.save(batchObject);

        for (IngredientLearningCase er : ingredientLearningCasesFromDb) {
            try {
                IngredientPhraseParsingProcessObject parsingAPhrase = this.ingredientParser.processSingleCase(er);

                ParsingResult ingredientResult = this.ingredientParser.createResultObject(parsingAPhrase);
                //IngredientPhraseParsingResult ingredientPhraseParsingResult = this.ingredientParser.saveResultAndPhrasesInDb(parsingAPhrase, batchObject);


                MatchingProcessResult match = new MatchingProcessResult();


                match.setIngredientParsingDetails(ingredientResult);
                List<ProductParsingResult> parsingResultList = retrieveProductCandidates(parsingAPhrase);
                for (ProductParsingResult pr : parsingResultList) {
                    ProductData product = getProductFromDb(pr);
                    if (product != null) {
                        ProductParsingProcessObject pppo = new ProductParsingProcessObject(product, new ProductLearningCase());
                        this.productParser.parseProductParsingObjectWithNamesComparison(pppo);
                        ParsingResult ppr = this.productParser.createResultObject(pppo);
                        ProductMatchingResult pmr = calculateMatchingResult(parsingAPhrase, pppo, ppr);

                        match.addProductsConsideredParsingResults(pmr);

                        //  ProductParsingResult productParsingResult = this.productParser.saveResultInDb(pppo, batchObject);

                    }
                }
                match.setProductsConsideredParsingResults(match.getProductsConsideredParsingResults().stream().sorted((o1, o2) -> o1.isCalculatedVerdict() ? 1 : (o2.isCalculatedVerdict() ? -1 : 0)).collect(Collectors.toList()));
                retValue.add(match);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return retValue;
    }

    private ProductMatchingResult calculateMatchingResult(IngredientPhraseParsingProcessObject parsingAPhrase, ProductParsingProcessObject pppo, ParsingResult ppr) {
        ProductMatchingResult pmr = new ProductMatchingResult(ppr);


        List<QualifiedToken> ingredientWordsMarked = parsingAPhrase.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
        List<QualifiedToken> productWordsMarked = pppo.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
        List<String> matched = new ArrayList<>();
        List<String> ingredientSurplus = new ArrayList<>();
        List<String> productSurplus = new ArrayList<>();


        for (QualifiedToken ingredientQt : ingredientWordsMarked) {
            if (productWordsMarked.stream().filter(productQt -> productQt.compareWithToken(ingredientQt)).findAny().isPresent()) {
                matched.add(ingredientQt.getLemma());
            } else {
                ingredientSurplus.add(ingredientQt.getLemma());
            }

        }
        productSurplus = productWordsMarked.stream().filter(qt -> !matched.contains(qt.getLemma())).map(t -> t.getLemma()).collect(Collectors.toList());


        CalculatedResults cr = new CalculatedResults(ingredientSurplus, matched, productSurplus, matched);
        pmr.setWordsMatching(cr);
        pmr.setCalculatedVerdict(ingredientSurplus.isEmpty() && productSurplus.isEmpty());
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

    private List<ProductParsingProcessObject> searchForProducts(String markedWords) {
        List<ProductParsingProcessObject> retValue = new ArrayList<>();
        //return this.productParser.tescoSearchForParsings(markedWords);
        List<String> wordsToFind = Arrays.asList(markedWords.split(" "));
        List<Morrisons_Product> names = morrisonsClientService.searchInDbAndApiFor(markedWords);

        for (Morrisons_Product product : names) {
            String nameLowercase = product.getName().toLowerCase();
            if (!(wordsToFind.stream().filter(wordToFind -> !nameLowercase.contains(wordToFind)).count() > 0)) {
        //        Tesco_Product tesco_product = tescoFromFileService.nameToProducts.get(name);
                ProductParsingProcessObject parse = new ProductParsingProcessObject(product, new ProductLearningCase());
                retValue.add(parse);
            }
        }
        return retValue;
    }

    private List<ProductParsingResult> searchForProductResults(String markedWords) {
        List<ProductParsingResult> retValue = new ArrayList<>();
        //return this.productParser.tescoSearchForParsings(markedWords);
        List<String> wordsToFind = Arrays.asList(markedWords.split(" "));
//		Set<String> names = tescoFromFileService.nameToProducts.keySet();

        Iterable<ProductParsingResult> allProdResults = productParsingResultRepository.findAll();

        for (ProductParsingResult productParsingResult : allProdResults) {
            String nameLowercase = productParsingResult.getMinimalResultsCalculated().toLowerCase();
            if (!(wordsToFind.stream().filter(wordToFind -> !nameLowercase.contains(wordToFind)).count() > 0)) {

                if (retValue.stream().filter(ppr -> ppr.getOriginalName().equals(productParsingResult.getOriginalName())).count() < 1)
                    retValue.add(productParsingResult);
            }
        }


        return retValue;
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
