package mariusz.ambroziak.kassistant.logic;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.CalculatedResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractParser {


    protected CalculatedResults calculateWordsFound(String expected, List<QualifiedToken> finalResults) {
        return calculateWordsFound(Arrays.asList(expected.split(" ")),finalResults);
    }


    protected CalculatedResults calculateWordsFound(List<String> expected, List<QualifiedToken> finalResults) {
        List<String> found=new ArrayList<String>();
        List<String> mistakenlyFound=new ArrayList<String>();

        for(QualifiedToken qt:finalResults) {
            String qtText = qt.getText().toLowerCase();
            String qtLemma=qt.getLemma();
            if(qt.getWordType()== WordType.ProductElement) {
                if(expected.contains(qtText)||expected.contains(qtLemma)) {
                    if(expected.contains(qtText)){
                        found.add(qtText);
                        expected=expected.stream().filter(s->!s.equals(qtText)).collect(Collectors.toList());
                    }else {
                        found.add(qtLemma);
                        expected = expected.stream().filter(s -> !s.equals(qtLemma)).collect(Collectors.toList());
                    }
                }else {
                    mistakenlyFound.add(qtText);
                }
            }
        }

        List<String> wordsMarked=finalResults.stream().filter(qualifiedToken -> qualifiedToken.getWordType()==WordType.ProductElement).map(qualifiedToken -> qualifiedToken.getText()).collect(Collectors.toList());

        return new CalculatedResults(expected,found,mistakenlyFound,wordsMarked);
    }
}
