package mariusz.ambroziak.kassistant.ai.edamam.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import mariusz.ambroziak.kassistant.ai.enums.ProductType;
import mariusz.ambroziak.kassistant.ai.tesco.ProductData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

@Service
public class EdamanIngredientParsingService {

	private final RestTemplate restTemplate;
	
	@Autowired
	private ResourceLoader resourceLoader;
	private Resource inputFileResource;
	//private Resource outputFileResource;
	private Resource expectedOutputFileResource;

	
	

	private final String baseUrl="https://api.edamam.com/api/nutrition-details?app_id=1d006ca9&app_key=d089c348b9338fc421bdc6695ff34e8c";
	private final String csvSeparator=";";
	
	
	
	@Autowired
	public EdamanIngredientParsingService(RestTemplateBuilder restTemplateBuilder, ResourceLoader resourceLoader) {
		this.restTemplate = restTemplateBuilder.build();
		
		this.resourceLoader = resourceLoader;
		
		this.inputFileResource=this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
		this.expectedOutputFileResource=this.resourceLoader.getResource("classpath:/teachingResources/wordsTestset1");
		

	}

	public List<LearningTuple> retrieveDataFromFile() throws IOException {
		InputStream inputStream = expectedOutputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();
		List<LearningTuple> listOfExpectedResults=new ArrayList<LearningTuple>();
		
		while(line!=null) {
			if(!line.startsWith("#")) {
				String[] elements = line.split(";", 5);

				String phrase = elements[0];
				String foodFound = elements[1];
				String multiplierString = elements[2];
				String containerPhraseString = elements[3];
				ProductType pt = elements.length <= 4 ? ProductType.unknown : ProductType.parseType(elements[4]);

				float multiplier = Float.parseFloat(multiplierString);
				PreciseQuantity pq = EdamanApiQuantityExtractor.getResultingQuantity(multiplier, containerPhraseString);
				LearningTuple er = new LearningTuple(phrase, pq.getAmount(), containerPhraseString, foodFound, pt);

				listOfExpectedResults.add(er);
			}
			line = br.readLine();

		}

		return listOfExpectedResults;
	}
	
	
	public EdamamNlpResponseData findInApi(List<String> ingredientLines) {
		if(ingredientLines.isEmpty()) {
			return EdamamNlpResponseData.createEmpty();
		}
		JSONObject bodyJson=new JSONObject();
		JSONArray jArr=new JSONArray();
		for(String ingLine:ingredientLines) {
			jArr.put(ingLine);
		}
		
		bodyJson.put("ingr", jArr);

		final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        final HttpEntity<String> entity = new HttpEntity<String>(bodyJson.toString(),headers);
        
		ResponseEntity<EdamamNlpResponseData> retValue=this.restTemplate.exchange(baseUrl, HttpMethod.POST,entity, EdamamNlpResponseData.class);
		
		return retValue.getBody();
	}

	public EdamamNlpResponseData find(String param) {
		if(param==null||param.isEmpty()) {
			return EdamamNlpResponseData.createEmpty();
		}
		List<String> paramList=new ArrayList<String>();
		paramList.add(param);
		return findInApi(paramList);
	}
	
	
	public void retrieveAndSaveEdamanParsingDataFromFile() throws IOException {
		List<String> lines = readAllIngredientLines();
		
		EdamamNlpResponseData found = this.findInApi(lines);
		StringBuilder sb=new StringBuilder();
		for(EdamamNlpIngredientOuter outer:found.getIngredients()) {
			String original=outer.getText();
			for(EdamamNlpSingleIngredientInner inner:outer.getParsed()) {
				String line=original+csvSeparator+inner.getFoodMatch()+csvSeparator
						+inner.getQuantity()+csvSeparator+inner.getMeasure();
				System.out.println(line);
			}
		}
		
		
		
		
	}
	
	
	public void retrieveEdamanParsingDataFromFileSequentially() throws IOException {
		
		
		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();
		List<String> lines=new ArrayList<String>();
		
		while(line!=null) {
			try {
				EdamamNlpResponseData found = this.find(line);
			
				for(EdamamNlpIngredientOuter outer:found.getIngredients()) {
					String original=outer.getText();
					for(EdamamNlpSingleIngredientInner inner:outer.getParsed()) {
						String lineOut=original+csvSeparator+inner.getFoodMatch()+csvSeparator
								+inner.getQuantity()+csvSeparator+inner.getMeasure();
						System.out.println(lineOut);
					}
				}
			}catch(UnknownHttpStatusCodeException e) {
				System.out.println(line+";"+e.getLocalizedMessage());
			}
			line=br.readLine();
		}
		
		
		
		
		
		
	}

	private List<String> readAllIngredientLines() throws IOException {
		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();
		List<String> lines=new ArrayList<String>();
		
		while(line!=null) {
			lines.add(line);
			line=br.readLine();
		}
		return lines;
	}

	
}
