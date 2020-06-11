package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.pojos.shop.ProductNamesComparison;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordComparisonResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParseCompareProductNames {
    private TokenizationResults firstPhrase;
    private TokenizationResults secondPhrase;


//    private String shorterPhrase;
//    private String longerPhrase;

    List<WordComparisonResult> firstPhraseResults;
    List<WordComparisonResult> secondPhraseResults;
    private int currentFirstPhraseReadingIndex;
    private int currentSecondPhraseReadingIndex;


    public ParseCompareProductNames(TokenizationResults firstPhrase, TokenizationResults secondPhrase) {
        if(firstPhrase==null||secondPhrase==null){
            throw new IllegalArgumentException("empty arguments");
        }
        this.firstPhrase = firstPhrase;
        this.secondPhrase = secondPhrase;

        firstPhraseResults=new ArrayList<>();
        secondPhraseResults=new ArrayList<>();
    }

    public static ProductNamesComparison parseTwoPhrases(TokenizationResults firstPhrase, TokenizationResults secondPhrase) {
        ParseCompareProductNames thisObject=new ParseCompareProductNames(firstPhrase,secondPhrase);
        return thisObject.calculateResults();

    }



    private ProductNamesComparison calculateResults(){
        ProductNamesComparison retValue=checkForEmptyPhrases();
        if(retValue!=null){
            return retValue;
        }else {
            List<Token> firstPhraseTokens = firstPhrase.getTokens();
            List<Token> secondPhraseTokens = secondPhrase.getTokens();

            currentFirstPhraseReadingIndex = 0;
            currentSecondPhraseReadingIndex = 0;
            while (currentFirstPhraseReadingIndex < firstPhraseTokens.size() || currentSecondPhraseReadingIndex < secondPhraseTokens.size()) {


                if (currentFirstPhraseReadingIndex >= firstPhraseTokens.size() && currentSecondPhraseReadingIndex < secondPhraseTokens.size()) {
                    Token secondPhraseWord = secondPhraseTokens.get(currentSecondPhraseReadingIndex);
                    currentSecondPhraseReadingIndex++;
                    addWordToSecondListEmptyToFirstList(secondPhraseWord.getText());
                } else if (currentSecondPhraseReadingIndex >= secondPhraseTokens.size() && currentFirstPhraseReadingIndex < firstPhraseTokens.size()) {
                    Token firstPhraseWord = firstPhraseTokens.get(currentFirstPhraseReadingIndex);
                    currentFirstPhraseReadingIndex++;
                    addWordToFirstListEmptyToSecondList(firstPhraseWord.getText());
                } else {
                    String firstPhraseWord = firstPhraseTokens.get(currentFirstPhraseReadingIndex).getText();
                    String secondPhraseWord = secondPhraseTokens.get(currentSecondPhraseReadingIndex).getText();
                    boolean equals = compareWords(firstPhraseWord, secondPhraseWord);

                    if (equals) {
                        addToBothResultLists(secondPhraseWord);
                    } else {

                        neitherPhraseIsFinished(firstPhraseTokens, secondPhraseTokens, firstPhraseWord, secondPhraseWord);

                    }

                }

            }


            retValue = new ProductNamesComparison();

            retValue.setDetailsNameResults(this.firstPhraseResults);
            retValue.setSearchNameResults(this.secondPhraseResults);

            String resultPhrase=this.secondPhraseResults.stream().filter(s->s.isMatch()).map(s->s.getWord()).collect(Collectors.joining(" "));
            resultPhrase=resultPhrase.replaceAll("  "," ").trim();

            retValue.setResultPhrase(resultPhrase);

            return retValue;
        }
    }

    private boolean compareWords(String firstPhraseWord, String secondPhraseWord) {
        boolean equals= firstPhraseWord.equals(secondPhraseWord);

        if(!equals){
            equals=checkForImproperEquals(firstPhraseWord,secondPhraseWord);
        }

        return equals;
    }

    private boolean checkForImproperEquals(String firstPhraseWord, String secondPhraseWord) {
        if(firstPhraseWord.toLowerCase().equals("toms")&&secondPhraseWord.toLowerCase().equals("tomatoes"))
            return true;
        if(firstPhraseWord.toLowerCase().equals("fin")&&secondPhraseWord.toLowerCase().equals("finest"))
            return true;

        return false;
    }

    private ProductNamesComparison checkForEmptyPhrases() {
        ProductNamesComparison retValue=null;
        if(checkIfEmpty(firstPhrase)&&checkIfEmpty(secondPhrase)){
            return handleForBothEmpty();
        }

        if(checkIfEmpty(firstPhrase)){
            return handleForFirstEmpty();
        }
        if(checkIfEmpty(secondPhrase)){
            return handleForSecondEmpty();
        }
        return null;
    }

    private ProductNamesComparison handleForSecondEmpty() {
        ProductNamesComparison retValue;
        firstPhraseResults= firstPhrase.getTokens().stream().map(s->new WordComparisonResult(s.getText(),true)).collect(Collectors.toList());
        secondPhraseResults= firstPhrase.getTokens().stream().map(s->new WordComparisonResult("",false)).collect(Collectors.toList());
        retValue=new ProductNamesComparison();
        retValue.setDetailsNameResults(firstPhraseResults);
        retValue.setSearchNameResults(secondPhraseResults);
        retValue.setResultPhrase(firstPhrase.getPhrase());
        return retValue;
    }

    private ProductNamesComparison handleForFirstEmpty() {
        ProductNamesComparison retValue;
        retValue=new ProductNamesComparison();
        secondPhraseResults= secondPhrase.getTokens().stream().map(s->new WordComparisonResult(s.getText(),true)).collect(Collectors.toList());
        firstPhraseResults=secondPhrase.getTokens().stream().map(s->new WordComparisonResult("",false)).collect(Collectors.toList());
        retValue=new ProductNamesComparison();
        retValue.setDetailsNameResults(firstPhraseResults);
        retValue.setSearchNameResults(secondPhraseResults);
        retValue.setResultPhrase(secondPhrase.getPhrase());
        return retValue;
    }

    private ProductNamesComparison handleForBothEmpty() {
        ProductNamesComparison retValue;
        retValue=new ProductNamesComparison();
        retValue.setDetailsNameResults(new ArrayList<>());
        retValue.setSearchNameResults(new ArrayList<>());
        retValue.setResultPhrase("");
        return retValue;
    }

    private boolean checkIfEmpty(TokenizationResults tokenized) {
        return tokenized ==null|| tokenized.getTokens()==null|| tokenized.getTokens().isEmpty();
    }

    private void neitherPhraseIsFinished(List<Token>  firstPhraseSplitted, List<Token> secondPhraseSplitted, String firstPhraseWord, String secondPhraseWord) {
        Optional<Token> foundInFirstPhraseList = firstPhraseSplitted.subList(currentFirstPhraseReadingIndex,firstPhraseSplitted.size()).stream().filter(s -> compareWords(s.getLemma(), secondPhraseWord)).findFirst();
        Optional<Token> foundInSecondPhraseList = secondPhraseSplitted.subList(currentSecondPhraseReadingIndex,secondPhraseSplitted.size()).stream().filter(s -> compareWords(s.getLemma(), firstPhraseWord)).findFirst();

        if(foundInFirstPhraseList.isPresent()&&foundInSecondPhraseList.isPresent()) {
            int foundInFirstPhraseIndex=IntStream.range(currentFirstPhraseReadingIndex,firstPhraseSplitted.size()).filter(i->firstPhraseSplitted.get(i).equals(secondPhraseWord)).findFirst().getAsInt();
            int foundInSecondPhraseIndex=IntStream.range(currentSecondPhraseReadingIndex,secondPhraseSplitted.size()).filter(i->secondPhraseSplitted.get(i).equals(firstPhraseWord)).findFirst().getAsInt();
            if(foundInFirstPhraseIndex==foundInSecondPhraseIndex){
                addConflictingWordsToLists(firstPhraseWord,secondPhraseWord);
            }else if(foundInFirstPhraseIndex>foundInSecondPhraseIndex){
                skipCurrentWordInSecondList(secondPhraseWord);
            }else{

                skipCurrentWordinFirstList(firstPhraseWord);

            }

        }else if(!foundInFirstPhraseList.isPresent()&&!foundInSecondPhraseList.isPresent()){
            addConflictingWordsToLists(firstPhraseWord,secondPhraseWord);
        }else{
            if(foundInFirstPhraseList.isPresent()){
                //the word from second phrase is later in first phrase; we "skip over" the current word from first phrase
                skipCurrentWordinFirstList(firstPhraseWord);
            }
            if(foundInSecondPhraseList.isPresent()){
                skipCurrentWordInSecondList(secondPhraseWord);
            }

        }
    }

    private void skipCurrentWordInSecondList(String word) {
        addWordToSecondListEmptyToFirstList(word);
        currentSecondPhraseReadingIndex++;
    }

    private void skipCurrentWordinFirstList(String word) {
        addWordToFirstListEmptyToSecondList(word);
        currentFirstPhraseReadingIndex++;
    }

    private void addToBothResultLists(String word){
        currentFirstPhraseReadingIndex++;
        currentSecondPhraseReadingIndex++;
        secondPhraseResults.add(new WordComparisonResult(word, true));
        firstPhraseResults.add(new WordComparisonResult(word, true));
    }
    private void addConflictingWordsToLists(String firstPhraseWord, String secondPhraseWord){
        currentFirstPhraseReadingIndex++;
        currentSecondPhraseReadingIndex++;
        secondPhraseResults.add(new WordComparisonResult(secondPhraseWord, false));
        firstPhraseResults.add(new WordComparisonResult(firstPhraseWord, false));
    }
//    private void addToSecondResultListOnly(String word){
//        currentFirstPhraseReadingIndex++;
//        firstPhraseResults.add(new WordParsed("", false));
//        secondPhraseResults.add(new WordParsed(word, false));
//    }
//
//    private void addToFirstResultListOnly(String word){
//        currentSecondPhraseReadingIndex++;
//        firstPhraseResults.add(new WordParsed(word, false));
//        secondPhraseResults.add(new WordParsed("", false));
//    }


    private void addWordToFirstListEmptyToSecondList(String word){
        firstPhraseResults.add(new WordComparisonResult(word, false));
        secondPhraseResults.add(new WordComparisonResult("", false));

    }
    private void addEmptyToSecond(){
        secondPhraseResults.add(new WordComparisonResult("", false));

    }
    private void addWordToSecondListEmptyToFirstList(String word){
        secondPhraseResults.add(new WordComparisonResult(word, false));
        firstPhraseResults.add(new WordComparisonResult("", false));

    }

    private void addEmptyToFirst(){
        firstPhraseResults.add(new WordComparisonResult("", false));

    }


}
