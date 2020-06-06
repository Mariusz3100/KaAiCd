package mariusz.ambroziak.kassistant.pojos.parsing;

import java.util.ArrayList;
import java.util.List;

public class ParsingResultList {
	private List<ParsingResult> results;

	public List<ParsingResult> getResults() {
		if(results==null)
			results=new ArrayList<ParsingResult>();
		return results;
	}

	public void setResults(List<ParsingResult> results) {
		this.results = results;
	}
	
	public void addResult(ParsingResult result) {
		getResults().add(result);
	}
	
}
