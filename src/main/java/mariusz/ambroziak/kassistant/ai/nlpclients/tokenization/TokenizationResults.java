package mariusz.ambroziak.kassistant.ai.nlpclients.tokenization;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenizationResults {
	
	private String phrase;
	
	private DependencyTreeNode dependencyTree;
	
	public DependencyTreeNode getDependencyTree() {
		return dependencyTree;
	}

	public void setDependencyTree(DependencyTreeNode dependencyTree) {
		this.dependencyTree = dependencyTree;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	private List<Token> tokens;

	public List<Token> getTokens() {
		return tokens;
	}



	@Override
	public String toString() {
		return "TokenizationResults [phrase=" + phrase + ", tokens=" + tokens + "]";
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	public static TokenizationResults createEmpty() {
		TokenizationResults tr=new TokenizationResults();
		tr.setPhrase("");
		tr.setTokens(new ArrayList<Token>());
		return tr;
	}

	public List<ConnectionEntry> getAllTwoWordDependencies() {
		return this.getDependencyTree().getAllTwoWordDependencies(this.getTokens());
		
	}
	
	
	public Token findToken(List<Token> tokenList, String text) {
		Optional<Token> found = tokenList.stream().filter(t->t.getText().equals(text)).findFirst();
		
		if(found.isPresent())
			return found.get();
		else
			return new Token(text, null, null);
	}
}
