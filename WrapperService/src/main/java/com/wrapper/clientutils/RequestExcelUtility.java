package com.wrapper.clientutils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class RequestExcelUtility {
	Workbook workbook;	
	Sheet sheet = null; // sheet can be used as common for XSSF and HSSF WorkBook

	public Sheet setExcel(String path, String sheetname) {
		File src = new File(path);
		try {

			FileInputStream fis = new FileInputStream(src);
			workbook = WorkbookFactory.create(fis);
			sheet = workbook.getSheet(sheetname);
//			System.out.println("Sheet Name " + sheet.getSheetName());
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return sheet;
	}

	public void setFirstSheet(String path) {
		File src = new File(path);
		try {
			FileInputStream fis = new FileInputStream(src);
			workbook = WorkbookFactory.create(fis);
			sheet = workbook.getSheetAt(0);

//			System.out.println("Sheet Name " + ((org.apache.poi.ss.usermodel.Sheet) sheet).getSheetName());

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public int getRowCount() {
		int rowCount = 0;
		try {
			rowCount = ((org.apache.poi.ss.usermodel.Sheet) sheet).getLastRowNum() + 1;

		} catch (Exception e) {

		}
		return rowCount;
	}

	/*
	 * public String getdata(int rownum, int cellnum) { String data = null; try {
	 * DataFormatter formatter = new DataFormatter(); data = formatter
	 * .formatCellValue(((org.apache.poi.ss.usermodel.Sheet)
	 * sheet).getRow(rownum).getCell(cellnum));
	 * 
	 * return data; } catch (Exception e) { } return data; }
	 */

	
	public String getdata(int rownum, int cellnum) {
		String data = "";
		try {
			DataFormatter formatter = new DataFormatter();
			Cell cell = ((org.apache.poi.ss.usermodel.Sheet) sheet).getRow(rownum).getCell(cellnum);
			if (cell.getCellTypeEnum() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
				try
				{
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				data =formatter.formatCellValue(
						((org.apache.poi.ss.usermodel.Sheet) sheet).getRow(rownum).getCell(cellnum));
				
				}
				catch(Exception ex)
				{
					System.out.println(ex.toString());
				}
//				System.out.println(data);
				if (data.equals("")) {
					data = formatter.formatCellValue(
							((org.apache.poi.ss.usermodel.Sheet) sheet).getRow(rownum).getCell(cellnum));
				}
			}

			else {

				data = formatter
						.formatCellValue(((org.apache.poi.ss.usermodel.Sheet) sheet).getRow(rownum).getCell(cellnum));
//				System.out.println(data);
			}
			return data;
		} catch (Exception e) {
		}
		return data;
	}
	public int getcellCount() {
		int cellCount = 0;
		try {
			cellCount = ((org.apache.poi.ss.usermodel.Sheet) sheet).getRow(0).getPhysicalNumberOfCells();

		} catch (Exception e) {

		}
		return cellCount;

	}

}
