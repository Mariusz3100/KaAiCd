package mariusz.ambroziak.kassistant.inputs;


import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.model.ProductLearningCase;
import mariusz.ambroziak.kassistant.hibernate.repository.ProductLearningCaseRepository;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.tesco.TescoDetailsApiClientService;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class TescoDetailsTestCases {
	private static final String headerName="Ocp-Apim-Subscription-Key";
	private static final String headerValue="bb40509242724f799153796d8718c3f3";


	private ResourceLoader resourceLoader;
	private Resource inputFileResource;
	private TescoDetailsApiClientService tescoDetailsService;
	private ProductLearningCaseRepository learningCaseRepository;

	@Autowired
	public TescoDetailsTestCases(ResourceLoader resourceLoader, TescoDetailsApiClientService searchService,ProductLearningCaseRepository learningCaseRepository) {
		this.resourceLoader = resourceLoader;
		this.inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/tomatoProducts");
		this.tescoDetailsService = searchService;
		this.learningCaseRepository=learningCaseRepository;


	}

	public List<ProductParsingProcessObject> getProduktsFromFile() throws IOException {
		List<ProductLearningCase> testCasesFromFile = getTestCasesFromFile();
		List<ProductParsingProcessObject> retValue=testCasesFromFile.stream().map(
				c->new ProductParsingProcessObject(getProductDataFromDbOrApi(c),c)).collect(Collectors.toList());
		return retValue;


	}

	private Tesco_Product getProductDataFromDbOrApi(ProductLearningCase c) {
		return this.tescoDetailsService.getFullDataFromDbOrApi(c.getUrl());
	}


	public List<ProductLearningCase> getTestCasesFromFile() throws IOException {
		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
		List<ProductLearningCase> retValue=new ArrayList<ProductLearningCase>();
		String line=br.readLine();
		while(line!=null) {
			if(!line.startsWith("#")) {

				String[] elements = line.split(";");
				String name= elements[0];

				String type = elements[2];
				ProductType foundType = ProductType.parseType(type);
				String url=elements[1];
				String minimalExpected = elements[3].toLowerCase();
				String extendedExpected = elements[4].toLowerCase();
				ProductLearningCase learningCase=new ProductLearningCase();
				learningCase.setExtended_words_expected(extendedExpected);
				learningCase.setMinimal_words_expected(minimalExpected);
				learningCase.setName(name);
				learningCase.setType_expected(foundType);
				learningCase.setUrl(url);
				System.out.println("Parsed from file" + learningCase.getName());
				retValue.add(learningCase);
			}
			line=br.readLine();
		}
		return retValue;


	}

	public List<ProductLearningCase> getTestCasesFromDb() {
		Iterator<ProductLearningCase> all = this.learningCaseRepository.findAll().iterator();
		List<ProductLearningCase> retValue=new ArrayList<>();

		all.forEachRemaining(c->retValue.add(c));
		return retValue;
	}

	public List<ProductParsingProcessObject> getParsingObjectsFromDb() {
		List<ProductParsingProcessObject> retValue=getTestCasesFromDb().stream()
				.map(s->new ProductParsingProcessObject(getProductDataFromDbOrApi(s),s)).collect(Collectors.toList());
		return retValue;
	}

	public void copyTestCasesFromFileToDb() throws IOException {
		List<ProductLearningCase> testCasesFromFile = getTestCasesFromFile();

		for(ProductLearningCase testCase:testCasesFromFile){
			List<ProductLearningCase> byUrl = learningCaseRepository.findByUrl(testCase.getUrl());
			if(byUrl ==null||byUrl.isEmpty()) {
				learningCaseRepository.save(testCase);
			}
		}

	}



}
