package net.relinc.libraries.imgdata;

public class ImageSize {
	public int width;
	public int height;
	public String sizeName;
	public int size;
	
	public ImageSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return sizeName + ": (" + width + " x " + height + ")";
	}
}
