package com.ybs.pullapidata.forecastgrib;

public class Grid 
{
	private String x;
	private String y;
	
	public String getX() {
		return x;
	}
	public void setX(String x) {
		this.x = x;
	}
	public String getY() {
		return y;
	}
	public void setY(String y) {
		this.y = y;
	}
	
	Grid(String _x, String _y)
	{
		x = _x;
		y = _y;
	}
}
