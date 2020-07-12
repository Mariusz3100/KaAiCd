package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.hibernate.model.MatchExpected;
import mariusz.ambroziak.kassistant.hibernate.repository.EdamanResponseRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.IngredientPhraseLearningCaseRepository;
import mariusz.ambroziak.kassistant.hibernate.repository.MatchExpectedRepository;
import mariusz.ambroziak.kassistant.pojos.quantity.PreciseQuantity;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpectedMatchesService {

	private final RestTemplate restTemplate;

	@Autowired
	private ResourceLoader resourceLoader;
	private Resource inputFileResource;

	@Autowired
	MatchExpectedRepository matchExpectedRepository;


	public static final String csvSeparator=";";


	@Autowired
	public ExpectedMatchesService(RestTemplateBuilder restTemplateBuilder, ResourceLoader resourceLoader) {
		this.restTemplate = restTemplateBuilder.build();

		this.resourceLoader = resourceLoader;

		this.inputFileResource=this.resourceLoader.getResource("classpath:/teachingResources/MatchesInput");


	}



	public void retrieveMatchesExpectedDataFromFileSequentially() throws IOException {


		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
		String line=br.readLine();
		String phrase="";
		while(line!=null) {
			if(!line.startsWith("#")) {
				try {
					if(line.startsWith("+")){
						phrase=line.substring(1);
					}else if(line.startsWith("-")){
						String productName=line.substring(1);

						if(phrase==null||phrase.isEmpty()){
							System.err.println("Empty ingedient phrase");
						}else{
							MatchExpected me=new MatchExpected();
							me.setIngredient(phrase);
							me.setProduct(productName);
							me.setExpectedVerdict(true);
							this.matchExpectedRepository.save(me);
						}

					}
				} catch (Exception e) {
					System.out.println(line + ";" + e.getLocalizedMessage());
				}
			}
			line=br.readLine();
		}

//		String name= elements[0];
//
//		String type = elements[2];
//		ProductType foundType = ProductType.parseType(type);
//		String url=elements[1];
//		String minimalExpected = elements[3].toLowerCase();
//		String extendedExpected = elements[4].toLowerCase();
//		ProductLearningCase learningCase=new ProductLearningCase();
//		learningCase.setExtended_words_expected(extendedExpected);
//		learningCase.setMinimal_words_expected(minimalExpected);
//		learningCase.setName(name);
//		learningCase.setType_expected(foundType);
//		learningCase.setUrl(url);




	}

	private String correctErrors(String phrase) {
		phrase=phrase.replaceFirst("½", "1/2");
		phrase=phrase.replaceFirst("¼", "1/4");
		phrase=phrase.replaceAll("é", "e");
		return phrase;
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
