package mariusz.ambroziak.kassistant.pojos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mariusz.ambroziak.kassistant.pojos.shop.ProductNamesComparison;
import mariusz.ambroziak.kassistant.webclients.spacy.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.LearningTuple;

import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntity;
import mariusz.ambroziak.kassistant.webclients.spacy.ner.NerResults;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;

public abstract class AbstractParsingObject {
	private String bracketLessPhrase;
	private String entitylessString;
	private NerResults nerResults;
	private TokenizationResults entitylessTokenized;
	private TokenizationResults correctedToknized;
	private TokenizationResults productPhraseTokenized;
	private List<QualifiedToken> finalResults;
	private List<QualifiedToken> permissiveFinalResults;
	private ProductType foodTypeClassified;
	List<ConnectionEntry> entitylessConotations;
	List<ConnectionEntry> correctedConotations;
	List<ConnectionEntry> productPhraseConotations;
	List<ConnectionEntry> dependencyConotationsFound;
	private List<List<String>> adjacentyConotationsFound;


	private ProductNamesComparison initialNames;
	private ProductNamesComparison finalNames;

	public List<List<String>> getAdjacentyConotationsFound() {
		if(this.adjacentyConotationsFound==null)
			adjacentyConotationsFound=new ArrayList<List<String>>();

		return adjacentyConotationsFound;
	}

	public void setAdjacentyConotationsFound(List<List<String>> adjacentyConotationsFound) {
		this.adjacentyConotationsFound = adjacentyConotationsFound;
	}

	public List<ConnectionEntry> getDependencyConotationsFound() {
		if(this.dependencyConotationsFound==null)
			dependencyConotationsFound=new ArrayList<ConnectionEntry>();


		return dependencyConotationsFound;
	}

	public void setConotationsFound(List<ConnectionEntry> conotationsFound) {
		this.dependencyConotationsFound = conotationsFound;
	}

	Map<Integer, QualifiedToken> futureTokens;

	public Map<Integer, QualifiedToken> getFutureTokens() {
		if(this.futureTokens==null)
			this.futureTokens=new HashMap<Integer, QualifiedToken>();

		return futureTokens;
	}

	public void addFutureToken(Integer index,QualifiedToken futureToken) {

		this.getFutureTokens().put(index,futureToken);
	}

	public TokenizationResults getProductTokenized() {
		return productPhraseTokenized;
	}

	public void setProductPhraseTokenized(TokenizationResults productTokenized) {
		this.productPhraseTokenized = productTokenized;
	}
	public List<ConnectionEntry> getProductPhraseConotations() {
		return productPhraseConotations;
	}

	public void setProductPhraseConotations(List<ConnectionEntry> productPhraseConotations) {
		this.productPhraseConotations = productPhraseConotations;
	}

	private String quantityPhrase;
	private String productPhrase;

	public TokenizationResults getCorrectedToknized() {
		return correctedToknized;
	}

	public void setCorrectedToknized(TokenizationResults correctedToknized) {
		this.correctedToknized = correctedToknized;
	}

	public List<ConnectionEntry> getFromEntityLessConotations() {
		if(this.entitylessConotations==null)
			entitylessConotations=new ArrayList<ConnectionEntry>();

		return entitylessConotations;
	}

	public void setFromEntityLessConotations(List<ConnectionEntry> originalConotations2) {
		this.entitylessConotations = originalConotations2;
	}

	public List<ConnectionEntry> getCorrectedConotations() {
		return correctedConotations;
	}

	public void setCorrectedConotations(List<ConnectionEntry> correctedConotations) {
		this.correctedConotations = correctedConotations;
	}

	public ProductType getFoodTypeClassified() {
		if(foodTypeClassified==null){
			return ProductType.unknown;
		}else{
			return foodTypeClassified;
		}

	}

	public void setFoodTypeClassified(ProductType productClassified) {
		this.foodTypeClassified = productClassified;
	}

	public List<QualifiedToken> getPermissiveFinalResults() {
		return permissiveFinalResults;
	}

	public void setPermissiveFinalResults(List<QualifiedToken> permissiveFinalResults) {
		this.permissiveFinalResults = permissiveFinalResults;
	}

	public List<Token> getCorrectedtokens() {
		return this.getCorrectedToknized().getTokens();
	}


	public String getQuantityPhrase() {
		return quantityPhrase;
	}

	public void setQuantityPhrase(String quantityPhrase) {
		this.quantityPhrase = quantityPhrase;
	}

	public String getProductPhrase() {
		return productPhrase;
	}

	public void setProductPhrase(String productPhrase) {
		this.productPhrase = productPhrase;
	}

	public List<QualifiedToken> getFinalResults() {
		if(this.finalResults==null)
			this.finalResults=new ArrayList<>();

		return finalResults;
	}

	public String getFinalResultsString(){
		if(getFinalResults().isEmpty())
			return "";
		else
			return getFinalResults().stream().map(s->s.getText()).collect(Collectors.joining(" "));
	}

	public String getPermissiveFinalResultsString(){
		if(getPermissiveFinalResults().isEmpty())
			return "";
		else
			return getPermissiveFinalResults().stream().map(s->s.getText()).collect(Collectors.joining(" "));
	}

	public NerResults getEntities() {
		return nerResults;
	}

	public void setEntities(NerResults entities) {
		this.nerResults = entities;
	}

	public List<NamedEntity> getCardinalEntities() {
		NerResults allEntities=this.getEntities();
		List<NamedEntity> retValue=new ArrayList<NamedEntity>();
		if(allEntities==null||allEntities.getEntities()==null||allEntities.getEntities().isEmpty()) {
			return new ArrayList<NamedEntity>();
		}else {
			for(NamedEntity ner:allEntities.getEntities()) {
				if(ner.getLabel().equals(PythonSpacyLabels.entitiesCardinalLabel)) {
					retValue.add(ner);
				}
			}
		}
		return retValue;
	}

	public NerResults getNerResults() {
		return nerResults;
	}

	public void setNerResults(NerResults nerResults) {
		this.nerResults = nerResults;
	}

	public TokenizationResults getEntitylessTokenized() {
		return entitylessTokenized;
	}

	public void setEntitylessTokenized(TokenizationResults entitylessTokenized) {
		this.entitylessTokenized = entitylessTokenized;
	}

	public String calculateEntitylessString(String phrase) {
		NerResults allEntities=this.getEntities();
		String retValue=phrase;
		if(allEntities==null||allEntities.getEntities()==null||allEntities.getEntities().isEmpty()) {
			return retValue=phrase;
		}else {
			for(NamedEntity ner:allEntities.getEntities()) {
				if(ner.getLabel().equals(PythonSpacyLabels.entitiesCardinalLabel)) {

				}else {
					retValue=retValue.replaceAll(ner.getText(),"").replaceAll("  ", " ").trim();
				}
			}
			retValue=retValue.replaceAll("( )+", " ");
			return retValue;
		}
	}


//	public abstract String getEntitylessString();

	protected abstract String getOriginalPhrase();

	public String createCorrectedPhrase() {

		return this.getQuantityPhrase()+" of "+this.getProductPhrase();
	}

	public LearningTuple calculateResultFromCollectedData() {
		LearningTuple retValue=new LearningTuple(productPhrase, 0, productPhrase, productPhrase, null);

		return retValue;
	}

	public void addPermissiveResult(int index, QualifiedToken qt) {
		if(this.getPermissiveFinalResults()==null)
			this.permissiveFinalResults=new ArrayList<>();

		if(index<this.getPermissiveFinalResults().size()) {
			this.getPermissiveFinalResults().set(index,qt);
		}else{
			while(index>this.getPermissiveFinalResults().size()) {
				this.getPermissiveFinalResults().add(QualifiedToken.createNullObject());
			}
			this.getPermissiveFinalResults().add(qt);
		}

	}

	public void addResult(int index, QualifiedToken qt) {
		if(this.getFinalResults()==null)
			this.finalResults=new ArrayList<>();

		if(index<this.getFinalResults().size()) {
			this.getFinalResults().set(index,qt);
		}else{
			while(index>this.getFinalResults().size()) {
				this.getFinalResults().add(QualifiedToken.createNullObject());
			}
			this.getFinalResults().add(qt);
		}

	}

	public ProductNamesComparison getInitialNames() {
		return initialNames;
	}

	public void setInitialNames(ProductNamesComparison initialNames) {
		this.initialNames = initialNames;
	}

	public ProductNamesComparison getFinalNames() {
		return finalNames;
	}

	public void setFinalNames(ProductNamesComparison finalNames) {
		this.finalNames = finalNames;
	}

	public String getBracketLessPhrase() {
		return bracketLessPhrase;
	}

	public void setBracketLessPhrase(String bracketLessPhrase) {
		this.bracketLessPhrase = bracketLessPhrase;
	}

	public String getEntitylessString() {
		return entitylessString;
	}

	public void setEntitylessString(String entitylessString) {
		this.entitylessString = entitylessString;
	}
}
