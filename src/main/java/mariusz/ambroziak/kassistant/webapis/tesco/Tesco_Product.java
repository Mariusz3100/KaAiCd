package mariusz.ambroziak.kassistant.webapis.tesco;

public class Tesco_Product extends ProductData{


	private String quantityString;
	private String superdepartment;
	private String ingredients;
	private long tbnp;


	public String getIngredients() {
		return ingredients;
	}

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	public String getQuantityString() {
		return quantityString;
	}
	public void setQuantityString(String quantityString) {
		this.quantityString = quantityString;
	}

	public String getSuperdepartment() {
		return superdepartment;
	}
	public void setSuperdepartment(String superdepartment) {
		this.superdepartment = superdepartment;
	}

	public Tesco_Product(String name, String detailsUrl, String description, String quantityString, String department,
			String superdepartment, long tbnp) {
		super();
		this.name = name;
		this.detailsUrl = detailsUrl;
		this.description = description;
		this.quantityString = quantityString;
		this.department = department;
		this.superdepartment = superdepartment;
		this.tbnp=tbnp;
	}


	@Override
	public String getId() {
		return Long.toString(tbnp);
	}


}
