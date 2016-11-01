package net.relinc.libraries.imgdata;

import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;

public class ResizableImage {
	
	private ImageSize originalSize;
	private BufferedImage originalImage;
	
	public static final int SIZE_ORIGINAL = Integer.MAX_VALUE;
	public static final int SIZE_XLARGE = 1200;
	public static final int SIZE_LARGE = 1000;
	public static final int SIZE_MEDIUM = 800;
	public static final int SIZE_SMALL = 500;
	
	public ResizableImage(File image) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(image);
			this.originalImage = img;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(img != null) {
			originalSize = new ImageSize(img.getWidth(), img.getHeight());
		}
	}
	
	public ResizableImage(BufferedImage bufferedImage) {
		if(bufferedImage != null) {
			this.originalImage = bufferedImage;
			originalSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight());
		}
	}
	
	public ImageSize getImageSize(int size) {
		
		if(originalSize == null) return null;
		
		ImageSize resizedImageSize = new ImageSize(originalSize.width, originalSize.height);
		
		if(resizedImageSize.width > size) {
			double hScale = (double)size/resizedImageSize.width*resizedImageSize.height;
			resizedImageSize = new ImageSize(size, (int)hScale);
		}
		
		if(resizedImageSize.height > size) {
			double wScale = (double)size/resizedImageSize.height*resizedImageSize.width;
			resizedImageSize = new ImageSize((int)wScale, size);
		}
		
		resizedImageSize.size = size;
		
		switch (size) {
			case SIZE_ORIGINAL:
				resizedImageSize.sizeName = "Original";
				break;
			case SIZE_XLARGE:
				resizedImageSize.sizeName = "Extra Large";
				break;
			case SIZE_LARGE:
				resizedImageSize.sizeName = "Large";
				break;
			case SIZE_MEDIUM:
				resizedImageSize.sizeName = "Medium";
				break;
			case SIZE_SMALL:
				resizedImageSize.sizeName = "Small";
				break;
			default:
				resizedImageSize.sizeName = "Custom";
				break;
		}
		
		return resizedImageSize;
	}
	
	public ArrayList<ImageSize> getAvailableImageSizes() {
		ArrayList<ImageSize> imageSizes = new ArrayList<>();
		ImageSize original = getImageSize(SIZE_ORIGINAL);
		
		if(original != null) {
			imageSizes.add(original);
			if(original.width > SIZE_XLARGE || original.height > SIZE_XLARGE)
				imageSizes.add(getImageSize(SIZE_XLARGE));
			if(original.width > SIZE_LARGE || original.height > SIZE_LARGE)
				imageSizes.add(getImageSize(SIZE_LARGE));
			if(original.width > SIZE_MEDIUM || original.height > SIZE_MEDIUM)
				imageSizes.add(getImageSize(SIZE_MEDIUM));
			if(original.width > SIZE_SMALL || original.height > SIZE_SMALL)
				imageSizes.add(getImageSize(SIZE_SMALL));
		}
		
		return imageSizes;
	}
	
	public BufferedImage getOriginalImage() {
		return originalImage == null ? null : originalImage;
	}
	
	public static File resizeImage(ResizableImage resizableImage, int size, String resizedImagePath) {
		ImageSize imageSize = resizableImage.getImageSize(size);
		File resizedImage = new File(resizedImagePath);
		try {
			ImageIO.write(Scalr.resize(resizableImage.originalImage, Scalr.Method.ULTRA_QUALITY, imageSize.width, imageSize.height), "JPG", resizedImage);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ImagingOpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resizedImage;
	}
	
}
