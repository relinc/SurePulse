package net.relinc.processor.fxControls;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;

public class NumberTextField extends TextField
{
	
	public boolean isRequired = true;	
	public Label unitLabel = new Label();
	private String standardUnits, metricUnits;
	
	public NumberTextField(String standardUnits, String metricUnits) {
		setUnits(standardUnits, metricUnits);
	}

    @Override
    public void replaceText(int start, int end, String text)
    {
        if (validate(text))
        {
        	super.replaceText(start, end, text);
        	updateLabelPosition();
        }
        
    }

    @Override
    public void replaceSelection(String text)
    {
        if (validate(text))
        {
            super.replaceSelection(text);
            updateLabelPosition();
        }
    }
    
    public void setNumberText(String text) {
    	Double number;
    	try {
    		number = Double.parseDouble(text);
    		number = SPOperations.round(number, 4);
    	} catch(Exception e) {
    		e.printStackTrace();
    		return;
    	}
    	setText(Double.toString(number));
    	updateLabelPosition();
    }
    
    public void setUnits(String standard, String metric) {
    	standardUnits = standard;
    	metricUnits = metric;
    	unitLabel.setOpacity(.4);
		unitLabel.setPadding(new Insets(0,0,0,12));
		unitLabel.setMouseTransparent(true);
    	updateLabelPosition();
    	updateTextFieldLabelUnits();
    }
    
    public void updateTextFieldLabelUnits() {
    	if(SPSettings.metricMode.getValue()) {
    		unitLabel.setText(metricUnits);
    	} else {
    		unitLabel.setText(standardUnits);
    	}
    }

    private boolean validate(String text)
    {    
    	text = text.replaceAll(",", "");
    	int decimalPointCount = (this.getText() + text).length() - (this.getText() + text).replaceAll("\\.", "").length();
        return ("".equals(text) || (text.matches("[.0-9]*") && (decimalPointCount <= 1 || this.getSelectedText().contains("."))));
    }
    
    public Double getDouble() throws NumberFormatException {
    	String noCommas = this.getText().replaceAll(",", "");
    	double val = -1;
    	try{
    		val = Double.parseDouble(noCommas);
    	}
    	catch(Exception e){
    		val = -1;
    	}
    	return val;
    }
    
    public void updateLabelPosition() {
    	Text t = new Text(getText());
        unitLabel.setPadding(new Insets(0,0,0,t.getLayoutBounds().getWidth() + 14));
        //System.out.println(unitLabel.getPadding());
    }

}
