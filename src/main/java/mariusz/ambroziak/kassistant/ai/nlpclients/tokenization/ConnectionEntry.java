package mariusz.ambroziak.kassistant.ai.nlpclients.tokenization;

public class ConnectionEntry {

	Token head;
	Token child;
	
	
	public ConnectionEntry(Token head, Token child) {
		super();
		this.head = head;
		this.child = child;
	}
	public Token getHead() {
		return head;
	}
	public void setHead(Token head) {
		this.head = head;
	}
	public Token getChild() {
		return child;
	}
	public void setChild(Token child) {
		this.child = child;
	}
	@Override
	public String toString() {
		return child.getText() + "("+ head.getText()+")";
	}
	
	
	
}
