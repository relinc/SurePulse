package net.relinc.viewer.GUI;

import net.relinc.libraries.application.LineChartWithMarkers.chartDataType;

import java.util.Optional;

public class ChartUserPreferences {
    public ChartingPreferences stressStrainPreferences;
    public ChartingPreferences stressTimePreferences;
    public ChartingPreferences strainTimePrefrences;
    public ChartingPreferences strainRateTimePrefrences;
    public ChartingPreferences faceForceTimePreferences;
    public ChartingPreferences loadDisplacementPreferences;
    public ChartingPreferences loadTimePreferences;
    public ChartingPreferences displacementTimePreferences;
    public ChartingPreferences displacementRateTimePreferences;

    public ChartUserPreferences() {
        stressStrainPreferences = new ChartingPreferences();
        stressTimePreferences = new ChartingPreferences();
        strainTimePrefrences = new ChartingPreferences();
        strainRateTimePrefrences = new ChartingPreferences();
        faceForceTimePreferences = new ChartingPreferences();
        loadDisplacementPreferences = new ChartingPreferences();
        loadTimePreferences = new ChartingPreferences();
        displacementTimePreferences = new ChartingPreferences();
        displacementRateTimePreferences = new ChartingPreferences();
    }

    public ChartingPreferences getChartingPreferenceType(chartDataType xType, chartDataType yType) {
        switch (xType) {
            case TIME:
                switch (yType) {
                    case STRESS:
                        return stressTimePreferences;
                    case STRAIN:
                        return strainTimePrefrences;
                    case STRAINRATE:
                        return strainRateTimePrefrences;
                    case FACEFORCE:
                        return faceForceTimePreferences;
                    case LOAD:
                        return loadTimePreferences;
                    case DISPLACEMENT:
                        return displacementTimePreferences;
                    case DISPLACEMENTRATE:
                        return displacementRateTimePreferences;
                }
            case STRAIN:
                return stressStrainPreferences;
            case DISPLACEMENT:
                return loadDisplacementPreferences;
            default:
                throw new IllegalStateException("Unexpected value:" + xType + yType);
        }
    }
}

