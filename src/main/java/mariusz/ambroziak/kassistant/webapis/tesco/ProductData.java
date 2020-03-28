package mariusz.ambroziak.kassistant.webapis.tesco;

import mariusz.ambroziak.kassistant.webapis.edamamnlp.PreciseQuantity;

public abstract class ProductData {
    protected String name;
    protected String detailsUrl;
    protected String description;
    protected String department;
    protected PreciseQuantity quantity;
    protected PreciseQuantity totalQuantity;


    public abstract String getId();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public PreciseQuantity getQuantity() {
        return quantity;
    }

    public void setQuantity(PreciseQuantity quantity) {
        this.quantity = quantity;
    }

    public PreciseQuantity getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(PreciseQuantity totalQuantity) {
        this.totalQuantity = totalQuantity;
    }


}
