package mariusz.ambroziak.kassistant.logic.shops;

import mariusz.ambroziak.kassistant.pojos.Product;
import mariusz.ambroziak.kassistant.webclients.spacy.tokenization.WordParsed;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParseCompareProductNamesTest {
    String firstPhrase1 ="Tesco Finest Sugardrop Tomatoes 220G";
    String secondPhrase1 ="Tesco Finest Sugardrop Tomatoes 220G";

    String firstPhrase2 ="Tesco Baby Plum Tomatoes 180G";
    String secondPhrase2 ="Tesco Baby Plum Tomatoes 325G";

    String firstPhrase3 ="Tesco Finest Kent Piccolo Cherry Tomatoes 220G";
    String secondPhrase3 ="Tesco Finest Piccolo Cherry Tomatoes 220G";


    String firstPhrase4 ="Tesco Cheshire Vine Ripened Tomatoes 230G";
    String secondPhrase4 ="Tesco Sweet Vine Ripened Tomatoes 230G";




    @Test
    void parseFirstEmpty() {
        Product product = ParseCompareProductNames.parseTwoPhrases("", secondPhrase1);

        for (WordParsed wp: product. getSearchNameResults()){
            assertTrue(wp.isMatch());
        }

        for (WordParsed wp: product. getDetailsNameResults()){
            assertFalse(wp.isMatch());
        }


        for (WordParsed wp: product. getDetailsNameResults()){
            assertEquals("",wp.getWord());
        }



        assertEquals("Tesco",product.getSearchNameResults().get(0).getWord());
        assertEquals("Finest",product.getSearchNameResults().get(1).getWord());
        assertEquals("Sugardrop",product.getSearchNameResults().get(2).getWord());


    }




    @Test
    void parseSecondEmpty() {
        Product product = ParseCompareProductNames.parseTwoPhrases(secondPhrase1,"");

        for (WordParsed wp: product. getSearchNameResults()){
            assertFalse(wp.isMatch());
        }

        for (WordParsed wp: product. getDetailsNameResults()){
            assertTrue(wp.isMatch());
        }


        for (WordParsed wp: product. getSearchNameResults()){
            assertEquals("",wp.getWord());
        }



        assertEquals("Tesco",product.getDetailsNameResults().get(0).getWord());
        assertEquals("Finest",product.getDetailsNameResults().get(1).getWord());
        assertEquals("Sugardrop",product.getDetailsNameResults().get(2).getWord());


    }







    @Test
    void parseTwoPhrases1() {
        Product product = ParseCompareProductNames.parseTwoPhrases(firstPhrase1, secondPhrase1);

        for (WordParsed wp: product.getDetailsNameResults()){
            assertTrue(wp.isMatch());
        }

        for (WordParsed wp: product.getSearchNameResults()){
            assertTrue(wp.isMatch());
        }

        assertEquals(product.getDetailsNameResults().get(0).getWord(),"Tesco");
        assertEquals(product.getDetailsNameResults().get(1).getWord(),"Finest");
        assertEquals(product.getDetailsNameResults().get(2).getWord(),"Sugardrop");
        assertEquals(product.getDetailsNameResults().get(3).getWord(),"Tomatoes");

        assertEquals(product.getSearchNameResults().get(0).getWord(),"Tesco");
        assertEquals(product.getSearchNameResults().get(1).getWord(),"Finest");
        assertEquals(product.getSearchNameResults().get(2).getWord(),"Sugardrop");
        assertEquals(product.getSearchNameResults().get(3).getWord(),"Tomatoes");



    }


    @Test
    void parseTwoPhrases2() {
        Product product = ParseCompareProductNames.parseTwoPhrases(firstPhrase2, secondPhrase2);

        for (int i=0;i<product.getDetailsNameResults().size()-1;i++){
            assertTrue(product.getDetailsNameResults().get(i).isMatch());
        }

        for (int i=0;i<product.getSearchNameResults().size()-1;i++){
            assertTrue(product.getSearchNameResults().get(i).isMatch());
        }

        assertFalse(product.getSearchNameResults().get(product.getSearchNameResults().size()-1).isMatch());

        assertFalse(product.getSearchNameResults().get(product.getSearchNameResults().size()-1).isMatch());

        String result1=product.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
        assertEquals(result1,this.firstPhrase2);

        String result2=product.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
        assertEquals(result2,this.secondPhrase2);
    }




    @Test
    void parseTwoPhrases3() {
        Product product = ParseCompareProductNames.parseTwoPhrases(firstPhrase3, secondPhrase3);

        print(product);
        for (int i=0;i<product.getDetailsNameResults().size()-1;i++){
            if(i==2) {
                assertFalse(product.getDetailsNameResults().get(i).isMatch());

            }else{
                assertTrue(product.getDetailsNameResults().get(i).isMatch());
            }
        }

        for (int i=0;i<product.getSearchNameResults().size()-1;i++){
            if(i==2) {
                assertFalse(product.getSearchNameResults().get(i).isMatch());

            }else{
                assertTrue(product.getSearchNameResults().get(i).isMatch());
            }
        }

        String result1=product.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
        assertEquals(result1,this.firstPhrase3);

        String result2=product.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
        result2=result2.replaceAll("  "," ");
        assertEquals(result2,this.secondPhrase3);

    }


    @Test
    void parseTwoPhrases4() {
        Product product = ParseCompareProductNames.parseTwoPhrases(firstPhrase4, secondPhrase4);

        print(product);
        for (int i=0;i<product.getDetailsNameResults().size()-1;i++){
            if(i==1) {
                assertFalse(product.getDetailsNameResults().get(i).isMatch());

            }else{
                assertTrue(product.getDetailsNameResults().get(i).isMatch());
            }
        }

        for (int i=0;i<product.getSearchNameResults().size()-1;i++){
            if(i==1) {
                assertFalse(product.getSearchNameResults().get(i).isMatch());

            }else{
                assertTrue(product.getSearchNameResults().get(i).isMatch());
            }
        }

        String result1=product.getDetailsNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
        assertEquals(result1,this.firstPhrase4);

        String result2=product.getSearchNameResults().stream().map(wp->wp.getWord()).collect(Collectors.joining(" "));
        result2=result2.replaceAll("  "," ");
        assertEquals(result2,this.secondPhrase4);

    }



    public static void print(Product product){
        for (int i=0;i<product.getDetailsNameResults().size();i++){
            System.out.println(product.getDetailsNameResults().get(i)+" : "+product.getSearchNameResults().get(i));
        }
    }

}