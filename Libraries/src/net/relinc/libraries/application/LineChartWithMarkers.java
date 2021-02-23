package net.relinc.libraries.application;

import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import net.relinc.viewer.GUI.ChartingPreferences;

import javax.swing.*;

public class LineChartWithMarkers<X,Y> extends LineChart<X,Y> {

    public chartDataType xDataType;
    public chartDataType yDataType;

    public enum chartDataType {
        TIME, DISPLACEMENT, LOAD, STRAINRATE, STRAIN, STRESS, DISPLACEMENTRATE, FACEFORCE;
    }

    private ObservableList<Data<X, Y>> horizontalMarkers;
    private ObservableList<Data<X, Y>> verticalMarkers;

    private ObservableList<Data<X, X>> verticalRangeMarkers;
    public LineChartWithMarkers(Axis<X> xAxis, Axis<Y> yAxis, chartDataType xData, chartDataType yData) {
        super(xAxis, yAxis);

        xDataType = xData;
        yDataType = yData;

        horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[]{data.YValueProperty()});
        horizontalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
        verticalMarkers = FXCollections.observableArrayList(data -> new Observable[]{data.XValueProperty()});
        verticalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());

        verticalRangeMarkers = FXCollections.observableArrayList(data -> new Observable[]{data.XValueProperty()});
        verticalRangeMarkers = FXCollections.observableArrayList(data -> new Observable[]{data.YValueProperty()}); // 2nd type of the range is X type as well
        verticalRangeMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
    }

    public void addHorizontalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (horizontalMarkers.contains(marker)) return;
        Line line = new Line();
        marker.setNode(line);
        getPlotChildren().add(line);
        horizontalMarkers.add(marker);
    }

    public void removeHorizontalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        horizontalMarkers.remove(marker);
    }

    public void addVerticalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        line.setFill(Color.GRAY);
        line.setStroke(Color.GRAY);
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
        setMouseTransparentToEverythingButBackground();
    }

    public void addVerticalValueMarker(Data<X, Y> marker, Color col) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        line.setFill(col);
        line.setStroke(col);
        line.setStrokeWidth(2);
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
        setMouseTransparentToEverythingButBackground();
    }

    public void removeVerticalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        verticalMarkers.remove(marker);

    }

    public void clearVerticalMarkers() {
        for (Data<X, Y> d : verticalMarkers) {
            getPlotChildren().remove(d.getNode());
        }
        for (Data<X, X> d : verticalRangeMarkers) {
            getPlotChildren().remove(d.getNode());
        }

        verticalMarkers.clear();
        verticalRangeMarkers.clear();
    }

    public void clearHorizontalMarkers() {
        for (Data<X, Y> d : horizontalMarkers) {
            getPlotChildren().remove(d.getNode());
        }
//        	for(Data<X,X> d : horizontalMarkers){
//        		getPlotChildren().remove(d.getNode());
//        	}

        horizontalMarkers.clear();
        //verticalRangeMarkers.clear();
    }


    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (Data<X, Y> horizontalMarker : horizontalMarkers) {
            Line line = (Line) horizontalMarker.getNode();
            line.setStartX(0);
            line.setEndX(getBoundsInLocal().getWidth());
            line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
            line.setEndY(line.getStartY());
            line.toFront();
        }
        for (Data<X, Y> verticalMarker : verticalMarkers) {
            Line line = (Line) verticalMarker.getNode();
            line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
            line.setEndX(line.getStartX());
            line.setStartY(0d);
            line.setEndY(getBoundsInLocal().getHeight());
            line.toFront();
        }

        for (Data<X, X> verticalRangeMarker : verticalRangeMarkers) {

            Rectangle rectangle = (Rectangle) verticalRangeMarker.getNode();
            rectangle.setX(getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()) + 0.5);  // 0.5 for crispness
            rectangle.setWidth(getXAxis().getDisplayPosition(verticalRangeMarker.getYValue()) - getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()));
            rectangle.setY(0d);
            rectangle.setHeight(getBoundsInLocal().getHeight());
            rectangle.toBack();

        }

    }

    public void addVerticalRangeMarker(Data<X, X> marker, Color color) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalRangeMarkers.contains(marker)) return;

        Rectangle rectangle = new Rectangle(0, 0, 0, 0);
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(color.deriveColor(1, 1, 1, 0.2));

        marker.setNode(rectangle);

        getPlotChildren().add(rectangle);
        verticalRangeMarkers.add(marker);
        setMouseTransparentToEverythingButBackground();
    }

    public void setMouseTransparentToEverythingButBackground() {
        final Node chartBackground = lookup(".chart-plot-background");
        for (Node n : chartBackground.getParent().getChildrenUnmodifiable()) {
            if (n != chartBackground) {
                n.setMouseTransparent(true);
            }
        }
    }

    public void removeVerticalRangeMarker(Data<X, X> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        verticalRangeMarkers.remove(marker);
    }

    public void applyPreferences(ChartingPreferences preference) {
        String defaultTitle = this.getDefaultTitle();
        if (preference.title == null) {
            this.setTitle(defaultTitle);
        } else {
            this.setTitle(preference.title.get());
        }
        if (preference.xMin != null) {
            this.getXAxis().setAutoRanging(false);
            ((NumberAxis) this.getXAxis()).setLowerBound(preference.xMin.get());
        }
        if (preference.xMax != null) {
            this.getXAxis().setAutoRanging(false);
            ((NumberAxis) this.getXAxis()).setUpperBound(preference.xMax.get());
        }
        if (preference.xMin == null & preference.xMax == null)
            this.getXAxis().setAutoRanging(true);
        if (preference.yMin != null) {
            this.getYAxis().setAutoRanging(false);
            ((NumberAxis) this.getYAxis()).setLowerBound(preference.yMin.get());
        }
        if (preference.yMax != null) {
            this.getYAxis().setAutoRanging(false);
            ((NumberAxis) this.getYAxis()).setUpperBound(preference.yMax.get());
        }
        if (preference.yMin == null & preference.yMax == null)
            this.getYAxis().setAutoRanging(true);
    }

    private String getDefaultTitle() {
        String title = "";
        switch (xDataType) {
            case TIME:
                switch (yDataType){
                    case STRESS:
                        title = "Stress Vs Time";
                        break;
                    case STRAIN:
                        title = "Strain Vs Time";
                        break;
                    case STRAINRATE:
                        title = "Strain Rate Vs Time";
                        break;
                    case FACEFORCE:
                        title = "Face Force Vs Time";
                        break;
                    case LOAD:
                        title = "Load Vs Time";
                        break;
                    case DISPLACEMENT:
                        title = "Displacement Vs Time";
                        break;
                    case DISPLACEMENTRATE:
                        title = "Displacement Rate Vs Time";
                }
            case STRAIN:
                title = "Stress Vs Strain";
                break;
            case DISPLACEMENT:
                title = "Load Vs Displacement";
                break;
        }
        return title;
    }

    public void showNewLabelsDialog(ChartingPreferences preference) {
        Stage stage = new Stage();
        stage.setTitle("Edit Labels");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        String oldTitle = this.getTitle();
        Double oldXMin = ((NumberAxis) this.getXAxis()).getLowerBound();
        Double oldXMax = ((NumberAxis) this.getXAxis()).getUpperBound();
        Double oldYMin = ((NumberAxis) this.getYAxis()).getLowerBound();
        Double oldYMax = ((NumberAxis) this.getYAxis()).getUpperBound();

        Label title = new Label("Title");
        TextField newTitle = new TextField();
        Label xAxisLabel = new Label("xAxis");
        Label xMinLabel = new Label("Min");
        TextField newXMin = new TextField();
        Label xMaxLabel = new Label("Max");
        TextField newXMax = new TextField();
        Label yAxisLabel = new Label("yAxis");
        Label yMinLabel = new Label("Min");
        TextField newYMin = new TextField();
        Label yMaxLabel = new Label("Max");
        TextField newYMax = new TextField();

        Button done = new Button("Done");
        Button reset = new Button("Reset");
        done.setDefaultButton(true);
        done.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Double xMin = oldXMin;
                Double xMax = oldXMax;
                Double yMin = oldYMin;
                Double yMax = oldYMax;
                boolean changed = false;
                if (!newTitle.getText().equals("")) {
                    changed = true;
                    preference.setTitle(newTitle.getText());
                }
                if(!newXMin.getText().equals("")) {
                    changed = true;
                    try{
                        xMin = Double.parseDouble(newXMin.getText());
                        preference.setXMin(xMin);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null,
                                "Invalid Input",
                                "Input Error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                if(!newXMax.getText().equals("")) {
                    changed = true;
                    try{
                        xMax = Double.parseDouble(newXMax.getText());
                        preference.setXMax(xMax);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null,
                                "Invalid Input",
                                "Input Error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                if(!newYMin.getText().equals("")) {
                    changed = true;
                    try{
                        yMin = Double.parseDouble(newYMin.getText());
                        preference.setYMin(yMin);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null,
                                "Invalid Input",
                                "Input Error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                if(!newYMax.getText().equals("")) {
                    changed = true;
                    try{
                        yMax = Double.parseDouble(newYMax.getText());
                        preference.setYMax(yMax);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null,
                                "Invalid Input",
                                "Input Error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                if (changed) {
                    if (xMax>xMin & yMax>yMin) {
                        applyPreferences(preference);
                        stage.close();
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Max must be greater than Min",
                                "Input Error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    stage.close();
                }
            }
        });
        reset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newTitle.setText("");
                getXAxis().setAutoRanging(true);
                getYAxis().setAutoRanging(true);
                newXMin.setText("");
                newXMax.setText("");
                newYMin.setText("");
                newYMax.setText("");
                preference.title = null;
                preference.xMin = null;
                preference.xMax = null;
                preference.yMin = null;
                preference.yMax = null;
                applyPreferences(preference);
            }
        });

        newTitle.setPrefWidth(250);
        newXMin.setPrefWidth(100);
        newXMax.setPrefWidth(100);
        newYMin.setPrefWidth(100);
        newYMax.setPrefWidth(100);

        grid.add(title, 0, 0, 1, 1);
        grid.add(newTitle, 1, 0, 3, 1);
        grid.add(xAxisLabel, 0, 2);
        grid.add(xMinLabel, 0, 3);
        grid.add(newXMin, 1, 3);
        grid.add(xMaxLabel, 2, 3);
        grid.add(newXMax, 3, 3);
        grid.add(yAxisLabel, 0, 4);
        grid.add(yMinLabel, 0, 5);
        grid.add(newYMin, 1, 5);
        grid.add(yMaxLabel, 2, 5);
        grid.add(newYMax, 3, 5);
        grid.add(done, 0, 6);
        grid.add(reset, 1, 6);
        Scene scene = new Scene(grid, 400, 240);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
