package net.relinc.libraries.splibraries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Operations {
	public static void writeStringToFile(String file, String path){
		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			writer.write(file);

			//Close writer
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeListToFile(ArrayList<String> list, String path){
		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
			for(String s : list) {
				writer.write(s);
			}

			//Close writer
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String readStringFromFile(String path){
		String text = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			text = sb.toString();
			br.close();
		}
		catch(IOException a){
			a.printStackTrace();
		}
		return text;
	}

	public static void unzip(){
		String source = "some/compressed/file.zip";
		String destination = "some/destination/folder";
		String password = "password";

		try {
			ZipFile zipFile = new ZipFile(source);
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(password);
			}
			zipFile.extractAll(destination);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}



	public static void deleteFolder(File folder) {

	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

	public static void findFiles(File dir, TreeItem<String> parent, TreeView treeView, String folderPath, String filePath) {
		TreeItem<String> root = new TreeItem<>(dir.getName(), getIcon(folderPath));
		root.setExpanded(true);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				findFiles(file,root,treeView,folderPath,filePath);
			} else {
				if(file.getName().endsWith(".txt"))
					root.getChildren().add(new TreeItem<>(file.getName().substring(0, file.getName().length() - 4), getIcon(filePath)));
			}
		}
		if(parent==null){
			treeView.setRoot(root);
		} else {

			parent.getChildren().add(root);
		}
	} 
	public static Node getIcon(String location){
		ImageView rootIcon = new ImageView(
				new Image(Operations.class.getResourceAsStream(location))//"/images/folderIcon.jpeg"))
				);
		double height = rootIcon.getImage().getHeight();
		double width = rootIcon.getImage().getWidth();
		if(height / width > 1){
			//it's taller than wide
			rootIcon.setFitWidth(16 * (width / height));
			rootIcon.setFitHeight(16);
		}
		else{
			//it's wider than tall
			rootIcon.setFitHeight(16 * (height / width));
			rootIcon.setFitWidth(16);
		}
		//rootIcon.setFitHeight(16);
		//rootIcon.setFitWidth(16);
		Node a = rootIcon;
		return a;
	}
}
