package com.zed3.sipua.ui.anta;


public class Linkman {
	public boolean isSelected;
	public String name;
	public String number;
	//add by hu 2014/2/15
	public  int imgId;
	public  boolean selectEnabled;
	
	public Linkman() {
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return number.equals(((Linkman)o).number);
	}
	
	/*@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return number.hashCode();
	}*/
}