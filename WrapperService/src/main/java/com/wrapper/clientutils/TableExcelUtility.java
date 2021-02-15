package com.wrapper.clientutils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TableExcelUtility {
	Workbook wb = null;
	Sheet sheet;

	public void setExcel(String path, String sheetname) {
		File src = new File(path);
		try {

			FileInputStream fis = new FileInputStream(src);
			wb = WorkbookFactory.create(fis);
//			System.out.println(wb);
			sheet = wb.getSheet(sheetname);
		

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public int getRowCount() {
		int rowCount = 0;
		try {
			rowCount = sheet.getLastRowNum() + 1;

		} catch (Exception e) {

		}
		return rowCount;
	}

	public String getdata(int rownum, int cellnum) {
		String data = null;
		try {
			DataFormatter formatter = new DataFormatter();
			data = formatter.formatCellValue(sheet.getRow(rownum).getCell(cellnum));
			return data;
		} catch (Exception e) {

		}
		return data;
	}

	public void setFirstSheet(String path) {
		File src = new File(path);
		try {
			FileInputStream fis = new FileInputStream(src);
			wb = WorkbookFactory.create(fis);
			sheet = wb.getSheetAt(0);

			

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}
