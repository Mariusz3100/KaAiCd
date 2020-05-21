package mariusz.ambroziak.kassistant.logic;

import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PhraseDependenciesComparator {

    @Autowired
    private TokenizationClientService tokenizer;




    public boolean comparePhrases(String first,String second){
        if(first.equals(second))
            return true;

        TokenizationResults firstTokenized = tokenizer.parse(first);
        TokenizationResults secondTokenized = tokenizer.parse(second);


        if(!arePhrasesSameLength(firstTokenized, secondTokenized))
        {
            return false;
        }else{
            List<ConnectionEntry> allTwoWordDependenciesOfFirst= firstTokenized.getAllTwoWordDependencies().stream().filter(e->!checkifHeadOrChildIsPunctation(e)).collect(Collectors.toList());
            List<ConnectionEntry> allTwoWordDependenciesOfSecond= secondTokenized.getAllTwoWordDependencies().stream().filter(e->!checkifHeadOrChildIsPunctation(e)).collect(Collectors.toList());

            for(int i=0;i<allTwoWordDependenciesOfFirst.size();i++){
                ConnectionEntry searchFor = allTwoWordDependenciesOfFirst.get(i);

                if(allTwoWordDependenciesOfSecond.stream().filter(e->e.permissiveEquals(searchFor)).count()<1){
                    return false;
                }
            }
            return true;
        }

    }

    private boolean arePhrasesSameLength(TokenizationResults firstTokenized, TokenizationResults secondTokenized) {
        List<Token> firstCollected = firstTokenized.getTokens().stream().filter(s -> !checkifPunctation(s)).collect(Collectors.toList());

        List<Token> secondCollected = secondTokenized.getTokens().stream().filter(s -> !checkifPunctation(s)).collect(Collectors.toList());

        boolean result= firstCollected.size()==secondCollected.size();

        return result;

    }

    private boolean checkifPunctation( Token s) {
        return Pattern.matches(WordClasifier.punctuationRegex,s.getText());
    }

    private boolean checkifHeadOrChildIsPunctation(ConnectionEntry e) {
        return checkifPunctation(e.getHead())||checkifPunctation(e.getChild());
    }

}
