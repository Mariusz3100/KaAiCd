package mariusz.ambroziak.kassistant.ai.nlpclients.tokenization;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mariusz.ambroziak.kassistant.ai.categorisation.NlpConstants;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;

public class DependencyTreeNode {

		private String text;
		private String relationToParent;
		private String pos;
		private List<DependencyTreeNode> children;
		
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getRelationToParent() {
			return relationToParent;
		}
		public void setRelationToParent(String relationToParent) {
			this.relationToParent = relationToParent;
		}
		public String getPos() {
			return pos;
		}
		public void setPos(String pos) {
			this.pos = pos;
		}
		public List<DependencyTreeNode> getChildren() {
			return children;
		}
		public void setChildren(List<DependencyTreeNode> children) {
			this.children = children;
		}
		
		
		
		public List<ConnectionEntry> getAllTwoWordDependencies(List<Token> tokenList){
			if(children==null||children.isEmpty()) {
				return new ArrayList<ConnectionEntry>();
			}else {
				List<ConnectionEntry> retValue=new ArrayList<ConnectionEntry>();
				
				for(DependencyTreeNode child:children) {
					retValue.addAll(child.getAllTwoWordDependencies(tokenList));
					
					Token childNode=findToken(tokenList, child);
					Token thisNode=findToken(tokenList, this);
					retValue.add(new ConnectionEntry(thisNode,childNode));
					if(NlpConstants.of_Word.equals(child.getText())&&!child.getChildren().isEmpty()&&child.getChildren().size()>0) {
						for(DependencyTreeNode grandChild:child.getChildren()) {
							Token grandChildNode=findToken(tokenList, grandChild);
							retValue.add(new ConnectionEntry(thisNode, grandChildNode));
						}
					}
				}
				return retValue;

			}
			
			
		}
		private Token findToken(List<Token> tokenList, DependencyTreeNode child) {
			Optional<Token> found = tokenList.stream().filter(t->t.getText().equals(child.getText())).findFirst();
			
			if(found.isPresent())
				return found.get();
			else
				return new Token(child.text, null, null);
		}
		
}
