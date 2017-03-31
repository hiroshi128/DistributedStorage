package DistributedStorageSystem;

import java.util.ArrayList;
import java.util.List;

/* This code is adopted from http://qiita.com/ukiuki/items/1c70a7f907c5ac12b39f (in Japanese) */

/**
 * FileTreeDisplay class. This class prints out file tree structure.
 *
 */
public class FileTreeDisplay {
	private static final String L = "  „¤-";
	private static final String T = "  „¥-";
	private static final String SPACE = "    ";
	private static final String PIPE = "  | ";

	private Node root;

	public FileTreeDisplay(Node root) {
		this.root = root;
	}

	public void paint() {
		List<Boolean> isLasts = new ArrayList<>();
		paintNode(root, isLasts);
	}

	public void paintNode(Node node, List<Boolean> isLasts) {
		for (int i = 0; i < isLasts.size(); i++) {
			if (i == isLasts.size() - 1) {
				System.out.print(isLasts.get(i) ? L : T);

			} else {
				System.out.print(isLasts.get(i) ? SPACE : PIPE);
			}
		}

		System.out.println(node.getName());

		if (node instanceof DirectoryNode) {
			List<Node> children = ((DirectoryNode) node).getChildren();
			for (int i = 0; i < children.size(); i++) {
				Node child = children.get(i);
				isLasts.add(i == children.size() - 1);
				paintNode(child, isLasts);
				isLasts.remove(isLasts.size() - 1);
			}
		}
	}
}