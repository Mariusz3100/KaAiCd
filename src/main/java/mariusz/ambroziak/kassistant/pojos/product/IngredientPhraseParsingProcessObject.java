package mariusz.ambroziak.kassistant.pojos.product;

import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.webclients.edamamnlp.LearningTuple;

public class IngredientPhraseParsingProcessObject extends AbstractParsingObject {
	private LearningTuple learningCase;
	public IngredientPhraseParsingProcessObject(LearningTuple er) {
		super();
		this.learningCase=er;
	}

	public LearningTuple getLearningTuple() {
		return learningCase;
	}
	public void setLearningTuple(LearningTuple expectedResult) {
		this.learningCase = expectedResult;
	}

	@Override
	protected String getOriginalPhrase() {
		return this.getLearningTuple().getOriginalPhrase();
	}


	public String getEntitylessString(){
		return  getEntitylessString(this.getOriginalPhrase());
	}
}
