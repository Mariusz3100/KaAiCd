package mariusz.ambroziak.kassistant.ai.logic;

import java.util.List;

import mariusz.ambroziak.kassistant.ai.enums.AmountTypes;

public class CalculatedResults{
	private List<String> notFound;
	private List<String> found; 
	private List<String> improperlyFound;
	private List<String> markedWords;

	public List<String> getMarkedWords() {
		return markedWords;
	}

	public void setMarkedWords(List<String> markedWords) {
		this.markedWords = markedWords;
	}


	public CalculatedResults(List<String> notFound, List<String> found, List<String> improperlyFound, List<String> markedWords) {
		this.notFound = notFound;
		this.found = found;
		this.improperlyFound = improperlyFound;
		this.markedWords = markedWords;
	}

	public List<String> getNotFound() {
		return notFound;
	}

	public void setNotFound(List<String> notFound) {
		this.notFound = notFound;
	}
	public List<String> getFound() {
		return found;
	}
	public void setFound(List<String> found) {
		this.found = found;
	}
	public List<String> getImproperlyFound() {
		return improperlyFound;
	}
	public void setImproperlyFound(List<String> improperlyFound) {
		this.improperlyFound = improperlyFound;
	}
	
	
	
	
	
	
	
}
