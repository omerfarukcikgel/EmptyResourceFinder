package com.erf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * <p>Title: EmptyResourceFinder.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: LBS</p>
 * <p>Created on: 12 Tem 2018
 * @author OmerFaruk.Cikgel
 * @version 1.0
 */



public class EmptyResourceFinder
{

	private static final String[] pathList = {"D:\\Projects\\jguar_GIT_Set\\jprod\\UnityServer\\WebContent\\Reporting",
									   "D:\\Projects\\jguar_GIT_Set\\jaf\\LbsApplication.Server\\reporting\\",
									   "D:\\Projects\\jguar_GIT_Set\\jaf\\LbsWorkflow\\reporting\\"};
	

	
	private static FileWriter fw = null;
	private static HashSet<String> captionFilterList = new HashSet<String>();
	private static HSSFWorkbook workBook;

	
	
	
	private static boolean canCheckTag(String type, boolean forResourceLink)
	{
		if(forResourceLink)
		{
			switch(type)
			{
				case "com.lbs.filter.JLbsFilterNumeric":
				case "com.lbs.filter.JLbsFilterNumericRange":
				case "com.lbs.filter.JLbsFilterString":
				case "com.lbs.filter.JLbsFilterTime":
				case "com.lbs.filter.JLbsFilterSelection":
				case "com.lbs.reporting.JLbsReportVariable":
				case "com.lbs.reporting.JLbsReportCalcVariable":
				case "com.lbs.filter.JLbsFilterStringRange":
				case "com.lbs.filter.JLbsFilterDate":
				case "com.lbs.filter.JLbsFilterGroupSelection":
				case "com.lbs.filter.JLbsFilterDateRange":
					return true;
			}			
		}
		else
		{
			switch(type)
			{
				case "com.lbs.customization.report.controls.JLbsRCMemo":
				case "com.lbs.customization.report.controls.JLbsRCHorizontalLine":
				case "com.lbs.customization.report.controls.JLbsRCCaptionedControl":
				case "com.lbs.customization.report.controls.JLbsRCRectangle":
				case "com.lbs.customization.report.controls.JLbsRCBarcode":
				case "com.lbs.customization.report.designer.JLbsReportSection":
				case "com.lbs.customization.report.controls.JLbsRCLine":
				case "com.lbs.customization.report.controls.JLbsRCField":
				case "com.lbs.customization.report.controls.JLbsRCImage":
				case "com.lbs.customization.report.controls.JLbsRCVerticallLine":
				case "com.lbs.customization.report.designer.db.JLbsDBReportSection":
					return true;
			}
		}
		
		return false;
	}
	
	
	
	public static boolean equals(String a, String b)
	{
		if (a == null)
			a = "";
		if (b == null)
			b = "";
		return a.equals(b);
	}
	
	private static Element findProp(NodeList list, String name)
	{
		for (int i = 0; i < list.getLength(); i++)
		{
			Element element = (Element) list.item(i);
			String attr = element.getAttribute("name");

			if (equals(attr, name))
				return element;
		}
		return null;
	}
	
	static Duration total = Duration.ZERO;
	static int filenum = 0;
	private static void validateObjects(Element element, String filePath) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		NodeList list = element.getElementsByTagName("object");
		Element subElement;
		if(list == null)
		{
			return;
		}
		
		
		//TODO: LORPItemPurchaseSalesExtract Hatalı dosya!!!!!!!!!! 
		if(filePath.equals("D:\\Projects\\jguar_GIT_Set\\jprod\\UnityServer\\WebContent\\Reporting\\LORPItemPurchaseSalesExtract.jrf")) 
		{
			return;
		}
		
		
		//Reader here
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-16"));
		int lineNumber = 1;
	
		filenum++;
		
		Instant sarts = Instant.now();
		for (int i = 0; i < list.getLength(); i++)
		{
			subElement = (Element) list.item(i);
			lineNumber = validateObject(subElement, bufferedReader, lineNumber);
		}
		Instant ends = Instant.now();
		Duration dur = Duration.between(sarts, ends);
		total = total.plus(dur);
		System.out.println("File #" + filenum + " | Time: " + dur + " | for: " + filePath + " | total time elapsed: " + total);
		
		bufferedReader.close();
	}
	
	
	
	private static int validateObject(Element element, BufferedReader bufferedReader, int linec) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		String type = element.getAttribute("type");
		
		Element prop = null;
		boolean canCheck = false;
		boolean forResourceLink = false;
		if(canCheckTag(type, true))
		{
			forResourceLink = true;
			canCheck = true;
			NodeList props = element.getElementsByTagName("prop");
			prop = findProp(props, "ResourceLink");
		}
		
		if(canCheckTag(type, false))
		{
			canCheck = true;
			NodeList props = element.getElementsByTagName("prop");
			prop = findProp(props, "CaptionResource");
		}
		
		
		int linesRead = 0;
		
		if(canCheck)
		{
			if(prop == null)
			{
				linesRead = addResourceMissingLine(element,forResourceLink ? "ResourceLink eksik" : "CaptionResource eksik", forResourceLink, bufferedReader, linec);
			}
			else
			{
				String value = prop.getAttribute("value");
				
				
				if(value == null || value.length() == 0)
				{
					linesRead = addResourceMissingLine(element,"value eksik", forResourceLink, bufferedReader, linec);
				}
				else
				{
					String x[] = value.split("\\|");
					if(x == null || x.length != 2)
					{
						linesRead = addResourceMissingLine(element,"value değeri yanlış girilmiş", forResourceLink, bufferedReader, linec);
					}
					else
					{
						try
						{	
							if(Integer.valueOf(x[0]) == 0)
							{
								linesRead = addResourceMissingLine(element,"value değerleri 0 girilmiş", forResourceLink, bufferedReader, linec);
							}
						}
						catch (Exception e) 
						{
							System.out.println(e);
							linesRead = addResourceMissingLine(element,"value değerlerini numeric girilmemiş", forResourceLink, bufferedReader, linec);
							
						}
					}
				}
			}
			return linesRead;
		}
		else 
		{
			return linesRead;
		}


	}
	
	//static ArrayList<String> processedFiles = new ArrayList<String>();
	private static int addResourceMissingLine(Element element, String message, boolean forResourceLink, BufferedReader bufferedReader, int linec) throws IOException
	{
		
		try
		{
			
			if(fw == null)
				fw = new FileWriter("D:\\out.txt");
				
			int i = element.getOwnerDocument().getDocumentURI().lastIndexOf("/");
			String fileName = element.getOwnerDocument().getDocumentURI().substring(i > 0 ? i + 1 : 0);
			String objectName = element.getAttribute("type");
			
			String controlID = "";
			String DescriptionCaption = "";
			String captionResourceLink = "";
			//String text = "-";
			

			
			NodeList props = element.getElementsByTagName("prop");
			if(props != null)	
			{
				Element prop = findProp(props, forResourceLink ? "Id" : "_ControlID");
				
				if(prop != null)
				{
					controlID = " " +(forResourceLink ? "Id = " : "_ControlID = ") +prop.getAttribute("value");		
					//text = controlID;			
				}
					
				
				prop = findProp(props, forResourceLink ? "ResourceLink" : "Caption");
                if(prop != null)
                {
                      if(captionFilterList.contains(prop.getAttribute("value")))
                             return 0;

                      captionResourceLink = (forResourceLink ? "ResourceLink = " : "CaptionResource = ") +prop.getAttribute("value");
                      //text += "\t " + captionResourceLink;
               } 


				prop = findProp(props, forResourceLink ? "Description" : "Caption");
				if(prop != null)
				{
					if(captionFilterList.contains(prop.getAttribute("value")))
						return 0;

				    DescriptionCaption = (forResourceLink ? "Description = " : "Caption = ") +prop.getAttribute("value");
					//text += "\t " + DescriptionCaption;
				}
				
			}
			
			String line = null;
			int lineCount = linec;

			
			//Count lines if the line is found return the linecounter
			while((line = bufferedReader.readLine()) != null) 
			{
			
				//Pattern r = Pattern.compile("(?=.*"+ objectName + ")" +"(?=.*(value=\"0[|]0))"); // + "(?=.*(type=\"" + objectName + "))"
				//(?=.*(value=\"0[|]0\"))(?=.*(" + "name=\"" + captionResourceLink +  "\"" + "))
				Pattern r = Pattern.compile("(?=.*(value=\"0[|]0))");
				Matcher m = r.matcher(line);
				if(m.find()) 
				{

					//fw.write(fileName + "\t " + objectName + "\t " + text + "\t "+ message +"\t"+ lineCount +"\n");
					String[] data = new String[] {(lineCount)+"", fileName, objectName, controlID, captionResourceLink, DescriptionCaption, message};
					ExcelManager.AppendData(data, workBook, workBook.getSheet("sheet1"));
					
					return lineCount + 1;
				}
				else 
				{
					lineCount++;
				}

			}
			
			
			return 0;	//TODO: Throw exception here, no item found with regular expression given
		}
		
		catch (IOException e)
		{
			System.out.println(e);
		}
		return 0;

	} 

	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, NullPointerException
	{
		
		workBook = ExcelManager.CreateExcelWorkbook();
		ExcelManager.CreateExcelSheet(workBook, "sheet1");
		ExcelManager.setColNames(new String[] {"Line Number", "File Name", "Type", "ID", "CaptionResourceLink", "Description/Caption", "Msg"}, workBook, workBook.getSheet("sheet1"));
		
		
		HashSet<String> fileExceptionList = new HashSet<String>();
		
		FileReader fr = new FileReader("D:\\Exception.txt");
		BufferedReader br = new BufferedReader(fr);
				
		
       	String line ;

        while ((line=br.readLine()) != null)
        	 fileExceptionList.add(line);
        br.close();
        
        
		FileReader fr1 = new FileReader("D:\\a.txt");
		BufferedReader br1 = new BufferedReader(fr1);	
		
		
		
 
        while ((line=br1.readLine()) != null)
        	captionFilterList.add(line);
        br1.close();

		try 
		{
		
			for(int i = 0; i < pathList.length; i++)
			{
				File folder = new File(pathList[i]);
				FilenameFilter filter = new FilenameFilter()
				{
					
					@Override
					public boolean accept(File dir, String name)
					{
						return name != null && name.contains(".jrf");
					}
					
				};
				
				
				File[] listOfFiles = folder.listFiles(filter);
				for (File file : listOfFiles) 
				{					
					
				    if (file.isFile()) 
				    {
				    	boolean flag = false;
				    	for(String f : fileExceptionList)
				    	{
				    		if(file.getName().matches(f))
				    		{
				    			flag = true;
				    			break;
				    		}	
				    	}
				    	if(flag)
				    		continue;
				    	
				    	String filePath = file.getAbsolutePath();
				    	DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document document = documentBuilder.parse(file);
						validateObjects(document.getDocumentElement(), filePath);
						
							    
				    }
				}		
			}
			if(fw != null)
				fw.close();

			
			
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}
		
		
		for(int h = 0; h < 7; h++) 
		{
			workBook.getSheet("sheet1").autoSizeColumn(h);
		}
		
		//ExcelManager.markMatches("[a]", ExcelManager.genBasicCellStyle(IndexedColors.BLUE, HSSFPredefinedColors, workbook), columnIndex, workbook, worksheet)
		
		
		
		
		ExcelManager.SaveWorkbook(workBook, "Out.xls");
		System.out.println("Bitti, Excel @ " + System.getenv("SystemDrive") + "\\Out.xls");
		
	}
	

}

