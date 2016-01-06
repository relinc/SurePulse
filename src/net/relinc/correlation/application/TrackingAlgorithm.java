package net.relinc.correlation.application;

import net.relinc.correlation.staticClasses.SPTargetTracker.TrackingAlgo;

public class TrackingAlgorithm {
	public TrackingAlgo algo;
	public TrackingAlgorithm(TrackingAlgo algo){
		this.algo = algo;
	}
	
	@Override public String toString(){
		switch(algo){
		case CIRCULAR:
			return "Circular";
		case SPARSEFLOW:
			return "Sparse Flow";
		case TLD:
			return "TLD";
//		case MEANSHIFTCOMANICIU2003:
//			return "Mean Shift Comaniciu2003";
//		case MEANSHIFTLIKELIHOOD:
//			return "Mean Shift Likelihood";
		case SIMPLECORRELATE:
			return "Simple Correlate";
		default :
			return "NULL";
		}
	}
}
