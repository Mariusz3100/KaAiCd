package mariusz.ambroziak.kassistant.logic;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UsdaPhraseComparator {

    public static boolean comparePhrases(String phrase1,String phrase2){
        if(phrase1.toLowerCase().equals(phrase2.toLowerCase()))
            return true;
        else {
            if(phrase1.contains(",")&&!phrase2.contains(",")){
                return compareForSecondContainingComma(phrase2,phrase1);
            }else if(!phrase1.contains(",")&&phrase2.contains(",")){
                return compareForSecondContainingComma(phrase1,phrase2);

            }

        }


        return false;
    }


    private static boolean compareForSecondContainingComma(String comaless,String withComma){
        List<String> strings = Arrays.asList(withComma.split(","));

        if(strings.size()>2)
        {
            return false;
        }else{
            String withCommaReversed=(strings.get(1)+ " "+strings.get(0)).replaceAll("  "," ").trim();
            return withCommaReversed.equalsIgnoreCase(comaless);
        }






    }

}
