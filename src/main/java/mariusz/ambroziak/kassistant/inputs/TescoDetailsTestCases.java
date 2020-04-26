package mariusz.ambroziak.kassistant.inputs;


import mariusz.ambroziak.kassistant.enums.ProductType;
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


@Component
public class TescoDetailsTestCases {
	private static final String headerName="Ocp-Apim-Subscription-Key";
	private static final String headerValue="bb40509242724f799153796d8718c3f3";


	private ResourceLoader resourceLoader;
	private Resource inputFileResource;
	private TescoDetailsApiClientService tescoDetailsService;

	@Autowired
	public TescoDetailsTestCases(ResourceLoader resourceLoader, TescoDetailsApiClientService searchService) {
		this.resourceLoader = resourceLoader;
		this.inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/tomatoProducts");
		this.tescoDetailsService = searchService;


	}

	public List<ProductParsingProcessObject> getProduktsFromFile() throws IOException {
		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
		List<ProductParsingProcessObject> retValue=new ArrayList<ProductParsingProcessObject>();
		String line=br.readLine();
		while(line!=null) {
			if(!line.startsWith("#")) {

				String[] elements = line.split(";");
				Tesco_Product product = this.tescoDetailsService.getFullDataFromDbOrApi(elements[1]);
				ProductParsingProcessObject parseObj = new ProductParsingProcessObject(product);
				String type = elements[2];
				ProductType foundType = ProductType.parseType(type);
				parseObj.setExpectedType(foundType);
				String[] expected = elements[3].toLowerCase().split(" ");
				parseObj.setExpectedWords(Arrays.asList(expected));
				String[] allExpected = elements[4].toLowerCase().split(" ");

				parseObj.setAllExpectedWords(Arrays.asList(allExpected));
				System.out.println("Parsed: " + product.getName());
				retValue.add(parseObj);
			}
			line=br.readLine();
		}
		return retValue;


	}




}
