package net.relinc.libraries.application;

import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class LineChartWithMarkers<X,Y> extends LineChart<X,Y> {

        private ObservableList<Data<X, Y>> horizontalMarkers;
        private ObservableList<Data<X, Y>> verticalMarkers;
        
        private ObservableList<Data<X, X>> verticalRangeMarkers;

        public LineChartWithMarkers(Axis<X> xAxis, Axis<Y> yAxis) {
            super(xAxis, yAxis);
            horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
            horizontalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
            verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
            
            verticalRangeMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
            verticalRangeMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()}); // 2nd type of the range is X type as well
            verticalRangeMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
        }

        public void addHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (horizontalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
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
            marker.setNode(line );
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
            marker.setNode(line );
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
        public void clearVerticalMarkers(){
        	for(Data<X,Y> d : verticalMarkers){
        		getPlotChildren().remove(d.getNode());
        	}
        	for(Data<X,X> d : verticalRangeMarkers){
        		getPlotChildren().remove(d.getNode());
        	}
        	
        	verticalMarkers.clear();
        	verticalRangeMarkers.clear();
        }
        public void clearHorizontalMarkers(){
        	for(Data<X,Y> d : horizontalMarkers){
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
                rectangle.setX( getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()) + 0.5);  // 0.5 for crispness
                rectangle.setWidth( getXAxis().getDisplayPosition(verticalRangeMarker.getYValue()) - getXAxis().getDisplayPosition(verticalRangeMarker.getXValue()));
                rectangle.setY(0d);
                rectangle.setHeight(getBoundsInLocal().getHeight());
                rectangle.toBack();

            }

        }
        
        public void addVerticalRangeMarker(Data<X, X> marker, Color color) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (verticalRangeMarkers.contains(marker)) return;

            Rectangle rectangle = new Rectangle(0,0,0,0);
            rectangle.setStroke(Color.TRANSPARENT);
            rectangle.setFill(color.deriveColor(1, 1, 1, 0.2));
            
            marker.setNode(rectangle);

            getPlotChildren().add(rectangle);
            verticalRangeMarkers.add(marker);
            setMouseTransparentToEverythingButBackground();
        }
        
        public void setMouseTransparentToEverythingButBackground(){
        	final Node chartBackground = lookup(".chart-plot-background");
            for (Node n: chartBackground.getParent().getChildrenUnmodifiable()) {
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
        

    }
