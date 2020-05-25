package net.relinc.libraries.staticClasses;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.math3.util.Pair;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.relinc.libraries.sample.Sample;

public final class ImageOps {

	public static String getFFmpegLocation() {
		if(SPSettings.currentOS.contains("Win")){
			return "lib/ffmpeg.exe";
		} else {
			return "lib/ffmpeg";
		}
	}

	public static void exportImagesToVideo(String imagesString, String videoExportString, double frameRate) {

		ProcessBuilder pb;
		Process p;

		if(SPSettings.currentOS.contains("Win")){

		}
		else{
			String[] grantPermission = {"chmod", "777", getFFmpegLocation()};
			pb = new ProcessBuilder(grantPermission);
			try {
				p = pb.start();
				p.waitFor();
				p.destroyForcibly();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String[] command = {getFFmpegLocation(), "-framerate", Double.toString(frameRate), "-i", imagesString, "-pix_fmt", "yuv420p", videoExportString};

		for(int i = 0; i < command.length; i++)
			System.out.println(command[i]);
		File errorFile = new File(SPSettings.applicationSupportDirectory + "/RELFX/ffmpegErrorFile.txt"); 
		pb = new ProcessBuilder(command);
		pb.redirectError(errorFile);
		try {
			p = pb.start();
			p.waitFor();
			p.destroyForcibly();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void exportVideoToImages(String videoLocation, String tempImagesExportLocation, double frameRate) {
		//ffmpeg -i video.webm -vf fps=1 image-%03d.png

		ProcessBuilder pb;
		Process p;

		if(SPSettings.currentOS.contains("Win")){

		}
		else{
			String[] grantPermission = {"chmod", "777", getFFmpegLocation()};
			pb = new ProcessBuilder(grantPermission);
			try {
				p = pb.start();
				p.waitFor();
				p.destroyForcibly();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

			String[] command = {getFFmpegLocation(),"-i", videoLocation,  tempImagesExportLocation + "/im-%04d.png"};
			for(int i = 0; i < command.length; i++)
				System.out.println(command[i]);
			File errorFile = new File(SPSettings.applicationSupportDirectory + "/RELFX/ffmpegErrorFile.txt"); 
			pb = new ProcessBuilder(command);
			pb.redirectError(errorFile);
			try {
				p = pb.start();
				p.waitFor();
				p.destroyForcibly();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public static void copyImagesToSampleFile(File savedImagesLocation, String sampleZipPath) {
		File tempDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/");
		SPOperations.deleteFolder(tempDir);
		tempDir.mkdirs();


		ZipFile imageZipFile = null;
		try {
			File imagesZipFile = new File(savedImagesLocation.getParent() + "/Images" + ".zip");
			if(imagesZipFile.exists())
				imagesZipFile.delete();

			imageZipFile = new ZipFile(imagesZipFile);
			// Initiate Zip Parameters which define various properties such
			// as compression method, etc.
			ZipParameters imageZipParameters = new ZipParameters();

			// set compression method to store compression
			imageZipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

			// Set the compression level
			imageZipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			for(File image : savedImagesLocation.listFiles())
				imageZipFile.addFile(image, imageZipParameters);

		}
		catch(Exception e){
			e.printStackTrace();
		}

		ZipParameters parameters = new ZipParameters();

		// set compression method to store compression
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

		// Set the compression level
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		ZipFile sampleZip;
		try {
			sampleZip = new ZipFile(sampleZipPath);
			sampleZip.addFile(imageZipFile.getFile(), parameters);
			//			if(barSetup.IncidentBar != null && barSetup.TransmissionBar != null)
			//				sampleZip.addFile(barSetup.createZipFile(tempDir + "/" + barSetup.name).getFile(), parameters);
		} catch (ZipException e) {
			e.printStackTrace();
		}

		imageZipFile.getFile().delete();
	}

	public static File extractSampleImagesToDirectory(Sample sampleZipFile, File tempImageLoadLocation) {

		File tempUnzippedSample = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse" + "/TempViewerUnzipLocation");

		if(tempUnzippedSample.exists())
			SPOperations.deleteFolder(tempUnzippedSample);
		tempUnzippedSample.mkdir();

		ZipFile zippedSample;
		File imagesUnzipLocation = null;
		try {
			zippedSample = new ZipFile(sampleZipFile.loadedFromLocation);
			zippedSample.extractAll(tempUnzippedSample.getPath());

			ZipFile imagesZipFile = new ZipFile(new File(tempUnzippedSample + "/Images.zip"));
			imagesUnzipLocation = new File(tempUnzippedSample.getPath() + "/Images");

			imagesZipFile.extractAll(imagesUnzipLocation.getPath());

		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return imagesUnzipLocation;

	}

	public enum PlacementPosition {
		TOPLEFT, TOPCENTER, TOPRIGHT, MIDDLELEFT, MIDDLECENTER, MIDDLERIGHT, BOTTOMLEFT, BOTTOMCENTER, BOTTOMRIGHT
	}

	/**
	 * Generate a watermarked image.
	 * 
	 * @param originalImage
	 * @param watermarkImage
	 * @param position
	 * @param watermarkSizeMaxPercentage
	 * @return image with watermark
	 * @throws IOException
	 */
	public static BufferedImage watermark(BufferedImage originalImage,
			BufferedImage watermarkImage, PlacementPosition position,
			double watermarkSizeMaxPercentage) throws IOException {

		int imageWidth = originalImage.getWidth();
		int imageHeight = originalImage.getHeight();

		int watermarkWidth = getWatermarkWidth(originalImage, watermarkImage,
				watermarkSizeMaxPercentage);
		int watermarkHeight = getWatermarkHeight(originalImage, watermarkImage,
				watermarkSizeMaxPercentage);

		// We create a new image because we want to keep the originalImage
		// object intact and not modify it.
		//	    BufferedImage bufferedImage = new BufferedImage(imageWidth,
		//	            imageHeight, BufferedImage.TYPE_INT_RGB);
		BufferedImage bufferedImage = originalImage;
		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
		//	    g2d.drawImage(originalImage, 0, 0, null);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int x = 0;
		int y = 0;
		if (position != null) {
			switch (position) {
			case TOPLEFT:
				x = 0;
				y = 0;
				break;
			case TOPCENTER:
				x = (imageWidth / 2) - (watermarkWidth / 2);
				y = 0;
				break;
			case TOPRIGHT:
				x = imageWidth - watermarkWidth;
				y = 0;
				break;

			case MIDDLELEFT:
				x = 0;
				y = (imageHeight / 2) - (watermarkHeight / 2);
				break;
			case MIDDLECENTER:
				x = (imageWidth / 2) - (watermarkWidth / 2);
				y = (imageHeight / 2) - (watermarkHeight / 2);
				break;
			case MIDDLERIGHT:
				x = imageWidth - watermarkWidth;
				y = (imageHeight / 2) - (watermarkHeight / 2);
				break;

			case BOTTOMLEFT:
				x = 0;
				y = imageHeight - watermarkHeight;
				break;
			case BOTTOMCENTER:
				x = (imageWidth / 2) - (watermarkWidth / 2);
				y = imageHeight - watermarkHeight;
				break;
			case BOTTOMRIGHT:
				x = imageWidth - watermarkWidth;
				y = imageHeight - watermarkHeight;
				break;

			default:
				break;
			}
		}

		//System.out.println("Drawing watermark with width: " + watermarkWidth + " and height: " + watermarkHeight + " on image of width: " + imageWidth);
		g2d.drawImage(Scalr.resize(watermarkImage, Method.ULTRA_QUALITY,
				watermarkWidth, watermarkHeight), x, y, null);

		return bufferedImage;

	}

	/**
	 * 
	 * @param originalImage
	 * @param watermarkImage
	 * @param maxPercentage
	 * @return
	 */
	private static Pair<Double, Double> calculateWatermarkDimensions(
			BufferedImage originalImage, BufferedImage watermarkImage,
			double maxPercentage) {

		double imageWidth = originalImage.getWidth();
		double imageHeight = originalImage.getHeight();

		double maxWatermarkWidth = imageWidth / 100.0 * maxPercentage;
		double maxWatermarkHeight = imageHeight / 100.0 * maxPercentage;

		double watermarkWidth = watermarkImage.getWidth();
		double watermarkHeight = watermarkImage.getHeight();

		if (watermarkWidth > maxWatermarkWidth) {
			double aspectRatio = watermarkWidth / watermarkHeight;
			watermarkWidth = maxWatermarkWidth;
			watermarkHeight = watermarkWidth / aspectRatio;
		}

		if (watermarkHeight > maxWatermarkHeight) {
			double aspectRatio = watermarkWidth / watermarkHeight;
			watermarkHeight = maxWatermarkHeight;
			watermarkWidth = watermarkHeight / aspectRatio;
		}
		return new Pair<Double, Double>(watermarkWidth, watermarkHeight);
		//return Pair.of(watermarkWidth, watermarkHeight);
	}

	/**
	 * 
	 * @param originalImage
	 * @param watermarkImage
	 * @param maxPercentage
	 * @return
	 */
	public static int getWatermarkWidth(BufferedImage originalImage,
			BufferedImage watermarkImage, double maxPercentage) {

		return calculateWatermarkDimensions(originalImage, watermarkImage,
				maxPercentage).getFirst().intValue();

	}

	/**
	 * 
	 * @param originalImage
	 * @param watermarkImage
	 * @param maxPercentage
	 * @return
	 */
	public static int getWatermarkHeight(BufferedImage originalImage,
			BufferedImage watermarkImage, double maxPercentage) {

		return calculateWatermarkDimensions(originalImage, watermarkImage,
				maxPercentage).getSecond().intValue();

	}

	public static BufferedImage getImageWithEvenHeightAndWidth(BufferedImage buf) {
		int height = buf.getHeight() % 2 == 1 ? buf.getHeight() - 1 : buf.getHeight();
		int width = buf.getWidth() % 2 == 1 ? buf.getWidth() - 1 : buf.getWidth();
		buf = buf.getSubimage(0, 0, width, height);
		return buf;
	}
}
