package com.erf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;

import org.xml.sax.*;

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
	
	
	private static void validateObjects(Element element, String filePath) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		NodeList list = element.getElementsByTagName("object");
		Element subElement;
		if(list == null)
			return;
		for (int i = 0; i < list.getLength(); i++)
		{
			subElement = (Element) list.item(i);
			validateObject(subElement, filePath);
		}
	}
	
	private static void validateObject(Element element, String filePath) throws ParserConfigurationException, SAXException, IOException, TransformerException
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
		
		if(canCheck)
		{
			if(prop == null)
			{
				addResourceMissingLine(element,forResourceLink ? "ResourceLink eksik" : "CaptionResource eksik", forResourceLink, filePath);
			}
			else
			{
				String value = prop.getAttribute("value");
				
				
				if(value == null || value.length() == 0)
				{
					addResourceMissingLine(element,"value eksik", forResourceLink, filePath);
				}
				else
				{
					String x[] = value.split("\\|");
					if(x == null || x.length != 2)
					{
						addResourceMissingLine(element,"value değeri yanlış girilmiş", forResourceLink, filePath);
					}
					else
					{
						try
						{	
							if(Integer.valueOf(x[0]) == 0)
							{
								addResourceMissingLine(element,"value değerleri 0 girilmiş", forResourceLink, filePath);
							}
						}
						catch (Exception e) 
						{
							System.out.println(e);
							addResourceMissingLine(element,"value değerlerini numeric girilmemiş", forResourceLink, filePath);
							
						}
					}
				}
			}			
		}


	}
	
	private static void addResourceMissingLine(Element element, String message, boolean forResourceLink, String filePath) throws IOException
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
			String text = "-";
			

			
			NodeList props = element.getElementsByTagName("prop");
			if(props != null)	
			{
				Element prop = findProp(props, forResourceLink ? "Id" : "_ControlID");
				
				if(prop != null)
				{
					controlID = " " +(forResourceLink ? "Id = " : "_ControlID = ") +prop.getAttribute("value");		
					text = controlID;			
				}
					

				
				prop = findProp(props, forResourceLink ? "Description" : "Caption");
				if(prop != null)
				{
					if(captionFilterList.contains(prop.getAttribute("value")))
						return;

				    DescriptionCaption = (forResourceLink ? "Description = " : "Caption = ") +prop.getAttribute("value");
					text += "\t " + DescriptionCaption;
				}
				
			}

			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-16"));
			
			String line = null;
			int lineCount = 1;
			int sheetCounter = 1;
			
			while((line = bufferedReader.readLine()) != null) 
			{
				
				//type="com.lbs.reporting.JLbsResourceLink" value="0|0"
				Pattern r = Pattern.compile("value=\"0[|]0");
				Matcher m = r.matcher(line);
				if(m.find()) 
				{
					//System.out.println("Found value: " + m.group(0) +" "+ lineCount);
					fw.write(fileName + "\t " + objectName + "\t " + text + "\t "+ message +"\t"+ lineCount +"\n");
					String[] data = new String[] {lineCount+"", fileName, objectName, controlID, DescriptionCaption, message};
					
					//if(sheetCounter == 1)
					//{
						ExcelManager.AppendData(data, workBook, workBook.getSheet("sheet1"));
					//}
					//else
					//{
					//	ExcelManager.AppendData(data, workBook, workBook.getSheet("sheet" + sheetCounter));
					//}
					lineCount++;
				}
				else 
				{
					lineCount++;
				}
				
				/*
				if(lineCount == 12500) 
				{
					sheetCounter++;
					ExcelManager.CreateExcelSheet(workBook, "sheet" + sheetCounter);
					ExcelManager.setColNames(new String[] {"Line Number", "File Name", "Type", "ID", "Description/Caption", "Msg"}, workBook, workBook.getSheet("sheet" + sheetCounter));	
					lineCount = 1;
				}
				*/
				
			}
			bufferedReader.close();
			
			
			

		}
		
		catch (IOException e)
		{
			System.out.println(e);
		}

	} 
	
	
	public static int lineNumber(NodeList nodeList)
	{
	  if (nodeList == null || nodeList.getLength() == 0)
	    return -1;

	  Node node = (Node) nodeList.item(0);
	  int nodeHandler = ((DTMNodeProxy) node).getDTMNodeNumber();
	  DTM dtm = ((DTMNodeProxy)node).getDTM();
	  SourceLocator locator = dtm.getSourceLocatorFor(nodeHandler);

	  if (locator != null)
	    return locator.getLineNumber();
	  else
	    return -1;
	}

	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, NullPointerException
	{
		
		workBook = ExcelManager.CreateExcelWorkbook();
		ExcelManager.CreateExcelSheet(workBook, "sheet1");
		ExcelManager.setColNames(new String[] {"Line Number", "File Name", "Type", "ID", "Description/Caption", "Msg"}, workBook, workBook.getSheet("sheet1"));
		
		
		HashSet<String> fileExceptionList = new HashSet<String>();
		
		FileReader fr = new FileReader("Exception.txt");
		BufferedReader br = new BufferedReader(fr);
				
		
       	String line ;

        while ((line=br.readLine()) != null)
        	 fileExceptionList.add(line);

        
        
		FileReader fr1 = new FileReader("a.txt");
		BufferedReader br1 = new BufferedReader(fr1);	
		
		
		
 
        while ((line=br1.readLine()) != null)
        	captionFilterList.add(line);

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
		
		
		for(int h = 0; h < 6; h++) 
		{
			workBook.getSheet("sheet1").autoSizeColumn(h);
		}
		
		//ExcelManager.markMatches("[a]", ExcelManager.genBasicCellStyle(IndexedColors.BLUE, HSSFPredefinedColors, workbook), columnIndex, workbook, worksheet)
		ExcelManager.SaveWorkbook(workBook, "Out.xls");
		
		
	}
	

}

