package com.ybs.pullapidata.forecastgrib;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ForeCast {
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException 
	{
		String BaseDate;
		String BaseTime;
		long time = System.currentTimeMillis();
		SimpleDateFormat _date = new SimpleDateFormat("YYYYMMdd");
		SimpleDateFormat _time = new SimpleDateFormat("HHmm");
		BaseDate = _date.format(new Date(time));
		BaseTime = _time.format(new Date( time));
		
		// DB 연결
		String host = "192.168.0.53";
		String name = "HVI_DB";
		String user = "root";
		String pass = "dlatl#001";
		DbConnection dbconnection = new DbConnection(host, name, user, pass);
	    dbconnection.Connect();
	    
	   // 쿼리 수행
	    String sql = "Select distinct X_GRID, Y_GRID from KMA_COORD where X_GRID = '60'";
	    dbconnection.runQuery(sql);
	    
	    // 결과 저장
	    List<Grid> grid = new ArrayList<Grid>();
	    while(dbconnection.getResult().next())
	    {
	    	String x = dbconnection.getResult().getString("X_GRID");
	    	String y = dbconnection.getResult().getString("Y_GRID");
	    	grid.add(new Grid(x, y));
	    }
	    
	    // api data 받아서 csv파일 생성
	    String FileName = "FORECAST_GRIB_" + BaseDate + BaseTime + ".csv";
	    BufferedWriter bufWriter = new BufferedWriter(new FileWriter(FileName));
	    CreateCSV(bufWriter);
	    List<String> category, obsr_value;
	    ApiConnection apiconnection = new ApiConnection();
	    for(int i = 0; i < grid.size(); i++)
	    {
			apiconnection.setUrl("http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib");
			apiconnection.setBaseDate(BaseDate);
			apiconnection.setBaseTime(BaseTime);
			apiconnection.setNx(grid.get(i).getX());
			apiconnection.setNy(grid.get(i).getY());
			apiconnection.setServiceKey("=aq%2Bd7pEryGFmGFAAIFv8VQps%2FF5YNIGe4RZX%2F2SW4h1%2BGHoWs6c4M9QptIPsQPZ2yHhm5iBOnoKKS89LJtlDNA%3D%3D");
			apiconnection.setPageNo("1");
			apiconnection.makeUrlBuilder();
			apiconnection.pullData();
			System.out.println(apiconnection.urlBuilder);
			category = apiconnection.getResult("category");
			obsr_value = apiconnection.getResult("obsrValue");
			WriteCSV(bufWriter, BaseDate, BaseTime, grid.get(i), category, obsr_value);
	    }
		if(bufWriter != null)
	    {
	    	bufWriter.close();
	    }
		
		 // DB에 입력
	    sql = "LOAD DATA LOCAL INFILE '" + FileName + "' INTO TABLE FORECAST_GRIB FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 LINES";
	    dbconnection.LoadLocalData(sql);
	}
	
	public static void CreateCSV(BufferedWriter bufWriter)
	{
		try
		{
			bufWriter.write("BASE_DATE, BASE_TIME, X_GRID, Y_GRID, TITLE, OBSR_VAL");
			System.out.println("BASE_DATE, BASE_TIME, X_GRID, Y_GRID, TITLE, OBSR_VAL\n");
			bufWriter.newLine();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void WriteCSV(BufferedWriter bufWriter, String BaseDate, String BaseTime, Grid grid, List<String> category, List<String> obsr_value) throws IOException
	{
		for(int i = 0; i < category.size(); i++)
		{
			bufWriter.write(BaseDate + "," + BaseTime + "," + grid.getX() + "," + grid.getY() + "," + category.get(i) + "," + obsr_value.get(i));
			System.out.println(BaseDate + "," + BaseTime + "," + grid.getX() + "," + grid.getY() + "," + category.get(i) + "," + obsr_value.get(i) + "\n");
			bufWriter.newLine();
		}
	}
}