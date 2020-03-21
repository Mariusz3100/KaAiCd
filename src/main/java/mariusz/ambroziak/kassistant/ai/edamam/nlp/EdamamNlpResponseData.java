package mariusz.ambroziak.kassistant.ai.edamam.nlp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EdamamNlpResponseData {

	private String uri;
	private List<EdamamNlpIngredientOuter> ingredients;

	public List<EdamamNlpIngredientOuter> getIngredients() {
		return ingredients;
	}
	public void setIngredients(List<EdamamNlpIngredientOuter> ingredients) {
		this.ingredients = ingredients;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
	
	public String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
        
	}
	
	public static EdamamNlpResponseData createEmpty() {
		return new EdamamNlpResponseData("",new ArrayList<EdamamNlpIngredientOuter>());
	}
	public boolean isEmpty() {
		return this.getUri().isEmpty()&&this.getIngredients().size()<1;
	}
	
	public EdamamNlpResponseData(String uri, List<EdamamNlpIngredientOuter> ingredients) {
		super();
		this.uri = uri;
		this.ingredients = ingredients;
	}
	
	
	
}
