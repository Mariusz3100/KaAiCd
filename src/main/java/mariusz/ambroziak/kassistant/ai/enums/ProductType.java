package mariusz.ambroziak.kassistant.ai.enums;

import java.util.Arrays;
import java.util.Optional;

public enum ProductType{
	fresh("fresh"),
	processed("processed"),
	unknown("unknown");

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	ProductType(String name) {
		this.name = name;
	}

	public static ProductType parseType(String phrase){
		if(phrase==null||phrase.isEmpty())
			return ProductType.unknown;

		Optional<ProductType> first = Arrays.stream(values()).filter(pt -> pt.getName().equals(phrase)).findFirst();
		if(first.isPresent())
			return first.get();
		else
			return ProductType.unknown;
	}
}
