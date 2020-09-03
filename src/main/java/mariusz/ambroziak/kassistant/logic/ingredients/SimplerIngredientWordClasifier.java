package mariusz.ambroziak.kassistant.logic.ingredients;

import mariusz.ambroziak.kassistant.enums.PhraseSourceType;
import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.AdjacencyPhraseConsidered;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.DependencyPhraseConsidered;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.SavedToken;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.AdjacencyPhraseConsideredRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.CustomPhraseConsideredRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.DependencyPhraseConsideredRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.PhraseConsideredRepository;
import mariusz.ambroziak.kassistant.logic.PhraseDependenciesComparator;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.usda.SingleResult;
import mariusz.ambroziak.kassistant.webclients.usda.UsdaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SimplerIngredientWordClasifier extends IngredientWordsClasifier {
    @Autowired
    PhraseConsideredRepository phraseConsideredRepository;

    @Autowired
    CustomPhraseConsideredRepository customPhraseConsideredRepository;

    @Autowired
    AdjacencyPhraseConsideredRepository adjacencyPhraseConsideredRepository;

    @Autowired
    DependencyPhraseConsideredRepository dependencyPhraseConsideredRepository;

    @Autowired
    PhraseDependenciesComparator dependenciesComparator;

//    public UsdaResponse findInUsdaApi(String quantitylessTokensWithPluses, int i) {
//
//
//        List<String> types = new ArrayList<>();
//        types.add("Survey (FNDDS)");
//
//
//
//        return this.usdaApiClient.findInApi()
//    }
//
//

    public void calculateWordTypesForWholePhrase(AbstractParsingObject parsingAPhrase) {
        initializePrimaryTokensAndConnotations(parsingAPhrase);
        initialCategorization(parsingAPhrase);
        fillQuanAndProdPhrases(parsingAPhrase);
        initializeProductPhraseTokensAndConnotations(parsingAPhrase);
        //if(parsingAPhrase.getFinalResults().stream().filter(t->t.getWordType()==null||t.getWordType()==WordType.Unknown).findAny().isPresent()) {

        if(checkifAreUnclassifiedTokesLeft(parsingAPhrase)) {
            emptyProductTokensAndPhrases(parsingAPhrase);
            //lookForWholePhrasesInDb(parsingAPhrase);
//            checkAllTokensForAdjacencyPhrases(parsingAPhrase);
            simpleCheckForAdjacencyPhrases(parsingAPhrase);

        }
  //      recategorize(parsingAPhrase);
        //}
        calculateProductType(parsingAPhrase);

        categorizeAllElseAsProducts(parsingAPhrase);
    }

    private void simpleCheckForAdjacencyPhrases(AbstractParsingObject parsingAPhrase) {
        Map<String, Integer> retValue = new HashMap<>();

        for (int i = 0; i < parsingAPhrase.getFinalResults().size() - 1; i++) {
            QualifiedToken qt1 = parsingAPhrase.getFinalResults().get(i);
            QualifiedToken qt2 = parsingAPhrase.getFinalResults().get(i + 1);
            String text=qt1.getText() + " " + qt2.getText();
            if (WordType.QuantityElement != qt1.getWordType() && WordType.QuantityElement != qt2.getWordType()) {
                if (!Pattern.matches(punctuationRegex, qt1.getText()) && !Pattern.matches(punctuationRegex, qt2.getText())) {
                    boolean found=checkWithPhrasesInDb(parsingAPhrase, i, text);
                    if (!found) {
                        found = checkWordsApi(parsingAPhrase, i, text);
                    }
                    if (!found) {
                        found = checkUsdaApiForAdjacencyEntry(parsingAPhrase, i, text);
                    }
                }
            }

        }

    }



    protected boolean checkUsdaApiForAdjacencyEntry(AbstractParsingObject parsingAPhrase, int index, String entry) {
        UsdaResponse inApi = findInUsdaApiExceptBranded(entry, 10);

        for (SingleResult sp : inApi.getFoods()) {
            String desc = sp.getDescription();
            if (dependenciesComparator.comparePhrases(entry.toLowerCase(),desc.toLowerCase())) {
                return markFoundAdjacencyResults(parsingAPhrase, index, sp);
            }
        }
        return false;
    }


    protected void fillQuanAndProdPhrases(AbstractParsingObject parsingAPhrase) {
        String quantityPhrase = "", productPhrase = "";
        for (int i = 0; i < parsingAPhrase.getFinalResults().size(); i++) {
            QualifiedToken qt = parsingAPhrase.getFinalResults().get(i);
            if (WordType.QuantityElement == qt.getWordType()) {//&&productPhrase.equals("")) {
                quantityPhrase += " "+qt.getText();
            } else if (WordType.PunctuationElement == qt.getWordType()) {
                productPhrase += qt.getText();
            } else {
                productPhrase += " "+qt.getText();
            }

        }
        productPhrase = productPhrase.trim();
        quantityPhrase = quantityPhrase.trim();

        parsingAPhrase.setQuantityPhrase(quantityPhrase);
        parsingAPhrase.setQuantitylessPhrase(productPhrase);
    }

    private void categorizeFromRefinedPhrases(AbstractParsingObject parsingAPhrase) {
        categorizeFromAdjacencyPhrasesConsidered(parsingAPhrase);

//        if(checkifAreUnclassifiedTokesLeft(parsingAPhrase)){
//            List<ConnectionEntry> quantitylessDependenciesConnotations = parsingAPhrase.getQuantitylessConnotations().stream().filter(s -> !s.getHead().getText().equals("ROOT")).collect(Collectors.toList());
//
//            List<ConnectionEntry> filteredQuantitylessDependenciesConnotations = quantitylessDependenciesConnotations.stream()
//                    .filter(t -> !NlpConstants.of_Word.equals(t.getHead().getText()) && !NlpConstants.of_Word.equals(t.getChild().getText()))
//                    .filter(t -> !t.getHead().getText().trim().isEmpty() && !t.getChild().getText().trim().isEmpty())
//                    .filter(ce -> !Pattern.matches(punctuationRegex, ce.getChild().getText()) && !Pattern.matches(punctuationRegex, ce.getHead().getText()))
//                    .collect(Collectors.toList());
//
//            if (filteredQuantitylessDependenciesConnotations != null && filteredQuantitylessDependenciesConnotations.isEmpty()) {
//                for (ConnectionEntry entry : filteredQuantitylessDependenciesConnotations) {
//                    checkWordsApi(parsingAPhrase, entry);
//                }
//            }
//            if (checkifAreUnclassifiedTokesLeft(parsingAPhrase)) {
//                for (ConnectionEntry ce : filteredQuantitylessDependenciesConnotations) {
//                    categorizeFromDependenciesWithPhrasesConsidered(parsingAPhrase, ce);
//                }
//            }
//        }

    }

    private void categorizeFromDependenciesWithPhrasesConsidered(AbstractParsingObject parsingAPhrase, ConnectionEntry ce) {

        Set<DependencyPhraseConsidered> depPhrases =
                this.customPhraseConsideredRepository
                        .findDependencyPhrasesCompatible(ce.getHead().getLemma(), ce.getChild().getLemma());

        if(depPhrases!=null&&!depPhrases.isEmpty()) {
            depPhrases.forEach(dependencyPhraseConsidered ->
            {
                SavedToken child = dependencyPhraseConsidered.getChild();
                SavedToken head = dependencyPhraseConsidered.getHead();

                parsingAPhrase.getFinalResults().stream()
                        .filter(qualifiedToken ->child.equalsToken(qualifiedToken)||head.equalsToken(qualifiedToken))
                        .forEach(qualifiedToken ->{
                                qualifiedToken.setReasoning("[accepted phrase: " + dependencyPhraseConsidered.getPc_id() + "]");
                                qualifiedToken.setWordType(WordType.ProductElement);
                        });


            });
        }

        System.err.println();

    }

    private void categorizeFromAdjacencyPhrasesConsidered(AbstractParsingObject parsingAPhrase) {
        Map<String, Integer> adjacentyConotations = calculateAdjacencies(parsingAPhrase);

        if (adjacentyConotations != null) {
            for (String entry : adjacentyConotations.keySet()) {
                List<AdjacencyPhraseConsidered> byPhrase =
                        this.adjacencyPhraseConsideredRepository.findByPhraseContainingAndSourceAndAcceptedTrue(entry, PhraseSourceType.Match);

                byPhrase.forEach(adjacencyPhraseConsidered -> {
                    if(parsingAPhrase.getQuantitylessPhrase().contains(adjacencyPhraseConsidered.getPhrase())){
                        Integer index = adjacentyConotations.get(adjacencyPhraseConsidered.getPhrase());
                        markFoundRefinedAdjacencyResults(parsingAPhrase, index,adjacencyPhraseConsidered);
                    }

                });

            }
        }
    }

//    @Override
//    protected UsdaResponse findInUsdaApi(String quantitylessTokensWithPluses, int i) {
//        return UsdaResponse.createEmpty();
//        //return super.findInUsdaApi(quantitylessTokensWithPluses, i);
//    }

    private boolean markFoundRefinedAdjacencyResults(AbstractParsingObject parsingAPhrase, int index, AdjacencyPhraseConsidered phrase) {
        try {
            String[] wordsOfPhrase = phrase.getPhrase().split(" ");
            if(wordsOfPhrase.length!=2){
                System.err.println("incorrect size of phrase considered phrase");
            }else {
                QualifiedToken qualifiedToken1 = parsingAPhrase.getFinalResults().get(index);
                if (qualifiedToken1.getText().equals(wordsOfPhrase[0])) {
                    addProductResult(parsingAPhrase, index, qualifiedToken1, "[accepted phrase: " + phrase.getPc_id() + "]");

                }

                QualifiedToken qualifiedToken2 = parsingAPhrase.getFinalResults().get(index + 1);
                if (qualifiedToken2.getText().equals(wordsOfPhrase[1])) {
                    addProductResult(parsingAPhrase, index+1, qualifiedToken2, "[accepted phrase: " + phrase.getPc_id() + "]");

                }

                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    protected boolean checkWithResultsFromUsdaApi(AbstractParsingObject parsingAPhrase, int index, Token t) {

        return false;
    }
    protected boolean checkWithPhrasesInDb(AbstractParsingObject parsingAPhrase, int index, Token t) {

        return false;

    }
}
