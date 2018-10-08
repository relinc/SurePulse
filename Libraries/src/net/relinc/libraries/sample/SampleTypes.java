package net.relinc.libraries.sample;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SampleTypes {
    private static final Map<Class<? extends Sample>, SampleConstants> sampleConstantsMap;
    static {
    	// LinkedHashMap is ordered, this is a requirement! It sets the order in the UI.
        Map<Class<? extends Sample>, SampleConstants> map = new LinkedHashMap<Class<? extends Sample>, SampleConstants>(); 
		
        map.put(CompressionSample.class, CompressionSample.getSampleConstants());
        map.put(TensionRectangularSample.class, TensionRectangularSample.getSampleConstants());
        map.put(TensionRoundSample.class, TensionRoundSample.getSampleConstants());
        map.put(ShearCompressionSample.class, ShearCompressionSample.getSampleConstants());
        map.put(LoadDisplacementSample.class, LoadDisplacementSample.getSampleConstants());
        map.put(TorsionSample.class, TorsionSample.getSampleConstants());
        map.put(BrazilianTensileSample.class, BrazilianTensileSample.getSampleConstants());
        
        if(map.values().stream().map(SampleConstants::getExtension).distinct().count() != map.size())
        {
        	throw new RuntimeException("Detected duplicate extensions! This should be remedied.");
        }
        
        if(map.values().stream().map(SampleConstants::getName).distinct().count() != map.size())
        {
        	throw new RuntimeException("Detected duplicate names! This should be remedied.");
        }
        
        if(map.values().stream().map(SampleConstants::getShortName).distinct().count() != map.size())
        {
        	throw new RuntimeException("Detected duplicate short names! This should be remedied.");
        }
        
        
		sampleConstantsMap = Collections.unmodifiableMap(map);
    }
    
    public static Map<Class<? extends Sample>, SampleConstants> getSampleConstantsMap() {
    	return sampleConstantsMap;
    }
	
	public static SampleConstants getSampleConstants(Class<? extends Sample> c) {
		return sampleConstantsMap.get(c);
	}
}
