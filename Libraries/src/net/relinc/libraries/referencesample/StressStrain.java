package net.relinc.libraries.referencesample;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

enum StressStrainMode {
    ENGINEERING, TRUE;
}

public class StressStrain {

    private List<Double> stress; // pa
    private List<Double> strain; // unitless
    private StressStrainMode mode;

    public StressStrain(List<Double> stress, List<Double> strain, StressStrainMode mode) {
        this.stress = stress;
        this.strain = strain;
        this.mode = mode;
    }


    private static StressStrain toEngineering(StressStrain stressStrain) {
        if(stressStrain.mode == StressStrainMode.ENGINEERING) {
            return stressStrain;
        }

        List<Double> engineeringStrain = stressStrain.strain.stream().map(trueStrain -> Math.pow(Math.E, trueStrain) - 1).collect(Collectors.toList());
        List<Double> engineeringStress = IntStream.range(0, stressStrain.stress.size())
                .mapToDouble(idx -> stressStrain.stress.get(idx) / (1+engineeringStrain.get(idx)))
                .boxed()
                .collect(Collectors.toList());

        return new StressStrain(engineeringStress, engineeringStrain, StressStrainMode.ENGINEERING);

    }

    private static StressStrain toTrue(StressStrain stressStrain) {
        if(stressStrain.mode == StressStrainMode.TRUE) {
            return stressStrain;
        }

        List<Double> trueStrain = stressStrain.strain.stream().map(s -> Math.log(1 + s)).collect(Collectors.toList());

        List<Double> trueStress = IntStream.range(0, stressStrain.stress.size())
                .mapToDouble(idx -> stressStrain.stress.get(idx) * (1 + stressStrain.strain.get(idx))).boxed().collect(Collectors.toList());


        return new StressStrain(trueStress, trueStrain, StressStrainMode.TRUE);
    }

}
