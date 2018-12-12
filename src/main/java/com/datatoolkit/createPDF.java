/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datatoolkit;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;  

import org.json.*;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Michael
 * In order to test this process, you can go directly to the url and change the
 * display option to test:
 * http://localhost:7001/datatoolkit/createPDF?p_increment=3&p_display=Test
 */
public class createPDF extends HttpServlet {

    private final static String OUTPUT_FILE_PATH = "./tmp/";
    private static String commentList;
    private final static String TEST_PASSWORD = "72F4CD-!4u2-P16Oct!8";
    private final static String PROD_PASSWORD = "8601C6-!4u2-P16Oct!8";
    private final static String DEMO_PASSWORD = "password";
    
    // Root of the classpath: D:/Oracle/Middleware/user_projects/domains/ils_domain
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //response.setContentType("text/html;charset=UTF-8");
        String outputOption = "";
        String user = "";
        String testing = "";
            
         
        try {
            String AbsolutePath = new File(".").getAbsolutePath();
            commentList = "Starting! Absolute Path: " + AbsolutePath;
            
            File directory = new File(OUTPUT_FILE_PATH);
            if (!directory.exists()) {
                commentList += ",Creating the directory for output!";
                directory.mkdirs();
            }

            outputOption = request.getParameter("p_display");
            user = request.getParameter("p_increment");
            testing = request.getParameter("p_test");
            if (testing == null) { testing = "Demo"; }
            commentList += ", radDisplay: " + outputOption;
            commentList += ", user: " + user;
            commentList += ", testing: " + testing;
            
            String url = request.getRequestURL().toString();
            commentList += ", Current URL: " + url;
            
            if ( "HTML".equals(outputOption) ) {
                PrintWriter out = response.getWriter();
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet createPDF</title>");            
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Servlet createPDF at " + request.getContextPath() + "</h1>");

                if (user != null) {
                    out.println("<h2> User Id " + user + "</h2>");
                } else {
                    out.println("<h2>There was no user value submitted...</h2>");
                }

                // Connect to the database and dispaly the json
                displayJsonFromDb(Integer.valueOf(user), out, testing);

                out.println("<a href='" + request.getContextPath() + "'>Return to Main</a>");
                out.println("</body>");
                out.println("</html>");                
            } else {
                commentList += ", Creating PDF to stream to user. ";
                
                // Test out creating a barcode.
                //testBarCode();
                
                // Create a PDF.
                if (!createPDF(user, OUTPUT_FILE_PATH, testing) ) {
                    throw new Exception("processRequest: No file was created.");
                }

                // Stream / Send the PDF as a file for the user to download.
                streamFileToUser(response);
                
                // Log the last 3000 characters of the comment list.
                commentList += ", Finished creating PDF. ";
                
                logComment(Integer.valueOf(user), testing, commentList, true);
                
            }

        } catch (Exception e) {
            PrintWriter out = response.getWriter();

            if ("Test".equals(outputOption)) {
                out.println("An error occurred, check out the output window to see more details.");
                out.println("Exception:  " + e.toString());
                out.println("Comment List:  " + commentList);
            } else {
                // This is what will be displahyed to the user in the normal production and test environments.
                out.println("An unexpected error has occurred");
                out.println("");
                out.println("Please wait a few minutes before retrying.");
                out.println("");
                out.println("If the problem persists then:  ");
                out.println("");
                out.println("     In themain menu click on Misc + Feedback to send a description of the problem,");
                out.println("");
                out.println("     Or, in themenu click on Misc + Forum and see if the problem has been reported there,");
                out.println("");
                out.println("     Or, contact NAVY311 and request a help ticket for the NAVSUP GLS ILS Application.");
            }
            
            out.close();
            
            try {
                // Try to log a comment if the user is set.
                if ("".equals(user)) {
                } else {
                    logComment(Integer.valueOf(user), testing, commentList, false);
                }
            } catch (Exception e2) {
                commentList += ", Exception encountered" + e2;
            }
        } finally {
//            out.close();
        }
    }

    private static void streamFileToUser(HttpServletResponse response) throws IOException, Exception {
     
        commentList += ",File stream starting.  Path: " + OUTPUT_FILE_PATH + "Merged_1348.pdf";
        
        //throw new java.lang.Exception("Testing out the error display...");
        
        File existingFile = new File(OUTPUT_FILE_PATH + "Merged_1348.pdf");
        
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "muyst-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentType("application/pdf");
        //response.setContentLength(existingFile.length());
        OutputStream oos = response.getOutputStream();
        
        byte[] buf = new byte[8192];
        
        InputStream is = new FileInputStream(existingFile);
        
        int c;
        
        while (( c = is.read(buf, 0, buf.length)) > 0 ) {
            oos.write(buf, 0, c);
            oos.flush();
        }
        
        oos.close();
        commentList += ",File stream finished";
        is.close();
        
    }
    
    
// <editor-fold defaultstate="collapsed" desc="Methods for creating the PDF.">
    /*
        Method for creating the PDF>
    */
    private boolean createPDF(String userId, String OUTPUT_FILE_PATH, String testing) throws DocumentException, IOException {
        commentList += ", createPDF: In createPDF.  Output file path " + OUTPUT_FILE_PATH;
        commentList += ", createPDF: User Id " + userId;
        List<InputStream> list = new ArrayList<InputStream>();
        List<File> fileList = new ArrayList<File>();
        String unknownText = "";
        // Get the json from the database.
        PdfContents contents = getJson(Integer.valueOf(userId), testing);
        if (null == contents) { return false; }
        commentList += ", createPDF: File Name: " + contents.getFileName();
        //commentList += ",createPDF: Json: " + contents.getJsonString();
        JSONObject obj = new JSONObject(contents.getJsonString());
        JSONArray arrDetails = obj.getJSONArray("details");
        commentList += ",createPDF: PDF Count to be created: " + arrDetails.length();
        // Declare and create the reader which will read in the blank 1348.
        PdfReader reader;
        // Declarations of items used in the loop.
        FileOutputStream individualPdfStream ;
        PdfTemplate template;
        PdfStamper stamper;
        Rectangle pagesize;
        Barcode39 newBarcode;
        BarcodeQRCode newQrBarcode;
        float x;
        float y;
        float qrX;
        float qrY;
        String nsn;
        float secondFormOffset = 380;
        JSONArray arrLocations;
        JSONObject detail;
        String locString;
        String shipFrom;
        String shipTo;
        String category;
        String priceStr;
        String quantity;
        Integer nFind;
        String fileName;
        
        // Loop through each of the 'items' in the json object.
        int loopLength = arrDetails.length();
        // loopLength = 5; // For testing so we don't make too many PDFs
        commentList += ", createPDF: Beginning XML Loop with last index: " + loopLength;
        for (int i = 0; i < loopLength; i++) {
            commentList += ", Loop variable is " + i;
            
            detail = arrDetails.getJSONObject(i);
            commentList += ", New File Name: " + OUTPUT_FILE_PATH + "1348_" + detail.getNumber("WF_DETAIL_ID");
            
            // Determine which version of the 1348 we should be using.  It has different colored text depending on the
            //  category.  I could not find a way to just change the font color and put the desired text in so I made diff
            //  files for each category.
            category = getFieldFromJson(detail, "CATEGORY", objectType.String);
            commentList += ", Category: " + category;
            if ( "P".equals(getFieldFromJson(detail, "DEMIL_CODE", objectType.String)) ) {
                fileName = "1348_Barcodes_P.pdf";
            } else {
                if ( "DLR".equals(category)) {
                    fileName = "1348_Barcodes_DLR.pdf";
                } else {
                    if ( "HAZMAT".equals(category)) {
                        fileName = "1348_Barcodes_HAZMAT.pdf";
                    } else {
                        fileName = "1348_Barcodes.pdf";
                    }
                }
            }
            
            reader = new PdfReader( contents.getFileName() + fileName );
            commentList += ", createPDF: Created pdf reader";
            individualPdfStream = new FileOutputStream(OUTPUT_FILE_PATH + "1348_" + detail.getNumber("WF_DETAIL_ID") + ".pdf");
            stamper = new PdfStamper(reader, individualPdfStream);
            
            AcroFields form = stamper.getAcroFields();
            //scanFields(form);
            commentList += ",Populating detail.  Index: " + i;
            
            form.setField("NMFC", unknownText);
            form.setField("Ric_Uic_Qty_Barcode", unknownText);
            form.setField("Adv", unknownText);
            form.setField("DocIden", getFieldFromJson(detail, "DI", objectType.String));
            form.setField("ApprovedForTransfer", getFieldFromJson(detail, "APPROVED_FOR_TRANSFER", objectType.String));
            form.setField("TypeCargo", unknownText);
            form.setField("SupAdd", "V" + getFieldFromJson(detail, "SHIP_UIC", objectType.String));
            form.setField("RecdDelDate", unknownText);
            form.setField("DocNum", unknownText);
            form.setField("Cond", getFieldFromJson(detail, "COND_CODE", objectType.String));
            form.setField("UnitCube", unknownText);
            form.setField("Distribution", "7G");
            form.setField("TYCount", unknownText);
            form.setField("Fund", unknownText);
            form.setField("RecdBy", unknownText);
            form.setField("Project", unknownText);
            form.setField("FreightClassNomen", unknownText);
            commentList += ",createPDF: Unit of Issue: " + getFieldFromJson(detail, "UI", objectType.String);
            form.setField("UnitIss", getFieldFromJson(detail, "UI", objectType.String));
            form.setField("PointOfContact", getFieldFromJson(detail, "SHIP_SUPPLY_OFFICER", objectType.String) + "/r" +
                    getFieldFromJson(detail, "PHONE_NUMBER", objectType.String));
            priceStr = getFieldFromJson(detail, "EMV", objectType.Number);
            if ( priceStr.equals("") ) { priceStr = "0.00"; }
            nFind = priceStr.indexOf(".");
            if (nFind < 0) { nFind = priceStr.indexOf(","); } // Try using a comma as the price delimiter.
            if (nFind < 0) {
                form.setField("TPCents", "00");
                form.setField("TPDollars", "0");
            } else {
                form.setField("TPCents", priceStr.substring(nFind + 1, priceStr.length()) );
                form.setField("TPDollars", priceStr.substring(0, nFind));
            }
            
            priceStr = getFieldFromJson(detail, "UNIT_PRICE", objectType.Number);
            if ( priceStr.equals("") ) { priceStr = "0.00"; }
            nFind = priceStr.indexOf(".");
            if (nFind < 0) { nFind = priceStr.indexOf(","); } // Try using a comma as the unit price delimiter.
            if (nFind < 0) {
                form.setField("UPDollars", "0");
                form.setField("UPCents", "00" );
                priceStr = "0.00";
            } else {
                form.setField("UPDollars", priceStr.substring(0, nFind) );
                form.setField("UPCents", priceStr.substring(nFind + 1, priceStr.length()) );
            }
            form.setField("MGT", "C");
            form.setField("UP", unknownText);
            form.setField("QtyRecd", unknownText);
            form.setField("TotalWeight", unknownText);
            form.setField("UFC", unknownText);
            form.setField("MandS", unknownText);
            quantity = String.format("%05d", Integer.parseInt(getFieldFromJson(detail, "TOT_OFFLOAD_QTY", objectType.Number) )) ;
            form.setField("Quant", quantity);
            form.setField("PS", unknownText);
            nsn = getFieldFromJson(detail, "FSC", objectType.Number) +
                    getFieldFromJson(detail, "NIIN", objectType.String);
            form.setField("NSNa", nsn);
            form.setField("TotalCube", unknownText);
            form.setField("RICUIQTY", unknownText);
            form.setField("RetainQty", unknownText);
            form.setField("RIFrom", getFieldFromJson(detail, "SHIP_ROUTING_ID_FROM", objectType.String));
            form.setField("Pri", "13");
            if ( "P".equals(getFieldFromJson(detail, "DEMIL_CODE", objectType.String)) ) {
                form.setField("Special_Type", "P");
            } else {
                if ( !"Standard".equals(category)) {
                    form.setField("Special_Type", category);
                } else {
                    form.setField("Special_Type", "");                   
                }
            }
            form.setField("SER", unknownText);
            form.setField("FRTRate", unknownText);
            form.setField("DocDate", getFieldFromJson(detail, "JULIAN", objectType.String));
            form.setField("NOCount", unknownText);
            form.setField("ItemNomen", getFieldFromJson(detail, "NOMENCLATURE", objectType.String));
            form.setField("Sig", unknownText);
            form.setField("Ri", getFieldFromJson(detail, "SHIP_ROUTING_ID_TO", objectType.String));
            form.setField("SI", getFieldFromJson(detail, "SHELF_LIFE_CODE", objectType.String));
            form.setField("UnitWeight", unknownText);
            form.setField("OP", "A");
            form.setField("MarkFor", unknownText);
            form.setField("DateRecd", unknownText);
            
            // Add the locations to the location area.
            arrLocations = detail.getJSONArray("LOCATIONS");
            locString = "";
            for (int i_l = 0; i_l< arrLocations.length(); i_l++) {
                locString += getFieldFromJson(arrLocations.getJSONObject(i_l), "LOCATION", objectType.String) + "\r";
            }
            form.setField("Locations", locString);
            
            // Build the ship from
            shipFrom = getFieldFromJson(detail, "SHIP_ADR_LINE_1", objectType.String) + "\r" +
                    getFieldFromJson(detail, "SHIP_ADR_LINE_2", objectType.String) + "\r" +
                    getFieldFromJson(detail, "SHIP_CITY", objectType.String) + " " +
                    getFieldFromJson(detail, "SHIP_STATE_ABBR", objectType.String) +  " "  +
                    getFieldFromJson(detail, "SHIP_ZIP", objectType.String);
            form.setField("ShipFrom", "V" + getFieldFromJson(detail, "SHIP_UIC", objectType.String));
            form.setField("Ship_From", shipFrom);
            
            // Build the ship to
            shipTo = getFieldFromJson(detail, "ATTENTION", objectType.String) + "\r" +
                    getFieldFromJson(detail, "ADR_LINE_1", objectType.String) + "\r" +
                    getFieldFromJson(detail, "CITY", objectType.String) + " " +
                    getFieldFromJson(detail, "STATE_ABBR", objectType.String) + " " +
                    getFieldFromJson(detail, "ZIP", objectType.String);
            form.setField("ShipTo", getFieldFromJson(detail, "DESTINATION_UIC", objectType.String));
            form.setField("Ship_To", shipTo);
            
            // Create a new barcode (https://stackoverflow.com/questions/26325712/add-image-to-existing-document)
            PdfContentByte over = stamper.getOverContent(1);
            pagesize = reader.getPageSize(1);
            
            // Code to add the document number bar code
            x = pagesize.getLeft() + 90;
            y = pagesize.getTop() - 145;
            newBarcode = new Barcode39();
            String docNbr = getFieldFromJson(detail, "DOCUMENT_NUMBER", objectType.String);
            if ( docNbr.equals("") ) { docNbr = "0000000000000"; }
            newBarcode.setCode(docNbr);
            template = newBarcode.createTemplateWithBarcode(over, BaseColor.BLACK, BaseColor.BLACK);
            over.addTemplate(template, x, y); 
            y -= secondFormOffset;
            over.addTemplate(template, x, y);
            commentList += ",Added Doc Number Barcode. Position: ";// + x + "," + y;
            
            // Add the QR barcode for doc number:  http://www.mysamplecode.com/2012/11/itext-generate-barcode-and-qrcode.html
            qrX = pagesize.getLeft() + 325;
            qrY = pagesize.getTop() - 137;
            newQrBarcode = new BarcodeQRCode(docNbr, 1, 1, null);
            Image qrCodeImg = newQrBarcode.getImage();
            qrCodeImg.setAbsolutePosition(qrX, qrY);
            over.addImage(qrCodeImg);
            qrCodeImg.setAbsolutePosition(qrX, qrY - secondFormOffset);
            over.addImage(qrCodeImg);
            commentList += ", Added QR Doc Nbr barcode at " + qrX + "," + qrY;
            
            // Code to add the nsn bar code            
            y = pagesize.getTop() - 195;
            newBarcode = new Barcode39();
            newBarcode.setCode(nsn);
            template = newBarcode.createTemplateWithBarcode(over, BaseColor.BLACK, BaseColor.BLACK);
            over.addTemplate(template, x, y);
            y -= secondFormOffset;
            over.addTemplate(template, x, y);
            commentList += ",Added NSN Barcode 1. Position: "; // + x + "," + y;
            
            // Add the QR NSN bar code
            qrY = pagesize.getTop() - 187;
            newQrBarcode = new BarcodeQRCode(nsn, 1, 1, null);
            qrCodeImg = newQrBarcode.getImage();
            qrCodeImg.setAbsolutePosition(qrX, qrY);
            over.addImage(qrCodeImg);
            qrCodeImg.setAbsolutePosition(qrX, qrY - secondFormOffset);
            over.addImage(qrCodeImg);
            commentList += ", Added QR NSN barcode at " + qrX + "," + qrY;
            
            // Code to add the ui/qty/con coe, etc bar code
            y = pagesize.getTop() - 245;
            newBarcode = new Barcode39();
            String uiQtyCon;
            if (nFind < 0) {
                uiQtyCon = getFieldFromJson(detail, "SHIP_ROUTING_ID_FROM", objectType.String) +
                        getFieldFromJson(detail, "UI", objectType.String)+
                        quantity + 
                        getFieldFromJson(detail, "COND_CODE", objectType.String) +
                        "00" +                                                  //dIST
                        "0" +                                                   // Unit price dollars.
                        "00"                                                    // Unit price cents.
                        ;
            } else {
                uiQtyCon = getFieldFromJson(detail, "SHIP_ROUTING_ID_FROM", objectType.String) + 
                        getFieldFromJson(detail, "UI", objectType.String)+
                        quantity + 
                        getFieldFromJson(detail, "COND_CODE", objectType.String) +
                        "00" +                                                  //dIST
                        priceStr.substring(0, nFind) +                          // Unit price dollars.
                        priceStr.substring(nFind + 1, priceStr.length())        // Unit price cents.
                        ;
            }
            newBarcode.setCode(uiQtyCon);
            template = newBarcode.createTemplateWithBarcode(over, BaseColor.BLACK, BaseColor.BLACK);
            over.addTemplate(template, x, y);
            y -= secondFormOffset;
            over.addTemplate(template, x, y);
            commentList += ",Added NSN Barcode 2. Position: " + x + "," + y;
            
            // Add the QR UI, Qty, Con, etc. bar code
            qrY = pagesize.getTop() - 237;
            newQrBarcode = new BarcodeQRCode(uiQtyCon, 1, 1, null);
            qrCodeImg = newQrBarcode.getImage();
            qrCodeImg.setAbsolutePosition(qrX, qrY);
            over.addImage(qrCodeImg);
            qrCodeImg.setAbsolutePosition(qrX, qrY - secondFormOffset);
            over.addImage(qrCodeImg);
            commentList += ", Added QR UI Qty, Con, etc. barcode at " + qrX + "," + qrY;
            
            stamper.setFormFlattening(true);
            stamper.close();
            reader.close();
            
            // All 1348 copies get at least 2 pages printed.
            list.add(new FileInputStream(new File(OUTPUT_FILE_PATH + "1348_" + detail.getNumber("WF_DETAIL_ID") + ".pdf")));
            list.add(new FileInputStream(new File(OUTPUT_FILE_PATH + "1348_" + detail.getNumber("WF_DETAIL_ID") + ".pdf")));
            
            // If this is a DRMO 1348, then we need to create a total of 3 pages of the 1348, so add another here.
            if ( getFieldFromJson(detail, "DEFAULT_TURN_IN", objectType.String).equals("DRMO") ) {
                commentList += ",DRMO turn in, creating 2 more copies.";
                list.add(new FileInputStream(new File(OUTPUT_FILE_PATH + "1348_" + detail.getNumber("WF_DETAIL_ID") + ".pdf")));
            }
            
            // Save off the file name so it can later be deleted.
            fileList.add(new File(OUTPUT_FILE_PATH + "1348_" + detail.getNumber("WF_DETAIL_ID") + ".pdf"));
            
            commentList += ",Finished Creating PDF in loop";
        }
        // Create the output stream
        commentList += ",Creating Output Stream";
        OutputStream out = new FileOutputStream(new File(OUTPUT_FILE_PATH + "Merged_1348.pdf"));
        // Merge the files together.
        doMerge(list, out);
        // Delete the files which were used to create the merged PDF
        commentList += ",Deleting old files";
        for(File file: fileList) {
            file.delete();
        }      
        commentList += ",Finished createPDF." ;
        return true;

    };
    
    /*
        Method for returning the file name and json string via object PdfContents
    */
    private static com.datatoolkit.PdfContents getJson(Integer userId, String testing) {
        String query;
        
        try {
            commentList += ",User Id: " + userId;
            
            // load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // create  the connection object  
            Connection con;
            con = getConn(testing);

            // create the statement object  
            Statement stmt = con.createStatement();

            // execute query  
            query = "select json, base_form from create_file_json where user_id = " + userId + " and printed is null";       
            ResultSet rs = stmt.executeQuery(query);
            
            if (!rs.next()) { return null;};
            PdfContents results = new PdfContents(rs.getString(1), rs.getString(2));
            
            //close the connection object  
            con.close();

            return results;
            
        } catch (Exception e) {
            commentList += ", getJson exception: " + e.toString();
        }
        
        return null;
    };
    
    private static Connection getConn(String testing) throws SQLException {
        Connection con = null;

        if (testing.equals("Test")) {
            con = DriverManager.getConnection("jdbc:oracle:thin:@214.21.99.70:1521:ewi", "ops$ils", TEST_PASSWORD);
        }

        if (testing.equals("Prod")) {
            con = DriverManager.getConnection("jdbc:oracle:thin:@214.21.106.143:1521:ewp", "ops$ils", PROD_PASSWORD);
        }

        if (testing.equals("Demo")) {
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "mike", DEMO_PASSWORD);
        }
        
        return con;

    }
    
    private void logComment(Integer userId, String testing, String comments, Boolean markDone) throws Exception {
        String query;
        
        try {           
            // load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // create  the connection object  
            Connection con = getConn(testing);
 
            // create the statement object  
            Statement stmt = con.createStatement();
            if (comments.length() > 3000) {
                comments = comments.substring(comments.length() - 3000);
            }
            
            comments = comments.replaceAll("'", "."); // Single quotes will break sql!
            comments = comments.replaceAll(",", "<br>");

            // Update the comments and indicate the file has been printed if we should mark it as done.
            if (markDone) {
                query = "update create_file_json set printed = current_timestamp, comments = '" + comments + "' where printed is null and user_id = " + userId;
            } else {
                query = "update create_file_json set comments = '" + comments + "' where printed is null and user_id = " + userId;
            }
            commentList += ",log Query: " + query;
            stmt.execute(query);
            
            //close the connection object  
            con.close();
            
        } catch (Exception e) {
            commentList += ",logComment Exception: " + e.toString();
            throw e;
        }
    };
    
    public enum objectType {
        Number, String
    }
    public String getFieldFromJson(JSONObject obj, String fieldName, objectType type) {
        try {
            switch (type) {
                case Number:
                    return obj.getNumber(fieldName).toString();

                case String:
                    return obj.getString(fieldName);

                default:
                    return "";
            }
        } catch(JSONException ex) {
            // Switch the type to see if that works.
            try {
                if (type.equals(objectType.String) ) {
                    type = objectType.Number;
                } else {
                    type = objectType.String;
                }
                switch (type) {
                    case Number:
                        return obj.getNumber(fieldName).toString();

                    case String:
                        return obj.getString(fieldName);

                    default:
                        return "";
                }
            } catch(JSONException ex2) {
                //commentList += ",Unable to find field in JSON: " + fieldName + " in " + obj ;
                commentList += ",Unable to find field in JSON: " + fieldName;
                return "";
            } catch(Exception ex3) {
                commentList += ",General Exception: Field: " + fieldName + ", Ex: " + ex3.getMessage() ;
                return "";
            }
  
        }
    }
    
    /**
     * Merge multiple pdf into one pdf
     * 
     * @param list
     *            of pdf input stream
     * @param outputStream
     *            output file output stream
     * @throws DocumentException
     * @throws IOException
     */
    public static void doMerge(List<InputStream> list, OutputStream outputStream)
            throws DocumentException, IOException {
        commentList += ",Merging Documents together";
        
        // Testing:  Print out all files in the output directory.
        File folder = new File(OUTPUT_FILE_PATH);
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                commentList += ",doMerge: Found a file: " + listOfFile.getName();
            } else if (listOfFile.isDirectory()) {
                commentList += ",doMerge: Found a directory: " + listOfFile.getName();
            }
        }
        
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        commentList += ",doMerge: opening document";
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        
        commentList += ",doMerge: beginning loop";
        for (InputStream in : list) {
            PdfReader reader = new PdfReader(in);
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                document.newPage();
                //import the page from source pdf
                PdfImportedPage page = writer.getImportedPage(reader, i);
                //add the page to the destination pdf
                cb.addTemplate(page, 0, 0);
            }
        }
        
        commentList += ",doMerge: flushing output string";
        outputStream.flush();
        document.close();
        outputStream.close();
    }

// </editor-fold>
 
    
// <editor-fold defaultstate="collapsed" desc="Testing procedures">
    
        
    /*
    Connect to Oracle and dispaly the json on the screen.
    */
    private static void displayJsonFromDb(Integer userId, PrintWriter out, String testing) {
        try {
            commentList += ",displayJsonFromDb: User Id: " + userId;
            
            PdfContents contents = getJson(Integer.valueOf(userId), testing);
            if (null == contents) { return; }
            commentList += ",displayJsonFromDb: Base form File Name: " + contents.getFileName();
            commentList += ",displayJsonFromDb: Json: " + contents.getJsonString();
                        
            //Output the information.
            out.println("<h3>Database Values:</h3>");            
            out.println("<h3>Original JSON: " + contents.getJsonString() + "</h3>");
            out.println("<h3>Base File Name: " + contents.getFileName() + "</h3>");
            JSONObject obj = new JSONObject(contents.getJsonString());
            
            JSONArray arrDetails = obj.getJSONArray("details");    

            // Loop through each of the 'items' in the json object.
            for (int i = 0; i< arrDetails.length(); i++) {
                out.println("<h3>New PDF</h3>");
                out.println("<h4>Detail Id: " + arrDetails.getJSONObject(i).getNumber("WF_DETAIL_ID") + "</h4>");  
                out.println("<h4>FSC: " + arrDetails.getJSONObject(i).getString("FSC") + "</h4>");      
                              
            };

        } catch (Exception e) {
            commentList += ", displayJsonFromDb Exception: " + e.toString();
        };

    };
    
    // Get all the fields in the pdf and display them.
    private static void scanFields(AcroFields acroFields) {
        Set<String> fldNames = acroFields.getFields().keySet();
        
        for (String fldName : fldNames) {
            commentList += "," + fldName + ": " + acroFields.getField( fldName ) ;
        }
    }
    
// </editor-fold>
    
    
// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Pull in information from the database and create a PDF";
    }// </editor-fold>

}


