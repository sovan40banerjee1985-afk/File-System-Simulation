package org.example;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    public String name;
    public boolean isFile;
    public String content;
    public Map<String,TrieNode> children;
    public TrieNode symlinkTarget;
    String permissions;
    long createAt;
    long modifiedAt;

    public TrieNode(String name,boolean isFile) {
        this.name = name;
        this.isFile = isFile;
        this.content = "";
        this.children = new HashMap<String,TrieNode>();
        this.symlinkTarget = null;
        this.permissions = "rwx";
        this.createAt = System.currentTimeMillis();
        this.modifiedAt= System.currentTimeMillis();
    }
}
