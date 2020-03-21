package mariusz.ambroziak.kassistant.ai.nlpclients.ner;

public class NamedEntity{
	private String text;
	private String label;
	private int start;
	private int end;

	public String getText() {
		return text;
	}
	@Override
	public String toString() {
		return "NamedEntity [text=" + text + ", label=" + label + ", start=" + start + ", end=" + end + "]";
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
}