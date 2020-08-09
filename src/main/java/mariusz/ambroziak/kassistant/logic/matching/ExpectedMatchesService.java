package mariusz.ambroziak.kassistant.logic.matching;

import mariusz.ambroziak.kassistant.enums.AmountTypes;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.IngredientLearningCase;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.MatchExpected;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.IngredientPhraseLearningCaseRepository;
import mariusz.ambroziak.kassistant.hibernate.parsing.repository.MatchExpectedRepository;
import mariusz.ambroziak.kassistant.webclients.edamam.nlp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class ExpectedMatchesService {

	private final RestTemplate restTemplate;

	@Autowired
	private ResourceLoader resourceLoader;
	private Resource inputFileResource;

	@Autowired
	MatchExpectedRepository matchExpectedRepository;
	@Autowired
	IngredientPhraseLearningCaseRepository ingredientPhraseLearningCaseRepository;

	@Autowired
	EdamanIngredientParsingService edamanParsingService;

	public static final String csvSeparator=";";


	@Autowired
	public ExpectedMatchesService(RestTemplateBuilder restTemplateBuilder, ResourceLoader resourceLoader) {
		this.restTemplate = restTemplateBuilder.build();

		this.resourceLoader = resourceLoader;

		this.inputFileResource=this.resourceLoader.getResource("classpath:/teachingResources/MatchesInput");


	}

	public void retrieveAllMatchExpectedAndIngredientData() throws IOException {

		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
		String line=br.readLine();
		String phrase="";
		Set<String> ingredients=new HashSet<>();
		while(line!=null) {
			if(!line.startsWith("#")) {
				try {
					if(line.startsWith("+")){
						phrase=line.substring(1);
					}else if(line.startsWith("=")){
							ingredients.add(phrase);
							MatchExpected me=new MatchExpected();
							me.setIngredient(phrase);
							me.setProduct("");
							me.setExpectedVerdict(false);
							this.matchExpectedRepository.save(me);
						}else if(line.startsWith("-")){
						String productName=line.substring(1);

						if(phrase==null||phrase.isEmpty()){
							System.err.println("Empty ingedient phrase");
						}else{
							ingredients.add(phrase);

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

		for(String ingredient:ingredients) {
			boolean saved=this.edamanParsingService.createAndSaveIngredientLearningCase(ingredient);

			if(!saved) {

				IngredientLearningCase ilc = new IngredientLearningCase(ingredient,1,AmountTypes.pcs);
				this.ingredientPhraseLearningCaseRepository.save(ilc);
			}

		}

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
