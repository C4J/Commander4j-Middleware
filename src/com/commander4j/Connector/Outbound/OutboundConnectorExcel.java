package com.commander4j.Connector.Outbound;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorExcel extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorExcel.class));
	private JPropQuickAccess qa = new JPropQuickAccess();

	public OutboundConnectorExcel(OutboundInterface inter)
	{
		super(Connector_Excel, inter);
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);

		String fullPath = path + File.separator + filename;


		fullPath = fullPath + "." + getOutboundInterface().getOutputFileExtension().toLowerCase();

		String tempFilename = fullPath + ".tmp";
		String finalFilename = fullPath;

		FileUtils.deleteQuietly(new File(tempFilename));

		logger.debug("connectorSave [" + fullPath + "]");

		JXMLDocument document = new JXMLDocument();
		document.setDocument(getData());

		int numberOfSheets = 1;
		String sheetName = "";
		int numberOfColumns = 1;
		int numberOfRows = 1;
		int currentSheet = 1;
		int currentColumn = 1;
		int currentRow = 1;
		String currentColumnHeading = "";
		String currentColumnType = "";
		String currentData = "";
		HashMap<Integer,String> columnType = new HashMap<Integer, String>();
		String overrideFilename = "";

		try
		{

			// WORKBOOK //

			Workbook wb = new XSSFWorkbook();

			System.out.println(document.documentToString(getData()));

			numberOfSheets = Integer.valueOf(util.replaceNullStringwithBlank(document.findXPath("/excel/@sheets").trim()));
			overrideFilename = util.replaceNullStringwithBlank(document.findXPath("/excel/@filename").trim());
			
			if (overrideFilename.equals("")==false)
			{
				finalFilename = path + File.separator + overrideFilename + "." + getOutboundInterface().getOutputFileExtension().toLowerCase();
				tempFilename = path + File.separator + overrideFilename + "." + getOutboundInterface().getOutputFileExtension().toLowerCase() + ".tmp";
			}

			logger.debug("numberOfSheets=" + numberOfSheets);

			for (currentSheet = 1; currentSheet <= numberOfSheets; currentSheet++)
			{

				sheetName = util.replaceNullStringwithBlank(document.findXPath("/excel/sheet[@id='" + String.valueOf(currentSheet) + "']/@name").trim());
				numberOfColumns = Integer.valueOf(util.replaceNullStringwithBlank(document.findXPath("/excel/sheet[@id='" + String.valueOf(currentSheet) + "']/@cols").trim()));
				numberOfRows = Integer.valueOf(util.replaceNullStringwithBlank(document.findXPath("/excel/sheet[@id='" + String.valueOf(currentSheet) + "']/@rows").trim()));

				// SHEET //

				Sheet sheet = wb.createSheet(sheetName);
				sheet.setDisplayGridlines(true);
				sheet.setPrintGridlines(false);
				sheet.setFitToPage(true);
				sheet.setHorizontallyCenter(true);

				// PRINT FORMAT //

				PrintSetup printSetup = sheet.getPrintSetup();
				printSetup.setLandscape(true);
				sheet.setAutobreaks(true);
				printSetup.setFitHeight((short) 1);
				printSetup.setFitWidth((short) 1);

				// BUILD STYLES //

				Map<String, CellStyle> styles = createStyles(wb);

				Row headerRow = sheet.createRow(0);
				headerRow.setHeightInPoints(12.75f);

				logger.debug("currentSheet=" + currentSheet + " sheetName=" + sheetName + " numberOfColumns=" + numberOfColumns + " numberOfRows=" + numberOfRows);

				// HEADER //
				
				columnType.clear();

				for (currentColumn = 1; currentColumn <= numberOfColumns; currentColumn++)
				{
					currentColumnHeading = util.replaceNullStringwithBlank(document.findXPath("/excel/sheet[@id='" + String.valueOf(currentSheet) + "']/header/col[@id='" + String.valueOf(currentColumn) + "']").trim());

					Cell cell = headerRow.createCell(currentColumn - 1);
					cell.setCellValue(currentColumnHeading);
					cell.setCellStyle(styles.get("header"));

					currentColumnType = util.replaceNullStringwithBlank(document.findXPath("/excel/sheet[@id='" + String.valueOf(currentSheet) + "']/header/col[@id='" + String.valueOf(currentColumn) + "']/@type").trim());
					
					columnType.put(currentColumn, currentColumnType);

					logger.debug("currentSheet=" + currentSheet + " sheetName=" + sheetName + " currentColumn=" + currentColumn + " currentColumnHeading=" + currentColumnHeading);

				}

				// ROWS //

				for (currentRow = 1; currentRow <= numberOfRows; currentRow++)
				{
					Row row = sheet.createRow(currentRow);

					// COLUMNS FOR ROW //

					for (currentColumn = 1; currentColumn <= numberOfColumns; currentColumn++)
					{
						currentData = util.replaceNullStringwithBlank(document.findXPath("/excel/sheet[@id='" + String.valueOf(currentSheet) + "']/row[@id='" + String.valueOf(currentRow) + "']/col[@id='" + String.valueOf(currentColumn) + "']").trim());

						Cell celldata = row.createCell(currentColumn - 1);
						
						switch (columnType.get(currentColumn).toLowerCase())
						{
						case "string":
							celldata.setCellStyle(styles.get("cell_normal"));
							celldata.setCellValue(currentData);
							break;
						case "double":
							celldata.setCellStyle(styles.get("cell_double"));
							celldata.setCellValue(Double.valueOf(currentData));
							break;
						case "isodate":
							celldata.setCellStyle(styles.get("cell_iso_date"));
							celldata.setCellValue(util.getTimeStampFromISOString(currentData));
							break;
						default:
							celldata.setCellStyle(styles.get("cell_normal"));
							celldata.setCellValue(currentData);
							break;
						}

						logger.debug("currentColumn=" + currentColumn + " currentRow=" + currentRow + " currentData=" + currentData);
					}

				}

				// AUTO SIZE COLUMNS //

				for (currentColumn = 1; currentColumn <= numberOfColumns; currentColumn++)
				{
					wb.getSheetAt(currentSheet - 1).autoSizeColumn(currentColumn - 1);
				}

			}

			// WRITE TO TEMP FILE //

			FileOutputStream out = new FileOutputStream(new File(tempFilename));
			wb.write(out);
			out.close();
			wb.close();

			// MAKE SURE FINAL FILENAME DOES NOT EXIST //

			FileUtils.deleteQuietly(new File(finalFilename));

			// RENAME TEMP FILE TO FINAL FILENAME //

			FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

			result = true;
			
			System.out.println(qa.getString(Common.props, qa.getMapInputURL(getOutboundInterface().getMapId(), getOutboundInterface().getId()) + "//description"));
		}
		catch (Exception ex)
		{
			logger.error("Message failed to process.");

			ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Host Name", util.getClientName()));
			ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
			ept.addRow(new ExceptionMsg("Stage","connectorSave"));
			ept.addRow(new ExceptionMsg("Map Id",getOutboundInterface().getMap().getId()));
			ept.addRow(new ExceptionMsg("Map Description",qa.getString(Common.props, qa.getMapURL(getOutboundInterface().getMap().getId())+"//description")));
			ept.addRow(new ExceptionMsg("Connector Id",getOutboundInterface().getId()));
			ept.addRow(new ExceptionMsg("Connector Description",qa.getString(Common.props, qa.getMapInputURL(getOutboundInterface().getMapId(), getOutboundInterface().getId()) + "//description")));
			ept.addRow(new ExceptionMsg("Type",getType()));
			ept.addRow(new ExceptionMsg("Source",fullPath));
			if (getOutboundInterface().getXSLTFilename().equals("")==false)
			{
				ept.addRow(new ExceptionMsg("XSLT Path",getOutboundInterface().getXSLTPath()));
				ept.addRow(new ExceptionMsg("XSLT File",getOutboundInterface().getXSLTFilename()));
			}
			ept.addRow(new ExceptionMsg("Prefix",prefix));
			ept.addRow(new ExceptionMsg("is83GUIDFilenameReqd",String.valueOf(getOutboundInterface().is83GUIDFilenameReqd())));
			ept.addRow(new ExceptionMsg("File Extension",getOutboundInterface().getOutputFileExtension().toLowerCase()));
			ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
			
			Common.emailqueue.addToQueue(getOutboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");
			

		}
		finally
		{
			try
			{

			}
			catch (Exception e)
			{
				// Suppress Error
			}

			document = null;
			fullPath = null;
			tempFilename = null;
			finalFilename = null;
		}

		return result;
	}

	private Map<String, CellStyle> createStyles(Workbook wb)
	{
		Map<String, CellStyle> styles = new HashMap<>();
		DataFormat df = wb.createDataFormat();

		CellStyle style;
		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(headerFont);
		styles.put("header", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(headerFont);
		style.setDataFormat(df.getFormat("d-mmm"));
		styles.put("header_date", style);

		Font font1 = wb.createFont();
		font1.setBold(true);
		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setFont(font1);
		styles.put("cell_b", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(font1);
		styles.put("cell_b_centered", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setFont(font1);
		style.setDataFormat(df.getFormat("d-mmm"));
		styles.put("cell_b_date", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setFont(font1);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setDataFormat(df.getFormat("d-mmm"));
		styles.put("cell_g", style);

		Font font2 = wb.createFont();
		font2.setColor(IndexedColors.BLUE.getIndex());
		font2.setBold(true);
		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setFont(font2);
		styles.put("cell_bb", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setFont(font1);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setDataFormat(df.getFormat("d-mmm"));
		styles.put("cell_bg", style);

		Font font3 = wb.createFont();
		font3.setFontHeightInPoints((short) 14);
		font3.setColor(IndexedColors.DARK_BLUE.getIndex());
		font3.setBold(true);
		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setFont(font3);
		style.setWrapText(true);
		styles.put("cell_h", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setWrapText(true);
		styles.put("cell_normal", style);
		
		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setWrapText(true);
		styles.put("cell_double", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setWrapText(true);
		styles.put("cell_normal_centered", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setWrapText(true);
		style.setDataFormat(df.getFormat("d-mmm"));
		styles.put("cell_normal_date", style);
		
		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setWrapText(true);
		style.setDataFormat(df.getFormat("yyyy-mm-dd HH:MM:SS"));
		styles.put("cell_iso_date", style);

		style = createBorderedStyle(wb);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setIndention((short) 1);
		style.setWrapText(true);
		styles.put("cell_indented", style);

		style = createBorderedStyle(wb);
		style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("cell_blue", style);

		return styles;
	}

	private CellStyle createBorderedStyle(Workbook wb)
	{
		BorderStyle thin = BorderStyle.THIN;
		short black = IndexedColors.BLACK.getIndex();

		CellStyle style = wb.createCellStyle();
		style.setBorderRight(thin);
		style.setRightBorderColor(black);
		style.setBorderBottom(thin);
		style.setBottomBorderColor(black);
		style.setBorderLeft(thin);
		style.setLeftBorderColor(black);
		style.setBorderTop(thin);
		style.setTopBorderColor(black);
		return style;
	}

}
