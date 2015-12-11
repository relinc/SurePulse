package net.relinc.correlation.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SplashpageController {
	@FXML ImageView runDICImageView;
	@FXML ScrollBar scrollBar;
	@FXML Label imageNameLabel;
	
	public Stage stage;
	public int imageBeginIndex;
	public int imageEndIndex;
	public Point2D beginRectangle = new Point2D(0, 0);
	public Point2D endRectangle = new Point2D(0, 0);
	List<File> imagePaths;
	public double displayImageToRealImageSizeRatio = 1;
	
	@FXML
	public void initialize(){
		runDICImageView.fitWidthProperty().bind(((VBox)runDICImageView.getParent()).widthProperty());
		
		scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    //vb.setLayoutY(-new_val.doubleValue());
            	
                 renderRunROITab();

                    
            }

			
        });
		
		
	}
	@FXML
	public void loadImagesFired(){
		
		FileChooser fileChooser = new FileChooser();
		imagePaths =
                fileChooser.showOpenMultipleDialog(stage);
		resetEverything();
		renderRunROITab();
            
	}
	@FXML
	public void drawROIFired(){
		System.out.println("Fired");
		runDICImageView.fitHeightProperty().unbind();
		runDICImageView.fitWidthProperty().unbind();
		runDICImageView.setFitHeight(10);
		runDICImageView.setFitWidth(10);
		runDICImageView.setFitHeight(-1);
		runDICImageView.setFitWidth(-1);
		System.out.println(runDICImageView.getParent().getParent());
		
		//runDICImageView.fitWidthProperty().bind(stage.widthProperty()); 
		if(runDICImageView.getImage().getHeight() / runDICImageView.getImage().getWidth() > ((AnchorPane)runDICImageView.getParent().getParent()).heightProperty().doubleValue() / runDICImageView.getScene().widthProperty().doubleValue()){
			System.out.println("Fitting based on height");
			//runDICImageView.setFitHeight(runDICImageView.getParent().prefHeight(0));
			runDICImageView.fitHeightProperty().bind(((AnchorPane)runDICImageView.getParent().getParent()).heightProperty());
			
		}
		else	
		{
			System.out.println("Fitting based on width");
			runDICImageView.fitWidthProperty().bind(runDICImageView.getScene().widthProperty());
		}
		
	}
	@FXML
	public void setBeginFired(){
		System.out.println("Set begin fired");
		imageBeginIndex = (int)scrollBar.getValue();
		renderRunROITab();
	}
	@FXML
	public void setEndFired(){
		System.out.println("Set end fired");
		imageEndIndex = (int)scrollBar.getValue();
		renderRunROITab();
	}
	public void imageViewMousePressedFired(MouseEvent e){
		System.out.println("Moused pressed: " + e.getX() + "," + e.getY());
		beginRectangle = new Point2D(e.getX(), e.getY());
		endRectangle = new Point2D(e.getX(), e.getY());
		renderRunROITab();
	}
	public void imageViewMouseReleasedFired(MouseEvent e){
		//System.out.println("Mouse Released: " + e.getX() + "," + e.getY());
	}
	public void imageViewMouseDraggedFired(MouseEvent e){
		//System.out.println("Mouse Dragged: " + e.getX() + "," + e.getY());
		endRectangle = new Point2D(e.getX(), e.getY());
		renderRunROITab();
	}
	@FXML
	public void runDICButtonFired(){
		double sizeRatio = runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight();
		Rectangle rect = getRectangleFromPoints(beginRectangle, endRectangle, 1 / sizeRatio);
		Image currentImage = runDICImageView.getImage();
		WritableImage wImage = new WritableImage(
                (int)currentImage.getWidth(),
                (int)currentImage.getHeight());
        PixelWriter pixelWriter = wImage.getPixelWriter();
        
        for(int readY=0; readY < currentImage.getHeight(); readY++){
            for(int readX=0; readX < currentImage.getWidth(); readX++){
            	if(readX >= rect.getMinX() && readX <= rect.getMaxX() 
            			&& readY >= rect.getMinY() && readY <= rect.getMaxY())
            		pixelWriter.setColor(readX,readY,new javafx.scene.paint.Color(1, 1, 1, 1));
            	else
            		pixelWriter.setColor(readX, readY, new javafx.scene.paint.Color(0, 0, 0, 1));
            }
        }
        runDICImageView.setImage(wImage);
        
	}
	
	private void resetEverything() {
		imageBeginIndex = 0;
		imageEndIndex = imagePaths.size() - 1;
	}

	private void renderRunROITab() {
		scrollBar.setMin(imageBeginIndex);
		scrollBar.setMax(imageEndIndex);

		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
		} catch (IOException e) {
		}
		
		Graphics2D g2d = img.createGraphics();
	    
		
		System.out.println(runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight());
		double sizeRatio = runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight();
		   // Draw on the buffered image
		   g2d.setColor(Color.red);
		   g2d.setStroke(new BasicStroke(Math.max(img.getHeight() / 200 + 1, img.getWidth()/200 + 1)));
		   Rectangle rect = getRectangleFromPoints(beginRectangle, endRectangle, 1/sizeRatio);
		   g2d.draw(rect);
		   g2d.dispose();

        //ImageIO.write(img, "jpg", new File("images/template.jpg"));
        //runDICImageView.setImage(new Image(imagePaths.get((int)scrollBar.getValue()).toURI().toString()));
        runDICImageView.setImage(SwingFXUtils.toFXImage(img,null));
        //runDICImageView.setFitHeight(200);
        imageNameLabel.setText(imagePaths.get((int)scrollBar.getValue()).getName());
	}

	private Rectangle getRectangleFromPoints(Point2D p1, Point2D p2, double sizeRatio) {
		p1 = p1.multiply(sizeRatio);
		p2 = p2.multiply(sizeRatio);
		
		int height = (int)Math.abs(p1.getY() - p2.getY());
		int width = (int)Math.abs(p1.getX() - p2.getX());
		
		int startX = (int)Math.min(p1.getX(), p2.getX());
		int startY = (int)Math.min(p1.getY(), p2.getY());

		return new Rectangle(startX, startY, width, height);

		
	}
	
}
