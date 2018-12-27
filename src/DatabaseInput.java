import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.sqlite.JDBC;

public class DatabaseInput {
    public static void main(String[] args) {
        if(args[0] == null) {
            System.out.println("Bad input!");
            return;
        }

        String fileName = args[1];
        String sheetName = "";
        String tableName = "defaultTable";
        String dbName = args[0];
        String SQLStruct = "";
        ArrayList<String> fieldNames = new ArrayList<>();
        ArrayList<String> typeList = new ArrayList<>();
        ArrayList<String> innerList = null;
        ArrayList<ArrayList<String>> outerList = new ArrayList<>();
        int maxCharLength = 0;
        int stRows, stColumns;

        /*set loading arguments*/
        if(args.length >= 3) {
            sheetName = args[2];
            if(args.length >= 4) {
                tableName = args[3];
            }
            else {
                tableName = args[2];
            }
        }
        else{
            String[] fullName = fileName.split("\\\\");
            if(fullName.length > 1) {
                tableName = getFileNameNoEx(fullName[fullName.length - 1]);
            }
        }

        File file = new File(fileName);
        Workbook wb = null;   // excel file to read
        try {
            wb = Workbook.getWorkbook(file);
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
        Sheet sheet;    // sheet number

        /*read data from excel files*/
        if (wb != null) {
            if(sheetName.equals("")) {
                sheet = wb.getSheet(0);
            }
            else
                sheet = wb.getSheet(sheetName);
            stRows = sheet.getRows();   // number of non-empty rows
            stColumns = sheet.getColumns();
            for(int i = 0; i < stColumns; i++) {
                fieldNames.add(sheet.getCell(i ,0).getContents());  // reading line 0 as name of fields
            }
            for(int i = 1; i < stRows; i++) {
                innerList = new ArrayList<>();
                for(int j = 0; j < stColumns; j++) {
                    if(sheet.getCell(j, i).getContents().length() > maxCharLength) {
                        maxCharLength = sheet.getCell(j, i).getContents().length();
                    }
                    innerList.add(sheet.getCell(j, i).getContents());   // put a line into a List<String>
                }
                outerList.add(innerList);
            }
            for(int i = 0; i < stColumns; i++) {
                if(isInteger(sheet.getCell(i, 1).getContents())) {
                    typeList.add("integer");
                }
                else if(isDouble(sheet.getCell(i, 1).getContents())) {
                    typeList.add("real");
                }
                else {
                    typeList.add("char(" + maxCharLength + ")");
                }
            }
            wb.close();
        }
        else {
            System.out.println("WorkBook is Empty or not existed.");
            return;
        }

        Connection conn;
        Statement stat;
        try {
            Class cl = Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbName;
            StringBuilder fieldsToAdd = new StringBuilder();
            StringBuilder insertSql = new StringBuilder();
            fieldsToAdd.append("ID integer primary key, ");
            for(int i = 0; i < stColumns - 1; i++) {
                fieldsToAdd.append(fieldNames.get(i)).append(" ").append(typeList.get(i)).append(", ");
            }
            fieldsToAdd.append(fieldNames.get(stColumns - 1)).append(" ").append(typeList.get(stColumns - 1));
            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
            stat.executeUpdate("drop table if exists " + tableName + ";");
            SQLStruct = "Create table " + tableName + "(" + fieldsToAdd.toString()+ ");";
            stat.executeUpdate(SQLStruct);
            insertSql.append("insert into ").append(tableName).append(" values(?,");
            for(int i = 0; i < stColumns - 1; i++) {
                insertSql.append("?,");
            }
            insertSql.append("?);");
            PreparedStatement prs = conn.prepareStatement(insertSql.toString());
            for (int i = 0; i < outerList.size(); i++) {
                prs.setInt(1, i);
                for (int j = 0; j < innerList.size(); j++) {
                    if(typeList.get(j).equals("integer")) {
                        prs.setInt(j+2, Integer.parseInt(outerList.get(i).get(j)));
                    }
                    else if(typeList.get(j).equals("real")) {
                        prs.setDouble(j+2, Double.parseDouble(outerList.get(i).get(j)));
                    }
                    else
                        prs.setString(j + 2, outerList.get(i).get(j));
                }
                prs.addBatch();
                conn.setAutoCommit(false);
                prs.executeBatch();
                conn.setAutoCommit(true);
            }
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        System.out.println(SQLStruct);
        System.out.println("Number of Rows: " + stRows);
    }

    private static boolean isInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    private static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    private static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}