package mariusz.ambroziak.kassistant.ai.wordsapi;

import java.util.ArrayList;

public class WordsApiResult {

	private String originalWord;
	private String baseWord;
	private String definition;
	private ArrayList<String> typeOf;
	private ArrayList<String> synonyms;
	private ArrayList<String> attribute;

	
	


	public ArrayList<String> getAttribute() {
		return attribute;
	}
	public void setAttribute(ArrayList<String> attribute) {
		this.attribute = attribute;
	}
	public ArrayList<String> getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(ArrayList<String> synonyms) {
		this.synonyms = synonyms;
	}
	private String partOfSpeech;
	private ArrayList<String> childTypes;
	
	

	public WordsApiResult(String originalWord, String baseWord, String definition, ArrayList<String> typeOf,
			ArrayList<String> synonyms, ArrayList<String> attribute, String partOfSpeech,
			ArrayList<String> childTypes) {
		super();
		this.originalWord = originalWord;
		this.baseWord = baseWord;
		this.definition = definition;
		this.typeOf = typeOf;
		this.synonyms = synonyms;
		this.attribute = attribute;
		this.partOfSpeech = partOfSpeech;
		this.childTypes = childTypes;
	}
	public WordsApiResult() {
		// TODO Auto-generated constructor stub
	}
	public ArrayList<String> getTypeOf() {
		return typeOf;
	}
	public void setTypeOf(ArrayList<String> typeOf) {
		this.typeOf = typeOf;
	}
	
	public void addTypeOf(String type) {
		if(this.typeOf==null)
			this.typeOf=new ArrayList<String>();
		
		this.typeOf.add(type);
	}
	public ArrayList<String> getChildTypes() {
		return childTypes;
	}
	public void setChildTypes(ArrayList<String> childTypes) {
		this.childTypes = childTypes;
	}
	public void addChildType(String childType) {
		if(childTypes==null)
		
		this.childTypes.add(childType);
	}

	public String getOriginalWord() {
		return originalWord;
	}
	public void setOriginalWord(String originalWord) {
		this.originalWord = originalWord;
	}
	public String getBaseWord() {
		return baseWord;
	}
	public void setBaseWord(String baseWord) {
		this.baseWord = baseWord;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public String getPartOfSpeech() {
		return partOfSpeech;
	}
	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}
}
