package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ExcelTemplateProcessor {

    private static final String BASE_JSON_TEMPLATE = """
{
  "templatesBody":[
    {
      "label":"Template 1",
      "id":3285532,
      "languages":["French"],
      "fields":[{"type":"editor","name":"New Text Area 1","placeholder":"Template Field","row":10,"showAs":"textArea","onlyThisTemplate":true,"direction":"auto","value":""}],
      "expanded":true
    },
    {
      "languages":["English"],
      "fields":[{"type":"editor","name":"New Text Area 1","placeholder":"Template Field","row":10,"showAs":"textArea","onlyThisTemplate":true,"direction":"auto","value":""}]
    },
    {
      "languages":["Kikongo"],
      "fields":[{"type":"editor","name":"New Text Area 1","placeholder":"Template Field","row":10,"showAs":"textArea","onlyThisTemplate":true,"direction":"auto","value":""}]
    },
    {
      "languages":["Swahili"],
      "fields":[{"type":"editor","name":"New Text Area 1","placeholder":"Template Field","row":10,"showAs":"textArea","onlyThisTemplate":true,"direction":"auto","value":""}]
    },
    {
      "languages":["Lingala"],
      "fields":[{"type":"editor","name":"New Text Area 1","placeholder":"Template Field","row":10,"showAs":"textArea","onlyThisTemplate":true,"direction":"auto","value":""}]
    },
    {
      "languages":["Tshiluba"],
      "fields":[{"type":"editor","name":"New Text Area 1","placeholder":"Template Field","row":10,"showAs":"textArea","onlyThisTemplate":true,"direction":"auto","value":""}]
    }
  ],
  "sendAllLanguage":false,
  "useCase":"Use For All UseCases",
  "offerName":"",
  "tags":[]
}
""";


    public static void main(String[] args) throws Exception {

        String filePath = "C:\\Users\\ashutosh.singh4\\OneDrive - Comviva Technologies Ltd\\Documents\\vdrc\\J4U_USSD.xlsx";

        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        DataFormatter formatter = new DataFormatter();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header

            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell criteriaCell = row.getCell(2); // Column C
            if (criteriaCell == null) continue;

            String criteria = formatter.formatCellValue(criteriaCell);

            // Detect flags
            boolean morning = criteria.contains("MORNING_OFFER_FLAG@ == 1");
            boolean consent = criteria.contains("CONSENT_FLAG@ == 1");
            boolean airtime = criteria.contains("AIRTIME_BALANCE_FLAG@ == 1");
            boolean social = criteria.contains("SOCIAL_OFFER_FLAG@ == 1");
            boolean loyalty = criteria.contains("LOYALTY_MENU_FLAG@ == 1");
            boolean town = criteria.contains("TOWN_OFFER_FLAG@ == 1");
            boolean lastPurchase = criteria.contains("LAST_PURCHASE_OFFER@ == 1");

            MenuResult englishResult = buildEnglishTemplateWithMapping(
                    morning, consent, airtime, social, loyalty, town, lastPurchase
            );

            String secondLanguageTemplate = buildFrenchTemplate(
                    morning, consent, airtime, social, loyalty, town, lastPurchase
            );


            Cell colE = row.createCell(4);  // Column E
            colE.setCellValue(englishResult.mappingText);

            String finalJson = buildFinalJson(
                    englishResult.menuText,
                    secondLanguageTemplate
            );

            Cell colB = row.createCell(1); // Column B
            colB.setCellValue(finalJson);

        }

        fis.close();

        FileOutputStream fos = new FileOutputStream(filePath);
        workbook.write(fos);
        workbook.close();
        fos.close();

        System.out.println("Templates generated successfully.");
    }

    private static String buildFinalJson(String englishMenu,
                                         String frenchMenu) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(BASE_JSON_TEMPLATE);

        ArrayNode templatesBody = (ArrayNode) root.get("templatesBody");

        for (JsonNode template : templatesBody) {

            ArrayNode languages = (ArrayNode) template.get("languages");
            String language = languages.get(0).asText();

            ArrayNode fields = (ArrayNode) template.get("fields");
            ObjectNode field = (ObjectNode) fields.get(0);

            if ("English".equalsIgnoreCase(language)) {
                field.put("value", englishMenu);
            } else {
                // French + all African languages use French template
                field.put("value", frenchMenu);
            }
        }

        return mapper.writeValueAsString(root);
    }


    private static MenuResult buildEnglishTemplateWithMapping(boolean morning,
                                                              boolean consent,
                                                              boolean airtime,
                                                              boolean social,
                                                              boolean loyalty,
                                                              boolean town,
                                                              boolean lastPurchase) {

        List<String> lines = new ArrayList<>();
        List<String> mapping = new ArrayList<>();

        if (!airtime)
            lines.add("Just For You\n");
        else
            lines.add("Just For You\nBalance: @main_balance@ USD");

        // Offer 0 always fixed
        if (lastPurchase) {
            lines.add("0. @OfferName0@");
            mapping.add("LAST_PURCHASE_OFFER:0");
        }

        int index = 1;

        // Voice
        lines.add(index + ". Voice");
        mapping.add("N_VOICE_OFFER:" + index);
        index++;

        // Data
        lines.add(index + ". Data");
        mapping.add("N_DATA_OFFER:" + index);
        index++;

        // Integrated
        lines.add(index + ". Integrated & SMS");
        mapping.add("N_INTEGRATED_OFFER:" + index);
        index++;

        if (town) {
            lines.add(index + ". Just For your Town");
            mapping.add("N_TOWN_OFFER:" + index);
            index++;
        }

        if (morning) {
            lines.add(index + ". Good Morning");
            mapping.add("N_MORNING_OFFER:" + index);
            index++;
        }

        if (loyalty) {
            lines.add(index + ". Vodalar");
            mapping.add("N_VODALAR:" + index);
            index++;
        }

        if (social) {
            lines.add(index + ". Social media Offers");
            mapping.add("N_SOCIAL_OFFER:" + index);
            index++;
        }

        lines.add(index + ". My Rewards");
        mapping.add("N_MY_REWARDS:" + index);
        index++;

        if (consent) {
            lines.add(index + ". Opt-Out");
            mapping.add("N_CONSENT_OPTOUT:" + index);
            index++;
        }

        String pagedMenu = buildPagedMenu(lines, "Next", "Back", lastPurchase);

        String mappingString = String.join(",", mapping);

        return new MenuResult(pagedMenu, mappingString);
    }

    private static String buildFrenchTemplate(boolean morning,
                                              boolean consent,
                                              boolean airtime,
                                              boolean social,
                                              boolean loyalty,
                                              boolean town,
                                              boolean lastPurchase) {

        List<String> lines = new ArrayList<>();

        if(!airtime)
            lines.add("JUSTE POUR TOI\n");
        else
            lines.add("JUSTE POUR TOI\nSolde: @main_balance@ USD");


        // Offer 0 always fixed
        if (lastPurchase)
            lines.add("0. @OfferName0@");

        List<String> options = new ArrayList<>();

        options.add("Appels");
        options.add("Internet");
        options.add("3 en 1 et SMS");

        if (town)
            options.add("Pour Ta Ville");

        if (morning)
            options.add("Bonne journee");

        if (loyalty)
            options.add("Vodalar");

        if (social)
            options.add("Reseaux Sociaux");

        options.add("Pluie de Bonus!");

        if (consent)
            options.add("Se desengager");

        int index = 1;
        for (String opt : options) {
            lines.add(index + ". " + opt);
            index++;
        }

        return buildPagedMenu(lines, "Suivant", "Retour", lastPurchase);
    }


    private static String buildPagedMenu(List<String> lines,
                                         String nextLabel,
                                         String backLabel,
                                         boolean hasOfferZero) {

        int LIMIT = 160;

        String nextBlock = "#. " + nextLabel + "\n##";
        String backBlock = "*. " + backLabel;

        int extraBuffer = hasOfferZero ? 30 : 0;

        StringBuilder firstScreen = new StringBuilder();
        StringBuilder secondScreen = new StringBuilder();
        Boolean flag = false;
        for (String line : lines) {

            int projectedLength =
                    firstScreen.length()
                            + line.length()
                            + 1   // newline
                            + nextBlock.length()
                            + extraBuffer;

            if (projectedLength <= LIMIT && !flag) {
                firstScreen.append(line).append("\n");
            } else {
                flag = true;
                secondScreen.append(line).append("\n");
            }
        }

        if (secondScreen.length() > 0) {
            firstScreen.append(nextBlock);
            secondScreen.append("\n");
            secondScreen.append(backBlock);
            return firstScreen.toString() + "\n" + secondScreen.toString();
        } else {
            return firstScreen.toString();
        }
    }


}
