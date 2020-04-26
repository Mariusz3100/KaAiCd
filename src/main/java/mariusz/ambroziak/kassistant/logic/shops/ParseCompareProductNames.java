package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.pojos.shop.Product;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordParsed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParseCompareProductNames {
    private String firstPhrase;
    private String secondPhrase;


//    private String shorterPhrase;
//    private String longerPhrase;

    List<WordParsed> firstPhraseResults;
    List<WordParsed> secondPhraseResults;
    private int currentFirstPhraseReadingIndex;
    private int currentSecondPhraseReadingIndex;


    public ParseCompareProductNames(String firstPhrase, String secondPhrase) {
        if(firstPhrase==null||secondPhrase==null){
            throw new IllegalArgumentException("empty arguments");
        }
        this.firstPhrase = firstPhrase;
        this.secondPhrase = secondPhrase;

//        if(firstPhrase.length()>secondPhrase.length()){
//            longerPhrase=firstPhrase;
//            shorterPhrase=secondPhrase;
//        }else {
//            longerPhrase=secondPhrase;
//            shorterPhrase=firstPhrase;
//        }

        firstPhraseResults=new ArrayList<>();
        secondPhraseResults=new ArrayList<>();
    }

    public static Product  parseTwoPhrases(String firstPhrase, String secondPhrase){
        ParseCompareProductNames thisObject=new ParseCompareProductNames(firstPhrase,secondPhrase);
        return thisObject.calculateResults();

    }

    private Product calculateResults(){
        Product retValue=checkForEmptyPhrases();
        if(retValue!=null){
            return retValue;
        }else {
            List<String> firstPhraseSplitted = Arrays.asList(firstPhrase.split(" "));
            List<String> secondPhraseSplitted = Arrays.asList(secondPhrase.split(" "));

            int max = Math.max(firstPhrase.split(" ").length, secondPhrase.split(" ").length);

            currentFirstPhraseReadingIndex = 0;
            currentSecondPhraseReadingIndex = 0;
            while (currentFirstPhraseReadingIndex < firstPhraseSplitted.size() || currentSecondPhraseReadingIndex < secondPhraseSplitted.size()) {


                if (currentFirstPhraseReadingIndex >= firstPhraseSplitted.size() && currentSecondPhraseReadingIndex < secondPhraseSplitted.size()) {
                    String secondPhraseWord = secondPhraseSplitted.get(currentSecondPhraseReadingIndex);
                    currentSecondPhraseReadingIndex++;
                    addWordToSecondListEmptyToFirstList(secondPhraseWord);
                } else if (currentSecondPhraseReadingIndex >= secondPhraseSplitted.size() && currentFirstPhraseReadingIndex < firstPhraseSplitted.size()) {
                    String firstPhraseWord = firstPhraseSplitted.get(currentSecondPhraseReadingIndex);
                    currentFirstPhraseReadingIndex++;
                    addWordToFirstListEmptyToSecondList(firstPhraseWord);
                } else {
                    String firstPhraseWord = firstPhraseSplitted.get(currentFirstPhraseReadingIndex);
                    String secondPhraseWord = secondPhraseSplitted.get(currentSecondPhraseReadingIndex);
                    boolean equals = firstPhraseWord.equals(secondPhraseWord);

                    if (equals) {
                        addToBothResultLists(firstPhraseWord);
                    } else {

                        neitherPhraseIsFinished(firstPhraseSplitted, secondPhraseSplitted, firstPhraseWord, secondPhraseWord);

                    }

                }

            }


            retValue = new Product();

            retValue.setDetailsNameResults(this.firstPhraseResults);
            retValue.setSearchNameResults(this.secondPhraseResults);

            

            return retValue;
        }
    }

    private Product checkForEmptyPhrases() {
        Product retValue=null;
        if((firstPhrase==null||firstPhrase.isEmpty())&&(secondPhrase==null||secondPhrase.isEmpty())){
            retValue=new Product();
            retValue.setDetailsNameResults(new ArrayList<>());
            retValue.setSearchNameResults(new ArrayList<>());
            return retValue;
        }

        if(firstPhrase==null||firstPhrase.isEmpty()){
            retValue=new Product();
            secondPhraseResults= Arrays.asList(secondPhrase.split(" ")).stream().map(s->new WordParsed(s,true)).collect(Collectors.toList());;
            firstPhraseResults= Arrays.asList(secondPhrase.split(" ")).stream().map(s->new WordParsed("",false)).collect(Collectors.toList());;
            retValue=new Product();
            retValue.setDetailsNameResults(firstPhraseResults);
            retValue.setSearchNameResults(secondPhraseResults);
            return retValue;
        }
        if(secondPhrase==null||secondPhrase.isEmpty()){
            firstPhraseResults= Arrays.asList(firstPhrase.split(" ")).stream().map(s->new WordParsed(s,true)).collect(Collectors.toList());
            secondPhraseResults= Arrays.asList(firstPhrase.split(" ")).stream().map(s->new WordParsed("",false)).collect(Collectors.toList());
            retValue=new Product();
            retValue.setDetailsNameResults(firstPhraseResults);
            retValue.setSearchNameResults(secondPhraseResults);
            return retValue;
        }
        return null;
    }

    private void neitherPhraseIsFinished(List<String> firstPhraseSplitted, List<String> secondPhraseSplitted, String firstPhraseWord, String secondPhraseWord) {
        Optional<String> foundInFirstPhraseList = firstPhraseSplitted.subList(currentFirstPhraseReadingIndex,firstPhraseSplitted.size()).stream().filter(s -> s.equals(secondPhraseWord)).findFirst();
        Optional<String> foundInSecondPhraseList = secondPhraseSplitted.subList(currentSecondPhraseReadingIndex,secondPhraseSplitted.size()).stream().filter(s -> s.equals(firstPhraseWord)).findFirst();
        if(!foundInFirstPhraseList.isPresent()&&!foundInSecondPhraseList.isPresent()){
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
        secondPhraseResults.add(new WordParsed(word, true));
        firstPhraseResults.add(new WordParsed(word, true));
    }
    private void addConflictingWordsToLists(String firstPhraseWord, String secondPhraseWord){
        currentFirstPhraseReadingIndex++;
        currentSecondPhraseReadingIndex++;
        secondPhraseResults.add(new WordParsed(secondPhraseWord, false));
        firstPhraseResults.add(new WordParsed(firstPhraseWord, false));
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
        firstPhraseResults.add(new WordParsed(word, false));
        secondPhraseResults.add(new WordParsed("", false));

    }
    private void addEmptyToSecond(){
        secondPhraseResults.add(new WordParsed("", false));

    }
    private void addWordToSecondListEmptyToFirstList(String word){
        secondPhraseResults.add(new WordParsed(word, false));
        firstPhraseResults.add(new WordParsed("", false));

    }

    private void addEmptyToFirst(){
        firstPhraseResults.add(new WordParsed("", false));

    }


}
