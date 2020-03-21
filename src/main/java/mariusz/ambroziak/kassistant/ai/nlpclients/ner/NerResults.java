package mariusz.ambroziak.kassistant.ai.nlpclients.ner;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NerResults {
	private String phrase;
	private List<NamedEntity> entities;

	@Override
	public String toString() {
		return "NerResults [phrase=" + phrase + ", entities=" + entities + "]";
	}
	public String getPhrase() {
		return phrase;
	}
	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	public List<NamedEntity> getEntities() {
		return entities;
	}
	public void setEntities(List<NamedEntity> entities) {
		this.entities = entities;
	}





}