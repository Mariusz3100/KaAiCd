package mariusz.ambroziak.kassistant.pojos.usda;

import mariusz.ambroziak.kassistant.pojos.words.WordAssociacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsingFromUsdaResult {
    private List<UsdaLineParsed> lines;

    private List<UsdaElementParsed> productWords;


    private List<UsdaElementParsed> propertyWords;

    private Map<String, Integer> typesFoundWorkInProgress;

    private List<WordAssociacion> typesFoundFinal;

    public ParsingFromUsdaResult(List<UsdaLineParsed> lines, List<UsdaElementParsed> productWords, List<UsdaElementParsed> propertyWords) {
        this.lines = lines;
        this.productWords = productWords;
        this.propertyWords = propertyWords;
    }

    public ParsingFromUsdaResult() {
        this.lines = new ArrayList<>();
        this.productWords = new ArrayList<>();
        this.propertyWords = new ArrayList<>();
    }


    public List<UsdaLineParsed> getLines() {
        return lines;
    }

    public void setLines(List<UsdaLineParsed> lines) {
        this.lines = lines;
    }

    public List<UsdaElementParsed> getProductWords() {
        return productWords;
    }

    public void setProductWords(List<UsdaElementParsed> productWords) {
        this.productWords = productWords;
    }

    public List<UsdaElementParsed> getPropertyWords() {
        return propertyWords;
    }

    public void setPropertyWords(List<UsdaElementParsed> propertyWords) {
        this.propertyWords = propertyWords;
    }

    public Map<String, Integer> getTypesFoundWorkInProgress() {
        return typesFoundWorkInProgress;
    }


    public void setTypesFoundWorkInProgress(Map<String, Integer> typesFoundWorkInProgress) {
        this.typesFoundWorkInProgress = typesFoundWorkInProgress;
    }

    public void addTypeFoundWorkInProgress(String type) {
        if (typesFoundWorkInProgress == null) {
            typesFoundWorkInProgress = new HashMap<>();
        }

        Integer integer = typesFoundWorkInProgress.get(type);
        if (integer == null) {
            typesFoundWorkInProgress.put(type, 1);
        } else {
            typesFoundWorkInProgress.put(type, ++integer);
            ;
        }
    }


    public List<WordAssociacion> getTypesFoundFinal() {
        return typesFoundFinal;
    }

    public void setTypesFoundFinal(List<WordAssociacion> typesFoundFinal) {
        this.typesFoundFinal = typesFoundFinal;
    }
}
