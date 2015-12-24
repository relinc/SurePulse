package net.relinc.correlation.staticClasses;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.alg.feature.detect.template.TemplateMatching;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.factory.feature.detect.template.FactoryTemplateMatching;
import boofcv.factory.feature.detect.template.TemplateScoreType;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.Match;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.shapes.Quadrilateral_F64;
import javafx.geometry.Point2D;
import net.relinc.correlation.application.Target;

public final class SPTargetTracker {

	private static ImageUInt8 getBoofImage(File file) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			return null;
		}
		ImageUInt8 image = new ImageUInt8(img.getWidth(), img.getHeight());
		ConvertBufferedImage.convertFrom(img, image);
		return image;
	}

	public static ImageUInt8 threshold(ImageUInt8 input, int threshold) {
		ImageUInt8 binary = new ImageUInt8(input.getWidth(), input.getHeight());
		ThresholdImageOps.threshold(input, binary, threshold, false);
		return binary;
	}

	public static Point2D[] trackTargetImageCorrelate(List<File> imagePaths, int begin, int end, Target target) {
		// a simple tracker using image correlate.
		Point2D[] points = new Point2D[end - begin + 1];

		int binarizaionAdjustments = 0;
		double binarizeValue = target.getThreshold();

		ImageUInt8 refImage = getBoofImage(imagePaths.get(begin));
		ImageUInt8 targetImg = refImage.subimage((int) target.rectangle.getX(), (int) target.rectangle.getY(),
				(int) target.rectangle.getWidth() + (int) target.rectangle.getX(),
				(int) target.rectangle.getHeight() + (int) target.rectangle.getY());
		targetImg = threshold(targetImg, (int) binarizeValue);
		// Image<Gray, Byte> refImage = new Image<Gray, Byte>(imagePaths[0]);

		double previousBinarizeValue = (double) binarizeValue;
		double currentBinarizeAdjustValue = CorrSettings.getBinarizationAdjust();
		Point2D previousPoint = new Point2D(0, 0);
		boolean skipRestOfTrackingAttempt = false;
		int i = begin;
		while (i <= end) {
			if (skipRestOfTrackingAttempt) {
				i = end + 1;
				skipRestOfTrackingAttempt = false;
				continue;
			}
			ImageUInt8 img = getBoofImage(imagePaths.get(i));

			// Image<Gray, Byte> img = new Image<Gray, Byte>(imagePaths[i]);
			if (true) {
				img = img.subimage(0, (int) (target.rectangle.getY() - target.rectangle.getHeight() / 2),
						img.getWidth(), (int) (target.rectangle.getY() + target.rectangle.getHeight() * 3 / 2));
			}

			img = threshold(img, (int) binarizeValue);
			// selectedTargetImageView.setImage(getFXImageFromBoofCVImage(img));
			// runTargetTrackingImageView.setImage(getFXImageFromBoofCVImage(targetImg));

			// img = img.ThresholdBinary(new Gray(binarizeValue), new
			// Gray(255)).Canny(.5, .5);
			// create template matcher.
			// TemplateMatchingIntensity<ImageUInt8> matchIntensity =
			// FactoryTemplateMatching.createIntensity(TemplateScoreType.SUM_DIFF_SQ,
			// ImageUInt8.class);

			// apply the template to the image
			// matchIntensity.process(img, targetImg, null);

			// get the results
			// ImageFloat32 intensity = matchIntensity.getIntensity();

			// System.out.println("Max: " + ImageStatistics.min(intensity));

			TemplateMatching<ImageUInt8> matcher = FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_DIFF_SQ,
					ImageUInt8.class);

			matcher.setTemplate(targetImg, null, 1);
			// apply the template to the image
			matcher.process(img);

			// get the results
			List<Match> matches = matcher.getResults().toList();
			// System.out.println("Matches Length: " + matches.size());
			//matches.stream().forEach(mat -> System.out.print(mat.getX() + ", "));
			// Image<Gray, float> correlate = img.MatchTemplate(target,
			// Emgu.CV.CvEnum.TM_TYPE.CV_TM_CCOEFF);
			// double[] minValues, maxValues;
			// Point[] minLocations, maxLocations;
			// correlate.MinMax(out minValues, out maxValues, out minLocations,
			// out maxLocations);
			Point2D potentialPoint = new Point2D(matches.get(0).x + target.rectangle.getWidth() / 2,
					matches.get(0).y + target.rectangle.getHeight() / 2);
			// System.out.println("Pot: " + potentialPoint.getX() + " , " +
			// potentialPoint.getY());
			// System.out.println("Image Width: " + img.getWidth());
			// if (minLocations.Length != 1 || maxLocations.Length != 1)
			// MessageBox.Show("Weird");
			// MessageBox.Show("Left original image height: " +
			// refImage.Height.ToString() + " cropped image Height: " +
			// leftImg.Height.ToString());
			int pointIdx = i - begin;
			if (false) {
				if (pointIdx != 0) // if its first skip it
				{
					if (potentialPoint.distance(previousPoint) > CorrSettings.stepSize) {
						// Not a valid point, must change the binarization
						binarizeValue = binarizeValue + currentBinarizeAdjustValue;
						currentBinarizeAdjustValue = -currentBinarizeAdjustValue
								+ Math.signum(-currentBinarizeAdjustValue) * CorrSettings.getBinarizationAdjust();
						if (Math.abs(currentBinarizeAdjustValue
								/ CorrSettings.getBinarizationAdjust()) > CorrSettings.binarizeAdjustTriesBeforeFail) {
							// too many tries, failed.
							currentBinarizeAdjustValue = CorrSettings.getBinarizationAdjust();
							binarizeValue = previousBinarizeValue;
							points[pointIdx] = new Point2D(-1, -1);
							// displayImageIndex = i;
							// updateDisplayImage();
							// backgroundWorker1.ReportProgress(0);
							i++;
						}
						continue;
					}
				}
			}
			if (binarizeValue != previousBinarizeValue) {
				// binarize adjustment was made, show off
				binarizaionAdjustments++;
				// backgroundWorker1.ReportProgress(binarizaionAdjustments);
				System.out.println("Binarization adjustments made: " + binarizaionAdjustments);
			}
			previousBinarizeValue = binarizeValue;
			previousPoint = potentialPoint;
			points[pointIdx] = potentialPoint;
			// displayImageIndex = i;
			// updateDisplayImage();
			// backgroundWorker1.ReportProgress(0);
			i++;
			// img.Dispose();
			// correlate.Dispose();

		}
		return points;
	}

	public static Point2D[] trackTargetUnknownAlgo(List<File> imagePaths, int begin, int end, Target target) {
		// Create the tracker. Comment/Uncomment to change the tracker.
		Point2D[] points = new Point2D[end - begin + 1];
		TrackerObjectQuad<ImageUInt8> tracker = FactoryTrackerObjectQuad.circulant(null, ImageUInt8.class);
		// FactoryTrackerObjectQuad.sparseFlow(null,ImageUInt8.class,null);
		// FactoryTrackerObjectQuad.tld(null,ImageUInt8.class);
		// FactoryTrackerObjectQuad.meanShiftComaniciu2003(new
		// ConfigComaniciu2003(), ImageType.ms(3, ImageUInt8.class));
		// FactoryTrackerObjectQuad.meanShiftComaniciu2003(new
		// ConfigComaniciu2003(true),ImageType.ms(3,ImageUInt8.class));

		// Mean-shift likelihood will fail in this video, but is excellent at
		// tracking objects with
		// a single unique color. See ExampleTrackerMeanShiftLikelihood
		// FactoryTrackerObjectQuad.meanShiftLikelihood(30,5,255,
		// MeanShiftLikelihoodType.HISTOGRAM,ImageType.ms(3,ImageUInt8.class));

		// SimpleImageSequence video = media.openVideo(fileName,
		// tracker.getImageType());

		// specify the target's initial location and initialize with the first
		// frame
		Quadrilateral_F64 loc = new Quadrilateral_F64(target.rectangle.getX(), target.rectangle.getY(),
				target.rectangle.getX(), target.rectangle.getY() + target.rectangle.getHeight(),
				target.rectangle.getX() + target.rectangle.getWidth(),
				target.rectangle.getY() + target.rectangle.getHeight(),
				target.rectangle.getX() + target.rectangle.getWidth(), target.rectangle.getY());
		// Quadrilateral_F64 location = new
		// Quadrilateral_F64(211.0,162.0,326.0,153.0,335.0,258.0,215.0,249.0);
		ImageUInt8 refImage = getBoofImage(imagePaths.get(begin));
		ImageUInt8 frame = refImage;
		tracker.initialize(frame, loc);

		// For displaying the results
		// TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
		// gui.setPreferredSize(new
		// Dimension(frame.getWidth(),frame.getHeight()));
		// gui.setBackGround((BufferedImage)video.getGuiImage());
		// gui.setTarget(location,true);
		// ShowImages.showWindow(gui,"Tracking Results", true);

		// Track the object across each video frame and display the results
		// long previous = 0;
		int index = begin;
		while (index <= end) {
			frame = getBoofImage(imagePaths.get(index));

			boolean visible = tracker.process(frame, loc);

			double x = loc.a.getX() + target.rectangle.getWidth() / 2;
			double y = loc.a.getY() + target.rectangle.getHeight() / 2;
			points[index - begin] = new Point2D(x, y);

			// gui.setBackGround((BufferedImage) video.getGuiImage());
			// gui.setTarget(location, visible);
			// gui.repaint();

			// shoot for a specific frame rate
			// long time = System.currentTimeMillis();
			// BoofMiscOps.pause(Math.max(0,80-(time-previous)));
			// previous = time;
			index++;
		}
		return points;
	}
}
