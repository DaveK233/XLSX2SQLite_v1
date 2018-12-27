import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.*;
import org.sqlite.JDBC;

public class DatabaseInput {
    public static void main(String[] args) {
        if(args[0] == null) {
            System.out.println("Bad input!");
            return;
        }

        String fileName = args[1];  // excel file name
        String sheetName = "";  // excel sheet name
        String tableName = "defaultTable";  // database table name
        String dbName = args[0];    // database name
        String SQLStruct = "";  // to store SQL sentence
        ArrayList<String> fieldNames = new ArrayList<>();   // to store name of field
        ArrayList<String> typeList = new ArrayList<>(); // to store type
        ArrayList<String> innerList = null; // to store strings of each row
        ArrayList<ArrayList<String>> outerList = new ArrayList<>(); // to store rows
        int maxCharLength = 0;  // maximum length of char
        int trLength = 0, tdLength = 0; // number or rows and columns

        /*set loading arguments*/
        if(args.length >= 3) {
            sheetName = args[2];
            /*4 arguments, use tablename*/
            if(args.length >= 4) {
                tableName = args[3];
            }
            /*3 arguments, use sheetname*/
            else {
                tableName = args[2];
            }
        }
        else{
            /*to get the filename without suffix*/
            String[] fullName = fileName.split("\\\\");
            if(fullName.length > 1) {
                tableName = getFileNameNoEx(fullName[fullName.length - 1]);
            }
        }

        /*workbook operations*/
        InputStream ins = null;
        Workbook wb = null;
        try {
            ins=new FileInputStream(new File(fileName));
            wb = WorkbookFactory.create(ins);   // create workbook
            ins.close();
            Sheet sheet;
            if(sheetName.equals("")) {
                sheet = wb.getSheetAt(0);   // if only 2 arguments: get sheet(0)
            }
            else sheet = wb.getSheet(sheetName);
            trLength = sheet.getLastRowNum() + 1;   // row numbers
            Row row = sheet.getRow(0);
            tdLength = row.getLastCellNum();    // column numbers
            for(int i = 0; i < tdLength; i++) {
                Cell cell1 = row.getCell(i);
                fieldNames.add(cell1.toString());  // reading line 0 as name of fields
            }
            for(int i = 0; i < trLength; i++) {
                row = sheet.getRow(i);
                innerList = new ArrayList<>();
                for(int j = 0; j < tdLength; j++) {
                    if(row.getCell(j).toString().length() > maxCharLength) {
                        maxCharLength = row.getCell(j).toString().length(); // put strings into a line record
                    }
                    innerList.add(row.getCell(j).toString());   // put a line into a List<String>
                }
                outerList.add(innerList);
            }
            for(int i = 0; i < tdLength; i++) {
                Cell judgeCell = sheet.getRow(1).getCell(i);
                if(judgeCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat df = new DecimalFormat("#.#########");    // recovery integers
                    String cellText = df.format(sheet.getRow(1).getCell(i).getNumericCellValue());
                    if(isInteger(cellText)) {
                        typeList.add("integer");    // int->integer
                    }
                    else {
                        typeList.add("real");   // double->real
                    }
                }
                else {
                    typeList.add("char(" + maxCharLength + ")");    // others->char
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Connection conn;
        Statement stat;
        try {
            Class cl = Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbName;
            StringBuilder fieldsToAdd = new StringBuilder();
            StringBuilder insertSql = new StringBuilder();
            fieldsToAdd.append("ID integer primary key, "); // add PK
            for(int i = 0; i < tdLength - 1; i++) {
                // add field names and their types
                fieldsToAdd.append(fieldNames.get(i)).append(" ").append(typeList.get(i)).append(", ");
            }
            fieldsToAdd.append(fieldNames.get(tdLength - 1)).append(" ").append(typeList.get(tdLength - 1));
            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
            stat.executeUpdate("drop table if exists " + tableName + ";");  // drop duplicate table
            SQLStruct = "Create table " + tableName + "(" + fieldsToAdd.toString()+ ");";   // create table
            stat.executeUpdate(SQLStruct);
            insertSql.append("insert into ").append(tableName).append(" values(?,");
            for(int i = 0; i < tdLength - 1; i++) {
                insertSql.append("?,");
            }
            insertSql.append("?);");    // set number of values to bound
            PreparedStatement prs = conn.prepareStatement(insertSql.toString());
            for (int i = 1; i < outerList.size(); i++) {
                prs.setInt(1, i);
                for (int j = 0; j < innerList.size(); j++) {
                        prs.setString(j + 2, outerList.get(i).get(j));  // bound values
                }
                prs.addBatch(); // add an item
                conn.setAutoCommit(false);
                prs.executeBatch();
                conn.setAutoCommit(true);
            }
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        System.out.println(SQLStruct);  // print SQL struct
        System.out.println("Number of Rows: " + (trLength - 1));    // print number of rows
    }

    /*to judge if a number is integer(by string methods)*/
    private static boolean isInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /*to judge if a number is double(by string methods)*/
    private static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    /*drop suffix of a full filename*/
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