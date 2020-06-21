package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.model.*;
import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseLearningCaseRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ParsingBatchRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.ProductParsingResultRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.TescoProductRepository;
import mariusz.ambroziak.kassistant.logic.AbstractParser;
import mariusz.ambroziak.kassistant.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.logic.shops.ShopProductParser;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.*;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoFromFileService;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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


    public List<MatchingProcessResult> parseMatchAndSaveToDb() {
        List<IngredientLearningCase> ingredientLearningCasesFromDb = this.ingredientParser.getIngredientLearningCasesFromDb();
        List<MatchingProcessResult> retValue = new ArrayList<>();
        ParsingBatch batchObject = new ParsingBatch();
        parsingBatchRepository.save(batchObject);

        for (IngredientLearningCase er : ingredientLearningCasesFromDb) {
            IngredientPhraseParsingProcessObject parsingAPhrase = this.ingredientParser.processSingleCase(er);

            ParsingResult ingredientResult = this.ingredientParser.createResultObject(parsingAPhrase);
            IngredientPhraseParsingResult ingredientPhraseParsingResult = this.ingredientParser.saveResultAndPhrasesInDb(parsingAPhrase, batchObject);

            MatchingProcessResult match = new MatchingProcessResult();


            match.setIngredientParsingDetails(ingredientResult);
            String markedWords = ingredientResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));
            List<ProductParsingProcessObject> parsingResultList = searchForProducts(markedWords);

            for (ProductParsingProcessObject pr : parsingResultList) {
                this.productParser.parseProductParsingObjectWithNamesComparison(pr);
                ParsingResult ppr = this.productParser.createResultObject(pr);
                ProductMatchingResult pmr = new ProductMatchingResult(ppr);


                List<QualifiedToken> ingredientWordsMarked = parsingAPhrase.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
                List<QualifiedToken> productWordsMarked = pr.getFinalResults().stream().filter(t -> t.getWordType() == WordType.ProductElement).collect(Collectors.toList());
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
                pmr.setVerdict(ingredientSurplus.isEmpty() && productSurplus.isEmpty());

                match.addProductsConsideredParsingResults(pmr);

                ProductParsingResult productParsingResult = this.productParser.saveResultInDb(pr, batchObject);

            }

            retValue.add(match);

        }
        return retValue;
    }

    public List<MatchingProcessResult> parseMatchAndGetResultsFromDbAllCases() {
        List<IngredientLearningCase> ingredientLearningCasesFromDb = this.ingredientParser.getIngredientLearningCasesFromDb();
        List<MatchingProcessResult> parseMatchAndGetResultsFromDb = parseMatchAndGetResultsFromDb(ingredientLearningCasesFromDb);

        return parseMatchAndGetResultsFromDb;

    }

    public List<MatchingProcessResult> parseMatchAndGetResultsFromDbForSingleCase(long ingredientLEarningCaseID) {
        List<IngredientLearningCase> ingredientLearningCasesFromDb = new ArrayList<>();

        ingredientLearningCasesFromDb.add(this.ingredientPhraseLearningCaseRepository.findById(ingredientLEarningCaseID));
        List<MatchingProcessResult> parseMatchAndGetResultsFromDb = parseMatchAndGetResultsFromDb(ingredientLearningCasesFromDb);

        return parseMatchAndGetResultsFromDb;

    }

    public List<MatchingProcessResult> parseMatchAndGetResultsFromDb(List<IngredientLearningCase> ingredientLearningCasesFromDb) {

        List<MatchingProcessResult> retValue = new ArrayList<>();
        ParsingBatch batchObject = new ParsingBatch();
        parsingBatchRepository.save(batchObject);

        for (IngredientLearningCase er : ingredientLearningCasesFromDb) {
            try {
                IngredientPhraseParsingProcessObject parsingAPhrase = this.ingredientParser.processSingleCase(er);

                ParsingResult ingredientResult = this.ingredientParser.createResultObject(parsingAPhrase);
                IngredientPhraseParsingResult ingredientPhraseParsingResult = this.ingredientParser.saveResultAndPhrasesInDb(parsingAPhrase, batchObject);

                MatchingProcessResult match = new MatchingProcessResult();


                match.setIngredientParsingDetails(ingredientResult);
                String markedWords = parsingAPhrase.getFinalResults().stream().filter(s -> s.getWordType() == WordType.ProductElement).map(s -> s.getLemma()).collect(Collectors.joining(" "));//ingredientResult.getRestrictivelyCalculatedResult().getMarkedWords().stream().collect(Collectors.joining(" "));
                List<ProductParsingResult> parsingResultList = searchForProductResults(markedWords);
                parsingResultList = parsingResultList.subList(0, Math.min(30, parsingResultList.size()));
                for (ProductParsingResult pr : parsingResultList) {
                    Tesco_Product tesco_product = getProductFromDb(pr);
                    if (tesco_product != null) {
                        ProductParsingProcessObject pppo = new ProductParsingProcessObject(tesco_product, new ProductLearningCase());
                        this.productParser.parseProductParsingObjectWithNamesComparison(pppo);
                        ParsingResult ppr = this.productParser.createResultObject(pppo);
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
                        pmr.setVerdict(ingredientSurplus.isEmpty() && productSurplus.isEmpty());

                        match.addProductsConsideredParsingResults(pmr);

                        ProductParsingResult productParsingResult = this.productParser.saveResultInDb(pppo, batchObject);

                    }
                }
                match.setProductsConsideredParsingResults(match.getProductsConsideredParsingResults().stream().sorted((o1, o2) -> o1.isVerdict() ? 1 : (o2.isVerdict() ? -1 : 0)).collect(Collectors.toList()));
                retValue.add(match);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return retValue;
    }

    private Tesco_Product getProductFromDb(ProductParsingResult pr) {
        List<Tesco_Product> byName = this.tescoProductRepository.findByName(pr.getOriginalName());

        if (byName.size() > 0)
            return byName.get(0);
        else
            return null;

    }

    private List<ProductParsingProcessObject> searchForProducts(String markedWords) {
        List<ProductParsingProcessObject> retValue = new ArrayList<>();
        //return this.productParser.tescoSearchForParsings(markedWords);
        List<String> wordsToFind = Arrays.asList(markedWords.split(" "));
        Set<String> names = tescoFromFileService.nameToProducts.keySet();

        for (String name : names) {
            String nameLowercase = name.toLowerCase();
            if (!(wordsToFind.stream().filter(wordToFind -> !nameLowercase.contains(wordToFind)).count() > 0)) {
                Tesco_Product tesco_product = tescoFromFileService.nameToProducts.get(name);
                ProductParsingProcessObject parse = new ProductParsingProcessObject(tesco_product, new ProductLearningCase());
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

//	private ParsingResult createProductResultObject(ProductParsingProcessObject parsingAPhrase) {
//		ParsingResult object=new ParsingResult();
//		object.setOriginalPhrase(parsingAPhrase.getProduct().getName());
//		String fused=parsingAPhrase.getEntities()==null||parsingAPhrase.getEntities().getEntities()==null?"":parsingAPhrase.getEntities().getEntities().stream().map(s->s.getText()).collect( Collectors.joining("<br>") );
//
//		object.setEntities(fused);
//		object.setEntityLess(parsingAPhrase.getEntitylessString());
//		object.setTokens(parsingAPhrase.getFinalResults());
//		String expected=parsingAPhrase.getMinimalExpectedWords().stream().collect(Collectors.joining(" "));
//		IngredientLearningCase lp=new IngredientLearningCase(parsingAPhrase.getOriginalPhrase(),0,"empty",expected,parsingAPhrase.getExpectedType());
//		object.setExpectedResult(lp);
//		object.setProductTypeFound(parsingAPhrase.getFoodTypeClassified().toString());
//		object.setRestrictivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getMinimalExpectedWords(),parsingAPhrase.getFinalResults()));
//		object.setPermisivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getMinimalExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));
//
//		object.setRestrictivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getExtendedExpectedWords(),parsingAPhrase.getFinalResults()));
//		object.setPermisivelyCalculatedResultForPhrase(calculateWordsFound(parsingAPhrase.getExtendedExpectedWords(),parsingAPhrase.getPermissiveFinalResults()));
//
//		object.setBrand(parsingAPhrase.getProduct().getBrand());
//		object.setBrandless(parsingAPhrase.getBrandlessPhrase());
//
//		if(parsingAPhrase.getProduct() instanceof Tesco_Product){
//			String secondName="";
//			Tesco_Product product = (Tesco_Product) parsingAPhrase.getProduct();
//			secondName= product.getSearchApiName();
//			object.setAlternateName(secondName);
//
//			object.setIngredientPhrase(product.getIngredients());
//		}
//
//		object.setDescriptionPhrase(parsingAPhrase.getProduct().getDescription());
//
//		object.setInitialNames(parsingAPhrase.getInitialNames());
//
//		object.setQuantitylessPhrase(parsingAPhrase.getQuantitylessPhrase());
//		object.setFinalNames(parsingAPhrase.getFinalNames());
//
//		return object;
//	}

//	private ParsingResult createIngredientResultObject(IngredientPhraseParsingProcessObject parsingAPhrase) {
//		ParsingResult object=new ParsingResult();
//		object.setOriginalPhrase(parsingAPhrase.getLearningTuple().getOriginalPhrase());
//		List<QualifiedToken> primaryResults = parsingAPhrase.getFinalResults();
//		object.setTokens(primaryResults);
//
//		String fused=parsingAPhrase.getCardinalEntities().stream().map(s->s.getText()).collect( Collectors.joining(" ") );
//
//
//		object.setEntities(fused);
//		object.setEntityLess(parsingAPhrase.getEntitylessString());
//		object.setCorrectedPhrase(parsingAPhrase.createCorrectedPhrase());
//		//	object.setCorrectedTokens(parsingAPhrase.getCorrectedtokens());
//		object.setExpectedResult(parsingAPhrase.getLearningTuple());
//		object.setRestrictivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getLearningTuple().getFoodMatch(),parsingAPhrase.getFinalResults()));
//		object.setPermisivelyCalculatedResult(calculateWordsFound(parsingAPhrase.getLearningTuple().getFoodMatch(),parsingAPhrase.getPermissiveFinalResults()));
//		object.setProductTypeFound(parsingAPhrase.getFoodTypeClassified()==null? ProductType.unknown.name():parsingAPhrase.getFoodTypeClassified().name());
//		object.setCorrectedConnotations(parsingAPhrase.getCorrectedConotations());
//		object.setOriginalConnotations(parsingAPhrase.getFromEntityLessConotations());
//		object.setAdjacentyConotationsFound(parsingAPhrase.getAdjacentyConotationsFound());
//		object.setDependencyConotationsFound(parsingAPhrase.getDependencyConotationsFound());
//
//
//		object.setQuantitylessPhrase(parsingAPhrase.getQuantitylessPhrase());
//
//
//		return object;
//	}


}
