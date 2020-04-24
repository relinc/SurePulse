package net.relinc.viewer.GUI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.relinc.datafileparser.application.Home;
import net.relinc.libraries.referencesample.*;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;


// one json file?
// All controls in left pane?
// - new
// - delete
// - export
// - import
// - Parameter implementations? Option from top-level window?
// - Parameter implementation editable?
// - Which units to save in?

public class NewReferenceController implements Initializable {
    Stage stage;
    public Optional<ReferenceSample> clickedReferenceSample = Optional.empty();
    public List<XYChart.Series<Number, Number>> xyDatas = new ArrayList<>();

    List<Double> rawStrainData = new ArrayList();
    List<Double> rawStressData = new ArrayList();

    private boolean skipRender = false;


    @FXML
    LineChart<NumberAxis, NumberAxis> xyChart;

    @FXML
    RadioButton englishRadioButton;
    @FXML
    RadioButton metricRadioButton;

    ToggleGroup unitsToggleGroup = new ToggleGroup();


    @FXML
    TextField stressConversionTextBox;

    @FXML
    RadioButton inputTrueRadioButton;
    @FXML
    RadioButton inputEngineeringRadioButton;

    ToggleGroup inputEngineeringTrueToggleGroup = new ToggleGroup();


    @FXML
    RadioButton chartEngineeringRadioButton;
    @FXML
    RadioButton chartTrueRadioButton;

    ToggleGroup chartEngineeringTrueToggleGroup = new ToggleGroup();

    @FXML
    TextField nameTextField;


    @FXML
    Tab knTab;
    @FXML
    LineChart<NumberAxis, NumberAxis> knChart;

    @FXML
    Label knYoungsModulusLabel;
    @FXML
    TextField knYoungsModulusTextField;

    @FXML
    Label knYieldStressLabel;
    @FXML
    TextField knYieldStressTextField;

    @FXML
    TextField knKTextField;

    @FXML
    Label knKLabel;
    @FXML
    TextField knNTextField;

    @FXML
    TextField knNameTextField;


    @FXML
    Tab johnsonCookTab;

    @FXML
    Label johnsonCookYoungsModulusLabel;
    @FXML
    TextField johnsonCookYoungsModulusTextField;

    @FXML
    Label johnsonCookReferenceYieldStressLabel;
    @FXML
    TextField johnsonCookReferenceYieldStressTextField;

    @FXML
    Label johnsonCookYieldStressLabel;
    @FXML
    TextField johnsonCookStrainRateTextField;

    @FXML
    TextField johnsonCookReferenceStrainRateTextField;
    @FXML
    TextField johnsonCookRoomTemperatureTextField;
    @FXML
    TextField johnsonCookMeltingPointTextField;
    @FXML
    TextField johnsonCookSampleTemperatureTextField;
    @FXML
    TextField johnsonCookYieldStressTextField;
    @FXML
    TextField johnsonCookIntensityCoefficientTextField;
    @FXML
    TextField johnsonCookStrainRateCoefficientTextField;
    @FXML
    TextField johnsonCookStrainHardeningCoefficientTextField;
    @FXML
    TextField johnsonCookThermalSofteningCoefficientTextField;
    @FXML
    TextField johnsonCookNameTextField;

    @FXML
    LineChart<NumberAxis, NumberAxis> johnsonCookChart;


    @FXML
    Tab ludwikTab;


    @FXML
    Label ludwigYoungsModulusLabel;
    @FXML
    TextField ludwigYoungsModulusTextField;

    @FXML
    Label ludwigYieldStressLabel;
    @FXML
    TextField ludwigYieldStressTextField;

    @FXML
    TextField ludwigIntensityCoefficientTextField;
    @FXML
    TextField ludwigStrainHardeningCoefficientTextField;
    @FXML
    TextField ludwigNameTextField;

    @FXML
    LineChart<NumberAxis, NumberAxis> ludwigChart;


    @FXML
    Tab cowperSymondsTab;

    @FXML
    Label cowperSymondsYoungsModulusLabel;
    @FXML
    TextField cowperSymondsYoungsModulusTextField;

    @FXML
    Label cowperSymondsReferenceYieldStressLabel;
    @FXML
    TextField cowperSymondsReferenceYieldStressTextField;

    @FXML
    TextField cowperSymondsStrainRateTextField;

    @FXML
    Label cowperSymondsYieldStressLabel;
    @FXML
    TextField cowperSymondsYieldStressTextField;

    @FXML
    TextField cowperSymondsIntensityCoefficientTextField;
    @FXML
    TextField cowperSymondsStrainRateCoefficientTextField;
    @FXML
    TextField cowperSymondsStrainHardeningCoefficientTextField;
    @FXML
    TextField cowperSymondsStrainRateSensitivityCoefficientTextField;
    @FXML
    TextField cowperSymondsNameTextField;
    @FXML
    LineChart<NumberAxis, NumberAxis> cowperSymondsChart;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        englishRadioButton.setSelected(true);
        englishRadioButton.setToggleGroup(unitsToggleGroup);
        metricRadioButton.setToggleGroup(unitsToggleGroup);

        inputEngineeringRadioButton.setSelected(true);
        inputEngineeringRadioButton.setToggleGroup(inputEngineeringTrueToggleGroup);
        inputTrueRadioButton.setToggleGroup(inputEngineeringTrueToggleGroup);

        chartEngineeringRadioButton.setSelected(true);
        chartEngineeringRadioButton.setToggleGroup(chartEngineeringTrueToggleGroup);
        chartTrueRadioButton.setToggleGroup(chartEngineeringTrueToggleGroup);


        Stream.of(unitsToggleGroup, inputEngineeringTrueToggleGroup, chartEngineeringTrueToggleGroup).forEach(toggleGroup -> {
            toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    renderXY();
                }
            });
        });

        unitsToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                // change KN Labels
                skipRender = true;
                if (newValue.equals(metricRadioButton)) {
                    // KN
                    knYoungsModulusLabel.setText("Young's Modulus (MPa)");
                    knYieldStressLabel.setText("Yield Stress (MPa)");
                    knKLabel.setText("K (MPa)");

                    // Johnson Cook
                    johnsonCookYoungsModulusLabel.setText("Young's Modulus (MPa)");
                    johnsonCookReferenceYieldStressLabel.setText("Reference Yield Stress (MPa)");
                    johnsonCookYieldStressLabel.setText("Yield Stress (MPa)");

                    // ludwik
                    ludwigYoungsModulusLabel.setText("Young's Modulus (MPa)");
                    ludwigYieldStressLabel.setText("Yield Stress (MPa)");

                    // cowper
                    cowperSymondsYoungsModulusLabel.setText("Young's Modulus (MPa)");
                    cowperSymondsReferenceYieldStressLabel.setText("Reference Yield Stress (Pa)");
                    cowperSymondsYieldStressLabel.setText("Yield Stress (MPa)");

                    if (oldValue.equals(englishRadioButton)) {
                        // KN
                        convertTextFieldMpaFromKsi(knYoungsModulusTextField);
                        convertTextFieldMpaFromKsi(knYieldStressTextField);
                        convertTextFieldMpaFromKsi(knKTextField);

                        // Johnson Cook
                        convertTextFieldMpaFromKsi(johnsonCookYoungsModulusTextField);
                        convertTextFieldMpaFromKsi(johnsonCookReferenceYieldStressTextField);
                        convertTextFieldMpaFromKsi(johnsonCookYieldStressTextField);

                        // ludwik
                        convertTextFieldMpaFromKsi(ludwigYoungsModulusTextField);
                        convertTextFieldMpaFromKsi(ludwigYieldStressTextField);

                        // cowper
                        convertTextFieldMpaFromKsi(cowperSymondsYoungsModulusTextField);
                        convertTextFieldMpaFromKsi(cowperSymondsReferenceYieldStressTextField);
                        convertTextFieldMpaFromKsi(cowperSymondsYieldStressTextField);
                    }
                } else if (newValue.equals(englishRadioButton)) {
                    // KN
                    knYoungsModulusLabel.setText("Young's Modulus (ksi)");
                    knYieldStressLabel.setText("Yield Stress (ksi)");
                    knKLabel.setText("K (ksi)");

                    // Johnson Cook
                    johnsonCookYoungsModulusLabel.setText("Young's Modulus (ksi)");
                    johnsonCookReferenceYieldStressLabel.setText("Reference Yield Stress (ksi)");
                    johnsonCookYieldStressLabel.setText("Yield Stress (ksi)");

                    // ludwik
                    ludwigYoungsModulusLabel.setText("Young's Modulus (ksi)");
                    ludwigYieldStressLabel.setText("Yield Stress (ksi)");

                    // cowper
                    cowperSymondsYoungsModulusLabel.setText("Young's Modulus (MPa)");
                    cowperSymondsReferenceYieldStressLabel.setText("Reference Yield Stress (Pa)");
                    cowperSymondsYieldStressLabel.setText("Yield Stress (MPa)");

                    if (oldValue.equals(metricRadioButton)) {
                        // KN
                        convertTextFieldKsiFromMpa(knYoungsModulusTextField);
                        convertTextFieldKsiFromMpa(knYieldStressTextField);
                        convertTextFieldKsiFromMpa(knKTextField);

                        // Johnson Cook
                        convertTextFieldKsiFromMpa(johnsonCookYoungsModulusTextField);
                        convertTextFieldKsiFromMpa(johnsonCookReferenceYieldStressTextField);
                        convertTextFieldKsiFromMpa(johnsonCookYieldStressTextField);

                        // ludwik
                        convertTextFieldKsiFromMpa(ludwigYoungsModulusTextField);
                        convertTextFieldKsiFromMpa(ludwigYieldStressTextField);

                        // cowper
                        convertTextFieldKsiFromMpa(cowperSymondsYoungsModulusTextField);
                        convertTextFieldKsiFromMpa(cowperSymondsReferenceYieldStressTextField);
                        convertTextFieldKsiFromMpa(cowperSymondsYieldStressTextField);
                    }
                } else {
                    throw new RuntimeException("Units not recognized!");
                }

                skipRender = false;
                renderAll();
            }
        });

        // initializes labels
        unitsToggleGroup.selectToggle(metricRadioButton);
        unitsToggleGroup.selectToggle(englishRadioButton);


        Stream.of(knYoungsModulusTextField, knYieldStressTextField, knKTextField, knNTextField).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderKN();
                }
            });
        });


        Stream.of(
                johnsonCookYoungsModulusTextField,
                johnsonCookReferenceYieldStressTextField,
                johnsonCookStrainRateTextField,
                johnsonCookReferenceStrainRateTextField,
                johnsonCookRoomTemperatureTextField,
                johnsonCookMeltingPointTextField,
                johnsonCookSampleTemperatureTextField,
                johnsonCookYieldStressTextField,
                johnsonCookIntensityCoefficientTextField,
                johnsonCookStrainRateCoefficientTextField,
                johnsonCookStrainHardeningCoefficientTextField,
                johnsonCookThermalSofteningCoefficientTextField
        ).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderJohnsonCook();
                }
            });
        });

        Stream.of(
                ludwigYoungsModulusTextField,
                ludwigYieldStressTextField,
                ludwigIntensityCoefficientTextField,
                ludwigStrainHardeningCoefficientTextField
        ).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderLudwig();
                }
            });
        });


        Stream.of(
                cowperSymondsYoungsModulusTextField,
                cowperSymondsReferenceYieldStressTextField,
                cowperSymondsStrainRateTextField,
                cowperSymondsYieldStressTextField,
                cowperSymondsIntensityCoefficientTextField,
                cowperSymondsStrainRateCoefficientTextField,
                cowperSymondsStrainHardeningCoefficientTextField,
                cowperSymondsStrainRateSensitivityCoefficientTextField
        ).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderCowperSymonds();
                }
            });
        });


    }

    public void convertTextFieldKsiFromMpa(TextField tf) {
        try {
            Double val = Double.parseDouble(tf.getText());
            tf.setText(Double.toString(
                    Converter.ksiFromPa(
                            Converter.paFromMpa(val)
                    ))
            );
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    public void convertTextFieldMpaFromKsi(TextField tf) {
        try {
            Double val = Double.parseDouble(tf.getText());
            tf.setText(Double.toString(
                    Converter.MpaFromPa(
                            Converter.paFromKsi(val))
                    )
            );
        } catch (NumberFormatException e) {
            // ignore
        }
    }


    public void renderFromProps() {
        this.clickedReferenceSample.ifPresent(sample -> {
            if (sample instanceof ReferenceSampleKN) {
                ReferenceSampleKN knSample = (ReferenceSampleKN) sample;
                knTab.getTabPane().getSelectionModel().select(knTab);

                setKNModel(knSample);

            } else if (sample instanceof ReferenceSampleJohnsonCook) {
                ReferenceSampleJohnsonCook jcSample = (ReferenceSampleJohnsonCook) sample;

                johnsonCookTab.getTabPane().getSelectionModel().select(johnsonCookTab);

                setJohnsonCookModel(jcSample);
            } else if (sample instanceof ReferenceSampleLudwig) {
                ReferenceSampleLudwig ludwikSample = (ReferenceSampleLudwig) sample;
                ludwikTab.getTabPane().getSelectionModel().select(ludwikTab);

                setLudwikModel(ludwikSample);

            } else if (sample instanceof ReferenceSampleCowperSymonds) {
                ReferenceSampleCowperSymonds cowperSample = (ReferenceSampleCowperSymonds) sample;
                cowperSymondsTab.getTabPane().getSelectionModel().select(cowperSymondsTab);

                setCowperSymondsModel(cowperSample);

            }
        });
    }


    @FXML
    public void mpaButtonClicked() {
        stressConversionTextBox.setText("0.145038");
    }

    @FXML
    public void ksiButtonClicked() {
        stressConversionTextBox.setText("1");
    }

    @FXML
    public void paButtonClicked() {
        stressConversionTextBox.setText("1.45038e-7");
    }

    @FXML
    public void psiButtonClicked() {
        stressConversionTextBox.setText("0.001");

    }

    @FXML
    public void applyButtonClicked() {
        renderXY();
    }

    private List<Double> toPa(List<Double> inputStress) {
        List<Double> result = new ArrayList<Double>();
        Double conversionFactor = Double.parseDouble(stressConversionTextBox.getText());
        for (int i = 0; i < inputStress.size(); i++) {
            Double val = inputStress.get(i) * conversionFactor * 6.895e+6; // convert to Pa
            result.add(val);
        }
        return result;
    }

    private void setKNModel(ReferenceSampleKN sample) {

        if (englishRadioButton.isSelected()) {
            knYoungsModulusTextField.setText(Double.toString(Converter.ksiFromPa(sample.materialYoungsModulus)));
            knYieldStressTextField.setText(Double.toString(Converter.ksiFromPa(sample.referenceYieldStress)));
            knKTextField.setText(Double.toString(Converter.ksiFromPa(sample.K)));
        } else if (metricRadioButton.isSelected()) {
            knYoungsModulusTextField.setText(Double.toString(Converter.MpaFromPa(sample.materialYoungsModulus)));
            knYieldStressTextField.setText(Double.toString(Converter.MpaFromPa(sample.referenceYieldStress)));
            knKTextField.setText(Double.toString(Converter.MpaFromPa(sample.K)));
        }
        knNTextField.setText(Double.toString(sample.N));
        knNameTextField.setText(sample.getName());
    }

    @FXML
    public void kn6061Clicked() {
        skipRender = true;
        setKNModel(new ReferenceSampleKN(
                "6061 (KN)",
                "",
                530e6,
                0.14048,
                68.9e9,
                252e6
        ));
        skipRender = false;
        renderKN();
    }

    @FXML
    public void kn7075Clicked() {
        skipRender = true;
        setKNModel(new ReferenceSampleKN(
                "7075 (KN)",
                "",
                673e6,
                0.045,
                71.7e9,
                473e6
        ));
        skipRender = false;
        renderKN();
    }

    @FXML
    public void knSaveButtonClicked() {
        parseKNSample().ifPresent(sample -> {
            saveReference(sample, knNameTextField.getText());
            stage.close();
        });
    }

    private void setJohnsonCookModel(ReferenceSampleJohnsonCook sample) {
        if (englishRadioButton.isSelected()) {
            johnsonCookYoungsModulusTextField.setText(Double.toString(Converter.ksiFromPa(sample.materialYoungsModulus)));
            johnsonCookReferenceYieldStressTextField.setText(Double.toString(Converter.ksiFromPa(sample.referenceYieldStress)));
            johnsonCookYieldStressTextField.setText(Double.toString(Converter.ksiFromPa(sample.yieldStress)));
        } else if (metricRadioButton.isSelected()) {
            johnsonCookYoungsModulusTextField.setText(Double.toString(Converter.MpaFromPa(sample.materialYoungsModulus)));
            johnsonCookReferenceYieldStressTextField.setText(Double.toString(Converter.MpaFromPa(sample.referenceYieldStress)));
            johnsonCookYieldStressTextField.setText(Double.toString(Converter.MpaFromPa(sample.yieldStress)));
        }

        johnsonCookStrainRateTextField.setText(Double.toString(sample.strainRate));
        johnsonCookReferenceStrainRateTextField.setText(Double.toString(sample.referenceStrainRate));
        johnsonCookRoomTemperatureTextField.setText(Double.toString(sample.roomTemperature));
        johnsonCookMeltingPointTextField.setText(Double.toString(sample.meltingTemperature));
        johnsonCookSampleTemperatureTextField.setText(Double.toString(sample.sampleTemperature));
        johnsonCookIntensityCoefficientTextField.setText(Double.toString(sample.intensityCoefficient));
        johnsonCookStrainRateCoefficientTextField.setText(Double.toString(sample.strainRateCoefficient));
        johnsonCookStrainHardeningCoefficientTextField.setText(Double.toString(sample.strainHardeningCoefficient));
        johnsonCookThermalSofteningCoefficientTextField.setText(Double.toString(sample.thermalSofteningCoefficient));

        johnsonCookNameTextField.setText(sample.getName());
    }


    @FXML
    public void johnsonCook6061Clicked() {
        skipRender = true;
        setJohnsonCookModel(new ReferenceSampleJohnsonCook(
                "6061 (Johnson Cook)",
                "",
                68.9e9,
                252e6,
                1.0,

                1.0,
                294.26,
                925.37,
                300.0,
                252e6,
                203.4e6,
                0.011,
                0.35,
                1.34
        ));
        skipRender = false;
        renderJohnsonCook();
    }

    @FXML
    public void johnsonCook7075Clicked() {
        skipRender = true;
        setJohnsonCookModel(new ReferenceSampleJohnsonCook(
                "7075 (Johnson Cook)",
                "",
                71.7e9,
                4.73e8,
                1.0,

                1.0,
                294.26,
                893.0,
                300.0,
                4.73e8,
                2.1e8,
                0.033,
                0.3813,
                1.0
        ));
        skipRender = false;
        renderJohnsonCook();
    }

    @FXML
    public void johnsonCookDqskSteelClicked() {
        skipRender = true;
        setJohnsonCookModel(new ReferenceSampleJohnsonCook(
                "DQSK (Johnson Cook)",
                "",
                206e9,
                13e6,
                1.0,

                1.0,
                294.26,
                1808.0,
                300.0,
                1.3e7,
                7.3e8,
                0.045,
                0.15,
                0.5
        ));
        skipRender = false;
        renderJohnsonCook();
    }

    @FXML
    public void johnsonCookSaveButtonClicked() {
        Optional<ReferenceSampleJohnsonCook> optionalSample = parseJohnsonCookSample();

        optionalSample.ifPresent(sample -> {
            saveReference(sample, johnsonCookNameTextField.getText());

            stage.close();
        });
    }

    private void setLudwikModel(ReferenceSampleLudwig sample) {
        if(englishRadioButton.isSelected()) {
            ludwigYoungsModulusTextField.setText(Double.toString(Converter.ksiFromPa(sample.materialYoungsModulus)));
            ludwigYieldStressTextField.setText(Double.toString(Converter.ksiFromPa(sample.referenceYieldStress)));
        } else if(metricRadioButton.isSelected()) {
            ludwigYoungsModulusTextField.setText(Double.toString(Converter.MpaFromPa(sample.materialYoungsModulus)));
            ludwigYieldStressTextField.setText(Double.toString(Converter.MpaFromPa(sample.referenceYieldStress)));
        } else {
            System.err.println("Not expected, whoops");
        }

        ludwigIntensityCoefficientTextField.setText(Double.toString(sample.intensityCoefficient));
        ludwigStrainHardeningCoefficientTextField.setText(Double.toString(sample.strainHardeningCoefficient));

        ludwigNameTextField.setText(sample.getName());
    }

    @FXML
    public void ludwig6061Clicked() {
        skipRender = true;
        setLudwikModel(new ReferenceSampleLudwig(
                "6061 (Ludwig)",
                "",
                68.9e9,
                252e6,
                208.8e6,
                0.28
        ));
        skipRender = false;
        renderLudwig();
    }

    @FXML
    public void ludwig7075Clicked() {
        skipRender = true;
        setLudwikModel(new ReferenceSampleLudwig(
                "7075 (Ludwig)",
                "",
                7.17e10,
                4.73e8,
                1.295e8,
                0.293
        ));
        skipRender = false;
        renderLudwig();
    }

    @FXML
    public void ludwigSaveButtonClicked() {
        Optional<ReferenceSampleLudwig> optionalSample = parseLudwigSample();

        optionalSample.ifPresent(sample -> {
            saveReference(sample, ludwigNameTextField.getText());

            stage.close();
        });
    }

    private void setCowperSymondsModel(ReferenceSampleCowperSymonds sample) {
        if(englishRadioButton.isSelected()) {
            cowperSymondsYoungsModulusTextField.setText(Double.toString(Converter.ksiFromPa(sample.materialYoungsModulus)));
            cowperSymondsReferenceYieldStressTextField.setText(Double.toString(Converter.ksiFromPa(sample.referenceYieldStress)));
            cowperSymondsYieldStressTextField.setText(Double.toString(Converter.ksiFromPa(sample.yieldStress)));
        } else if(metricRadioButton.isSelected()) {
            cowperSymondsYoungsModulusTextField.setText(Double.toString(Converter.MpaFromPa(sample.materialYoungsModulus)));
            cowperSymondsReferenceYieldStressTextField.setText(Double.toString(Converter.MpaFromPa(sample.referenceYieldStress)));
            cowperSymondsYieldStressTextField.setText(Double.toString(Converter.MpaFromPa(sample.yieldStress)));
        } else {
            System.err.println("Failed to detect english vs metric");
        }

        cowperSymondsStrainRateTextField.setText(Double.toString(sample.strainRate));
        cowperSymondsIntensityCoefficientTextField.setText(Double.toString(sample.intensityCoefficient));
        cowperSymondsStrainRateCoefficientTextField.setText(Double.toString(sample.strainRateCoefficient));
        cowperSymondsStrainHardeningCoefficientTextField.setText(Double.toString(sample.strainHardeningCoefficient));
        cowperSymondsStrainRateSensitivityCoefficientTextField.setText(Double.toString(sample.strainRateSensitivityCoefficient));

        cowperSymondsNameTextField.setText(sample.getName());
    }

    @FXML
    public void cowperSymonds6061Clicked() {
        skipRender = true;

        setCowperSymondsModel(new ReferenceSampleCowperSymonds(
                "6061 (Cowper Symonds)",
                "",
                68.9e9,
                252e6,
                6.7e-4,
                252e6,
                1 * (600e6 * 72e9) / (72e9 - 600e6),
                25000.0,
                1.0,
                0.95
        ));

        skipRender = false;
        renderCowperSymonds();
    }


    @FXML
    public void cowperSymondsSaveButtonClicked() {
        Optional<ReferenceSampleCowperSymonds> optionalSample = parseCowperSymondsSample();

        optionalSample.ifPresent(sample -> {
            saveReference(sample, cowperSymondsNameTextField.getText());

            stage.close();
        });
    }

    private List<Double> toChartUnit(List<Double> stressPa) {
        List<Double> result = new ArrayList<Double>();

        for (int i = 0; i < stressPa.size(); i++) {
            Double val = stressPa.get(i);

            // if english, convert to ksi
            if (englishRadioButton.isSelected()) {
                val = val * 1.45038e-7;
            } else {
                // metric convert to Kpa
                val = val * 1e-6;
            }
            result.add(val);
        }

        return result;
    }


    private StressStrain getStressStrainFromInput() {
        return new StressStrain(toPa(rawStressData), rawStrainData, inputEngineeringRadioButton.isSelected() ? StressStrainMode.ENGINEERING : StressStrainMode.TRUE);
    }

    public void renderAxisLabels() {
        Stream.of(knChart, ludwigChart, cowperSymondsChart, johnsonCookChart).forEach(chart -> {
            chart.getXAxis().setLabel("True Strain (" + (englishRadioButton.isSelected() ? "in/in" : "mm/mm") + ")");
            chart.getYAxis().setLabel("True Stress (" + (englishRadioButton.isSelected() ? "Ksi" : "Mpa") + ")");
        });
    }


    public void renderXY() {

        xyChart.getXAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Strain (" + (englishRadioButton.isSelected() ? "in/in" : "mm/mm") + ")");
        xyChart.getYAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Stress (" + (englishRadioButton.isSelected() ? "Ksi" : "Mpa") + ")");

        xyChart.setTitle("Stress-Strain Reference");
        xyChart.getData().clear();
        xyChart.animatedProperty().setValue(false);

        this.xyDatas.stream().forEach(series -> {
            XYChart.Series<NumberAxis, NumberAxis> chartSeries = new XYChart.Series();
            series.getData().stream().forEach(d -> {
                chartSeries.getData().add(new XYChart.Data(d.getXValue().doubleValue(), d.getYValue().doubleValue()));
            });
            xyChart.getData().add(chartSeries);
        });

        StressStrain inputStressStrain = getStressStrainFromInput();

        StressStrain convertedStressStrain = chartEngineeringRadioButton.isSelected() ? StressStrain.toEngineering(inputStressStrain) : StressStrain.toTrue(inputStressStrain);

        List<Double> chartStress = toChartUnit(convertedStressStrain.getStress());

        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < convertedStressStrain.getStrain().size(); i++) {
            series.getData().add(new XYChart.Data(convertedStressStrain.getStrain().get(i), chartStress.get(i)));
        }

        xyChart.getData().add(series);

        xyChart.setCreateSymbols(false);
    }

    @FXML
    public void importXYClicked() {
        Stage anotherStage = new Stage();
        List<List<String>> output = new ArrayList<List<String>>();
        new Home(anotherStage, output);
        anotherStage.showAndWait();

        int numRows = output.get(0).size();

        rawStrainData.clear();
        rawStressData.clear();
        for (int i = 0; i < numRows; i++) {
            rawStrainData.add(Double.parseDouble(output.get(0).get(i)));
            rawStressData.add(Double.parseDouble(output.get(1).get(i)));
        }

        renderXY();
    }

    public static void saveReference(ReferenceSample s, String name) {
        // Save to file.
        String jsonString = s.getJson();

        SPOperations.writeStringToFile(jsonString, SPSettings.referencesLocation + "/" + name + ".json");
    }

    @FXML
    public void saveButtonClicked() {
        ReferenceSampleXY reference = new ReferenceSampleXY(nameTextField.getText(), getStressStrainFromInput(), null);

        saveReference(reference, nameTextField.getText());

        stage.close();
    }

    public StressStrainMode getStressStrainMode() {
        return chartEngineeringRadioButton.isSelected() ? StressStrainMode.ENGINEERING : StressStrainMode.TRUE;
    }

    public Optional<ReferenceSampleKN> parseKNSample() {
        Double youngsMod;
        Double yieldStress;
        Double K;
        Double N;
        try {
            youngsMod = Double.parseDouble(knYoungsModulusTextField.getText());
            yieldStress = Double.parseDouble(knYieldStressTextField.getText());
            K = Double.parseDouble(knKTextField.getText());
            N = Double.parseDouble(knNTextField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse!");
            return Optional.empty();
        }

        // do unit conversions
        if (englishRadioButton.isSelected()) {
            youngsMod = Converter.paFromKsi(youngsMod);
            yieldStress = Converter.paFromKsi(yieldStress);
            K = Converter.paFromKsi(K);
        } else if (metricRadioButton.isSelected()) {
            youngsMod = Converter.paFromMpa(youngsMod);
            yieldStress = Converter.paFromMpa(yieldStress);
            K = Converter.paFromMpa(K);
        } else {
            System.err.println("Ooops couldn't convert this!");
        }

        ReferenceSampleKN knSample = new ReferenceSampleKN(knNameTextField.getText(), "", K, N, youngsMod, yieldStress);
        return Optional.of(knSample);
    }


    public void renderKN() {
        if(skipRender) {
            return;
        }
        System.out.println("rendering KN");
        renderAxisLabels();

        knChart.getData().clear();
        knChart.animatedProperty().setValue(false);

        this.xyDatas.stream().forEach(series -> {
            XYChart.Series<NumberAxis, NumberAxis> chartSeries = new XYChart.Series();
            series.getData().stream().forEach(d -> {
                chartSeries.getData().add(new XYChart.Data(d.getXValue().doubleValue(), d.getYValue().doubleValue()));
            });
            knChart.getData().add(chartSeries);
        });

        parseKNSample().ifPresent(knSample -> {
            List<Double> chartStress = toChartUnit(knSample.getStress(StressStrainMode.TRUE, StressUnit.PA));
            List<Double> chartStrain = knSample.getStrain(StressStrainMode.TRUE);

            XYChart.Series<NumberAxis, NumberAxis> series = new XYChart.Series();
            for (int i = 0; i < chartStress.size(); i++) {
                series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
            }

            knChart.getData().add(series);
        });



        knChart.setCreateSymbols(false);

    }

    public Optional<ReferenceSampleJohnsonCook> parseJohnsonCookSample() {
        Double YoungsModulus;
        Double ReferenceYieldStress;
        Double StrainRate;

        Double ReferenceStrainRate;
        Double RoomTemperature;
        Double MeltingPoint;
        Double SampleTemperature;
        Double YieldStress;
        Double IntensityCoefficient;
        Double StrainRateCoefficient;
        Double StrainHardeningCoefficient;
        Double ThermalSofteningCoefficient;

        try {
            YoungsModulus = Double.parseDouble(johnsonCookYoungsModulusTextField.getText());
            ReferenceYieldStress = Double.parseDouble(johnsonCookReferenceYieldStressTextField.getText());
            StrainRate = Double.parseDouble(johnsonCookStrainRateTextField.getText());

            ReferenceStrainRate = Double.parseDouble(johnsonCookReferenceStrainRateTextField.getText());
            RoomTemperature = Double.parseDouble(johnsonCookRoomTemperatureTextField.getText());
            MeltingPoint = Double.parseDouble(johnsonCookMeltingPointTextField.getText());
            SampleTemperature = Double.parseDouble(johnsonCookSampleTemperatureTextField.getText());
            YieldStress = Double.parseDouble(johnsonCookYieldStressTextField.getText());
            IntensityCoefficient = Double.parseDouble(johnsonCookIntensityCoefficientTextField.getText());
            StrainRateCoefficient = Double.parseDouble(johnsonCookStrainRateCoefficientTextField.getText());
            StrainHardeningCoefficient = Double.parseDouble(johnsonCookStrainHardeningCoefficientTextField.getText());
            ThermalSofteningCoefficient = Double.parseDouble(johnsonCookThermalSofteningCoefficientTextField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse JC params.");
            return Optional.empty();
        }

        // units yay
        if (englishRadioButton.isSelected()) {
            YoungsModulus = Converter.paFromKsi(YoungsModulus);
            ReferenceYieldStress = Converter.paFromKsi(ReferenceYieldStress);
            YieldStress = Converter.paFromKsi(YieldStress);
        } else if (metricRadioButton.isSelected()) {
            YoungsModulus = Converter.paFromMpa(YoungsModulus);
            ReferenceYieldStress = Converter.paFromMpa(ReferenceYieldStress);
            YieldStress = Converter.paFromMpa(YieldStress);
        } else {
            System.err.println("darn couldn't convert!");
        }

        ReferenceSampleJohnsonCook sample = new ReferenceSampleJohnsonCook(
                johnsonCookNameTextField.getText(),
                "TEST",

                YoungsModulus,
                ReferenceYieldStress,
                StrainRate,

                ReferenceStrainRate,
                RoomTemperature,
                MeltingPoint,
                SampleTemperature,
                YieldStress,
                IntensityCoefficient,
                StrainRateCoefficient,
                StrainHardeningCoefficient,
                ThermalSofteningCoefficient
        );

        return Optional.of(sample);
    }

    public void renderJohnsonCook() {
        if(skipRender) {
            return;
        }
        renderAxisLabels();
        johnsonCookChart.getData().clear();
        johnsonCookChart.animatedProperty().setValue(false);

        this.xyDatas.stream().forEach(series -> {
            XYChart.Series<NumberAxis, NumberAxis> chartSeries = new XYChart.Series();
            series.getData().stream().forEach(d -> {
                chartSeries.getData().add(new XYChart.Data(d.getXValue().doubleValue(), d.getYValue().doubleValue()));
            });
            johnsonCookChart.getData().add(chartSeries);
        });

        Optional<ReferenceSampleJohnsonCook> sampleOptional = parseJohnsonCookSample();

        sampleOptional.ifPresent(sample -> {
            List<Double> chartStress = toChartUnit(sample.getStress(StressStrainMode.TRUE, StressUnit.PA));
            List<Double> chartStrain = sample.getStrain(StressStrainMode.TRUE);

            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < chartStress.size(); i++) {
                series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
            }

            johnsonCookChart.getData().add(series);
        });

        johnsonCookChart.setCreateSymbols(false);
    }

    public Optional<ReferenceSampleLudwig> parseLudwigSample() {
        Double youngsModulus;
        Double yieldStress;
        Double intensityCoefficient;
        Double strainHardeningCoefficient;

        try {
            youngsModulus = Double.parseDouble(ludwigYoungsModulusTextField.getText());
            yieldStress = Double.parseDouble(ludwigYieldStressTextField.getText());

            intensityCoefficient = Double.parseDouble(ludwigIntensityCoefficientTextField.getText());
            strainHardeningCoefficient = Double.parseDouble(ludwigStrainHardeningCoefficientTextField.getText());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        if(englishRadioButton.isSelected()) {
            youngsModulus = Converter.paFromKsi(youngsModulus);
            yieldStress = Converter.paFromKsi(yieldStress);
        } else if(metricRadioButton.isSelected()) {
            youngsModulus = Converter.paFromMpa(youngsModulus);
            yieldStress = Converter.paFromMpa(yieldStress);
        }

        return Optional.of(new ReferenceSampleLudwig(
                ludwigNameTextField.getText(),
                "",
                youngsModulus,
                yieldStress,
                intensityCoefficient,
                strainHardeningCoefficient
        ));

    }

    public void renderLudwig() {
        if(skipRender) {
            return;
        }
        renderAxisLabels();
        ludwigChart.getData().clear();
        ludwigChart.animatedProperty().setValue(false);

        this.xyDatas.stream().forEach(series -> {
            XYChart.Series<NumberAxis, NumberAxis> chartSeries = new XYChart.Series();
            series.getData().stream().forEach(d -> {
                chartSeries.getData().add(new XYChart.Data(d.getXValue().doubleValue(), d.getYValue().doubleValue()));
            });
            ludwigChart.getData().add(chartSeries);
        });

        Optional<ReferenceSampleLudwig> sampleOptional = parseLudwigSample();

        sampleOptional.ifPresent(sample -> {
            List<Double> chartStress = toChartUnit(sample.getStress(StressStrainMode.TRUE, StressUnit.PA));
            List<Double> chartStrain = sample.getStrain(StressStrainMode.TRUE);

            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < chartStress.size(); i++) {
                series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
            }

            ludwigChart.getData().add(series);
        });

        ludwigChart.setCreateSymbols(false);
    }

    public Optional<ReferenceSampleCowperSymonds> parseCowperSymondsSample() {
        Double YoungsModulus;
        Double ReferenceYieldStress;
        Double StrainRate;

        Double YieldStress;
        Double IntensityCoefficient;
        Double StrainRateCoefficient;
        Double StrainHardeningCoefficient;
        Double StrainRateSensitivityCoefficient;

        try {
            YoungsModulus = Double.parseDouble(cowperSymondsYoungsModulusTextField.getText());
            ReferenceYieldStress = Double.parseDouble(cowperSymondsReferenceYieldStressTextField.getText());
            StrainRate = Double.parseDouble(cowperSymondsStrainRateTextField.getText());

            YieldStress = Double.parseDouble(cowperSymondsYieldStressTextField.getText());
            IntensityCoefficient = Double.parseDouble(cowperSymondsIntensityCoefficientTextField.getText());
            StrainRateCoefficient = Double.parseDouble(cowperSymondsStrainRateCoefficientTextField.getText());
            StrainHardeningCoefficient = Double.parseDouble(cowperSymondsStrainHardeningCoefficientTextField.getText());
            StrainRateSensitivityCoefficient = Double.parseDouble(cowperSymondsStrainRateSensitivityCoefficientTextField.getText());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        if(englishRadioButton.isSelected()) {
            YoungsModulus = Converter.paFromKsi(YoungsModulus);
            ReferenceYieldStress = Converter.paFromKsi(ReferenceYieldStress);
            YieldStress = Converter.paFromKsi(YieldStress);
        } else if(metricRadioButton.isSelected()) {
            YoungsModulus = Converter.paFromMpa(YoungsModulus);
            ReferenceYieldStress = Converter.paFromMpa(ReferenceYieldStress);
            YieldStress = Converter.paFromMpa(YieldStress);
        }

        return Optional.of(new ReferenceSampleCowperSymonds(
                cowperSymondsNameTextField.getText(),
                "",

                YoungsModulus,
                ReferenceYieldStress,
                StrainRate,

                YieldStress,
                IntensityCoefficient,
                StrainRateCoefficient,
                StrainHardeningCoefficient,
                StrainRateSensitivityCoefficient
        ));
    }

    public void renderCowperSymonds() {
        if(skipRender) {
            return;
        }

        renderAxisLabels();
        cowperSymondsChart.getData().clear();
        cowperSymondsChart.animatedProperty().setValue(false);

        this.xyDatas.stream().forEach(series -> {
            XYChart.Series<NumberAxis, NumberAxis> chartSeries = new XYChart.Series();
            series.getData().stream().forEach(d -> {
                chartSeries.getData().add(new XYChart.Data(d.getXValue().doubleValue(), d.getYValue().doubleValue()));
            });
            cowperSymondsChart.getData().add(chartSeries);
        });

        Optional<ReferenceSampleCowperSymonds> sampleOptional = parseCowperSymondsSample();

        sampleOptional.ifPresent(sample -> {
            List<Double> chartStress = toChartUnit(sample.getStress(StressStrainMode.TRUE, StressUnit.PA));
            List<Double> chartStrain = sample.getStrain(StressStrainMode.TRUE);

            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < chartStress.size(); i++) {
                series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
            }

            cowperSymondsChart.getData().add(series);
        });

        cowperSymondsChart.setCreateSymbols(false);
    }


    public void renderAll() {
        renderXY();
        renderKN();
        renderJohnsonCook();
        renderLudwig();
        renderCowperSymonds();
    }


}

