package net.relinc.processor.pico;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;

import org.omg.PortableServer.POAPackage.WrongAdapter;

import com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

import javafx.application.Platform;
import javafx.concurrent.Task;
import net.relinc.libraries.splibraries.Settings;
import net.relinc.processor.controllers.TrimDataController;

public class PicoScopeCLI {

	public static final int PICO_VERSION_3000 = 0;
	public static final int PICO_VERSION_4000 = 1;
	public static final int PICO_VERSION_5000 = 2;

	private String picoExcecutablePath;

	public PicoScopeCLI(int picoVersion) {
		switch(picoVersion) {
		case PICO_VERSION_3000:
			picoExcecutablePath = "libs/pico/ps3000con.exe";
			break;
		}
	}

	public void startPico() {
				try {
					ProcessBuilder pb = new ProcessBuilder(picoExcecutablePath);
					pb.redirectError(Redirect.INHERIT);
					pb.redirectErrorStream(true);
					Process p = pb.start();
					/*Process p = pb.start();
					BufferedReader reader = 
							new BufferedReader(new InputStreamReader(p.getInputStream()));
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
					String line = null;
					while ( (line = reader.readLine()) != null) {
						System.out.println(line);
						if(line.contains("Operation:")) {
							System.out.println("Found it!");
							break;
						}
					} */
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
					writer.write("X");
					writer.flush();
					writer.close();
					BufferedReader reader = 
							new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = null;
					while ( (line = reader.readLine()) != null) {
						System.out.println(line);
						if(line.contains("Operation:")) {
							System.out.println("Found it!");
							break;
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				} 
	}

}
