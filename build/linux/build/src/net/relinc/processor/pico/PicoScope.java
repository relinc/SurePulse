package net.relinc.processor.pico;

import java.io.File;

public class PicoScope {
	public native short ps5000OpenUnit();
	public native short ps5000CloseUnit(short handle);

	static
	{
		System.loadLibrary("libs/ps5000Wrap");
	}

	public PicoScope() 
	{

	}
}
