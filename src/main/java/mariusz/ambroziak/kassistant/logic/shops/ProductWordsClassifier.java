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


    public static final int processedIngredientsLength = 5;

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
            if(!(parsingAPhrase instanceof ProductParsingProcessObject)) {
                System.err.println("Wrong parsing object supplied to ProductWordsClassifier");
            }else{
                checkDepartmentForKeywords((ProductParsingProcessObject) parsingAPhrase);
                calculateTypeFromReasonings(parsingAPhrase);
            }
        }

        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {

            calculateReasoningBasedOnIngredients(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }

        if(parsingAPhrase.getFoodTypeClassified()==null||parsingAPhrase.getFoodTypeClassified()==ProductType.unknown) {

            calculateReasoningsBaseOnClassifiedPhrases(parsingAPhrase);
            calculateTypeFromReasonings(parsingAPhrase);
        }

    }

    private void calculateReasoningBasedOnIngredients(AbstractParsingObject parsingAPhrase) {
        ProductData product = ((ProductParsingProcessObject) parsingAPhrase).getProduct();

        if(product instanceof Morrisons_Product){
            String ingredients = ((Morrisons_Product) product).getIngredients();

            if(ingredients!=null&&!ingredients.isEmpty()){
                String[] split = ingredients.split(",");
                if(split.length> processedIngredientsLength){
                    parsingAPhrase.getProductTypeReasoning().put("ingredient phrase with more than "+processedIngredientsLength+" ingredients", ProductType.processed);

                }
            }
        }

    }


    private void calculateReasoningsBaseOnPackagingAndPrepInstructions(AbstractParsingObject parsingAPhrase) {
        ProductParsingProcessObject parsing=(ProductParsingProcessObject)parsingAPhrase;

        ProductData product = parsing.getProduct();

        if(product instanceof Morrisons_Product){
            Morrisons_Product p=(Morrisons_Product)product;
            List<String> packageTypeSlip = Arrays.asList(p.getPackageType().split(MetadataConstants.stringListSeparator));
            List<String> prepAndUsage = Arrays.asList(p.getPrepAndUsage().split(MetadataConstants.stringListSeparator));

            for(String keyword:processedPackagingKeywords) {
                if (packageTypeSlip.stream().anyMatch(s -> s.equalsIgnoreCase(keyword))) {
                    parsingAPhrase.getProductTypeReasoning().put("packaged in a "+keyword, ProductType.processed);
                    //     parsingAPhrase.setFoodTypeClassified(ProductType.processed);
                }
            }

            if(packageTypeSlip.stream().anyMatch(s->s.equalsIgnoreCase("aerosol"))){
                parsingAPhrase.getProductTypeReasoning().put("packaged in an aerosol", ProductType.notFood);
                //     parsingAPhrase.setFoodTypeClassified(ProductType.processed);
            }
            if(packageTypeSlip.stream().anyMatch(s->s.equalsIgnoreCase("dispenser"))){
                parsingAPhrase.getProductTypeReasoning().put("packaged in a dispenser",ProductType.dried);
                //    parsingAPhrase.setFoodTypeClassified(ProductType.processed);
            }

            if(packageTypeSlip.stream().anyMatch(s->s.equalsIgnoreCase("Tray &amp; Heat Sealed"))){
                parsingAPhrase.getProductTypeReasoning().put("packaged in a heat sealed tray",ProductType.fresh);
           //     parsingAPhrase.setFoodTypeClassified(ProductType.fresh);
            }

            if(prepAndUsage.stream().anyMatch(s->s.startsWith("wash before use"))){
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



    public void checkDepartmentForKeywords(ProductParsingProcessObject parsingAPhrase) {
        ProductData product = parsingAPhrase.getProduct();
        String departmentList = product.getDepartmentList();
        for (String keyword : freshFoodKeywords) {
            if (departmentList != null && departmentList.toLowerCase().contains(keyword)) {
                parsingAPhrase.getProductTypeReasoning().put("department keyword: " + keyword, ProductType.fresh);

                //  return ProductType.fresh;
            }
        }

        for (String keyword : readyDishKeywords) {
            if (departmentList != null && departmentList.toLowerCase().contains(keyword)) {
                parsingAPhrase.getProductTypeReasoning().put("department keyword: " + keyword, ProductType.meal);

                //  return ProductType.fresh;
            }
        }
        if(!anyFoodDepartmentPrefix.stream().anyMatch(s -> departmentList.toLowerCase().startsWith(s.toLowerCase()))){
            parsingAPhrase.getProductTypeReasoning().put("not food department", ProductType.notFood);

        }


    }



}
