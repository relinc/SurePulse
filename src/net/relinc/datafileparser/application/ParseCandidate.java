package net.relinc.datafileparser.application;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ParseCandidate implements ObservableValue<ParseCandidate>{
	private String text;
	private boolean isParsable = false;
	
	public ParseCandidate(String s){
		this.text = s;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isParsable() {
		return isParsable;
	}
	public void setParsable(boolean isParsable) {
		this.isParsable = isParsable;
	}
	public boolean isDouble(){
		try{
			Double.parseDouble(text);
			return true;
		}
		catch(Exception e){
		return false;
		}
	} //isDouble

	@Override
	public void addListener(InvalidationListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(ChangeListener<? super ParseCandidate> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(ChangeListener<? super ParseCandidate> listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public ParseCandidate getValue() {
		return this;
	}

}
