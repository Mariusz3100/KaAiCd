package mariusz.ambroziak.kassistant.ai.edamam.nlp;

import mariusz.ambroziak.kassistant.ai.enums.AmountTypes;

public class PreciseQuantity {
	private float amount;
	private AmountTypes type;
	
	
	@Override
	public String toString() {
		return amount+" "+type;
	}
	public PreciseQuantity(float amount, AmountTypes type) {
		super();
		this.amount = amount;
		this.type = type;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public AmountTypes getType() {
		return type;
	}
	public void setType(AmountTypes type) {
		this.type = type;
	}


	public boolean isEmpty() {
		if(getAmount()==-1&&getType()==AmountTypes.pcs)
			return true;
		else 
			return false;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PreciseQuantity){
			PreciseQuantity x=((PreciseQuantity)obj);

			if(x.getAmount()==this.getAmount()&&x.getType().equals(this.getType())){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}


}


