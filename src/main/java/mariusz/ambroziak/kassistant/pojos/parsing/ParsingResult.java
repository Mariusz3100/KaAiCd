package mariusz.ambroziak.kassistant.pojos.parsing;

import java.util.List;

import mariusz.ambroziak.kassistant.pojos.QualifiedToken;
import mariusz.ambroziak.kassistant.pojos.shop.ProductNamesComparison;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
//class mainly for presenting in angular
public class ParsingResult{
	private String originalPhrase;
	private String alternateName;
	private String ingredientPhrase;
	private String descriptionPhrase;
	private String quantitylessPhrase;

	private IngredientLearningCase expectedResult;
	private CalculatedResults restrictivelyCalculatedResult;
	private CalculatedResults permisivelyCalculatedResult;
	private CalculatedResults restrictivelyCalculatedResultForPhrase;
	private CalculatedResults permisivelyCalculatedResultForPhrase;

	private String entities;
	private String entityLess;
	private String brand;
	private String brandless;
	private String tokenString;
	private List<QualifiedToken> tokens;

	private String correctedPhrase;
	private List<Token> correctedTokens;
	private String productTypeFound;
	private List<List<String>> productTypeReasoning;
	private ProductNamesComparison initialNames;
	private ProductNamesComparison finalNames;

	private List<ConnectionEntry> correctedConnotations;

	private List<ConnectionEntry> originalConnotations;
	List<ConnectionEntry> dependencyConotationsFound;
	private List<List<String>> adjacentyConotationsFound;


	public ProductNamesComparison getFinalNames() {
		return finalNames;
	}

	public void setFinalNames(ProductNamesComparison finalNames) {
		this.finalNames = finalNames;
	}

	public ProductNamesComparison getInitialNames() {
		return initialNames;
	}

	public void setInitialNames(ProductNamesComparison initialNames) {
		this.initialNames = initialNames;
	}

	public CalculatedResults getRestrictivelyCalculatedResultForPhrase() {
		return restrictivelyCalculatedResultForPhrase;
	}

	public void setRestrictivelyCalculatedResultForPhrase(CalculatedResults restrictivelyCalculatedResultForPhrase) {
		this.restrictivelyCalculatedResultForPhrase = restrictivelyCalculatedResultForPhrase;
	}

	public List<List<String>> getProductTypeReasoning() {
		return productTypeReasoning;
	}

	public void setProductTypeReasoning(List<List<String>> productTypeReasoning) {
		this.productTypeReasoning = productTypeReasoning;
	}

	public CalculatedResults getPermisivelyCalculatedResultForPhrase() {
		return permisivelyCalculatedResultForPhrase;
	}

	public void setPermisivelyCalculatedResultForPhrase(CalculatedResults permisivelyCalculatedResultForPhrase) {
		this.permisivelyCalculatedResultForPhrase = permisivelyCalculatedResultForPhrase;
	}

	public String getProductTypeFound() {
		return productTypeFound;
	}

	public void setProductTypeFound(String productTypeFound) {
		this.productTypeFound = productTypeFound;
	}


	public List<ConnectionEntry> getDependencyConotationsFound() {
		return dependencyConotationsFound;
	}

	public void setDependencyConotationsFound(List<ConnectionEntry> dependencyConotationsFound) {
		this.dependencyConotationsFound = dependencyConotationsFound;
	}

	public List<List<String>> getAdjacentyConotationsFound() {
		return adjacentyConotationsFound;
	}

	public void setAdjacentyConotationsFound(List<List<String>> adjacentyConotationsFound) {
		this.adjacentyConotationsFound = adjacentyConotationsFound;
	}

	public List<ConnectionEntry> getOriginalConnotations() {
		return originalConnotations;
	}

	public void setOriginalConnotations(List<ConnectionEntry> originalConnotations) {
		this.originalConnotations = originalConnotations;
	}

	public List<ConnectionEntry> getCorrectedConnotations() {
		return correctedConnotations;
	}

	public void setCorrectedConnotations(List<ConnectionEntry> correctedConnotations) {
		this.correctedConnotations = correctedConnotations;
	}




	public CalculatedResults getRestrictivelyCalculatedResult() {
		return restrictivelyCalculatedResult;
	}

	public void setRestrictivelyCalculatedResult(CalculatedResults restrictivelyCalculatedResult) {
		this.restrictivelyCalculatedResult = restrictivelyCalculatedResult;
	}

	public CalculatedResults getPermisivelyCalculatedResult() {
		return permisivelyCalculatedResult;
	}

	public void setPermisivelyCalculatedResult(CalculatedResults permisivelyCalculatedResult) {
		this.permisivelyCalculatedResult = permisivelyCalculatedResult;
	}



	public String getCorrectedPhrase() {
		return correctedPhrase;
	}

	public void setCorrectedPhrase(String correctedPhrase) {
		this.correctedPhrase = correctedPhrase;
	}

	public List<Token> getCorrectedTokens() {
		return correctedTokens;
	}

	public void setCorrectedTokens(List<Token> correctedtokens) {
		this.correctedTokens = correctedtokens;
	}

	public List<QualifiedToken> getTokens() {
		return tokens;
	}

	public void setTokens(List<QualifiedToken> tokens) {
		this.tokens = tokens;
	}

	public String getTokenString() {
		return tokenString;
	}

	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}

	public String getEntities() {
		return entities;
	}

	public void setEntities(String entities) {
		this.entities = entities;
	}

	public String getEntityLess() {
		return entityLess;
	}

	public void setEntityLess(String entityLess) {
		this.entityLess = entityLess;
	}

	public String getOriginalPhrase() {
		return originalPhrase;
	}

	public void setOriginalPhrase(String originalPhrase) {
		this.originalPhrase = originalPhrase;
	}
	public IngredientLearningCase getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(IngredientLearningCase expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getBrandless() {
		return brandless;
	}

	public void setBrandless(String brandless) {
		this.brandless = brandless;
	}

	public String getAlternateName() {
		return alternateName;
	}

	public void setAlternateName(String alternateName) {
		this.alternateName = alternateName;
	}

	public String getIngredientPhrase() {
		return ingredientPhrase;
	}

	public void setIngredientPhrase(String ingredientPhrase) {
		this.ingredientPhrase = ingredientPhrase;
	}

	public String getDescriptionPhrase() {
		return descriptionPhrase;
	}

	public void setDescriptionPhrase(String descriptionPhrase) {
		this.descriptionPhrase = descriptionPhrase;
	}

	public String getQuantitylessPhrase() {
		return quantitylessPhrase;
	}

	public void setQuantitylessPhrase(String quantitylessPhrase) {
		this.quantitylessPhrase = quantitylessPhrase;
	}
}