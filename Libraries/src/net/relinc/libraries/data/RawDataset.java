package net.relinc.libraries.data;


import java.util.ArrayList;
import java.util.List;

import net.relinc.libraries.data.DataInterpreter.dataType;

public class RawDataset {

	//public dataType DataType;
	public double[] data;
	public DataInterpreter interpreter;
	//public String name = "";
	public RawDataset(double[] d){
		data = d;
		interpreter = new DataInterpreter();
	}
	
	public List<DataSubset> extractDataset(double[] time) throws Exception{
		//use the interpreter to create the dataset object.
		//DataSubset dataToReturn = null;
		ArrayList<DataSubset> list = new ArrayList<DataSubset>();
		
		if(interpreter.DataType == null)//no data
			return list;
//			throw new Exception("Cannot create a Datasubset from a RawDataset whose interpreter doesn't have a"+
//					" dataType");
		for(int i = 0; i < data.length; i++){
			data[i] = data[i] / interpreter.multiplier;
		}
		
		if(interpreter.DataType == dataType.FORCE){
			Force f = new Force(time, data);
			f.name = interpreter.name;
			list.add(f);
		}
		else if(interpreter.DataType == dataType.ENGINEERINGSTRAIN){
			EngineeringStrain e = new EngineeringStrain(time, data);
			e.name = interpreter.name;
			list.add(e);
		}
		else if(interpreter.DataType == dataType.TRUESTRAIN){
			TrueStrain t = new TrueStrain(time, data);
			t.name = interpreter.name;
			list.add(t);
		}
		else if(interpreter.DataType == dataType.LAGRANGIANSTRAIN){
			LagrangianStrain t = new LagrangianStrain(time, data);
			t.name = interpreter.name;
			list.add(t);
		}
		else if(interpreter.DataType == dataType.DISPLACEMENT){
			Displacement d = new Displacement(time, data);
			d.name = interpreter.name;
			list.add(d);
		}
		else if(interpreter.DataType == dataType.INCIDENTSG){
			IncidentBarVoltagePulse p = new IncidentBarVoltagePulse(time, data);
			p.strainGauge = interpreter.strainGauge;
			p.strainGaugeName = interpreter.strainGaugeName;
			p.name = interpreter.name + " -Incident Pulse";
			list.add(p);
			ReflectedBarVoltagePulse r = new ReflectedBarVoltagePulse(time, data);
			r.strainGauge = interpreter.strainGauge;
			r.strainGaugeName = interpreter.strainGaugeName;
			r.name = interpreter.name + " -Reflected Pulse";
			list.add(r);
		}
		else if(interpreter.DataType == dataType.TRANSMISSIONSG){
			TransmissionBarVoltagePulse p = new TransmissionBarVoltagePulse(time, data);
			p.strainGauge = interpreter.strainGauge;
			p.name = interpreter.name;
			p.strainGaugeName = interpreter.strainGaugeName;
			list.add(p);
		}
		else if(interpreter.DataType == dataType.LOADCELL){
			LoadCell l = new LoadCell(time, data);
			l.name = interpreter.name;
			list.add(l);
		}
		else if(interpreter.DataType == dataType.INCIDENTBARSTRAIN){
			IncidentBarStrainPulse p = new IncidentBarStrainPulse(time, data);
			p.strainGauge = interpreter.strainGauge;
			p.strainGaugeName = interpreter.strainGaugeName;
			p.name = interpreter.name + " -Incident Pulse";
			list.add(p);
			ReflectedBarStrainPulse r = new ReflectedBarStrainPulse(time, data);
			r.strainGauge = interpreter.strainGauge;
			r.strainGaugeName = interpreter.strainGaugeName;
			r.name = interpreter.name + " -Reflected Pulse";
			list.add(r);
		}
		else if(interpreter.DataType == dataType.TRANSMISSIONBARSTRAIN){
			TransmissionBarStrainPulse p = new TransmissionBarStrainPulse(time, data);
			p.strainGauge = interpreter.strainGauge;
			p.name = interpreter.name;
			p.strainGaugeName = interpreter.strainGaugeName;
			list.add(p);
		}
		
		return list;
		
	}
}
