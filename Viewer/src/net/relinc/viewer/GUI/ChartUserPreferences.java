package net.relinc.viewer.GUI;

import net.relinc.libraries.application.LineChartWithMarkers.chartType;

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

    public void setPreferences(ChartingPreferences preference, chartType type) {
        this.getChartingPreferenceType(type).title = preference.title;
        this.getChartingPreferenceType(type).xMin = preference.xMin;
        this.getChartingPreferenceType(type).xMax = preference.xMax;
        this.getChartingPreferenceType(type).yMin = preference.yMin;
        this.getChartingPreferenceType(type).yMax = preference.yMax;
    }

    public ChartingPreferences getChartingPreferenceType(chartType type) {
        switch (type) {
            case STRESSSTRAIN:
                return stressStrainPreferences;
            case STRESSTIME:
                return stressTimePreferences;
            case STRAINTIME:
                return strainTimePrefrences;
            case STRAINRATETIME:
                return strainRateTimePrefrences;
            case FACEFORCETIME:
                return faceForceTimePreferences;
            case LOADDISPLACEMENT:
                return loadDisplacementPreferences;
            case LOADTIME:
                return loadTimePreferences;
            case DISCPLACEMENTTIME:
                return displacementTimePreferences;
            case DISPLACEMENTRATETIME:
                return displacementRateTimePreferences;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}

