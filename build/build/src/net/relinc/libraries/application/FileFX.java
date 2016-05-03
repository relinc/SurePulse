package net.relinc.libraries.application;

import java.io.File;

import net.relinc.libraries.staticClasses.SPOperations;

public class FileFX{
	public File file;
	
	public FileFX(File f) {
		file = f;
	}
	
	@Override
	public String toString(){
		if(file == null)
			return "";
		return SPOperations.stripExtension(file.getName());
	}
}
