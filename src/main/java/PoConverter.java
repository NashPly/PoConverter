import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PoConverter {

    public static void main(String[] args) throws IOException, InterruptedException {

        List<JSONObject> colorSlabList = new ArrayList<>();
        List<List> excelSlabList = new ArrayList<>(7);
        List<JSONObject> colorAccessoryList = new ArrayList<>();
        List<List> excelAccessoryList = new ArrayList<>(7);
        List<String> barList = new ArrayList<>();

        String poNum = "20446";

        HttpClient client = HttpClient.newBuilder().build();

        String contextID = login(client);

        JSONArray json = agilityPOLookup(client, contextID, poNum);

        var holder = new JSONObject();
        var holderCode = "";
        for(int i = 0; i < json.length(); i++)
        {
            holder = json.getJSONObject(i);
            holderCode = holder.getString("ItemCode");
            if(holderCode.endsWith("KSL") || holderCode.endsWith("VSL") || holderCode.endsWith("DSL") || holderCode.endsWith("BSL")){
                colorSlabList.add(holder);
            } else if (holderCode.endsWith("RCAP") || holderCode.endsWith("LCAP") || holderCode.endsWith("KSPL") ||
                    holderCode.endsWith("VSPL") || holderCode.endsWith("DSPL") || holderCode.endsWith("LSPL") ||
                    holderCode.endsWith("BSPL") || holderCode.endsWith("BCAP") || holderCode.endsWith("SCAP")){
                colorAccessoryList.add(holder);
            } else {
                System.out.println("Missing an assignment: " + holder);
            }
        }

        String profile = getOrderProfile(colorSlabList.get(0).getString("ItemCode"));

        System.out.println(profile);

        for( JSONObject item: colorSlabList){

            List<String> tempList = generateTempList(18);

            String colorCode= item.getString("ItemCode").replaceAll("[a-zA-Z]", "").replaceAll("-", "");
            colorCode = colorCode.substring(0,colorCode.length()-2) + "-" + colorCode.substring(colorCode.length()-2);
            String lineItem = item.getString("ItemCode").replaceAll("^([Pp][Ff][Tt](\\S{2})\\d{4}(-)?\\d{2})", "").toUpperCase();
            String size = item.getString("SIZE");

            if(lineItem.equals("BSL") && !barList.contains(size.substring(0,2))){
                barList.add(size.substring(0,2));
            }

            int destination = slabLineItemDestination(lineItem, size, barList);

            excelSlabList = addColorIfNew(excelSlabList,colorCode,tempList);

            int length = Integer.parseInt(size.replaceAll("^\\d{2}\"X", "").replaceAll("\"", "")) / 12;

            excelSlabList = plugInLineItemToSpreadsheetRows(excelSlabList, destination, colorCode, item.getInt("Quantity")/length);
        }

        for( JSONObject item: colorAccessoryList){

            List<String> tempList = generateTempList(18);

            String colorCode= item.getString("ItemCode").replaceAll("[a-zA-Z]", "").replaceAll("-", "");
            colorCode = colorCode.substring(0,colorCode.length()-2) + "-" + colorCode.substring(colorCode.length()-2);
            String lineItem = item.getString("ItemCode").replaceAll("^([Pp][Ff][Tt])(\\S{2})?\\d{4}(-)?\\d{2}", "").toUpperCase();

            int destination = accessoryLineItemDestination(lineItem);

            excelAccessoryList = addColorIfNew(excelAccessoryList, colorCode, tempList);

            excelAccessoryList = plugInLineItemToSpreadsheetRows(excelAccessoryList,destination, colorCode, item.getInt("Quantity"));
        }

//        System.out.println("excelAccessoryList");
//        System.out.println(excelAccessoryList);


        FileInputStream file = new FileInputStream("C:\\Users\\tbeals\\OneDrive - Top Shop\\OneDrive - Nashville Plywood\\Template Docs\\Cullman Countertop Order Form.xlsx");
        XSSFWorkbook workbookinput = new XSSFWorkbook(file);

//output new excel file to which we need to copy the above sheets
//this would copy entire workbook from source
        XSSFWorkbook workbookoutput= workbookinput;

        Sheet sheet = workbookoutput.getSheetAt(0);


        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();

        Row dateRow = sheet.getRow(3);
        dateRow.getCell(12).setCellValue(formatter.format(date));
        Row poRow = sheet.getRow(4);
        poRow.getCell(12).setCellValue(poNum);

        Row profileRow = sheet.getRow(6);
        profileRow.getCell(2).setCellValue(profile);

        for(int q = 0; q < excelSlabList.size(); q++) {

            Row row = sheet.getRow(10 + q);
            Cell cell = null;
            for (int i = 0; i < excelSlabList.get(q).size(); i++) {
                System.out.println("here");
                System.out.println(excelSlabList.get(q).get(i));
                System.out.println("Q: " + q);
                System.out.println("I: " + i);
                cell = row.getCell(i + 1);
                cell.setCellValue(excelSlabList.get(q).get(i).toString());
            }
        }


        if(barList.size() > 0){
            Row barSizeRow = sheet.getRow(8);

            for(int x = 0; x < barList.size(); x++){
                Cell barCell = barSizeRow.getCell(13 + (x * 3));
                barCell.setCellValue(barList.get(x));
            }
        }

        for(int q = 0; q < excelAccessoryList.size(); q++) {

            Row row = sheet.getRow(22 + q);
            Cell cell = null;
            for (int i = 0; i < excelAccessoryList.get(q).size(); i++) {
                cell = row.getCell(i + 1);
                cell.setCellValue(excelAccessoryList.get(q).get(i).toString());
            }
        }

//To write your changes to new workbook
        FileOutputStream out = new FileOutputStream("C:\\Users\\tbeals\\OneDrive - Top Shop\\OneDrive - Nashville Plywood\\Cullman PO Spreadsheets\\Cullman_NashPly_PO"+ poNum+".xlsx");
        workbookoutput.write(out);
        out.close();


        logout(client,contextID);
    }

    public static String getOrderProfile(String itemCode) {

        switch(itemCode.substring(3,5).toUpperCase()){
            case "CT" -> {
                return "CONTOUR";
            }
            case "OL" -> {
                return "OLYMPIC";
            }
            case "CN" -> {
                return "CONTINENTAL";
            }
            case "SO", "SL" -> {
                return "SOLIDO";
            }
            case "SN" -> {
                return "SATURN";
            }
            case "KS" -> {
                return "KEYSTONE";
            }
            case "BV", "BL" -> {
                return "BEVEL";
            }
            case "VN", "VS" -> {
                return "VENUS";
            }
            default -> {
                return "";
            }
        }
    }

    public static List<String> generateTempList(int size) {
        List<String> tempList = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            tempList.add("");
        }

        return tempList;
    }

    private static List<List> addColorIfNew(List<List> excelList, String colorCode, List<String> tempList) {
        if(!checkIfColorExists(excelList, colorCode)){
            tempList.remove(0);
            tempList.add(0, colorCode);
            excelList.add(tempList);
        }
        return excelList;
    }

    public static List<List> plugInLineItemToSpreadsheetRows(List<List> excelList, int destination, String colorCode, int quantity) {
        //TODO Refactor with IndexOF
        for(int i = 0; i< excelList.size(); i++){
            if (excelList.get(i).contains(colorCode)) {
                var appendValue = String.valueOf(excelList.get(i).get(destination))+ quantity;
                excelList.get(i).remove(destination);
                excelList.get(i).add(destination, appendValue);
            }
        }
        return excelList;
    }

    public static Boolean checkIfColorExists(List<List> list, String colorCode){

        return list.stream().anyMatch(list1 -> list1.contains(colorCode));

    }

    public static int slabLineItemDestination(String lineItem, String size, List<String> barList){

        lineItem.toUpperCase(Locale.ROOT);
        int baseDestination = 0;

        switch(lineItem) {
            case "KSL" -> {
                baseDestination = 1;
                System.out.println(lineItem);
            }
            case "VSL" -> {
                baseDestination = 4;
            }
            case "DSL" -> {
                baseDestination = 7;
            }
            case "BSL" -> {
                baseDestination = 10 + (3 * (barList.indexOf(size.substring(0,2))));
            }
        }

            switch(size.replaceFirst("\\d{2}\"X", "").replaceAll("\"","")){
                case "96" ->{
                }
                case "120" ->{
                    baseDestination += 1;
                }
                case "144" ->{
                    baseDestination += 2;
                }
            }

        return baseDestination;
    }

    public static int accessoryLineItemDestination(String lineItem){

        lineItem.toUpperCase(Locale.ROOT);
        int baseDestination = 0;

        switch(lineItem) {
            case "KSPL" -> {
                baseDestination = 1;
            }
            case "LCAP" -> {
                baseDestination = 3;
            }
            case "RCAP" -> {
                baseDestination = 4;
            }
            case "VSPL" -> {
                baseDestination = 5;
            }
            case "DSPL" -> {
                baseDestination = 7;
            }
            case "BCAP", "SCAP" -> {
                baseDestination = 9;
                System.out.println(lineItem);
            }
            case "BSPL" -> {
                baseDestination = 14;
                System.out.println(lineItem);
            }
        }

        return baseDestination;
    }

    public static String login(HttpClient client) throws IOException, InterruptedException {
        JSONObject innerRequestBody = new JSONObject();
        innerRequestBody.put("LoginID","tbeals");
        innerRequestBody.put("Password","123");

        JSONObject requestBody = new JSONObject();
        requestBody.put("request", innerRequestBody);

        var request = HttpRequest.newBuilder(
                URI.create("https://api-1086-1.dmsi.com/nashvilleplywoodprodAgilityPublic/rest/Session/Login"))
                .header("accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());

        return json.getJSONObject("response").getString("SessionContextId");
    }

    public static JSONArray agilityPOLookup(HttpClient client, String contextID, String PoNum) throws IOException, InterruptedException {

        JSONObject requestBody = new JSONObject();

        requestBody.put("PurchaseOrderID", PoNum);

        var response =  postAgilityAPICall(client, contextID, requestBody);

        return response.getJSONObject("response")
                .getJSONObject("PurchaseOrderResponse")
                .getJSONObject("dsPurchaseOrderResponse")
                .getJSONArray("dtPurchaseOrderDetail");
    }

    public static JSONObject postAgilityAPICall(HttpClient client, String contextID, JSONObject requestBody) throws IOException, InterruptedException {

        String url = "https://api-1086-1.dmsi.com/nashvilleplywoodprodAgilityPublic/rest/";
        var request = HttpRequest.newBuilder(
                URI.create(url + "Purchasing/PurchaseOrderGet"))
                .header("accept", "application/json")
                .header("ContextId", contextID)
                .header("Branch", "CABINETS")
                .POST(buildRequest(requestBody))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("- POST Call to Agility -");
        System.out.println(response);
        System.out.println(response.body());

        return new JSONObject(response.body());
    }

    public static HttpRequest.BodyPublisher buildRequest(JSONObject InnerRequestBody){
        JSONObject requestBody = new JSONObject();
        requestBody.put("request", InnerRequestBody);
        return HttpRequest.BodyPublishers.ofString(requestBody.toString());
    }

    public static void logout(HttpClient client, String contextId) throws IOException, InterruptedException {
        JSONObject innerRequestBody = new JSONObject();
        innerRequestBody.put("LoginID","tbeals");
        innerRequestBody.put("Password","123");

        JSONObject requestBody = new JSONObject();
        requestBody.put("request", innerRequestBody);

        var request = HttpRequest.newBuilder(
                URI.create("https://api-1086-1.dmsi.com/nashvilleplywoodprodAgilityPublic/rest/Session/Logout"))
                .header("accept", "application/json")
                .header("accept", "application/json")
                .header("ContextId", contextId)
                .header("Branch", "CABINETS")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
