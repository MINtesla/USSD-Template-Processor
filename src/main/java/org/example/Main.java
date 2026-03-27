package org.example;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        String todayDt =
                LocalDate.now().minusDays(9999).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        System.out.println(todayDt);
    }


    public static String evaluateJson() throws JSONException, Exception{

//		logger.debug("MoveCustomer :: waitExpression => " + waitExpression);
        String waitExpression = "[#cv('5000','/','1000','-','3948','/','1000')]";
        ExpressionParser parser = new SpelExpressionParser();
        List<String> expressionList = new ArrayList<String>();
        Pattern regex = Pattern.compile("\\[(.*?)\\]");
        Matcher regexMatcher = regex.matcher(waitExpression);
        int cvFlag = 0;
        int coFlag = 0;
        while (regexMatcher.find()) {//Finds Matching Pattern in String
            expressionList.add(regexMatcher.group(1));//Fetching Group from String
        }
        for(String datesExpression:expressionList) { //fetching the values for the given group []
            int count = 0;
            if(datesExpression.contains("#cd")) {
//                boolean actualResult = getExpressionValues(datesExpression);
//                waitExpression = waitExpression.replace("["+datesExpression+"]", Boolean.toString(actualResult));
            }
            if(datesExpression.contains("#tv")) {
                String actualResult = addDaysTime(datesExpression);
                waitExpression=waitExpression.replace("["+datesExpression+"]",actualResult);
            }
            if(datesExpression.contains("#cv")) {
                System.out.println("Date Expression with #cv: " + datesExpression);
                cvFlag = 1;
                String actualResult = addDaysTime(datesExpression);
                System.out.println(actualResult);
                waitExpression=waitExpression.replace("["+datesExpression+"]",actualResult);
                System.out.println("waitExpression after replacing #cv: " + waitExpression);
            }
            if(datesExpression.contains("#co") || datesExpression.contains("#CO")) {
//                coFlag = 1;
//                count= getCounterValues(datesExpression, count);
            }
            if(coFlag == 1) {
                waitExpression=waitExpression.replace("["+datesExpression+"]",Integer.toString(count));
            }
        }

        List<String> dateTimeSAList = new ArrayList<String>();
        Pattern systemAttrRegex = Pattern.compile("\\#SA\\[[^\\[]*\\]");
        Matcher systemAttrMatcher = systemAttrRegex.matcher(waitExpression);
        while (systemAttrMatcher.find()) {
            dateTimeSAList.add(systemAttrMatcher.group(0));
        }
        for (String dateTime : dateTimeSAList) {
            waitExpression = waitExpression.replace( dateTime, evaluateSA(dateTime));
        }

        waitExpression = waitExpression.replaceAll(".CONTAINS", ".contains");
        System.out.println(waitExpression);
//		logger.debug("WAIT EXPRESSION for #cv and #tv is >>>>>>>>>"+waitExpression);
        Expression expression = parser.parseExpression(waitExpression);
//		logger.debug("EXPRESSION for #cv and #tv is >>>>>>>>>"+expression.getValue().toString());

        return expression.getValue().toString();
      //  return "";
    }


    private static String systemAttributes(String parameter, boolean isWaitTimer) {
        String value="";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<String> getParameterValue =Arrays.asList(parameter.split("~"));
//        if(getParameterValue.size()>1) {
//            if(getParameterValue.get(1).equalsIgnoreCase("-")) {
//                value= new Timestamp(System.currentTimeMillis()).plusHours(hourOffset).toLocalDate().minusDays(Integer.parseInt(getParameterValue.get(2))).format(dateFormat);
//            }else if(getParameterValue.get(1).equalsIgnoreCase("+")) {
//                value= getDateFromTransactionTime().plusHours(hourOffset).toLocalDate().plusDays(Integer.parseInt(getParameterValue.get(2))).format(dateFormat);
//            }
//            if(isWaitTimer) {
//                value=value+" 00:00:00";
//            }
//        }else if(getParameterValue.get(0).equalsIgnoreCase("NOW")){
//            value=  getDateFromTransactionTime().plusHours(hourOffset).format(dateTimeFormat);
//        }else if(getParameterValue.get(0).equalsIgnoreCase("TODAY")){
//            value= getDateFromTransactionTime().plusHours(hourOffset).toLocalDate().format(dateFormat);;
//            if(isWaitTimer) {
//                value=value+" 00:00:00";
//            }
//        }
        return value;
    }


    private static String evaluateSA(String waitExpression) {
        // waitExpression= "#SA[('TODAY~-~5')>('2020-09-12 12:13:12')]";
        waitExpression = waitExpression.replaceAll("'","");
        ArrayList<String> expList= new ArrayList<>();
        Pattern regex = Pattern.compile("\\(([^()]+)\\)");
        Matcher regexMatcher = regex.matcher(waitExpression);
        while (regexMatcher.find()) {//Finds Matching Pattern in String
            expList.add(regexMatcher.group(1));
        }

        if(expList.get(0).contains("TODAY")) {
            expList.set(1,expList.get(1).split(" ")[0]);
        }else if(expList.get(1).contains("TODAY")) {
            expList.set(0,expList.get(0).split(" ")[0]);
        }

//        if(expList.get(0).contains("TODAY") ||expList.get(0).contains("NOW")){
//            expList.set(0, systemAttributes(expList.get(0),false));
//        }else if(expList.get(1).contains("TODAY") ||expList.get(1).contains("NOW")){
//            expList.set(1, systemAttributes(expList.get(1),false));
//        }

        String op="";
        Pattern regex1 = Pattern.compile("(<|>|==)");
        Matcher regexMatcher1 = regex1.matcher(waitExpression);
        while (regexMatcher1.find()) {//Finds Matching Pattern in String
            op=regexMatcher1.group(1);//Fetching Group from String

        }

        String result ="";
        expList.set(0,expList.get(0).replaceAll("[^0-9]",""));
        expList.set(0,expList.get(0)+"L");
        expList.set(1,expList.get(1).replaceAll("[^0-9]",""));
        expList.set(1,expList.get(1)+"L");
        result="("+expList.get(0)+ op + expList.get(1) +")";

        return result;
    }




    private static String addDaysTime(String dateExpression) throws JSONException, Exception {
        ExpressionParser parser = new SpelExpressionParser();
        List<String> dateListValues = new ArrayList<String>();
        Pattern pattern = Pattern.compile("'(.*?)'");
        Matcher regexMatch = pattern.matcher(dateExpression);
        while (regexMatch.find()) {//Finds Matching Pattern in String
            dateListValues.add(regexMatch.group(1));//Fetching Group from String
        }
        String result ="";
        if(dateListValues.size()==3) {
            List<String> timeInterval = new ArrayList<>();
            if(dateListValues.get(1).equalsIgnoreCase("midnight")) {
                timeInterval.add("0");
                timeInterval.add("DAYSATMIDNIGHT");
            } else if(dateListValues.get(1).split(" ").length > 1) {
                System.out.println("Time Interval: " + dateListValues.get(1));
                timeInterval = Stream.of(dateListValues.get(1).split(" ")).collect(Collectors.toList());
            } else {
                System.out.println("Invalid time interval format. Expected format: '<number> <interval>' or 'midnight'.");
                result = "'" + String.join("", dateListValues) + "'";
                Expression expression = parser.parseExpression(result);
                return expression.getValue().toString();
                /*
                 * //DATE PICKER
                 * String str=dateListValues.get(1);
                 * Date date=new SimpleDateFormat("yyyy-MM-dd").parse(str);
                 * SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                 * String strDate = formatter.format(date);
                 *
                 * formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                 * strDate = formatter.format(date);
                 * dateListValues.set(0, strDate);
                 * timeInterval.add("0");
                 * timeInterval.add("DAYSATMIDNIGHT");
                 */
            }

            if(dateListValues.get(0).contains("V.")) {
                result = getValuesFromDates(dateListValues.get(0).replace("V.", ""), Integer.valueOf(timeInterval.get(0)), timeInterval.get(1), dateListValues.get(2));
            } else {
                if(dateListValues.size()==3) {
                    result = getValuesFromDates(dateListValues.get(0), Integer.valueOf(timeInterval.get(0)), timeInterval.get(1), dateListValues.get(2));
                }
                else {
                    result = getValuesFromDates(dateListValues.get(0), Integer.valueOf(timeInterval.get(0)), timeInterval.get(1), "+");
                }
            }
        } else if(dateListValues.size() == 1) {
            result = dateListValues.get(0).isEmpty() ? "NULL" : dateListValues.get(0);
        }else {
            System.out.println("Invalid date expression format. Expected format: '<date> <number> <interval> <sign>'.");
            result = "'" + String.join("", dateListValues) + "'";
            Expression expression = parser.parseExpression(result);
            return expression.getValue().toString();
        }
        return "'"+result+"'";
    }


    public static String getValuesFromDates(String time, int offset, String interval, String sign) throws JSONException, Exception {
//		logger.debug("TIME :: "+time+" OFFSET :: "+offset+" INTERVAL :: "+interval+" OPERATOR :: "+sign);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        if(time.equalsIgnoreCase(" ")){ //if we get empty date then return
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            c.setTime(ts);
            c.add(Calendar.HOUR, (int) 60);
            time = sdf.format(c.getTime());
        }else {
            Timestamp ts = new Timestamp(((java.util.Date)sdf.parse(time)).getTime());
            c.setTime(ts);
        }
        if(sign.equalsIgnoreCase("+")) {
            if(interval.equalsIgnoreCase("DAYS")) {
                c.add(Calendar.DATE, offset);
            }
            else if(interval.equalsIgnoreCase("HOURS"))
            {
                c.add(Calendar.HOUR, offset);// Adding time
            }
            else if(interval.equalsIgnoreCase("MINS"))
            {
                c.add(Calendar.MINUTE, offset);
            }
            else { // fix for MRTMR-4375 //DAYSATMIDNIGHT
                c.add(Calendar.DATE, offset+1);
                c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
            }
        }else if(sign.equalsIgnoreCase("-")) {
            if(interval.equalsIgnoreCase("DAYS")) {
                c.add(Calendar.DATE, -offset);
            }
            else if(interval.equalsIgnoreCase("HOURS")){
                c.add(Calendar.HOUR, -offset);// Subtracting time
            } else if(interval.equalsIgnoreCase("MINS"))
            {
                c.add(Calendar.MINUTE, -offset);
            }
            else {
                c.add(Calendar.DATE, -offset);
                c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
            }
        }
        return sdf.format(c.getTime());
    }




}