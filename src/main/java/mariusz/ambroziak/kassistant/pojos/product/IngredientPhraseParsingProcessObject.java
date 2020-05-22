package mariusz.ambroziak.kassistant.pojos.product;

import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientLearningCase;

public class IngredientPhraseParsingProcessObject extends AbstractParsingObject {
	private IngredientLearningCase learningCase;
	public IngredientPhraseParsingProcessObject(IngredientLearningCase er) {
		super();
		this.learningCase=er;
	}

	public IngredientLearningCase getLearningTuple() {
		return learningCase;
	}
	public void setLearningTuple(IngredientLearningCase expectedResult) {
		this.learningCase = expectedResult;
	}

	@Override
	public String getOriginalPhrase() {
		return this.getLearningTuple().getOriginalPhrase();
	}


//	public String getEntitylessString(){
//		return  getEntitylessString(this.getOriginalPhrase());
//	}

	public String calculateEntitylessString(String originalPhrase){

		this.setEntitylessString(super.calculateEntitylessString(originalPhrase));
		return getEntitylessString();
	}
}
