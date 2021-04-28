package mariusz.ambroziak.kassistant.pojos.usda;

import java.util.ArrayList;
import java.util.List;

public class UsdaLineParsed {
    private String text;
    List<UsdaElementParsed> results;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<UsdaElementParsed> getResults() {
        return results;
    }

    public void setResults(List<UsdaElementParsed> results) {
        this.results = results;
    }

    public UsdaLineParsed(String text, UsdaElementParsed result) {
        this.text = text;
        this.results = new ArrayList<>();
        this.results.add(result);
    }

    public UsdaLineParsed(String text, List<UsdaElementParsed> results) {
        this.text = text;
        this.results = results;
    }
}
