package com.erf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
	
	
//	private static HashMap<String,Boolean> map = new HashMap<String,Boolean>();
	
	private static FileWriter fw = null;
	private static HashSet<String> captionFilterList = new HashSet<String>();
	
	
	
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
	
	
	private static void validateObjects(Element element) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		NodeList list = element.getElementsByTagName("object");
		Element subElement;
		if(list == null)
			return;
		for (int i = 0; i < list.getLength(); i++)
		{
			subElement = (Element) list.item(i);
			validateObject(subElement);
		}
	}
	
	private static void validateObject(Element element) throws ParserConfigurationException, SAXException, IOException, TransformerException
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
				addResourceMissingLine(element,forResourceLink ? "ResourceLink eksik" : "CaptionResource eksik", forResourceLink);
			}
			else
			{
				String value = prop.getAttribute("value");
				
				
				if(value == null || value.length() == 0)
				{
					addResourceMissingLine(element,"value eksik", forResourceLink);
				}
				else
				{
					String x[] = value.split("\\|");
					if(x == null || x.length != 2)
					{
						addResourceMissingLine(element,"value değeri yanlış girilmiş", forResourceLink);
					}
					else
					{
						try
						{	
							if(Integer.valueOf(x[0]) == 0)
							{
								addResourceMissingLine(element,"value değerleri 0 girilmiş", forResourceLink);
							}
						}
						catch (Exception e) 
						{
							addResourceMissingLine(element,"value değerlerini numeric girilmemiş", forResourceLink);
						}
					}
				}
			}			
		}

		
//		if(prop != null &&  prop.getParentNode() != null)
//			map.put(prop.getParentNode().getAttributes().getNamedItem("type").getNodeValue() + "---R", true);
//		prop = findProp(props, "CaptionResource");
//		if(prop != null &&  prop.getParentNode() != null)
//			map.put(prop.getParentNode().getAttributes().getNamedItem("type").getNodeValue() + "---C", true);

	}
	
	private static void addResourceMissingLine(Element element, String message, boolean forResourceLink) throws IOException
	{
		
		try
		{
			
			if(fw == null)
				fw = new FileWriter("D:\\out.txt");
				
			int i = element.getOwnerDocument().getDocumentURI().lastIndexOf("/");
			String fileName = element.getOwnerDocument().getDocumentURI().substring(i > 0 ? i + 1 : 0);
			String objectName = element.getAttribute("type");
			
			String text = "-";
			NodeList props = element.getElementsByTagName("prop");
			if(props != null)	
			{
				Element prop = findProp(props, forResourceLink ? "Id" : "_ControlID");
				if(prop != null)
					text = " " +(forResourceLink ? "Id = " : "_ControlID = ") +prop.getAttribute("value");									
			
//				prop = findProp(props, forResourceLink ? "Description" : "Caption");
//				if(prop != null)
//					text += "\t " +(forResourceLink ? "Description = " : "Caption = ") +prop.getAttribute("value");
				
				prop = findProp(props, forResourceLink ? "Description" : "Caption");
				if(prop != null)
				{
					if(captionFilterList.contains(prop.getAttribute("value")))
						return;
					text += "\t " +(forResourceLink ? "Description = " : "Caption = ") +prop.getAttribute("value");
				}
			}

			fw.write(fileName + "\t " + objectName + "\t " + text + "\t "+ message+"\n");
		
		}
		
		catch (IOException e)
		{
			System.out.println(e);
		}

	} 

	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, NullPointerException
	{
		
		HashSet<String> fileExceptionList = new HashSet<String>();
		
		FileReader fr = new FileReader("D:\\Exception.txt");
		BufferedReader br = new BufferedReader(fr);
				
		
       	String line ;

        while ((line=br.readLine()) != null)
        	 fileExceptionList.add(line);

        
        
		FileReader fr1 = new FileReader("D:\\a.txt");
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
//				    	if(list.contains(file.getName()))
//				    		continue;
				    	boolean flag = false;
				    	for(String f : fileExceptionList)
				    	{
				    		if(file.getName().matches(f))
				    		{
				    			flag = true;
				    			break;
				    		}	
				    	}
				    	if(!flag)
				    		continue;

				    	
//				    	System.out.println(file.getName());
				    	DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document document = documentBuilder.parse(file);
						validateObjects(document.getDocumentElement());
//						if (document.hasChildNodes()) 
//						{
////							printNodeList(document.getChildNodes());
//						}			    
				    }
				}		
			}
			if(fw != null)
				fw.close();
			
//			if(map != null)
//			{
//				for(String key : map.keySet())
//					System.out.println(key);
//			}

			
//			File[] listOfFiles = filterText.listFiles();
//
//			for (File file : listOfFiles) {
//			    if (file.isFile()) {
//			        System.out.println(file.getName());
//			    }
//			}
			
			
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}
	}
}

