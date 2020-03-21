package mariusz.ambroziak.kassistant.ai.logic;

import org.springframework.beans.Mergeable;

import mariusz.ambroziak.kassistant.ai.enums.MergeType;
import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;

public class QualifiedToken extends Token {
	private WordType wordType;
	private MergeType mergeType;


	public MergeType getMergeType() {
		return mergeType;
	}


	public void setMergeType(MergeType mergeType) {
		this.mergeType = mergeType;
	}


	public QualifiedToken(String text, String lemma, String tag, WordType wordType) {
		super(text, lemma, tag);
		this.wordType = wordType;

	}


	public QualifiedToken(Token originalToken,WordType wordType,String relationToHead,
			String relationType) {
		super(originalToken.getText(),originalToken.getLemma(), originalToken.getTag());
		setWordType(wordType);

	}
	
	public QualifiedToken(Token originalToken,WordType wordType) {
		super(originalToken.getText(),originalToken.getLemma(), originalToken.getTag());
		setWordType(wordType);
		setHead(originalToken.getHead());
		setRelationToParentType(originalToken.getRelationToParentType());
		setPos(originalToken.getPos());

	}


	public QualifiedToken(String text, String lemma, String tag, WordType wordType, WordType productelement, String relationToHead,
			String relationType) {
		super(text, lemma, tag);

	}


	private QualifiedToken(String text, String lemma, String tag, WordType wordType,
			String relationToParentType, String pos,String parent) {
		super(text, lemma, tag);
		setWordType(wordType);
		setHead(parent);
		setRelationToParentType(relationToParentType);
	}



	public WordType getWordType() {
		return wordType;
	}

	public void setWordType(WordType wordType) {
		this.wordType = wordType;
	}




	@Override
	public String toString() {
		return "Token [text=" + getText() + ", lemma=" + getLemma() + ", tag=" + getTag() + ", relationToParentType="
				+ getRelationToParentType() + ", pos=" + getPos() + ", head=" + getHead() + ", wordType=" + this.wordType + "]";
	}


	
	public static QualifiedToken createMerged(String fused,WordType type) {
		return new QualifiedToken(fused, "fused", "fused", type,"fused","fused","fused");
	}
	
	public static QualifiedToken createNullObject() {
		return new QualifiedToken("null", "null", "null", WordType.Unknown,"null","null","null");
	}
	
	public boolean isNullObject() {
		return this.getText().equals("null")&&this.getHead().equals("null");
	}
	
}
