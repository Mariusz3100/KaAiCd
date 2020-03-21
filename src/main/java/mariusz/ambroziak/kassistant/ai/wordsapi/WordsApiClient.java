package mariusz.ambroziak.kassistant.ai.wordsapi;

import java.net.URLEncoder;
import java.util.ArrayList;

import javax.ws.rs.core.MultivaluedMap;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import mariusz.ambroziak.kassistant.ai.rapidapi.RapidApiClient;


@Component
public class WordsApiClient extends RapidApiClient {

	private static final String baseUrl= "https://wordsapiv1.p.mashape.com/words/";
	private static final String typeOfSuffix="/typeOf";
	
	private static final String header1Value="wordsapiv1.p.rapidapi.com";
	//https://dev.tescolabs.com/grocry/products/?query=cucumber&offset=0&limit=10
	private String getResponse(String url) throws WordNotFoundException {
		ClientConfig cc = new DefaultClientConfig();
		cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
		Client c = Client.create();
		WebResource client = c.resource(url);
		Builder clientWithParamsAndHeader=client.header(header1Name, header1Value).header(header2Name, header2Value);

		String response1 ="";

		try{
			response1 = clientWithParamsAndHeader.accept("application/json").get(String.class);
			return response1;

		}catch( com.sun.jersey.api.client.UniformInterfaceException e){
			if(e.getMessage().contains("404")) {
				throw new WordNotFoundException(url);
			}else {
				System.err.println("UniformInterfaceException for words api, url: "+url+". Waiting and retrying");
			}

		}


		return response1;
	}

	
	
	public ArrayList<WordsApiResult> searchFor(String phrase)  {
		String url = baseUrl+UriUtils.encodePath(phrase, java.nio.charset.StandardCharsets.UTF_8.toString());
		
		
		//System.out.println("Retrieved: "+url);
		if(phrase==null||phrase.equals(""))
			return new ArrayList<WordsApiResult>();
		try {
		String response=getResponse(url);

		
		ArrayList<WordsApiResult> retValue=new ArrayList<WordsApiResult>();
		JSONObject jsonRoot=new JSONObject(response);
		try {
			String baseWord=jsonRoot.getString("word");
			if(jsonRoot.has("results")) {
				JSONArray resultsArray = jsonRoot.getJSONArray("results");

				for(int i=0;i<resultsArray.length();i++) {
					JSONObject jsonSingleResult = resultsArray.getJSONObject(i);
					WordsApiResult parsingResult = parseSingleJsonIntoWordObj(phrase, baseWord, jsonSingleResult);
					retValue.add(parsingResult);


				}
			}
		}catch(JSONException e) {
			System.err.println("Problem parsing words api response for "+phrase);
		}
		return retValue;
		}catch(WordNotFoundException e) {
			return new ArrayList<WordsApiResult>();
		}

	}

	public ArrayList<String> getTypesOf(String phrase) throws WordNotFoundException {
		String phraseEncoded;
		phraseEncoded = UriUtils.encodePath(phrase, java.nio.charset.StandardCharsets.UTF_8.toString());
		
		String url=baseUrl+phraseEncoded+typeOfSuffix;
		if(phrase==null||phrase.equals(""))
			return new ArrayList<String>();

		String response=getResponse(url);

		
		ArrayList<String> retValue=new ArrayList<String>();
		JSONObject jsonRoot=new JSONObject(response);
		try {
			if(jsonRoot.has("typeOf")) {
				JSONArray resultsArray = jsonRoot.getJSONArray("typeOf");

				retValue= parseJsonArrayOfStrings(resultsArray);
				
			}
		}catch(JSONException e) {
			System.err.println("Problem parsing words api response for "+phrase);
		}
		
		
		System.out.println(phrase+"->"+retValue);
		return retValue;

	}

	private static WordsApiResult parseSingleJsonIntoWordObj(String phrase, String baseWord,
			JSONObject jsonSingleResult) {
		String partOfSpeech="";
		ArrayList<String> typeOf=new ArrayList<String>();
		ArrayList<String> synonyms=new ArrayList<String>();
		ArrayList<String> hasTypes=new ArrayList<String>();
		ArrayList<String> attribute=new ArrayList<String>();

		
		
		if(jsonSingleResult.has("partOfSpeech")&&!jsonSingleResult.get("partOfSpeech").equals(JSONObject.NULL)) {
			partOfSpeech=jsonSingleResult.getString("partOfSpeech");
		}
		ArrayList<String> childTypes=new ArrayList<String>();

		
		if(jsonSingleResult.has("typeOf")&&!jsonSingleResult.get("typeOf").equals(JSONObject.NULL)) {
			JSONArray jsonArray = jsonSingleResult.getJSONArray("typeOf");
			
			typeOf=parseJsonArrayOfStrings(jsonArray);
		}

		if(jsonSingleResult.has("attribute")&&!jsonSingleResult.get("attribute").equals(JSONObject.NULL)) {
			JSONArray jsonArray = jsonSingleResult.getJSONArray("attribute");
			
			attribute=parseJsonArrayOfStrings(jsonArray);
		}
		
		if(jsonSingleResult.has("synonyms")&&!jsonSingleResult.get("synonyms").equals(JSONObject.NULL)) {
			JSONArray jsonArray = jsonSingleResult.getJSONArray("synonyms");
			
			synonyms=parseJsonArrayOfStrings(jsonArray);
		}
		
		
		
		if(jsonSingleResult.has("hasTypes")) {
			try {
				JSONArray jsonChildTypes=jsonSingleResult.getJSONArray("hasTypes");
				for(int j=0;j<jsonChildTypes.length();j++) {
					childTypes.add(jsonChildTypes.getString(j));
				}
			}catch(ClassCastException e) {
				System.err.println("'hasTypes' is of different type than list for "+phrase);
			}
		}
		String definition=jsonSingleResult.getString("definition");

		WordsApiResult parsingResult=new WordsApiResult();
		parsingResult.setBaseWord(baseWord);
		parsingResult.setChildTypes(childTypes);
		parsingResult.setDefinition(definition);
		parsingResult.setOriginalWord(phrase);
		parsingResult.setPartOfSpeech(partOfSpeech);
		parsingResult.setSynonyms(synonyms);
		parsingResult.setAttribute(attribute);
		parsingResult.setTypeOf(typeOf);
		return parsingResult;
	}



	private static ArrayList<String> parseJsonArrayOfStrings(JSONArray jsonArray) {
		ArrayList<String> typeOf=new ArrayList<String>();

		for(int i=0;i<jsonArray.length();i++) {
			String type=jsonArray.getString(i);
			typeOf.add(type);
		}
		return typeOf;
	}







//	public static void main(String[] arg) {
//		String x;
//		try {
//			x = getResponse(baseUrl+"tomato");
//			System.out.println(x);
//
//		} catch (WordNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
