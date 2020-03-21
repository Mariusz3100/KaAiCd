package mariusz.ambroziak.kassistant.ai.nlpclients.ner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NamedEntityRecognitionClientService {

	private final RestTemplate restTemplate;

	
	@Autowired
	public NamedEntityRecognitionClientService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public NerResults find(String name) {
		NerResults retValue=this.restTemplate.getForObject("http://127.0.0.1:8000/ner?param={param}", NerResults.class,name);
		
		return retValue;
	}

	
}
