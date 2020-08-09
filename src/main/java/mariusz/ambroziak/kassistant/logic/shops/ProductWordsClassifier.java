package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.constants.MetadataConstants;
import mariusz.ambroziak.kassistant.enums.ProductType;
import mariusz.ambroziak.kassistant.hibernate.parsing.model.ProductData;
import mariusz.ambroziak.kassistant.logic.WordClasifier;
import mariusz.ambroziak.kassistant.pojos.parsing.AbstractParsingObject;
import mariusz.ambroziak.kassistant.pojos.shop.ProductParsingProcessObject;
import mariusz.ambroziak.kassistant.webclients.morrisons.Morrisons_Product;
import mariusz.ambroziak.kassistant.webclients.wordsapi.WordNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductWordsClassifier extends WordClasifier {


    @Override
    public void classifySingleWord(AbstractParsingObject parsingAPhrase, int index) throws WordNotFoundException {


        super.classifySingleWord(parsingAPhrase, index);

    }


    @Override
    public void calculateWordTypesForWholePhrase(AbstractParsingObject parsingAPhrase) {


        super.calculateWordTypesForWholePhrase(parsingAPhrase);


//        initialCategorization(parsingAPhrase);
//
//        recategorize(parsingAPhrase);
//        calculateProductType(parsingAPhrase);
//
//        categorizeAllElseAsProducts(parsingAPhrase);

    }



    public void calculateProductType(AbstractParsingObject parsingAPhrase) {
        super.calculateProductType(parsingAPhrase);

        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {
            calculateReasoningsBaseOnPackagingAndPrepInstructions(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }
        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {

            calculateReasoningsBaseOnClassifiedPhrases(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }

    }



    private void calculateReasoningsBaseOnPackagingAndPrepInstructions(AbstractParsingObject parsingAPhrase) {
        ProductParsingProcessObject parsing=(ProductParsingProcessObject)parsingAPhrase;

        ProductData product = parsing.getProduct();

        if(product instanceof Morrisons_Product){
            Morrisons_Product p=(Morrisons_Product)product;
            List<String> packageTypeSlip = Arrays.asList(p.getPackageType().split(MetadataConstants.stringListSeparator));
            List<String> prepAndUsage = Arrays.asList(p.getPrepAndUsage().split(MetadataConstants.stringListSeparator));

            if(packageTypeSlip.stream().anyMatch(s->s.equalsIgnoreCase("can"))){
                parsingAPhrase.getProductTypeReasoning().put("packaged in a can", ProductType.processed);
           //     parsingAPhrase.setFoodTypeClassified(ProductType.processed);
            }
            if(packageTypeSlip.stream().anyMatch(s->s.equalsIgnoreCase("jar"))){
                parsingAPhrase.getProductTypeReasoning().put("packaged in a jar",ProductType.processed);
            //    parsingAPhrase.setFoodTypeClassified(ProductType.processed);
            }

            if(packageTypeSlip.stream().anyMatch(s->s.equalsIgnoreCase("Tray &amp; Heat Sealed"))){
                parsingAPhrase.getProductTypeReasoning().put("packaged in a heat sealed tray",ProductType.fresh);
           //     parsingAPhrase.setFoodTypeClassified(ProductType.fresh);
            }

            if(prepAndUsage.stream().anyMatch(s->s.equalsIgnoreCase("Wash before use")||s.equalsIgnoreCase("Wash before use."))){
                parsingAPhrase.getProductTypeReasoning().put("requires washing",ProductType.fresh);
          //      parsingAPhrase.setFoodTypeClassified(ProductType.fresh);
            }


        }
    }


//    public void calculateProductType(ProductParsingProcessObject parsingAPhrase) {
//        checkDepartmentForKeywords(parsingAPhrase);
//
//
//        checkQuantities(parsingAPhrase);
//
//
//
//    }


}
