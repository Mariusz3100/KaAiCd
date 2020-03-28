package mariusz.ambroziak.kassistant.webapis.tesco;


import java.util.ArrayList;

import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import mariusz.ambroziak.kassistant.ai.categorisation.MetadataConstants;
import mariusz.ambroziak.kassistant.ai.utils.ProblemLogger;





@Component
public class TescoApiClientService {
	private static final String DETAILS_BASE_URL = "http://localhost:8085/proxy/product?url=https://dev.tescolabs.com/product/?tpnb=";
	private static final String baseUrl= "https://dev.tescolabs.com/grocery/products/";
//	private static final String baseUrl= "http://localhost:8085/proxy/search";

	private static final int  productsReturnedLimit=100;

	private static final String headerName="Ocp-Apim-Subscription-Key";
	private static final String headerValue="bb40509242724f799153796d8718c3f3";

	
	@Autowired
	private ResourceLoader resourceLoader;
	private Resource inputFileResource;

	
	
	
	
	public TescoApiClientService(ResourceLoader resourceLoader) {
		super();
		this.resourceLoader = resourceLoader;
		this.inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/tomatoProducts");;
	}



	private String getResponse(String phrase, int limit) {
		ClientConfig cc = new DefaultClientConfig();
		cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);

		Client c = Client.create();
		WebResource client = c.resource(baseUrl);

		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("query", phrase);
		queryParams.add("offset", "0");
		queryParams.add("limit",Integer.toString(limit));
		WebResource clientWithParams = client.queryParams(queryParams);
		Builder clientWithParamsAndHeader = clientWithParams.header(headerName, headerValue);

		String response1 ="";

		try{
			response1 = clientWithParamsAndHeader.accept("application/json").get(String.class);
			return response1;

		}catch( com.sun.jersey.api.client.UniformInterfaceException e){
			ProblemLogger.logProblem("UniformInterfaceException for term: "+phrase+". Waiting and retrying");
			sleep(2000);
			try{
				response1 = clientWithParamsAndHeader.accept("application/json").get(String.class);
				return response1;

			}catch( com.sun.jersey.api.client.UniformInterfaceException ex){
				System.err.println("Double: "+ex);
				ProblemLogger.logProblem("Double: "+ex);
				ex.printStackTrace();

			}
		}


		return response1;
	}


	public ArrayList<Tesco_Product> getProduktsFor(String phrase){

		return getProduktsFor(phrase,10);
	}

	public ArrayList<Tesco_Product> getProduktsFor(String phrase, int limit){

		if(phrase==null|phrase.equals(""))
			return new ArrayList<Tesco_Product>();

		String response = getResponse(phrase,limit);

		ArrayList<Tesco_Product> list = parseResponse(response);


		return list;
	}


	private static float getPrice(JSONObject ApiProdukt, String url) {
		String minPrice=(String) ApiProdukt.get("minimumPrice");
		String maxPrice=(String) ApiProdukt.get("maximumPrice");

		if(minPrice==null||minPrice.equals("")||maxPrice==null||maxPrice.equals(""))
			ProblemLogger.logProblem("Problem with missing price(s) for produkt: "+url);

		if(!minPrice.equals(maxPrice))
			ProblemLogger.logProblem("Problem with max and min price not matching for produkt: "+url);

		float maxFloat=extractFloatPrice(maxPrice);

		return maxFloat;
	}


	private static float extractFloatPrice(String stringPrice) {
		stringPrice=stringPrice.replace("$", "");
		float floatPrice=Float.parseFloat(stringPrice);
		return floatPrice;
	}


	

	private static ArrayList<Tesco_Product> parseResponse(String response) {
		JSONObject jsonRoot=new JSONObject(response);


		JSONObject ukJson = jsonRoot.getJSONObject("uk");

		JSONObject jsonGhs = ukJson.getJSONObject("ghs");

		JSONObject jsonProducts =jsonGhs.getJSONObject("products");

		JSONArray jsonProductResultsArray=jsonProducts.getJSONArray("results");

		ArrayList<Tesco_Product> resultProductList = calculateProductList(jsonProductResultsArray);
		return resultProductList;
	}


	private static ArrayList<Tesco_Product> calculateProductList(JSONArray jsonProductResultsArray) {
		ArrayList<Tesco_Product> resultList=new ArrayList<Tesco_Product>();


		for(int i=0;i<jsonProductResultsArray.length();i++) {
			JSONObject singleProductJson = jsonProductResultsArray.getJSONObject(i);
			Tesco_Product result = createParticularProduct(singleProductJson);

			resultList.add(result);
		}

		return resultList;
	}


	private static Tesco_Product createParticularProduct(JSONObject singleProductJson) {
		String name =singleProductJson.has("name")?singleProductJson.getString("name"):"";
		String detailsUrl="";
		long tpnb=-1;
		if(singleProductJson.has("tpnb")) {
			tpnb =singleProductJson.getLong("tpnb");
			detailsUrl=DETAILS_BASE_URL+tpnb;
		}
//		String metadata=createMetadata(singleProductJson);
		String description = calculateDescription(singleProductJson);
		float price = singleProductJson.has("price")?(float)singleProductJson.getDouble("price"):0;

		String quantityString = calculateQuantityJspString(singleProductJson, detailsUrl);
		String department =singleProductJson.has("department")?singleProductJson.getString("department"):"";
		String superDepartment =singleProductJson.has("superDepartment")?singleProductJson.getString("superDepartment"):"";

		Tesco_Product result=new Tesco_Product(name,detailsUrl,description,quantityString,department,superDepartment,tpnb);
		return result;
	}


	private static String createMetadata(JSONObject singleProductJson) {
		JSONObject metadata=new JSONObject();
		String category1 = singleProductJson.has("superDepartment")?singleProductJson.getString("superDepartment"):"";
		String category2 = singleProductJson.has("department")?singleProductJson.getString("department"):"";

		metadata.put(MetadataConstants.categoryNameJsonName,category1 +MetadataConstants.stringListSeparator+category2);
	//	metadata.addProperty(MetadataConstants.categoryNameJsonPrefix,category2 );

		
		return metadata.toString();
	}


	private static String calculateDescription(JSONObject singleProductJson) {
		if(singleProductJson.has("description")) {
		
		JSONArray jsonArray = singleProductJson.getJSONArray("description");
		String retValue="";
		for(int i=0;i<jsonArray.length();i++) {
			String line = jsonArray.getString(i);
			retValue+=line+"\n";
		}

		return retValue;
		}else {
			return "";
		}
	}


	private static String calculateQuantityJspString(JSONObject singleProductJson, String detailsUrl) {
		String contentsMeasureType = singleProductJson.getString("ContentsMeasureType");
		int contentsQuantity = singleProductJson.getInt("ContentsQuantity");
	

		return contentsQuantity+" "+contentsMeasureType;
	}

	public Tesco_Product getProduktByShopId(String id){
		return null;
	}

	private static void sleep(long milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





}
