package DistributedStorageSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * DirectoryViewer class. This class provides a static method to print directory
 * structure recursively.
 * 
 * @author Hiroshi Arai
 *
 */
public class DirectoryViewer {
	// display file tree
	public static void showDirectory() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Please input directory path>");

		String path = br.readLine();

		FileTreeCreator creator = new FileTreeCreator(path);
		Node root = creator.getRoot();

		FileTreeDisplay display = new FileTreeDisplay(root);
		display.paint();
	}
}
