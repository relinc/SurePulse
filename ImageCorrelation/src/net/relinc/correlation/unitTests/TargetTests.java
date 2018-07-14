package net.relinc.correlation.unitTests;


import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import javafx.geometry.Point2D;
import net.relinc.correlation.application.Target;
import net.relinc.correlation.staticClasses.SPTargetTracker;
import net.relinc.correlation.staticClasses.SPTargetTracker.DisplacementDirection;

public class TargetTests {
	
	private Target getTarget(){
		Target tar = new Target();
		tar.pts = new Point2D[]{new Point2D(0, 0), new Point2D(1, 0), new Point2D(2, 0), new Point2D(3,0), new Point2D(4, 0)};
		return tar;
	}
	
	@Test
	public void testDisplacementCalculation(){
		Target tar = getTarget();
		assertTrue(Arrays.equals(SPTargetTracker.calculateDisplacement(tar, 2, false, DisplacementDirection.X), new double[]{0,2,4,6,8}));
	}
	
	@Test
	public void testSpeedCalculation(){
		Target tar = getTarget();
		assertTrue(Arrays.equals(SPTargetTracker.calculateSpeed(tar, 2, false, 10), new double[]{20, 20, 20, 20, 20}));
	}
	
	@Test
	public void testEngineeringStrainCalculation(){
		Target tar1 = getTarget();
		Target tar2 = new Target();
		tar2.pts = new Point2D[tar1.pts.length]; //0s
		for(int i = 0; i < tar2.pts.length; i++)
			tar2.pts[i] = new Point2D(0, 0);
		assertTrue(Arrays.equals(SPTargetTracker.calculateEngineeringStrain(tar1, tar2, 2, false, 3), new double[]{0, 2.0/3, 4.0/3, 6.0/3, 8.0/3}));
	}
}
