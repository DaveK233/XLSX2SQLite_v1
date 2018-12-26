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
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.sqlite.JDBC;

public class Main {
    public static void main(String[] args) {
        String fileName = args[1];
        String sheetName;
        String tableName;
        ArrayList<String> fieldNames = new ArrayList<>();
        ArrayList<String> innerList = null;
        ArrayList<ArrayList<String>> outerList = new ArrayList<>();

        /*set loading arguments*/
        sheetName = args[2];
        if(args[3] != null) {
            tableName = args[3];
        }
        else if(args[2] != null) {
            tableName = args[2];
        }
        else tableName = fileName;
        File file = new File(fileName);
        Workbook wb = null;   // excel file to read
        try {
            wb = Workbook.getWorkbook(file);
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
        Sheet sheet = null;    // sheet number

        /*read data from excel files*/
        if (wb != null) {
            sheet = wb.getSheet(sheetName);
            int stRows = sheet.getRows();   // number of non-empty rows
            int stColumns = sheet.getColumns();
            for(int i = 0; i < stColumns; i++) {
                fieldNames.add(sheet.getCell(i ,0).getContents());  // reading line 0 as name of fields
            }
            for(int i = 1; i < stRows; i++) {
                innerList = new ArrayList<>();
                for(int j = 0; j < stColumns; j++) {
                    innerList.add(sheet.getCell(j, i).getContents());   // put a line into a List<String>
                }
                outerList.add(innerList);
            }
            wb.close();
        }
        else {
            System.out.println("WorkBook is Empty or not existed.");
            return;
        }

    }
}
