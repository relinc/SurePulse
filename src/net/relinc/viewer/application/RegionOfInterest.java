package net.relinc.viewer.application;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.SPOperations;

public class RegionOfInterest {
	public double beginROITime;
	public double endROITime;

	//avg stress-strain
	public double averageEngineeringStress;
	public double averageTrueStress;
	public double averageEngineeringStrain;
	public double averageTrueStrain;
	public double averageEngineeringStrainRate;
	public double averageTrueStrainRate;
	
	//avg load-displacement
	public double averageLoad;
	public double averageDisplacement;
	public double averageDisplacementRate;

	//integrals stress-strain
	public double averageEngineeringStressVsTimeIntegral;
	public double averageTrueStressVsTimeIntegral;
	public double averageEngineeringStrainVsTimeIntegral;
	public double averageTrueStrainVsTimeIntegral;
	public double averageEngineeringStressVsStrainIntegral;
	public double averageTrueStressVsStrainIntegral;
	public double averageEngineeringStrainRateVsTimeIntegral;
	public double averageTrueStrainRateVsTimeIntegral;
	
	//integrals load-displacement
	public double averageLoadVsTimeIntegral;
	public double averageDisplacementVsTimeIntegral;
	public double averageLoadVsDisplacementIntegral;
	public double averageDisplacementRateVsTimeIntegral;
	
	//avg max stress-strain
	public double averageMaxEngineeringStress;
	public double averageMaxTrueStress;
	public double averageMaxEngineeringStrain;
	public double averageMaxTrueStrain;
	public double averageMaxEngineeringStrainRate;
	public double averageMaxTrueStrainRate;
	
	//avg max stress-strain
	public double averageMaxLoad;
	public double averageMaxDisplacement;
	public double averageMaxDisplacementRate;
	
	//K and N values stress-strain
	public double averageEngKValue;
	public double averageEngNValue;
	public double averageTrueKValue;
	public double averageTrueNValue;
	
	//K and N values load-displacement
	public double averageLoadKValue;
	public double averageLoadNValue;



	public void renderROIResults(List<Sample> inputSamples, boolean loadDisplacement, Sample specificSample){
		//if sample.placeholder = false -> normal behavior on sample list. else on individual sample
		
		if(inputSamples == null || inputSamples.size() == 0)
			return;

		List<Sample> samples = inputSamples;
		if(specificSample != null && !specificSample.placeHolderSample){
			samples = new ArrayList<Sample>();
			samples.add(specificSample);
		}
		
		double div = (double)samples.size();

		//avg stress-strain
		double sumEngStress = 0;
		double sumEngStrain = 0;
		double sumTrueStress = 0;
		double sumTrueStrain = 0;
		double sumEngStrainRate = 0;
		double sumTrueStrainRate = 0;
		
		//avg load-displacement
		double sumLoad = 0;
		double sumDisplacement = 0;
		double sumDisplacementRate = 0;
				
		//integral stress-strain
		double sumEngStressVsTimeIntegrals = 0;
		double sumTrueStressVsTimeIntegrals = 0;
		double sumEngStrainVsTimeIntegrals = 0;
		double sumTrueStrainVsTimeIntegrals = 0;
		double sumEngStressVsStrainIntegrals = 0;
		double sumTrueStressVsStrainIntegrals = 0;
		double sumEngStrainRateVsTimeIntegrals = 0;
		double sumTrueStrainRateVsTimeIntegrals = 0;
		
		//integral load-displacement
		double sumLoadVsTimeIntegrals = 0;
		double sumDisplacementVsTimeIntegrals = 0;
		double sumLoadVsDisplacementIntegrals = 0;
		double sumDisplacementRateVsTimeIntegrals = 0;
				
		//max stress-strain
		double sumEngStressMaxes = 0;
		double sumEngStrainMaxes = 0;
		double sumTrueStressMaxes = 0;
		double sumTrueStrainMaxes = 0;
		double sumEngStrainRateMaxes = 0;
		double sumTrueStrainRateMaxes = 0;
		
		//max load-displacement
		double sumLoadMaxes = 0;
		double sumDisplacementMaxes = 0;
		double sumDisplacementRateMaxes = 0;
		
		//k and n values stress-strain
		double sumEngKValues = 0;
		double sumEngNValues = 0;
		double sumTrueKValues = 0;
		double sumTrueNValues = 0;
		
		//k and n values load-displacement
		double sumLoadKValues = 0;
		double sumLoadNValues = 0;

		for(Sample s : samples){
			//if its the placeholder, use the specific sample begin/end times. Else, the ROI begin/end.
			double beginTime = (specificSample != null && !specificSample.placeHolderSample) ? s.getBeginROITime() : beginROITime;
			double endTime = (specificSample != null && !specificSample.placeHolderSample) ? s.getEndROITime() : endROITime;
			int begin = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, beginTime);
			int end = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, endTime);
			double div2 = (double)(end - begin + 1);
			
//			if(loadDisplacement){
//				
//			}
//			else{
//				
//			}
			double[] zeros = new double[s.results.time.length];
			double[] engStrain = loadDisplacement ? zeros : s.results.getEngineeringStrain();
			double[] engStress = loadDisplacement ? zeros : s.results.getEngineeringStress();
			double[] trueStress = loadDisplacement ? zeros : s.results.getTrueStress();
			double[] trueStrain = loadDisplacement ? zeros : s.results.getTrueStrain();
			
			double[] engStrainRate = loadDisplacement ? zeros : SPOperations.getDerivative(s.results.time, engStrain);
			double[] trueStrainRate = loadDisplacement ? zeros : SPOperations.getDerivative(s.results.time, trueStrain);
			
			double[] load = s.results.load;
			double[] displacement = s.results.displacement;
			double[] displacementRate = s.results.getDisplacementRate();
			
			//avg stress-strain
			double sumEngStressTemp = 0;
			double sumEngStrainTemp = 0;
			double sumTrueStressTemp = 0;
			double sumTrueStrainTemp = 0;
			double sumEngStrainRateTemp = 0;
			double sumTrueStrainRateTemp = 0;
			
			//avg load-displacement
			double sumLoadTemp = 0;
			double sumDisplacementTemp = 0;
			double sumDisplacementRateTemp = 0;
			
			//max stress-strain
			double maxEngStressTemp = Double.MIN_VALUE;
			double maxEngStrainTemp = Double.MIN_VALUE;
			double maxTrueStressTemp = Double.MIN_VALUE;
			double maxTrueStrainTemp = Double.MIN_VALUE;
			double maxEngStrainRateTemp = Double.MIN_VALUE;
			double maxTrueStrainRateTemp = Double.MIN_VALUE;
			
			//max load-displacement
			double maxLoadTemp = Double.MIN_VALUE;
			double maxDisplacementTemp = Double.MIN_VALUE;
			double maxDisplacementRateTemp = Double.MIN_VALUE;

			for(int i = begin; i <= end; i++){
				sumEngStressTemp += engStress[i];
				sumTrueStressTemp += trueStress[i];
				sumEngStrainTemp += engStrain[i];
				sumTrueStrainTemp += trueStrain[i];
				sumEngStrainRateTemp += engStrainRate[i];
				sumTrueStrainRateTemp += trueStrainRate[i];
				
				sumLoadTemp += load[i];
				sumDisplacementTemp += displacement[i];
				sumDisplacementRateTemp += displacementRate[i];
				
				if(engStress[i] > maxEngStressTemp)
					maxEngStressTemp = engStress[i];
				if(trueStress[i] > maxTrueStressTemp)
					maxTrueStressTemp = trueStress[i];
				if(engStrain[i] > maxEngStrainTemp)
					maxEngStrainTemp = engStrain[i];
				if(trueStrain[i] > maxTrueStrainTemp)
					maxTrueStrainTemp = trueStrain[i];
				if(engStrainRate[i] > maxEngStrainRateTemp)
					maxEngStrainRateTemp = engStrainRate[i];
				if(trueStrainRate[i] > maxTrueStrainRateTemp)
					maxTrueStrainRateTemp = trueStrainRate[i];
				if(load[i] > maxLoadTemp)
					maxLoadTemp = load[i];
				if(displacement[i] > maxDisplacementTemp)
					maxDisplacementTemp = displacement[i];
				if(displacementRate[i] > maxDisplacementRateTemp)
					maxDisplacementRateTemp = displacementRate[i];
			}
			sumEngStress += sumEngStressTemp / div2;
			sumTrueStress += sumTrueStressTemp / div2;
			sumEngStrain += sumEngStrainTemp / div2;
			sumTrueStrain += sumTrueStrainTemp / div2;
			sumEngStrainRate += sumEngStrainRateTemp / div2;
			sumTrueStrainRate += sumTrueStrainRateTemp / div2;
			
			sumLoad += sumLoadTemp / div2;
			sumDisplacement += sumDisplacementTemp / div2;
			sumDisplacementRate += sumDisplacementRateTemp / div2;

			//stress-strain
			double[] integral = SPOperations.integrate(s.results.time, engStress, begin, end);
			sumEngStressVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, trueStress, begin, end);
			sumTrueStressVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, engStrain, begin, end);
			sumEngStrainVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, trueStrain, begin, end);
			sumTrueStrainVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(engStrain, engStrain, begin, end);
			sumEngStressVsStrainIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(trueStrain, trueStress, begin, end);
			sumTrueStressVsStrainIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, engStrainRate, begin, end);
			sumEngStrainRateVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, trueStrainRate, begin, end);
			sumTrueStrainRateVsTimeIntegrals += integral[integral.length - 1];
			
			//load-displacement
			integral = null;
			integral = SPOperations.integrate(s.results.time, load, begin, end);
			sumLoadVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, displacement, begin, end);
			sumDisplacementVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.time, displacementRate, begin, end);
			sumDisplacementRateVsTimeIntegrals += integral[integral.length - 1];
			integral = null;
			integral = SPOperations.integrate(s.results.displacement, s.results.load, begin, end);
			sumLoadVsDisplacementIntegrals += integral[integral.length - 1];
			
			//stress-strain
			sumEngStressMaxes += maxEngStressTemp;
			sumEngStrainMaxes += maxEngStrainTemp;
			sumTrueStressMaxes += maxTrueStressTemp;
			sumTrueStrainMaxes += maxTrueStrainTemp;
			sumEngStrainRateMaxes += maxEngStrainRateTemp;
			sumTrueStrainRateMaxes += maxTrueStrainRateTemp;
			
			//load-displacement
			sumLoadMaxes += maxLoadTemp;
			sumDisplacementMaxes += maxDisplacementTemp;
			sumDisplacementRateMaxes += maxDisplacementRateTemp;
			
			//calculate K value for sample s.
			
//			List<Double> logEngStress = doubleArrayToDoubleList(engStress).stream().map(d -> Math.log10(d.doubleValue())).collect(Collectors.toList());
//			List<Double> logEngStrain = doubleArrayToDoubleList(engStrain).stream().map(d -> Math.log10(d.doubleValue())).collect(Collectors.toList());
//			List<Double> logTrueStress = doubleArrayToDoubleList(trueStress).stream().map(d -> Math.log10(d.doubleValue())).collect(Collectors.toList());
//			List<Double> logTrueStrain = doubleArrayToDoubleList(trueStrain).stream().map(d -> Math.log10(d.doubleValue())).collect(Collectors.toList());
			
			final WeightedObservedPoints logEngStressEngStrain = new WeightedObservedPoints();
			for(int i = begin; i <= end; i++){
				double strain = engStrain[i] > 0 ? Math.log(engStrain[i]) : 0;
				double stress = engStress[i] > 0 ? Math.log(engStress[i]) : 0;
				logEngStressEngStrain.add(new WeightedObservedPoint(1, strain, stress));
			}
			
			final WeightedObservedPoints logTrueStressTrueStrain = new WeightedObservedPoints();
			for(int i = begin; i <= end; i++){
				double strain = trueStrain[i] > 0 ? Math.log(trueStrain[i]) : 0;
				double stress = trueStress[i] > 0 ? Math.log(trueStress[i]) : 0;
				logTrueStressTrueStrain.add(new WeightedObservedPoint(1, strain, stress));
			}
			
			final WeightedObservedPoints logLoadDisplacement = new WeightedObservedPoints();
			for(int i = begin; i <= end; i++){
				double displacementTemp = displacement[i] > 0 ? Math.log(displacement[i]) : 0;
				double loadTemp = load[i] > 0 ? Math.log(load[i]) : 0;
				logLoadDisplacement.add(new WeightedObservedPoint(1, displacementTemp, loadTemp));
			}
			
			final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
			double[] coeff = fitter.fit(logEngStressEngStrain.toList());
			sumEngKValues += Math.pow(Math.E, coeff[0]);
			sumEngNValues += coeff[1];
			
			coeff = fitter.fit(logTrueStressTrueStrain.toList());
			sumTrueKValues += Math.pow(Math.E, coeff[0]);
			sumTrueNValues += coeff[1];
			
			coeff = fitter.fit(logLoadDisplacement.toList());
			sumLoadKValues += Math.pow(Math.E, coeff[0]);
			sumLoadNValues += coeff[1];
			
		}
		
		//stress-strain
		averageEngineeringStress = sumEngStress / div;
		averageTrueStress = sumTrueStress / div;
		averageEngineeringStrain = sumEngStrain / div;
		averageTrueStrain = sumTrueStrain / div;
		averageEngineeringStrainRate = sumEngStrainRate / div;
		averageTrueStrainRate = sumTrueStrainRate / div;
		
		//load-displacement
		averageLoad = sumLoad / div;
		averageDisplacement = sumDisplacement / div;
		averageDisplacementRate = sumDisplacementRate / div;

		//stress-strain
		averageEngineeringStressVsTimeIntegral = sumEngStressVsTimeIntegrals / div;
		averageTrueStressVsTimeIntegral = sumTrueStressVsTimeIntegrals / div;
		averageEngineeringStrainVsTimeIntegral = sumEngStrainVsTimeIntegrals / div;
		averageTrueStrainVsTimeIntegral = sumTrueStrainVsTimeIntegrals / div;
		averageEngineeringStressVsStrainIntegral = sumEngStressVsStrainIntegrals / div;
		averageTrueStressVsStrainIntegral = sumTrueStressVsStrainIntegrals / div;
		averageEngineeringStrainRateVsTimeIntegral = sumEngStrainRateVsTimeIntegrals / div;
		averageTrueStrainRateVsTimeIntegral = sumTrueStrainRateVsTimeIntegrals / div;
		
		//load-displacement
		averageLoadVsTimeIntegral = sumLoadVsTimeIntegrals / div;
		averageDisplacementVsTimeIntegral = sumDisplacementVsTimeIntegrals / div;
		averageLoadVsDisplacementIntegral = sumLoadVsDisplacementIntegrals / div;
		averageDisplacementRateVsTimeIntegral = sumDisplacementRateVsTimeIntegrals / div;
		
		//stress-strain
		averageMaxEngineeringStress = sumEngStressMaxes / div;
		averageMaxTrueStress = sumTrueStressMaxes / div;
		averageMaxEngineeringStrain = sumEngStrainMaxes / div;
		averageMaxTrueStrain = sumTrueStrainMaxes / div;
		averageMaxEngineeringStrainRate = sumEngStrainRateMaxes / div;
		averageMaxTrueStrainRate = sumTrueStrainRateMaxes / div;
		
		//load-displacement
		averageMaxLoad = sumLoadMaxes / div;
		averageMaxDisplacement = sumDisplacementMaxes / div;
		averageMaxDisplacementRate = sumDisplacementRateMaxes / div;
		
		//stress-strain
		averageEngKValue = sumEngKValues / div;
		averageEngNValue = sumEngNValues / div;
		averageTrueKValue = sumTrueKValues / div;
		averageTrueNValue = sumTrueNValues / div;
		
		//load-displacement
		averageLoadKValue = sumLoadKValues / div;
		averageLoadNValue = sumLoadNValues / div;
	}
	
//	private List<Double> doubleArrayToDoubleList(double[] arr){
//		List<Double> list = new ArrayList<Double>(arr.length);
//		for(int i = 0; i < arr.length; i++){
//			list.set(i, arr[i]);
//		}
//		return list;
//	}
}
