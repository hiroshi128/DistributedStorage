package DistributedStorageSystem;

/* This code is adopted from http://qiita.com/ukiuki/items/1c70a7f907c5ac12b39f (in Japanese) */

import java.util.List;

/**
 * Represent directory as a tree node.
 * This is used for showing directory structure.
 * 
 */
public class DirectoryNode extends Node {
	private List<Node> children;

	public DirectoryNode(String name) {
		super(name);
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}
}