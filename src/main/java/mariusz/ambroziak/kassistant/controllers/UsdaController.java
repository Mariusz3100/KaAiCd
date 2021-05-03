package mariusz.ambroziak.kassistant.controllers;

import mariusz.ambroziak.kassistant.logic.usda.UsdaClasifierJudgeService;
import mariusz.ambroziak.kassistant.logic.usda.UsdaWordsClasifierService;
import mariusz.ambroziak.kassistant.pojos.usda.ParsingFromUsdaResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class UsdaController {
    @Autowired
    UsdaWordsClasifierService usdaWordsClasifierService;

    @Autowired
    UsdaClasifierJudgeService usdaClasifierJudgeService;

    @CrossOrigin
    @ResponseBody
    @RequestMapping("/usdaParsing")
    public String  usdaParsing() throws IOException {
        this.usdaWordsClasifierService.parseUsdaData();

        return "Done";


    }

    @CrossOrigin
    @RequestMapping("/parseUsda")
    @ResponseBody
    public ParsingFromUsdaResult parseUsda() throws IOException{
//		ParsingResultList parseFromFile = this.ingredientPhraseParser.parseFromDbAndSaveAllToDb();
//
//
//		return parseFromFile;
        return this.usdaClasifierJudgeService.getUsdaAndJudgeLegacyDataWithTypes();

    }

}
