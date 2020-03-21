package mariusz.ambroziak.kassistant.ai.enums;

import java.util.ArrayList;


public enum AmountTypes {
	ml("ml","ml"),
	mg("mg","mg"),
	pcs("szt","pc."),
	kalorie("kcal","calories");
	
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
	
	private AmountTypes(String type, String engName) {
		this.type = type;
		this.engName = engName;
	}

	public  static AmountTypes retrieveTypeByName(String name){
		for(AmountTypes at:AmountTypes.values()){
			if(at.getType().equals(name)){
				return at;
			}
		}
		System.err.println("unparsable amount type: "+name);
		return AmountTypes.pcs;
		
	}
	
	public  static AmountTypes[] valuesWithoutCalories(){
		ArrayList<AmountTypes> retValue=new ArrayList<AmountTypes>();
		
		for(AmountTypes at:values()){
			if(!kalorie.equals(at))
				retValue.add(at);
		}
		
		return retValue.toArray(new AmountTypes[]{});
	}
	
	public String toString() {
		return engName;
	}
	
	String type;
	String engName;
	
}
