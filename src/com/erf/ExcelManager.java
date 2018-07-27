package com.erf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

public class ExcelManager {
	
	/**
	 * Creates a workbook and sets encoding.
	 * @return HSSFWorkbook
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static HSSFWorkbook CreateExcelWorkbook() throws FileNotFoundException, IOException 
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
		
		//Set encoding 
		HSSFFont wbFont;
		wbFont=workbook.createFont();
		wbFont.setCharSet(HSSFFont.ANSI_CHARSET);
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(wbFont);

		return workbook;
	}
	
	/**
	 * Opens an existing excel file
	 * @param dir
	 * @return Workbook at given dir
	 * @throws IOException
	 */
	public static HSSFWorkbook OpenExcelWorkbook(String dir) throws IOException 
	{
		FileInputStream fsIP = new FileInputStream(new File(dir));
		HSSFWorkbook wb = new HSSFWorkbook(fsIP);

		return wb;
	}
	
	/**
	 * Creates a new sheet and returns it
	 * @param workbook
	 * @param sheetName
	 * @return
	 */
	public static HSSFSheet CreateExcelSheet(HSSFWorkbook workbook, String sheetName) 
	{
		HSSFSheet sheet = workbook.createSheet(sheetName);
		return sheet;
	}
	
	/**
	 * Appends given string array to a row
	 * @param Data
	 * @param workbook
	 * @param worksheet
	 */
	public static void AppendData(String[] Data, HSSFWorkbook workbook, HSSFSheet worksheet) 
	{
		//CreationHelper createHelper = workbook.getCreationHelper();
		HSSFRow currentRow = worksheet.createRow(worksheet.getPhysicalNumberOfRows());
		for(int i = 0 ; i < Data.length; i++) 
		{
			HSSFCell c = currentRow.createCell(i, CellType.STRING);
			c.setCellValue(Data[i]);	
		}	
	}
	
	/**
	 * Appends given string array to a row with a cell style
	 * @param Data
	 * @param workbook
	 * @param worksheet
	 * @param style
	 */
	public static void AppendData(String[] Data, HSSFWorkbook workbook, HSSFSheet worksheet, CellStyle style) 
	{
		//CreationHelper createHelper = workbook.getCreationHelper();
		HSSFRow currentRow = worksheet.createRow(worksheet.getPhysicalNumberOfRows());
		for(int i = 0 ; i < Data.length; i++) 
		{
			HSSFCell c = currentRow.createCell(i);
			c.setCellStyle(style);
			c.setCellValue(Data[i]);	
			c.setCellStyle(style);
			
			int colIndex = c.getColumnIndex();
			worksheet.autoSizeColumn(colIndex);
		}	
	}
	
	/**
	 * Saves workbook to saveDir directory
	 * @param workbook
	 * @param saveDir
	 */
	public static boolean SaveWorkbook(HSSFWorkbook workbook, String saveDir) 
	{
		try (FileOutputStream fos = new FileOutputStream(new File(saveDir))) {
            workbook.write(fos);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
	}
	
	/**
	 * Appends a row with a special style
	 * @param ColNames
	 * @param workbook
	 * @param worksheet
	 */
	public static void setColNames( String[] ColNames, HSSFWorkbook workbook, HSSFSheet worksheet) 
	{
		CellStyle cs = workbook.createCellStyle();
		cs.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex()); 
		cs.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        font.setBold(true);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short)18);
        cs.setFont(font);
		
		AppendData(ColNames, workbook, worksheet, cs);
	}
	
	
	/**
	 * Attempts to regex and find matches in a given column and marks them with the specified style
	 * @param regexPattern
	 * @param style
	 * @param columnIndex
	 * @param workbook
	 * @param worksheet
	 * @return
	 */
	public static int markMatches(String regexPattern, CellStyle style, int columnIndex, HSSFWorkbook workbook, HSSFSheet worksheet) 
	{
		
		Pattern regPattern = Pattern.compile(regexPattern);	
		int changeCount = 0;
		
		Iterator rowIterator = worksheet.rowIterator();
		
		while(rowIterator.hasNext()) 
		{
			Iterator cellIterator = ((HSSFRow)(rowIterator.next())).cellIterator();
			while(cellIterator.hasNext()) 
			{
				HSSFCell currentCell = ((HSSFCell)cellIterator.next());
				
				if(currentCell.getColumnIndex() == columnIndex) //If this cell is at the given column index
				{
					Matcher m = regPattern.matcher(currentCell.getStringCellValue());	//Attempt to match cell value with given regex value
					if(m.find()) 
					{
						currentCell.setCellStyle(style);
						changeCount++;
					}
				}
			}
		}
		return changeCount;
	}
	
	/**
	 * Appends a list, every element is written to a different row.
	 * @param dataList
	 * @param workbook
	 * @param worksheet
	 */
	public static void AppendList(List<String[]> dataList, HSSFWorkbook workbook, HSSFSheet worksheet) 
	{
		for(String[] arr : dataList) 
		{
			AppendData(arr, workbook, worksheet);
		}
	}
	
	/**
	 * Creates a basic style for cells and returns it
	 * @param backGround
	 * @param text
	 * @param workbook
	 * @return cellstyle
	 */
	public CellStyle genBasicCellStyle(IndexedColors backGround, HSSFColorPredefined text, HSSFWorkbook workbook) 
	{
		CellStyle cs = workbook.createCellStyle();
		cs.setFillForegroundColor(backGround.getIndex()); 
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		Font font = workbook.createFont();
        font.setColor(text.getIndex());
        cs.setFont(font);
        
        return cs;
		
	}
	
	public void testfunc() throws FileNotFoundException, IOException 
	{
		 HSSFWorkbook workbook =  CreateExcelWorkbook();
		 HSSFSheet sheet = CreateExcelSheet(workbook, "sheet1test");
		 
		 setColNames(new String[]{"colname1", "colname2", "colnamek", "oof","fffff"}, workbook, sheet);
		 AppendData(new String[]{"this", "is", "a" , "test", "!"}, workbook, sheet);
		 AppendData(new String[] {"test", "d a t a", "t e s t", "data"}, workbook, sheet);
		 AppendData(new String[] {"test", "d a t a", "I am in a lot of pain", "data"}, workbook, sheet);
		 
		
		 
		 markMatches("\\s", genBasicCellStyle(IndexedColors.RED, HSSFColorPredefined.PLUM, workbook), 2, workbook, sheet);
		 
		 SaveWorkbook(workbook, "D:\\eXCEL\\testSheet.xls"); 
		 
		 HSSFWorkbook IAMBACK = OpenExcelWorkbook("D:\\eXCEL\\testSheet.xls");
		 AppendData(new String[] {"oof", "d a t a", "Please", "data"}, IAMBACK, IAMBACK.getSheet("sheet1test"));
		 
		 markMatches("oof", genBasicCellStyle(IndexedColors.BRIGHT_GREEN, HSSFColorPredefined.YELLOW, IAMBACK), 0, IAMBACK, IAMBACK.getSheet("sheet1test"));
		 SaveWorkbook(IAMBACK, "D:\\eXCEL\\IAMBACK.xls"); 
	 }

	
}
