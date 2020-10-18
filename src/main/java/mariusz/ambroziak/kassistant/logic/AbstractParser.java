package mariusz.ambroziak.kassistant.logic;

import mariusz.ambroziak.kassistant.enums.WordType;
import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.parsing.CalculatedResults;
import mariusz.ambroziak.kassistant.pojos.product.IngredientPhraseParsingProcessObject;

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
        expected=expected.stream().map(s->removeTrailingComma(s)).collect(Collectors.toList());
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


    private String removeTrailingComma(String x){
        if(x.endsWith(",")){
            x=x.substring(0,x.length()-1);
        }
        return x;
    }

    protected void correctErrorsHandleBracketsAndSetBracketLess(AbstractParsingObject parsingAPhrase) {
        String x=parsingAPhrase.getOriginalPhrase();
        x=correctErrors(x);
        if(x==null)
            parsingAPhrase.setBracketLessPhrase("");
        else {
            x=x.replaceAll("\\(.*\\)","");
            x=x.replaceAll("  "," ");
            parsingAPhrase.setBracketLessPhrase(x);
        }



    }

    protected String correctErrors(String phrase) {
        phrase=phrase.replaceAll("½", "1/2");
        phrase=phrase.replaceAll("¼", "1/4");
        phrase=phrase.replaceAll("é", "e");

        if(phrase.startsWith("M ")) {
            phrase=phrase.replaceFirst("M ", "Morrisons ");
        }
        if(phrase.startsWith("m ")) {
            phrase=phrase.replaceFirst("m ", "Morrisons ");
        }


        if(phrase.indexOf("&")>0) {
            phrase=phrase.replaceAll("&", " and ").replaceAll("  "," ");
        }

        if(phrase.toLowerCase().indexOf("mayo")>0) {
            //in case we have both mayonnaise and mayo
            phrase=phrase.replaceAll("mayonnaise", " mayo ").replaceAll("  "," ");

            phrase=phrase.replaceAll("mayo", " mayonnaise ").replaceAll("  "," ");
            phrase=phrase.replaceAll("Mayo", " mayonnaise ").replaceAll("  "," ");
        }

        if(phrase.indexOf("w/ ")>0) {
            phrase=phrase.replaceAll("w/ ", "with ");
        }


        return phrase.trim();
    }
}
