package com.erf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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

	private static final String[] pathList = { "D:\\Projects\\jguar_GIT_Set\\jprod\\UnityServer\\WebContent\\Reporting",
			"D:\\Projects\\jguar_GIT_Set\\jaf\\LbsApplication.Server\\reporting\\",
			"D:\\Projects\\jguar_GIT_Set\\jaf\\LbsWorkflow\\reporting\\" };

	//	private static FileWriter fw = null;
	private static HashSet<String> captionFilterList = new HashSet<String>();
	private static HSSFWorkbook workBook;

	private static boolean canCheckTag(String type, boolean forResourceLink)
	{
		if (forResourceLink)
		{
			switch (type)
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
			switch (type)
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

	private static void validateObjects(Element element)
			throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		NodeList list = element.getElementsByTagName("object");
		Element subElement;
		if (list == null)
			return;
		for (int i = 0; i < list.getLength(); i++)
		{
			subElement = (Element) list.item(i);
			validateObject(subElement);
		}
	}

	private static void validateObject(Element element)
			throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		String type = element.getAttribute("type");

		Element prop = null;
		boolean canCheck = false;
		int forResourceLink = 0;
		if (canCheckTag(type, true))
		{
			forResourceLink = 1;
			canCheck = true;
			NodeList props = element.getElementsByTagName("prop");
			prop = findProp(props, "ResourceLink");
		}

		if (canCheckTag(type, false))
		{
			if(type.compareTo("com.lbs.customization.report.designer.JLbsReportSection") == 0)
				forResourceLink = 2;
			canCheck = true;
			NodeList props = element.getElementsByTagName("prop");
			prop = findProp(props, "CaptionResource");
		}

		if (canCheck)
		{
			if (prop == null)
			{
				addResourceMissingLine(element, forResourceLink == 1? "ResourceLink eksik": "CaptionResource eksik", forResourceLink);
			}
			else
			{
				String value = prop.getAttribute("value");

				if (value == null || value.length() == 0)
				{
					addResourceMissingLine(element, "value eksik", forResourceLink);
				}
				else
				{
					String x[] = value.split("\\|");
					if (x == null || x.length != 2)
					{
						addResourceMissingLine(element, "value değeri yanlış girilmiş", forResourceLink);
					}
					else
					{
						try
						{
							if (Integer.valueOf(x[0]) == 0)
							{
								addResourceMissingLine(element, "value değerleri 0 girilmiş", forResourceLink);
							}
						}
						catch (Exception e)
						{
							System.out.println(e);
							addResourceMissingLine(element, "value değerlerini numeric girilmemiş", forResourceLink);

						}
					}
				}
			}
		}
	}

	private static void addResourceMissingLine(Element element, String message, int forResourceLink) throws IOException
	{

		try
		{

			//			if(fw == null)
			//				fw = new FileWriter("D:\\out.txt");

			int i = element.getOwnerDocument().getDocumentURI().lastIndexOf("/");
			String fileName = element.getOwnerDocument().getDocumentURI().substring(i > 0 ? i + 1 : 0);
			String objectName = element.getAttribute("type");

			String captionResourceLink = "";
			String controlID = "";
			String descriptionCaption = "";
			String text = "-";

			NodeList props = element.getElementsByTagName("prop");
			if (props != null)
			{
				String idKeyword = "_ControlID";
				if(forResourceLink == 1)
					idKeyword = "Id";
				else if(forResourceLink == 2)
					idKeyword = "SectionID";
					
				Element prop = findProp(props, idKeyword);

				if (prop != null)
				{
					controlID = " " + idKeyword + " = " + prop.getAttribute("value");
					text = controlID;
				}

				prop = findProp(props, forResourceLink == 1 ? "ResourceLink" : "CaptionResource");
				if (prop != null)
				{
					if (captionFilterList.contains(prop.getAttribute("value")))
						return;

					captionResourceLink = (forResourceLink == 1
							? "ResourceLink = "
							: "CaptionResource = ") + prop.getAttribute("value");
					text += "\t " + captionResourceLink;
				}

				prop = findProp(props, forResourceLink == 1 ? "Description" : "Caption");
				if (prop != null)
				{
					if (captionFilterList.contains(prop.getAttribute("value")))
						return;

					descriptionCaption = (forResourceLink == 1
							? "Description = "
							: "Caption = ") + prop.getAttribute("value");
					text += "\t " + descriptionCaption;
				}

				//				fw.write(fileName + "\t " + objectName + "\t " + text + "\t "+ message +"\t");

				String[] data = new String[] { fileName, objectName, controlID, captionResourceLink, descriptionCaption, message };
				ExcelManager.AppendData(data, workBook, workBook.getSheet("sheet"));
			}
			
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		
		
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, NullPointerException
	{
		if(JOptionPane.showConfirmDialog(new JFrame(),  "C:\\exception.txt = dosya filtresi \nC:\\resultFilter.txt = sonuç filtresi \ndosyalarını kullanılarak işlem başlatıldı.", "Excel Kayıt İşlemi", JOptionPane.YES_NO_OPTION)  == JOptionPane.YES_OPTION)
		{
			workBook = ExcelManager.CreateExcelWorkbook();
			ExcelManager.CreateExcelSheet(workBook, "sheet");
			ExcelManager.setColNames(new String[] { "File Name", "Type", "ID", "CaptionResourceLink", "Description/Caption", "Msg" },
					workBook, workBook.getSheet("sheet"));
			
			String line;
	
			HashSet<String> fileExceptionList = new HashSet<String>();
	
			try 
			{
				FileReader fr = new FileReader("C:\\exception.txt");
				if(fr != null)
				{
					BufferedReader br = new BufferedReader(fr);
					
					
					while ((line = br.readLine()) != null)
						fileExceptionList.add(line);
					
					br.close();
				}			
			}
			catch(Exception e)
			{
				
			}
			
			try 
			{
				FileReader fr1 = new FileReader("C:\\resultFilter.txt");
				if(fr1 != null)
				{
					BufferedReader br1 = new BufferedReader(fr1);
					
					while ((line = br1.readLine()) != null)
						captionFilterList.add(line);
					
					br1.close();			
				}
			}
			catch(Exception e)
			{
				
			}
			
			try
			{
				for (int i = 0; i < pathList.length; i++)
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
							for (String f : fileExceptionList)
							{
								if (file.getName().matches(f))
								{
									flag = true;
									break;
								}
							}
							if (flag)
								continue;
	
							DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							Document document = documentBuilder.parse(file);
							validateObjects(document.getDocumentElement());
	
						}
					}
				}
				//			if(fw != null)
				//				fw.close();	
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
	
			for (int h = 0; h < 6; h++)
			{
				workBook.getSheet("sheet").autoSizeColumn(h);
			}
	
			
			
			//ExcelManager.markMatches("[a]", ExcelManager.genBasicCellStyle(IndexedColors.BLUE, HSSFPredefinedColors, workbook), columnIndex, workbook, worksheet)
	
			String excelFileName = "Out_"+Calendar.getInstance().getTimeInMillis()+".xls";
			ExcelManager.SaveWorkBook(workBook, excelFileName);
			JOptionPane.showMessageDialog(new JFrame(), excelFileName+" dosyası oluşturuldu.\nİşleminiz tamamlanmıştır.", "Excel Kayıt İşlemi",JOptionPane.ERROR_MESSAGE);
		}	
	}
}