package mariusz.ambroziak.kassistant.webapis.wordsapi;

import mariusz.ambroziak.kassistant.ai.utils.QuantityTranslation;

public class WordsApiResultImpostor extends WordsApiResult{
	private QuantityTranslation quanTranslation;

	public WordsApiResultImpostor(QuantityTranslation quanTranslation) {
		super();
		this.quanTranslation = quanTranslation;
	}
	
	
	
}
