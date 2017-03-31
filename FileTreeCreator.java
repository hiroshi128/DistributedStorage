package DistributedStorageSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* This code is adopted from http://qiita.com/ukiuki/items/1c70a7f907c5ac12b39f (in Japanese) */
/**
 * FileTreeCreator class. This class create a tree structure below path argument.
 *
 */
public class FileTreeCreator {
	private Node root;

	public FileTreeCreator(String path) {
		root = getNode(new File(path));
	}

	public Node getNode(File rootFile) {
		Node node = null;

		if (!rootFile.isDirectory()) {
			node = new FileNode(rootFile.getName());

		} else {
			DirectoryNode dir = new DirectoryNode(rootFile.getName());

			List<Node> children = new ArrayList<>();
			File[] childfiles = rootFile.listFiles();
			for (File child : childfiles) {
				children.add(getNode(child));
			}
			dir.setChildren(children);

			node = dir;
		}

		return node;
	}

	public Node getRoot() {
		return root;
	}
}