package mariusz.ambroziak.kassistant.ai.utils;

import mariusz.ambroziak.kassistant.ai.edamam.nlp.LearningTuple;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;

public class ParsingProcessObject extends AbstractParsingObject{
	private LearningTuple learningCase;
	public ParsingProcessObject(LearningTuple er) {
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
	
	
}
