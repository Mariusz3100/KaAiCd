package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.pojos.shop.ProductNamesComparison;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordComparisonResult;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseCompareProductNamesComparisonNamesTest {
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

//    @Test
//    void parseFirstEmpty() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases("", secondPhrase1);
//
//        for (WordComparisonResult wp: productNamesComparison. getSearchNameResults()){
//            assertTrue(wp.isMatch());
//        }
//
//        for (WordComparisonResult wp: productNamesComparison. getDetailsNameResults()){
//            assertFalse(wp.isMatch());
//        }
//
//
//        for (WordComparisonResult wp: productNamesComparison. getDetailsNameResults()){
//            assertEquals("",wp.getWord());
//        }
//
//
//
//        assertEquals("Tesco", productNamesComparison.getSearchNameResults().get(0).getWord());
//        assertEquals("Finest", productNamesComparison.getSearchNameResults().get(1).getWord());
//        assertEquals("Sugardrop", productNamesComparison.getSearchNameResults().get(2).getWord());
//
//
//    }
//
//
//
//
//    @Test
//    void parseSecondEmpty() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases(secondPhrase1,"");
//
//        for (WordComparisonResult wp: productNamesComparison. getSearchNameResults()){
//            assertFalse(wp.isMatch());
//        }
//
//        for (WordComparisonResult wp: productNamesComparison. getDetailsNameResults()){
//            assertTrue(wp.isMatch());
//        }
//
//
//        for (WordComparisonResult wp: productNamesComparison. getSearchNameResults()){
//            assertEquals("",wp.getWord());
//        }
//
//
//
//        assertEquals("Tesco", productNamesComparison.getDetailsNameResults().get(0).getWord());
//        assertEquals("Finest", productNamesComparison.getDetailsNameResults().get(1).getWord());
//        assertEquals("Sugardrop", productNamesComparison.getDetailsNameResults().get(2).getWord());
//
//
//    }
//
//
//
//
//
//
//
//    @Test
//    void parseTwoPhrases1() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases(firstPhrase1, secondPhrase1);
//
//        for (WordComparisonResult wp: productNamesComparison.getDetailsNameResults()){
//            assertTrue(wp.isMatch());
//        }
//
//        for (WordComparisonResult wp: productNamesComparison.getSearchNameResults()){
//            assertTrue(wp.isMatch());
//        }
//
//        assertEquals(productNamesComparison.getDetailsNameResults().get(0).getWord(),"Tesco");
//        assertEquals(productNamesComparison.getDetailsNameResults().get(1).getWord(),"Finest");
//        assertEquals(productNamesComparison.getDetailsNameResults().get(2).getWord(),"Sugardrop");
//        assertEquals(productNamesComparison.getDetailsNameResults().get(3).getWord(),"Tomatoes");
//
//        assertEquals(productNamesComparison.getSearchNameResults().get(0).getWord(),"Tesco");
//        assertEquals(productNamesComparison.getSearchNameResults().get(1).getWord(),"Finest");
//        assertEquals(productNamesComparison.getSearchNameResults().get(2).getWord(),"Sugardrop");
//        assertEquals(productNamesComparison.getSearchNameResults().get(3).getWord(),"Tomatoes");
//
//
//
//    }
//
//
//    @Test
//    void parseTwoPhrases2() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases(firstPhrase2, secondPhrase2);
//
//        for (int i = 0; i< productNamesComparison.getDetailsNameResults().size()-1; i++){
//            assertTrue(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//        }
//
//        for (int i = 0; i< productNamesComparison.getSearchNameResults().size()-1; i++){
//            assertTrue(productNamesComparison.getSearchNameResults().get(i).isMatch());
//        }
//
//        assertFalse(productNamesComparison.getSearchNameResults().get(productNamesComparison.getSearchNameResults().size()-1).isMatch());
//
//        assertFalse(productNamesComparison.getSearchNameResults().get(productNamesComparison.getSearchNameResults().size()-1).isMatch());
//
//        String result1= productNamesComparison.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result1=result1.replaceAll("  "," ").trim();
//
//        assertEquals(result1,this.firstPhrase2);
//
//        String result2= productNamesComparison.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result2=result2.replaceAll("  "," ").trim();
//
//        assertEquals(result2,this.secondPhrase2);
//    }
//
//
//
//
//    @Test
//    void parseTwoPhrases3() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases(firstPhrase3, secondPhrase3);
//
//        print(productNamesComparison);
//        for (int i = 0; i< productNamesComparison.getDetailsNameResults().size()-1; i++){
//            if(i==2) {
//                assertFalse(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//
//            }else{
//                assertTrue(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//            }
//        }
//
//        for (int i = 0; i< productNamesComparison.getSearchNameResults().size()-1; i++){
//            if(i==2) {
//                assertFalse(productNamesComparison.getSearchNameResults().get(i).isMatch());
//
//            }else{
//                assertTrue(productNamesComparison.getSearchNameResults().get(i).isMatch());
//            }
//        }
//
//        String result1= productNamesComparison.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result1=result1.replaceAll("  "," ").trim();
//        assertEquals(result1,this.firstPhrase3);
//
//        String result2= productNamesComparison.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result2=result2.replaceAll("  "," ").trim();
//        assertEquals(result2,this.secondPhrase3);
//
//    }
//
//
//    @Test
//    void parseTwoPhrases4() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases(firstPhrase4, secondPhrase4);
//
//        print(productNamesComparison);
//        for (int i = 0; i< productNamesComparison.getDetailsNameResults().size()-1; i++){
//            if(i==1) {
//                assertFalse(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//
//            }else{
//                assertTrue(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//            }
//        }
//
//        for (int i = 0; i< productNamesComparison.getSearchNameResults().size()-1; i++){
//            if(i==1) {
//                assertFalse(productNamesComparison.getSearchNameResults().get(i).isMatch());
//
//            }else{
//                assertTrue(productNamesComparison.getSearchNameResults().get(i).isMatch());
//            }
//        }
//
//        String result1= productNamesComparison.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result1=result1.replaceAll("  "," ").trim();
//        assertEquals(result1,this.firstPhrase4);
//
//        String result2= productNamesComparison.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result2=result2.replaceAll("  "," ").trim();
//        assertEquals(result2,this.secondPhrase4);
//
//    }
//
//    @Test
//    void parseTwoPhrases5() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases(firstPhrase5, secondPhrase5);
//
//        print(productNamesComparison);
//        for (int i = 0; i< productNamesComparison.getDetailsNameResults().size()-1; i++){
//            if(i==2||i==3) {
//                assertTrue(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//            }else{
//                assertFalse(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//
//            }
//        }
//        for (int i = 0; i< productNamesComparison.getSearchNameResults().size()-1; i++){
//            if(i==2||i==3) {
//                assertTrue(productNamesComparison.getSearchNameResults().get(i).isMatch());
//            }else{
//                assertFalse(productNamesComparison.getSearchNameResults().get(i).isMatch());
//
//            }
//        }
//
//
//        String result1= productNamesComparison.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result1=result1.replaceAll("  "," ").trim();
//
//        assertEquals(result1,this.firstPhrase5);
//
//        String result2= productNamesComparison.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result2=result2.replaceAll("  "," ").trim();
//        assertEquals(result2,this.secondPhrase5);
//
//    }
//
//
//    @Test
//    void parseTwoPhrases6() {
//        ProductNamesComparison productNamesComparison = ParseCompareProductNames.parseTwoPhrases(firstPhrase6, secondPhrase6);
//
//        print(productNamesComparison);
//        for (int i = 0; i< productNamesComparison.getDetailsNameResults().size()-1; i++){
//            if(i==2||i==3) {
//                assertTrue(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//            }else{
//                assertFalse(productNamesComparison.getDetailsNameResults().get(i).isMatch());
//
//            }
//        }
//        for (int i = 0; i< productNamesComparison.getSearchNameResults().size()-1; i++){
//            if(i==2||i==3) {
//                assertTrue(productNamesComparison.getSearchNameResults().get(i).isMatch());
//            }else{
//                assertFalse(productNamesComparison.getSearchNameResults().get(i).isMatch());
//
//            }
//        }
//
//
//        String result1= productNamesComparison.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result1=result1.replaceAll("  "," ").trim();
//
//        assertEquals(result1,this.firstPhrase6);
//
//        String result2= productNamesComparison.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
//        result2=result2.replaceAll("  "," ").trim();
//        assertEquals(result2,this.secondPhrase6);
//
//    }


    public static void print(ProductNamesComparison productNamesComparison){
        for (int i = 0; i< productNamesComparison.getDetailsNameResults().size(); i++){
            System.out.println(productNamesComparison.getDetailsNameResults().get(i)+" : "+ productNamesComparison.getSearchNameResults().get(i));
        }
    }

}