package mariusz.ambroziak.kassistant.ai.logic;

import java.util.ArrayList;
import java.util.List;

public class ParsingResultList {
	private List<ParsingResult> results;

	public List<ParsingResult> getResults() {
		return results;
	}

	public void setResults(List<ParsingResult> results) {
		this.results = results;
	}
	
	public void addResult(ParsingResult result) {
		if(results==null)
			results=new ArrayList<ParsingResult>();
				
		this.results.add(result);
	}
	
}
