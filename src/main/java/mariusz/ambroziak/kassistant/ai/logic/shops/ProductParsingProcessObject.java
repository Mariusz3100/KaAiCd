package mariusz.ambroziak.kassistant.ai.logic.shops;

import java.util.List;

import mariusz.ambroziak.kassistant.ai.enums.ProductType;
import mariusz.ambroziak.kassistant.webapis.tesco.ProductData;
import mariusz.ambroziak.kassistant.webapis.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.ai.utils.AbstractParsingObject;

public class ProductParsingProcessObject extends AbstractParsingObject{
	private ProductData product;
	private List<String> expectedWords;
	private List<String> allExpectedWords;

	private ProductType expectedType;

	public List<String> getAllExpectedWords() {
		return allExpectedWords;
	}

	public void setAllExpectedWords(List<String> allExpectedWords) {
		this.allExpectedWords = allExpectedWords;
	}

	public List<String> getExpectedWords() {
		return expectedWords;
	}

	public void setExpectedWords(List<String> expectedWords) {
		this.expectedWords = expectedWords;
	}



	public ProductData getProduct() {
		return product;
	}

	public void setProduct(ProductData product) {
		this.product = product;
	}

	public ProductParsingProcessObject(Tesco_Product product) {
		super();
		this.product = product;
	}

	public ProductType getExpectedType() {
		return expectedType;
	}

	public void setExpectedType(ProductType expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	protected String getOriginalPhrase() {
//		if(this.getProduct() instanceof Tesco_Product){
//			String  ings=((Tesco_Product)this.getProduct()) .getIngredients();
//			if(ings!=null&&!ings.isEmpty()){
//				return ings.toLowerCase();
//			}
//		}

		return this.getProduct().getName();
	}

	
	
}
