package mariusz.ambroziak.kassistant.ai.utils;

import mariusz.ambroziak.kassistant.ai.enums.AmountTypes;

public class QuantityTranslation{
	private AmountTypes targetAmountType=null;
	private float multiplier=0;
	
	
	public QuantityTranslation(AmountTypes targetAmountType, float multiplier) {
		super();
		this.targetAmountType = targetAmountType;
		this.multiplier = multiplier;
	}
	public AmountTypes getTargetAmountType() {
		return targetAmountType;
	}
	public void setTargetAmountType(AmountTypes targetAmountType) {
		this.targetAmountType = targetAmountType;
	}
	public float getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(float multiplier) {
		this.multiplier = multiplier;
	}
	
	
//	public PreciseQuantity getQuantity() {
//		return getQuantity(1);
//	}		
//	
//	public PreciseQuantity getQuantity(float multiplier) {
//		if(getMultiplier()>0.0&&getTargetAmountType()!=null)
//			return new PreciseQuantity(getMultiplier()*multiplier, getTargetAmountType());
//		else
//			return null;
//	}
}