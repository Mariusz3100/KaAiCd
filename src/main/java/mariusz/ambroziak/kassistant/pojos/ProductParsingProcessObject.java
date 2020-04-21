package mariusz.ambroziak.kassistant.pojos;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.model.ProductData;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;

import java.util.List;

public class ProductParsingProcessObject extends AbstractParsingObject {
	private ProductData product;
	private String brandlessPhrase;
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

	public String getBrandlessPhrase() {
		return brandlessPhrase;
	}

	public void setBrandlessPhrase(String brandlessPhrase) {
		this.brandlessPhrase = brandlessPhrase;
	}

	public ProductType getExpectedType() {
		return expectedType;
	}

	public void setExpectedType(ProductType expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public String getOriginalPhrase() {
//		if(this.getProduct() instanceof Tesco_Product){
//			String  ings=((Tesco_Product)this.getProduct()) .getIngredients();
//			if(ings!=null&&!ings.isEmpty()){
//				return ings.toLowerCase();
//			}
//		}

		return this.getProduct().getName();
	}

	public String getEntitylessString(){
		return  getEntitylessString(this.brandlessPhrase);
	}
	
}
