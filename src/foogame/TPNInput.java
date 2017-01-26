package foogame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TPNInput {

	public static void main(String args[]) throws IOException {
		List<File> filesInFolder = Files.walk(Paths.get("D:\\Workspace\\Tak\\IncomingPTN")).filter(Files::isRegularFile)
				.map(Path::toFile).collect(Collectors.toList());

		for (File f : filesInFolder) {
			System.out.println(f.toString());
			processFile(f);
		}
	}

	private static void processFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		List<String> lines = new ArrayList<String>();
		String line = in.readLine();
		while (line != null) {
			// process out lines and stuff
			if (line.length() != 0 && line.charAt(0) != '[') {
				lines.add(line);
			}
			line = in.readLine();
		}
		doMovesAndThings(lines);
	}
	
	private static void doMovesAndThings(List<String> lines) {
		for (String line : lines) {
			// removes the "1. " part of the move line
			line = line.substring(line.indexOf(" ")+1);
			//System.out.println(line);
			String whiteMove = line.substring(0, line.indexOf(" "));
			String blackMove = line.substring(line.indexOf(" ")+1);
			System.out.printf("White's Move: %s%n", whiteMove);
			System.out.printf("Blacks's Move: %s%n", blackMove);
		}
		//System.out.println(lines.size());
	}

}
