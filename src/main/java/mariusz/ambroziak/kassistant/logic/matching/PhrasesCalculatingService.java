package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.constants.NlpConstants;
import mariusz.ambroziak.kassistant.enums.PhraseEqualityTypes;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.*;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.CustomPhraseConsideredRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.IngredientPhraseLearningCaseRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.MorrisonProductRepository;
import mariusz.ambroziak.kassistant.hibernate.statistical.model.IngredientWordOccurence;
import mariusz.ambroziak.kassistant.hibernate.statistical.model.ProductWordOccurence;
import mariusz.ambroziak.kassistant.hibernate.statistical.model.Word;
import mariusz.ambroziak.kassistant.hibernate.statistical.repository.WordRepository;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.phrasefinding.PhraseConsideredMatch;
import mariusz.ambroziak.kassistant.pojos.phrasefinding.PhraseFindingResults;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.pojos.words.WordAssociacion;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PhrasesCalculatingService {


    private Map<ProductParsingResult, List<PhraseConsidered>> productPhrasesConsidered;
    private Map<IngredientPhraseParsingResult, List<PhraseConsidered>> ingredientPhrasesConsidered;
    public static Map<String, TokenizationResults> tokenizedMap = new HashMap<>();

    @Autowired
    IngredientPhraseLearningCaseRepository ingredientPhraseLearningCaseRepository;

    @Autowired
    MorrisonProductRepository morrisonProductRepository;

    @Autowired
    TokenizationClientService tokenizationClientService;

    @Autowired
    CustomPhraseConsideredRepository phraseConsideredRepository;

    @Autowired
    WordRepository wordRepository;

    public static String doesContainPunctuationRegex = ".*[\\.,—\\-\\*\\(\\)]+.*";


    public static final String csvSeparator = ";";


    public List<PhraseConsidered> addProductPhraseConsidered(ProductParsingResult productParsingResult, ProductParsingProcessObject processObject) {
        List<PhraseConsidered> phrases = new ArrayList<>();

        try {
            phrases = calculateAllAdjacencyAndDependencyPhrases(processObject);

            phrases.stream().forEach(phraseConsidered -> phraseConsidered.setProductParsingResult(productParsingResult));
            this.phraseConsideredRepository.saveAllPhrases(phrases);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return phrases;

    }


    public List<PhraseConsidered> addIngredientPhrasesConsidered(IngredientPhraseParsingResult ingredientPhraseParsingResult, IngredientPhraseParsingProcessObject processObject) {
        List<PhraseConsidered> phrases = new ArrayList<>();

        try {
            phrases = calculateAllAdjacencyAndDependencyPhrases(processObject);

            phrases.stream().forEach(phraseConsidered -> phraseConsidered.setIngredientPhraseParsingResult(ingredientPhraseParsingResult));
            this.phraseConsideredRepository.saveAllPhrases(phrases);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return phrases;

    }

    private List<PhraseConsidered> calculateAllAdjacencyAndDependencyPhrases(AbstractParsingObject processObject) {
        List<PhraseConsidered> phrases = new ArrayList<>();

        for (int i = 0; i < processObject.getFinalResults().size() - 1; i++) {
            QualifiedToken qt1 = processObject.getFinalResults().get(i);
            QualifiedToken qt2 = processObject.getFinalResults().get(i + 1);
            if (qt1.getWordType() != WordType.QuantityElement && qt2.getWordType() != WordType.QuantityElement) {
                AdjacencyPhraseConsidered adjacencyPc = new AdjacencyPhraseConsidered();

                adjacencyPc.setPhrase(qt1.getText() + " " + qt2.getText());
                phrases.add(adjacencyPc);
            }


            List<QualifiedToken> collect = processObject.getFinalResults().stream()
                    .filter(t -> t.getText().equals(qt1.getText()) && t.getHead().equals(qt1.getHead()))
                    .filter(t -> t.getWordType() != WordType.QuantityElement && t.getWordType() != WordType.PunctuationElement)
                    .collect(Collectors.toList());


            if (collect.size() > 1) {
                System.err.println("two matching tokens");
            } else if (collect.size() == 1) {
                QualifiedToken qtChild = collect.get(0);
                QualifiedToken qtFather = processObject.getFinalResults().stream()
                        .filter(t -> t.getText().equals(qt1.getHead()))
                        .reduce((qualifiedToken, qualifiedToken2) -> pickOneWithTypeNotFilled(qt1, qt2)).orElse(null);

                if (qtFather != null && !qtChild.getRelationToParentType().equals(NlpConstants.ROOT_RELATION_TO_PARENT)) {
                    DependencyPhraseConsidered dependencyPc = new DependencyPhraseConsidered();
                    dependencyPc.setHead(new SavedToken(qtFather));
                    dependencyPc.setChild(new SavedToken(qtChild));
                    phrases.add(dependencyPc);
                }
            }



        }


        return phrases;
    }


    private QualifiedToken pickOneWithTypeNotFilled(QualifiedToken qt1, QualifiedToken qt2) {
        try {
            if (qt1.getWordType() == null) {
                if (qt2.getWordType() != null && !WordType.QuantityElement.equals(qt2.getWordType()) && !WordType.PunctuationElement.equals(qt2.getWordType()))
                    return qt2;
            }
            if (qt2.getWordType() == null) {
                if (qt1.getWordType() != null && !WordType.QuantityElement.equals(qt1.getWordType()) && !WordType.PunctuationElement.equals(qt1.getWordType()))
                    return qt1;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return qt1;
    }


    public PhraseFindingResults calculatePhraseFindingResults() {
        List<PhraseConsidered> ingredientTotal = new ArrayList<>();
        List<PhraseConsidered> productsTotal = new ArrayList<>();
        fillPhraseLists(productsTotal,ingredientTotal);

        List<PhraseConsidered> collectedProduct = productsTotal.stream().collect(Collectors.toList());//.filter(phraseConsidered -> ((DependencyPhraseConsidered) phraseConsidered).getHead().getText().equals("tomatoes") && ((DependencyPhraseConsidered) phraseConsidered).getChild().getText().equals("italian")).collect(Collectors.toList());
        List<PhraseConsidered> collectedIngredient = ingredientTotal.stream().collect(Collectors.toList());//.filter(phraseConsidered -> ((DependencyPhraseConsidered)phraseConsidered).getChild().getText().toLowerCase().equals("italian")).collect(Collectors.toList());


        List<PhraseConsideredMatch> matches = new ArrayList<>();
        List<PhraseConsidered> ingredientSurplus = new ArrayList<>();
        List<PhraseConsidered> productSurplus = productsTotal.stream().collect(Collectors.toList());

        Map<PhraseConsideredMatch, Map<Word, Integer>> wordOccurencesMap = new HashMap<>();

        for (PhraseConsidered consideredNow : ingredientTotal) {
            List<PhraseConsidered> collect = productsTotal.stream().filter(phraseConsidered -> arePhrasesInAnyWayConsideredEqual(consideredNow, phraseConsidered)).collect(Collectors.toList());

            //phrase appears in both lists
            if (collect.size() > 0) {
                List<PhraseConsideredMatch> matchAlreadyFound =
                        matches.stream().filter(phraseConsideredMatch -> arePhrasesInAnyWayConsideredEqual(consideredNow, phraseConsideredMatch.getMatch()))
                                .collect(Collectors.toList());
                PhraseConsideredMatch pcm = null;
                if (matchAlreadyFound.size() > 1) {
                    System.err.println("two matches");
                    PhraseConsideredMatch squashedOne = new PhraseConsideredMatch();
                    squashedOne.setMatch(matchAlreadyFound.get(0).getMatch());
                    HashMap<Word, Integer> squashedMap = new HashMap<>();

                    matchAlreadyFound.forEach(toBeAdded -> {
                        wordOccurencesMap.get(toBeAdded).forEach(
                                (word, integer) -> {addToSquashedMap(squashedMap, word, integer);}
                        );

                        wordOccurencesMap.remove(toBeAdded);
                        squashedOne.getIngredientPhraseMatched().addAll(toBeAdded.getIngredientPhraseMatched());
                        squashedOne.getProductPhraseMatched().addAll(toBeAdded.getProductPhraseMatched());
                        toBeAdded.getWordAssociacions().forEach(oldOneWA -> {
                            addAssociationsToSquashedOne(squashedOne, oldOneWA);
                        });
                    });


                    matches = matches.stream().filter(phraseConsideredMatch -> !arePhrasesInAnyWayConsideredEqual(consideredNow, phraseConsideredMatch.getMatch()))
                            .collect(Collectors.toList());
                    matches.add(squashedOne);
                    pcm = squashedOne;
                    wordOccurencesMap.put(squashedOne, squashedMap);
                } else if (matchAlreadyFound.size() == 0) {
                    pcm = createNewPhraseConsideredMatch(wordOccurencesMap, consideredNow, collect);
                    matches.add(pcm);
                } else {
                    pcm = addPhraseToExistingMatch(consideredNow, matchAlreadyFound);

                }

                countUpRelatedWords(wordOccurencesMap.get(pcm), consideredNow);

                productSurplus = productSurplus.stream().filter(phraseConsidered -> !arePhrasesInAnyWayConsideredEqual(consideredNow, phraseConsidered)).collect(Collectors.toList());
            } else {
                ingredientSurplus.add(consideredNow);
            }


        }

        fillWordAssociationsFromMap(wordOccurencesMap);
//        List<PhraseConsideredMatch> matchesCutDown = new ArrayList<>();
//
//        Map<PhraseConsidered, PhraseConsideredMatch> mat = matches.stream().collect(Collectors.toMap(phraseConsideredMatch -> phraseConsideredMatch.getMatch(), phraseConsideredMatch -> phraseConsideredMatch));
//        matchesCutDown.addAll(mat.values());

        PhraseFindingResults retValue = new PhraseFindingResults();
        retValue.setIngredientPhrases(collectedIngredient);
        retValue.setProductPhrases(collectedProduct);
        retValue.setMatchedPhrases(matches);
        retValue.setExtraIngredientPhrases(ingredientSurplus);
        retValue.setExtraProductPhrases(productSurplus);


        return retValue;
    }

    private PhraseConsideredMatch createNewPhraseConsideredMatch(Map<PhraseConsideredMatch, Map<Word, Integer>> wordOccurencesMap, PhraseConsidered consideredNow, List<PhraseConsidered> collect) {
        PhraseConsideredMatch pcm;
        pcm = new PhraseConsideredMatch();
        pcm.setMatch(consideredNow);
        pcm.addIngredientPhraseMatched(consideredNow);
        pcm.addProductPhraseMatched(collect.get(0));
        wordOccurencesMap.put(pcm, new HashMap<>());
        return pcm;
    }

    private void addAssociationsToSquashedOne(PhraseConsideredMatch squashedOne, WordAssociacion oldOneWA) {
        List<WordAssociacion> sameFound = squashedOne.getWordAssociacions().stream()
                .filter(squashedOneWA -> squashedOneWA.getText().equals(oldOneWA.getText())).collect(Collectors.toList());

        if (sameFound.size() > 1) {
            System.err.println("two matches!!!!!!");
        } else if (sameFound.size() == 1) {
            sameFound.get(0).setCount(sameFound.get(0).getCount() + oldOneWA.getCount());
        } else {
            squashedOne.getWordAssociacions().add(oldOneWA);
        }
    }

    private void addToSquashedMap(HashMap<Word, Integer> squashedMap, Word word, Integer integer) {
        if (squashedMap.get(word) == null)
            squashedMap.put(word, 0);
        squashedMap.put(word, squashedMap.get(word)+integer);
    }

    private PhraseConsideredMatch addPhraseToExistingMatch(PhraseConsidered consideredNow, List<PhraseConsideredMatch> matchAlreadyFound) {
        PhraseConsideredMatch pcm;
        PhraseConsideredMatch phraseConsideredAlreadyExisting = matchAlreadyFound.get(0);
        pcm = phraseConsideredAlreadyExisting;
        if (!phraseConsideredAlreadyExisting.getProductPhraseMatched()
                .stream().anyMatch(phraseConsidered -> phraseConsidered.equals(phraseConsideredAlreadyExisting))) {
            phraseConsideredAlreadyExisting.addProductPhraseMatchedIfNew(consideredNow);
        }
        if (!phraseConsideredAlreadyExisting.getIngredientPhraseMatched()
                .stream().anyMatch(phraseConsidered -> phraseConsidered.equals(phraseConsideredAlreadyExisting))) {
            phraseConsideredAlreadyExisting.addIngredientPhraseMatchedIfNew(consideredNow);
        }
        return pcm;
    }


    private void fillPhraseLists(List<PhraseConsidered> productsTotal , List<PhraseConsidered> ingredientsTotal ){
        Map<String, List<PhraseConsidered>> ingredientPhrasesCalculated = new HashMap<>();
        Map<String, List<PhraseConsidered>> productPhrasesCalculated = new HashMap<>();

        Iterable<PhraseConsidered> allPhrases = this.phraseConsideredRepository.findAllPhrases();

        allPhrases.forEach(phraseConsidered -> {
            if (checkValidity(phraseConsidered)) {

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





        ingredientPhrasesCalculated.forEach((s, phraseConsidereds) -> ingredientsTotal.addAll(phraseConsidereds));
        ingredientsTotal.sort(Comparator.comparingInt(PhraseConsidered::hashCode));


        productPhrasesCalculated.forEach((s, phraseConsidereds) -> productsTotal.addAll(phraseConsidereds));
        productsTotal.sort(Comparator.comparingInt(PhraseConsidered::hashCode));




    }

    private void fillWordAssociationsFromMap(Map<PhraseConsideredMatch, Map<Word, Integer>> wordOccurencesMap) {
        wordOccurencesMap.entrySet().stream().forEach(phraseConsideredMatchMapEntry -> {
            Map<Word, Integer> value = phraseConsideredMatchMapEntry.getValue();
            List<WordAssociacion> associacionsFound = value.entrySet().stream().map(wordIntegerEntry ->
                    new WordAssociacion(wordIntegerEntry.getKey().getText(), wordIntegerEntry.getValue(), wordIntegerEntry.getKey().getStatsWordType()))
                    .sorted(Comparator.comparingInt(WordAssociacion::getCount)).collect(Collectors.toList());
            phraseConsideredMatchMapEntry.getKey().setWordAssociacions(associacionsFound);
        });


    }

    private boolean     arePhrasesInAnyWayConsideredEqual(PhraseConsidered consideredNow, PhraseConsidered phraseConsidered) {
        return arePhrasesConsideredEffectivelyEqual(consideredNow, phraseConsidered).getPriority() > 0;
    }


    private PhraseEqualityTypes arePhrasesConsideredEffectivelyEqual(PhraseConsidered consideredNow, PhraseConsidered phraseConsidered) {
        if (phraseConsidered.equals(consideredNow)) {
            return PhraseEqualityTypes.equal;
        } else {
            if (consideredNow instanceof DependencyPhraseConsidered && phraseConsidered instanceof DependencyPhraseConsidered) {
                DependencyPhraseConsidered dependencyConsideredNow = (DependencyPhraseConsidered) consideredNow;
                DependencyPhraseConsidered dependencyPhraseConsidered = (DependencyPhraseConsidered) phraseConsidered;
                if (dependencyConsideredNow.lemmasEqualOrBetter(dependencyPhraseConsidered)) {
                    return PhraseEqualityTypes.lemmasEqual;
                }
                if (dependencyConsideredNow.equalsWithHeadAndChildReversedOrBetter(dependencyPhraseConsidered)) {
                    return PhraseEqualityTypes.orderReversed;
                }
            } else {
                if (checkForCompatibilityIfDifferentTypes(consideredNow, phraseConsidered)) {
                    return PhraseEqualityTypes.differentTypes;
                }
            }
        }
        return PhraseEqualityTypes.notEqual;
    }

    private boolean checkForCompatibilityIfDifferentTypes(PhraseConsidered consideredNow, PhraseConsidered phraseConsidered) {
        if (consideredNow.getClass().equals(phraseConsidered.getClass())) {
            return false;
        } else {
            if (consideredNow instanceof AdjacencyPhraseConsidered) {
                return castAdjacencyToDependency((AdjacencyPhraseConsidered) consideredNow, (DependencyPhraseConsidered) phraseConsidered);
            } else {
                    return castAdjacencyToDependency((AdjacencyPhraseConsidered) phraseConsidered, (DependencyPhraseConsidered) consideredNow);

            }
        }


    }

    private boolean castAdjacencyToDependency(AdjacencyPhraseConsidered adjacencyPhrase, DependencyPhraseConsidered dependencyPhrase) {
        TokenizationResults tokenizationResults = tokenizedMap.get(adjacencyPhrase.getPhrase());
        if (tokenizationResults == null) {
            tokenizationResults = this.tokenizationClientService.parse(adjacencyPhrase.getPhrase());
            ;
            tokenizedMap.put(adjacencyPhrase.getPhrase(), tokenizationResults);

        }


        List<ConnectionEntry> allTwoWordDependencies = tokenizationResults.getAllTwoWordDependencies();


        if (allTwoWordDependencies.size() == 1
                && areHeadsAndChildsCompatibleOrCrossCompatible(dependencyPhrase, allTwoWordDependencies)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean areHeadsAndChildsCompatibleOrCrossCompatible(DependencyPhraseConsidered dependencyPhrase, List<ConnectionEntry> allTwoWordDependencies) {
        SavedToken dependencyPhraseHead = dependencyPhrase.getHead();
        SavedToken dependencyPhraseChild = dependencyPhrase.getChild();
        Token fromTreeHead = allTwoWordDependencies.get(0).getHead();
        Token fromTreeChild = allTwoWordDependencies.get(0).getChild();


        return (dependencyPhraseHead.equalsToken(fromTreeHead)
                && dependencyPhraseChild.equalsToken(fromTreeChild))
                ||(dependencyPhraseHead.equalsToken(fromTreeChild)
                        && dependencyPhraseChild.equalsToken(fromTreeHead));
    }


    private void countUpRelatedWords(Map<Word, Integer> wordCount, PhraseConsidered consideredNow) {

        if (consideredNow.getProductParsingResult() != null) {
            ProductParsingResult productParsingResult = consideredNow.getProductParsingResult();
//			List<ProductWordOccurence> wordsOccuring = productParsingResult.getWordsOccuring();
//
//			List<Word> collect = wordsOccuring.stream().map(wo -> wo.getWord()).collect(Collectors.toList());
//			List<WordAssociacion> result = new ArrayList<>();
//
            for (ProductWordOccurence word : productParsingResult.getWordsOccuring()) {
                Word text = word.getWord();
                Integer retr = wordCount.get(text) == null ? 0 : wordCount.get(text);

                wordCount.put(text, ++retr);
            }
        }

        if (consideredNow.getIngredientPhraseParsingResult() != null) {
            IngredientPhraseParsingResult ingredientPhraseParsingResult = consideredNow.getIngredientPhraseParsingResult();

            for (IngredientWordOccurence word : ingredientPhraseParsingResult.getWordsOccuring()) {
                try {
                    Word text = word.getWord();
                    Integer retr = wordCount.get(text) == null ? 0 : wordCount.get(text);

                    wordCount.put(text, ++retr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private boolean checkValidity(PhraseConsidered phraseConsidered) {
        if (phraseConsidered != null) {
            if (phraseConsidered instanceof AdjacencyPhraseConsidered) {
                AdjacencyPhraseConsidered phraseConsidered1 = (AdjacencyPhraseConsidered) phraseConsidered;
                if (phraseConsidered1.getPhrase() == null
                        || phraseConsidered1.getPhrase().isEmpty()
                        || Pattern.matches(doesContainPunctuationRegex, phraseConsidered1.getPhrase())) {
                    return false;
                } else {
                    return true;
                }
            }

            if (phraseConsidered instanceof DependencyPhraseConsidered) {
                DependencyPhraseConsidered phraseConsidered1 = (DependencyPhraseConsidered) phraseConsidered;
                if (phraseConsidered1.getHead() == null || phraseConsidered1.getHead().getText() == null
                        || phraseConsidered1.getHead().getText().isEmpty()
                        || Pattern.matches(doesContainPunctuationRegex, phraseConsidered1.getHead().getText())
                        || phraseConsidered1.getChild() == null || phraseConsidered1.getChild().getText() == null || phraseConsidered1.getChild().getText().isEmpty()

                        || Pattern.matches(doesContainPunctuationRegex, phraseConsidered1.getChild().getText())) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        return false;

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
