package mariusz.ambroziak.kassistant.pojos.usda;

import java.util.ArrayList;
import java.util.List;

public class ParsingFromUsdaResult {
    private List<UsdaLineParsed> lines;

    private List<UsdaElementParsed> productWords;


    private List<UsdaElementParsed> propertyWords;



    public ParsingFromUsdaResult(List<UsdaLineParsed> lines, List<UsdaElementParsed> productWords, List<UsdaElementParsed> propertyWords) {
        this.lines = lines;
        this.productWords = productWords;
        this.propertyWords = propertyWords;
    }

    public ParsingFromUsdaResult() {
        this.lines = new ArrayList<>();
        this.productWords = new ArrayList<>();
        this.propertyWords =new ArrayList<>();
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
}
