package mariusz.ambroziak.kassistant.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.webclients.spacy.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.TokenizationResults;


@Service
public class IngredientPhraseTokenizerTest {
	private TokenizationClientService tokenizator;
	private ResourceLoader resourceLoader;
	private Resource inputFileResource;

	public IngredientPhraseTokenizerTest(TokenizationClientService tokenizator,
			NamedEntityRecognitionClientService nerRecognizer, ResourceLoader resourceLoader) {
		super();
		this.tokenizator = tokenizator;
		this.resourceLoader = resourceLoader;
		this.inputFileResource=this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
	}




	public Map<String, TokenizationResults> parseFromFile() throws IOException {
		Map<String,TokenizationResults> retValue=new HashMap<String,TokenizationResults>();

		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();

		while(line!=null) {
			line=correctErrors(line);
			TokenizationResults parse = this.tokenizator.parse(line);
			
			retValue.put(line,parse);

			line=br.readLine();
		}

		return retValue;

	}

	private static String correctErrors(String phrase) {

		phrase=phrase.replaceFirst("½", "1/2");
		phrase=phrase.replaceFirst("¼", "1/4");
		if(phrase.substring(0, phrase.length()<10?phrase.length():10).indexOf(" c ")>0) {
			phrase=phrase.replaceFirst(" c ", " cup ");
		}



		return phrase;
	}

}
