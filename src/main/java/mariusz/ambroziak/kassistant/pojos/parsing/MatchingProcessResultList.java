package mariusz.ambroziak.kassistant.pojos.parsing;

import java.util.ArrayList;
import java.util.List;

public class MatchingProcessResultList {
    private List<MatchingProcessResult> results;

    public List<MatchingProcessResult> getResults() {
        return results;
    }

    public void setResults(List<MatchingProcessResult> results) {
        this.results = results;
    }

    public void addResult(MatchingProcessResult result) {
        if(results==null)
            results=new ArrayList<MatchingProcessResult>();

        this.results.add(result);
    }

    public MatchingProcessResultList(List<MatchingProcessResult> results) {
        this.results = results;
    }
}
