package mariusz.ambroziak.kassistant.pojos.shop;

import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.model.ProductData;
import mariusz.ambroziak.kassistant.hibernate.model.ProductLearningCase;
import mariusz.ambroziak.kassistant.pojos.AbstractParsingObject;
import mariusz.ambroziak.kassistant.webclients.tesco.Tesco_Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductParsingProcessObject extends AbstractParsingObject {
	private ProductData product;
	private String brandlessPhrase;

	private ProductLearningCase testCase;

	public List<String> getMinimalExpectedWords() {
		String minimal_words_expected = getTestCase()==null||getTestCase().getMinimal_words_expected()==null?"":getTestCase().getMinimal_words_expected();
		return Arrays.asList(minimal_words_expected.split(" "));

	}


	public List<String> getExtendedExpectedWords() {
		String words_expected = getTestCase()==null||getTestCase().getMinimal_words_expected()==null?"":getTestCase().getExtended_words_expected();
		return Arrays.asList(words_expected.split(" "));	}




	public ProductData getProduct() {
		return product;
	}

	public void setProduct(ProductData product) {
		this.product = product;
	}

	public ProductParsingProcessObject(ProductData product, ProductLearningCase testCase) {
		this.product = product;
		this.testCase = testCase;
	}

	public String getBrandlessPhrase() {
		return brandlessPhrase;
	}

	public void setBrandlessPhrase(String brandlessPhrase) {
		this.brandlessPhrase = brandlessPhrase;
	}

	public ProductType getExpectedType() {
		return getTestCase().getType_expected();
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

	public ProductLearningCase getTestCase() {
		return testCase;
	}

	public void setTestCase(ProductLearningCase testCase) {
		this.testCase = testCase;
	}
}
