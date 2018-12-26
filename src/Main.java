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
        if (wb != null) {
            sheet = wb.getSheet(sheetName);
            int stRows = sheet.getRows();   // number of non-empty rows
            int stColumns = sheet.getColumns();


            wb.close();
        }
        else {
            System.out.println("WorkBook is Empty or not existed.");
        }
    }
}
