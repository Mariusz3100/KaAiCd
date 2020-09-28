package mariusz.ambroziak.kassistant.logic;

import mariusz.ambroziak.kassistant.constants.NlpConstants;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.webclients.spacy.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.webclients.usda.SingleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PhraseDependenciesComparator {

    @Autowired
    private TokenizationClientService tokenizer;

    public boolean comparePhrases(String searched,String description){
        return extendedComparePhrases(searched,description).isComparisonResults();
    }

    public PhraseDependenciesComparatotionResult extendedComparePhrases(String searched, SingleResult sr){
        PhraseDependenciesComparatotionResult result = extendedComparePhrases(searched, sr.getDescription());
        result.setSingleResult(sr);
        return result;
    }

    public PhraseDependenciesComparatotionResult extendedComparePhrases(String searched,String description){
        if(searched.equals(description))
            return new PhraseDependenciesComparatotionResult(true);

        TokenizationResults firstTokenized = tokenizer.parse(searched.toLowerCase());
        TokenizationResults secondTokenized = tokenizer.parse(description.toLowerCase());


        if(!isSecondOneAtMostOneLonger(firstTokenized, secondTokenized))
        {
            return new PhraseDependenciesComparatotionResult(false);
        }else{
            List<ConnectionEntry> allTwoWordDependenciesOfFirst= firstTokenized.getAllTwoWordDependencies().stream().filter(e->!checkifHeadOrChildIsPunctationOrOfWord(e)).collect(Collectors.toList());
            List<ConnectionEntry> allTwoWordDependenciesOfSecond= secondTokenized.getAllTwoWordDependencies()
                    .stream().filter(e->!checkifHeadOrChildIsPunctationOrOfWord(e))
                    .filter(connectionEntry ->! connectionEntry.getHead().getText().equalsIgnoreCase(connectionEntry.getChild().getText()))
                    .collect(Collectors.toList());
            if(firstTokenized.getTokens().size()==1&&secondTokenized.getTokens().size()==1){
                boolean onlyTokenequal= firstTokenized.getTokens().get(0).getText().equals(secondTokenized.getTokens().get(0))
                        ||firstTokenized.getTokens().get(0).getLemma().equals(secondTokenized.getTokens().get(0).getLemma());
                return new PhraseDependenciesComparatotionResult(onlyTokenequal);
            }


            for(int i=0;i<allTwoWordDependenciesOfFirst.size();i++){
                ConnectionEntry searchFor = allTwoWordDependenciesOfFirst.get(i);

                if(allTwoWordDependenciesOfSecond.stream().filter(e->e.permissiveEquals(searchFor)).count()>0){
                    allTwoWordDependenciesOfSecond.removeIf(e->e.permissiveEquals(searchFor));
                }else{
                    return new PhraseDependenciesComparatotionResult(false) ;
                }
            }
            if(allTwoWordDependenciesOfSecond.isEmpty()){
             //     if(allTwoWordDependenciesOfSecond.isEmpty()){
                return  new PhraseDependenciesComparatotionResult(true);
            }else{
                String missingMessage=allTwoWordDependenciesOfSecond.stream().map(e->e.getHead().getText()+" "+e.getChild().getText()).collect(Collectors.joining(","));

                Optional<Token> any = secondTokenized.getTokens().stream().filter(t1 -> !firstTokenized.getTokens().stream().anyMatch(t2 -> t2.getText().equals(t1.getText()))).findAny();
                String missingOne="";


                List<Token> missingWords = secondTokenized.getTokens().stream().filter(st -> !st.getPos().equals(PythonSpacyLabels.punctPos) && firstTokenized.getTokens().stream().filter(ft -> ft.getLemma().equals(st.getLemma())).count() == 0).collect(Collectors.toList());

                if(missingWords.size()==0){
                    return  new PhraseDependenciesComparatotionResult(true);
                }else if(missingWords.stream().filter(t->!WordClasifier.freshFoodKeywords.contains(t.getText())).count()==0){

              //      System.out.println("searching for "+searched+", found "+description+", extra: "+missingMessage+", extra word is acceptable: "+missingWords.get(0));
                    PhraseDependenciesComparatotionResult result = new PhraseDependenciesComparatotionResult(true);
                    result.setTypeDeduced(ProductType.fresh);
                    result.setKeywordsFound(missingWords.stream().map(token -> token.getText()).collect(Collectors.toList()));
                    result.setResultingPhrase(searched.toLowerCase());
                    return result;
                }else{
                    String missingWordsString=missingWords.stream().map(t->t.getText()).collect(Collectors.joining(", "));
               //     System.err.println("searching for "+searched+", found "+description+", extra: "+missingMessage+", extra word: ["+missingWordsString+"]");
                    return  new PhraseDependenciesComparatotionResult(false);
                }


            }


        }

    }

    public boolean isSecondDerivativeOfFirst(String original,String derivative){
        if(derivative.indexOf(original)>0)
            return true;

        TokenizationResults originalTokenized = tokenizer.parse(original.toLowerCase());
        TokenizationResults derivativeTokenized = tokenizer.parse(derivative.toLowerCase());

        if(originalTokenized.getTokens().stream()
                .filter(originalT-> derivativeTokenized.getTokens().stream().noneMatch(derivativeT -> derivativeT.getLemma().equals(originalT.getLemma())))
                .count()==0)
            return true;
        else
            return false;

    }

    private boolean isSecondOneAtMostOneLonger(TokenizationResults firstTokenized, TokenizationResults secondTokenized) {
        List<Token> firstCollected = firstTokenized.getTokens().stream().filter(s -> !checkifPunctation(s)&&!isOfWord(s)).collect(Collectors.toList());

        List<Token> secondCollected = secondTokenized.getTokens().stream().filter(s -> !checkifPunctation(s)&&!isOfWord(s)).collect(Collectors.toList());

        boolean result= firstCollected.size()==secondCollected.size()||firstCollected.size()==secondCollected.size()-1;

        return result;

    }

    private static boolean checkifPunctation( Token s) {
        return Pattern.matches(WordClasifier.punctuationRegex,s.getText());
    }

    public static boolean checkifHeadOrChildIsPunctationOrOfWord(ConnectionEntry e) {
        return checkifPunctation(e.getHead())||checkifPunctation(e.getChild())|| isOfWord(e.getHead()) || isOfWord(e.getChild());
    }

    private static boolean isOfWord(Token head) {
        return head.getText().equals(NlpConstants.of_Word);
    }

}
