package mariusz.ambroziak.kassistant.ai.edamam.nlp;

import java.util.List;

public class EdamamNlpIngredientOuter {
	private String text;
	private List<EdamamNlpSingleIngredientInner> parsed;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<EdamamNlpSingleIngredientInner> getParsed() {
		return parsed;
	}
	public void setParsed(List<EdamamNlpSingleIngredientInner> parsed) {
		this.parsed = parsed;
	}

}
