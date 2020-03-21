package mariusz.ambroziak.kassistant.ai.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.ingredients.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;

@RestController
public class TestNewTokenizerController {
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private TokenizationClientService tokenizator;


	@CrossOrigin
	@RequestMapping("/testTokenizer")
	@ResponseBody
	public List<String> phrasesParsing() throws IOException{
		Resource inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
		List<String> retValue=new ArrayList<String>();

		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();
		Map<String,String> differences=new HashMap<String,String>();

		while(line!=null) {

			retValue.add(line);

			TokenizationResults parse = this.tokenizator.parse(line);

			String result=line+": ";
			String [] bySpaceArr=line.split(" ");
			List<String> bySpace=Arrays.asList(bySpaceArr);

			List<Token> byTokenizer = parse.getTokens();

			if(bySpace.size()==(byTokenizer.size())) {
				for(int i=0;i<byTokenizer.size();i++) {
					if(!bySpace.get(i).equals(byTokenizer.get(i).getText())) {
						differences.put(line, bySpace.get(i)+" "+(byTokenizer.get(i).getText()));
					}

				}


			}else {
				differences.put(line, "diffrent size");

			}
			for(Token x:byTokenizer) {
				result+=x.getText()+" | ";

			}

			System.out.println(result);

			line=br.readLine();
		}
		System.out.println("\n\n");

		for(String x:differences.keySet()) {
			System.out.println(x+" : "+differences.get(x));
		}
		return retValue;
	}

	@RequestMapping("/testIfSameLemmas")
	@ResponseBody
	public List<String> testIfSameLemmas() throws IOException{
		Resource inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
		List<String> retValue=new ArrayList<String>();

		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();
		Map<String,String> differences=new HashMap<String,String>();

		while(line!=null) {

			retValue.add(line);

			TokenizationResults parse = this.tokenizator.parse(line);

			String result=line+": ";
			String [] bySpaceArr=line.split(" ");
			List<String> bySpace=Arrays.asList(bySpaceArr);

			List<Token> byTokenizer = parse.getTokens();

			for(int i=0;i<byTokenizer.size();i++) {
				String word = i>=bySpace.size()?
						"-":bySpace.get(i);
				TokenizationResults parse2 = this.tokenizator.parse(word);
				System.out.println(word+" -> "+parse2.getTokens().get(0).getLemma()+" ["+byTokenizer.get(i).getText()+" -> "+byTokenizer.get(i).getLemma()+"]");

				if((byTokenizer.get(i).getText().indexOf(word)>0||word.indexOf(byTokenizer.get(i).getText())>0)&&!parse2.getTokens().get(0).getLemma().equals(byTokenizer.get(i).getLemma())) {

					System.out.println("err "+word+" -> "+parse2.getTokens().get(0).getLemma()+" ["+byTokenizer.get(i).getText()+" -> "+byTokenizer.get(i).getLemma()+"]");
				}					

			}
			System.out.println("\n");



			line=br.readLine();
		}
		return retValue;
	}
	
	@RequestMapping("/testIfSamePos")
	@ResponseBody
	public List<String> testIfSamePos() throws IOException{
		Resource inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
		List<String> retValue=new ArrayList<String>();

		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();

		while(line!=null) {

			retValue.add(line);

			TokenizationResults parse = this.tokenizator.parse(line);

			String result=line+": ";
			String [] bySpaceArr=line.split(" ");
			List<String> bySpace=Arrays.asList(bySpaceArr);

			List<Token> byTokenizer = parse.getTokens();

			for(int i=0;i<byTokenizer.size();i++) {
				String word = i>=bySpace.size()?
						"-":bySpace.get(i);
				TokenizationResults parse2 = this.tokenizator.parse(word);
				System.out.println(word+" -> "+parse2.getTokens().get(0).getTag()+" ["+byTokenizer.get(i).getText()+" -> "+byTokenizer.get(i).getTag()+"]");

				if((byTokenizer.get(i).getText().indexOf(word)>=0||word.indexOf(byTokenizer.get(i).getText())>=0)&&!parse2.getTokens().get(0).getTag().equals(byTokenizer.get(i).getTag())) {

					System.out.println("err "+word+" -> "+parse2.getTokens().get(0).getTag()+" ["+byTokenizer.get(i).getText()+" -> "+byTokenizer.get(i).getTag()+"]");
				}					

			}
			System.out.println("\n");



			line=br.readLine();
		}
		return retValue;
	}

		@RequestMapping("/testNewTokenizer")
		@ResponseBody
		public List<String> testNewTokenizer() throws IOException{
			Resource inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
			List<String> retValue=new ArrayList<String>();

			InputStream inputStream = inputFileResource.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


			String line=br.readLine();

			while(line!=null) {

				retValue.add(line);

				List<String> parse = this.tokenizator.justTokenize(line);


				for(int i=0;i<parse.size();i++) {
					String x=parse.get(i);
					
					TokenizationResults parsed = this.tokenizator.parse(x);

					for(Token t:parsed.getTokens()) {
						if(!t.getText().equals(t.getLemma()))
							System.out.println(t.getText()+" -> "+t.getLemma()+" : "+t.getTag());
						
					}
					
				}
				System.out.println("\n");



				line=br.readLine();
			}



		return retValue;

	}
		
		
		
		@RequestMapping("/testTokenizerAdjusting")
		@ResponseBody
		public List<String> testTokenizerAdjusting() throws IOException{
			Resource inputFileResource = this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
			List<String> retValue=new ArrayList<String>();

			InputStream inputStream = inputFileResource.getInputStream();
			BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


			String line=br.readLine();

			while(line!=null) {

				retValue.add(line);

				TokenizationResults parse = this.tokenizator.parse(line);
				String effect=line+": ";

				for(int i=0;i<parse.getTokens().size();i++) {
					Token x=parse.getTokens().get(i);
					effect+=x.getText()+" ("+x.getTag()+") | ";

										
				}

				System.out.println(effect);


				line=br.readLine();
			}



		return retValue;

	}
}
