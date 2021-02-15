package com.wrapper.clientutils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


public class ResponseExcelUtility {

	Sheet sheet;
	Workbook wb;

	public void setExcel(String path, String sheetname) {
		File src = new File(path);
		try {

			FileInputStream fis = new FileInputStream(src);
			wb = WorkbookFactory.create(fis);
//			System.out.println(wb);
			sheet = wb.getSheet(sheetname);
//			System.out.println("Sheet Name " + sheet.getSheetName());

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
			// data = sheet.getRow(rownum).getCell(cellnum).getStringCellValue();
			// System.out.println(sheet.getSheetName());
			return data;
		} catch (Exception e) {
			// TODO: handle exception // currently NullPointerException will be thrown, to
			// modify
		}
		return data;
	}

}
