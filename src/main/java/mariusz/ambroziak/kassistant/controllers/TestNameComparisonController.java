package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.logic.shops.ParseCompareProductNames;
import mariusz.ambroziak.kassistant.logic.shops.ProductNameComparatorSevice;
import mariusz.ambroziak.kassistant.pojos.shop.ProductNamesComparison;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class TestNameComparisonController {

	@Autowired
	ProductNameComparatorSevice comparator;





	@CrossOrigin
	@RequestMapping("/testProductNamesComparator")
	@ResponseBody
	public String testPhraseDependenciesComparator() throws IOException{
		String firstPhrase1 ="Tesco Finest Sugardrop Tomatoes 220G";
		String secondPhrase1 ="Tesco Finest Sugardrop Tomatoes 220G";

		String firstPhrase2 ="Tesco Baby Plum Tomatoes 180G";
		String secondPhrase2 ="Tesco Baby Plum Tomatoes 325G";

		String firstPhrase3 ="Tesco Finest Kent Piccolo Cherry Tomatoes 220G";
		String secondPhrase3 ="Tesco Finest Piccolo Cherry Tomatoes 220G";


		String firstPhrase4 ="Tesco Cheshire Vine Ripened Tomatoes 230G";
		String secondPhrase4 ="Tesco Sweet Vine Ripened Tomatoes 230G";

		String firstPhrase5 ="Tomato Puree Tube";
		String secondPhrase5 ="Double Concentrate Tomato Puree";

		String firstPhrase6 ="Loose Brown Onions";
		String secondPhrase6 ="Tesco Brown Onions Loose";

		ProductNamesComparison comparison1 = this.comparator.parseTwoPhrases(firstPhrase1, firstPhrase2);


		String retValue=firstPhrase1+" : "+secondPhrase1+" : "+comparison1.getResultPhrase()+"<BR>";
		retValue+=firstPhrase2+" : "+secondPhrase2+" : "+this.comparator.parseTwoPhrases(firstPhrase1, firstPhrase2).getResultPhrase();

		return retValue;


	}



}
