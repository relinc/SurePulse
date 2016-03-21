package net.relinc.libraries.data;

import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.staticClasses.PochammerChreeDispersion;

public abstract class ReflectedPulse extends HopkinsonBarPulse {
	public ReflectedPulse(double[] t, double[] d){
		super(t, d);
	}
	
	public double[] getPochammerAdjustedArray(BarSetup setup){
		return PochammerChreeDispersion.runPochammer(getTrimmedData(), 
				PochammerChreeDispersion.SteelParameters,setup.IncidentBar.diameter/2 , Data.timeData[1] - Data.timeData[0],
				strainGauge.distanceToSample, setup.IncidentBar.getWaveSpeed());
	}
	
	public double[] getFrontFaceForce(Bar incidentBar, double[] incidentStrain, double sign){
		//this code is from the old sure-pulse
		//		FrontFaceForce[i] = -(theData.SGData.VoltageIncident[i + BeginReflectPulse] + theData.SGData.VoltageIncident[i + BeginIncidPulse]) / setUp.IncidentBar.VoltageFactor
//                * setUp.IncidentBar.YoungsModulus
//                * Math.Pow(setUp.IncidentBar.Diameter / 2, 2) * Math.PI;
		double[] reflectedStrain = getUsefulTrimmedData();
		int numData = Math.min(getTrimmedData().length, incidentStrain.length);
		double[] force = new double[numData];
		for(int i = 0; i < force.length; i++){
			force[i] = sign * (incidentStrain[i] + reflectedStrain[i]) * incidentBar.youngsModulus * Math.pow(incidentBar.diameter / 2, 2) * Math.PI;
		}
		return force;
	}
}
