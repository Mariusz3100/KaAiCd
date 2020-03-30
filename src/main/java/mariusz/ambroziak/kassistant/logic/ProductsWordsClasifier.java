package mariusz.ambroziak.kassistant.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mariusz.ambroziak.kassistant.pojos.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.spacy.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.enums.WordType;
import org.springframework.stereotype.Component;

import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntity;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.Token;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;

@Component
public class ProductsWordsClasifier extends WordClasifier{

	public void calculateWordsType(ProductParsingProcessObject parsingAPhrase) {
		Map<Integer, WordType> futureTokens=new HashMap<Integer,WordType>();
		for(int i=0;i<parsingAPhrase.getEntitylessTokenized().getTokens().size();i++) {
			Token t=parsingAPhrase.getEntitylessTokenized().getTokens().get(i);

			if(PythonSpacyLabels.tokenisationCardinalLabel.equals(t.getTag())) {
				addQuantityResult(parsingAPhrase,i, t);
				List<NamedEntity> cardinalEntities = parsingAPhrase.getCardinalEntities();

				for(NamedEntity cardinalEntity:cardinalEntities) {
					if(cardinalEntity.getText().contains(t.getText())){
						if(!PythonSpacyLabels.entitiesCardinalLabel.equals(cardinalEntity.getLabel())) {
							System.err.println("Tokenization and Ner labels do not match");
						}
					}
				}
			}else {
				try {
					classifyWord(parsingAPhrase,i);
				} catch (WordNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	
	
}
