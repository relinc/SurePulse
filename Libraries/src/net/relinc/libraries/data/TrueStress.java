package net.relinc.libraries.data;

public class TrueStress extends DataSubset {

    public TrueStress(double[] t, double[] d){
        super(t, d);
    }

    @Override
    public baseDataType getBaseDataType() {
        return baseDataType.LOAD;
    }

    @Override
    public String getUnitAbbreviation() {
        return "Pa";
    }

    @Override
    public String getUnitName() {
        return "Stress";
    }

    @Override
    public double[] getUsefulTrimmedData() {
        double[] stress = new double[getTrimmedData().length];
        double[] data = getTrimmedData();
        for(int i = 0; i < stress.length; i++){
            stress[i] = data[i];
        }
        return stress;
    }
}
