package org.example;
import java.util.*;

public class FileSystem {
    private TrieNode root;
    public TrieNode current;
    private List<String> currentPath;

    public FileSystem() {
        root = new TrieNode("/", false);
        current = root;
        currentPath = new ArrayList<>(List.of("/"));
    }

    private String[] parsePath(String path) {
        String raw = path.startsWith("/") ? path.substring(1) : path;
        return Arrays.stream(raw.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);
    }

    private TrieNode traverse(String path) {
        String[] parts = parsePath(path);
        TrieNode node = path.startsWith("/") ? root : current;
        List<String> tempPath = path.startsWith("/")
                ? new ArrayList<>(List.of("/"))
                : new ArrayList<>(currentPath);

        for (String part : parts) {
            if (part.equals("..")) {
                if (tempPath.size() > 1) {
                    tempPath.remove(tempPath.size() - 1);
                    node = root;
                    for (int i = 1; i < tempPath.size(); i++) {
                        node = node.children.get(tempPath.get(i));
                    }
                }
            } else if (part.equals(".")) {
                continue;
            } else {
                if (!node.children.containsKey(part)) {
                    System.out.println("Error: No such file or directory: " + part);
                    return null;
                }
                node = node.children.get(part);
                if (node.symlinkTarget != null) {
                    node = node.symlinkTarget;
                }
                tempPath.add(part);
            }
        }
        return node;
    }

    public void pwd() {
        System.out.println(String.join("/", currentPath).replace("//", "/"));
    }

    public void mkdir(String name) {
        if (current.children.containsKey(name)) {
            System.out.println("mkdir: " + name + " already exists");
            return;
        }
        current.children.put(name, new TrieNode(name, false));
        System.out.println("Directory created: " + name);
    }

    public void touch(String name) {
        if (current.children.containsKey(name)) {
            System.out.println("touch: " + name + " already exists");
            return;
        }
        current.children.put(name, new TrieNode(name, true));
        System.out.println("File created: " + name);
    }

    public void cd(String path) {
        if (path.equals("~")) {
            current = root;
            currentPath = new ArrayList<>(List.of("/"));
            return;
        }

        String[] parts = parsePath(path);
        TrieNode node = path.startsWith("/") ? root : current;
        List<String> tempPath = path.startsWith("/")
                ? new ArrayList<>(List.of("/"))
                : new ArrayList<>(currentPath);

        for (String part : parts) {
            if (part.equals("..")) {
                if (tempPath.size() > 1) {
                    tempPath.remove(tempPath.size() - 1);
                    node = root;
                    for (int i = 1; i < tempPath.size(); i++) {
                        node = node.children.get(tempPath.get(i));
                    }
                }
            } else if (part.equals(".")) {
                continue;
            } else {
                if (!node.children.containsKey(part)) {
                    System.out.println("cd: " + part + ": No such directory");
                    return;
                }
                TrieNode next = node.children.get(part);
                if (next.symlinkTarget != null) next = next.symlinkTarget;
                if (next.isFile) {
                    System.out.println("cd: " + part + ": Not a directory");
                    return;
                }
                node = next;
                tempPath.add(part);
            }
        }
        current = node;
        currentPath = tempPath;
    }

    public void ls(String path) {
        TrieNode node = path.isEmpty() ? current : traverse(path);
        if (node == null) return;
        if (node.isFile) {
            System.out.println("[FILE] " + node.name);
            return;
        }
        if (node.children.isEmpty()) {
            System.out.println("(empty directory)");
            return;
        }
        for (Map.Entry<String, TrieNode> entry : node.children.entrySet()) {
            String type = entry.getValue().isFile ? "FILE" : "DIR ";
            System.out.println("  [" + type + "] " + entry.getKey());
        }
    }

    public void cat(String name) {
        if (!current.children.containsKey(name)) {
            System.out.println("cat: " + name + ": No such file");
            return;
        }
        TrieNode node = current.children.get(name);
        if (!node.isFile) {
            System.out.println("cat: " + name + ": Is a directory");
            return;
        }
        System.out.println(node.content.isEmpty() ? "(empty file)" : node.content);
    }

    public void write(String name, String content) {
        if (!current.children.containsKey(name)) {
            System.out.println("write: " + name + ": No such file");
            return;
        }
        TrieNode node = current.children.get(name);
        if (!node.isFile) {
            System.out.println("write: " + name + ": Is a directory");
            return;
        }
        node.content = content;
        node.modifiedAt = System.currentTimeMillis();
    }

    public void rm(String name) {
        if (!current.children.containsKey(name)) {
            System.out.println("rm: " + name + ": No such file or directory");
            return;
        }
        TrieNode node = current.children.get(name);
        if (!node.isFile && !node.children.isEmpty()) {
            System.out.println("rm: " + name + ": Directory not empty");
            return;
        }
        current.children.remove(name);
        System.out.println("Removed: " + name);
    }

    public void mv(String srcName, String dstName) {
        if (!current.children.containsKey(srcName)) {
            System.out.println("mv: " + srcName + ": No such file or directory");
            return;
        }
        TrieNode node = current.children.get(srcName);
        current.children.remove(srcName);
        current.children.put(dstName, node);
        node.name = dstName;
        System.out.println("Moved: " + srcName + " → " + dstName);
    }

    public void cp(String srcName, String dstName) {
        if (!current.children.containsKey(srcName)) {
            System.out.println("cp: " + srcName + ": No such file");
            return;
        }
        TrieNode src = current.children.get(srcName);
        if (!src.isFile) {
            System.out.println("cp: " + srcName + ": Is a directory");
            return;
        }
        TrieNode copy = new TrieNode(dstName, true);
        copy.content = src.content;
        copy.permissions = src.permissions;
        current.children.put(dstName, copy);
        System.out.println("Copied: " + srcName + " → " + dstName);
    }

    public void symlink(String linkName, String targetPath) {
        TrieNode target = traverse(targetPath);
        if (target == null) return;

        if (hasCycle(target, new HashSet<>())) {
            System.out.println("symlink: cycle detected!");
            return;
        }
        TrieNode link = new TrieNode(linkName, false);
        link.symlinkTarget = target;
        current.children.put(linkName, link);
        System.out.println("Symlink created: " + linkName + " → " + targetPath);
    }

    private boolean hasCycle(TrieNode node, Set<TrieNode> visited) {
        if (visited.contains(node)) return true;
        visited.add(node);
        if (node.symlinkTarget != null) {
            return hasCycle(node.symlinkTarget, visited);
        }
        return false;
    }

    public void tree() {
        Queue<TrieNode[]> queue = new LinkedList<>();
        queue.offer(new TrieNode[]{current});
        Map<TrieNode, Integer> depthMap = new HashMap<>();
        depthMap.put(current, 0);

        while (!queue.isEmpty()) {
            TrieNode node = queue.poll()[0];
            int depth = depthMap.get(node);
            String indent = "    ".repeat(depth);
            String icon = node.isFile ? "📄" : "📁";
            System.out.println(indent + icon + " " + node.name);

            for (TrieNode child : node.children.values()) {
                depthMap.put(child, depth + 1);
                queue.offer(new TrieNode[]{child});
            }
        }
    }

    public void find(String name) {
        Queue<TrieNode> queue = new LinkedList<>();
        Queue<String> pathQueue = new LinkedList<>();
        queue.offer(current);
        pathQueue.offer(String.join("/", currentPath));

        boolean found = false;
        while (!queue.isEmpty()) {
            TrieNode node = queue.poll();
            String path = pathQueue.poll();

            if (node.name.equals(name)) {
                System.out.println("Found: " + path);
                found = true;
            }
            for (Map.Entry<String, TrieNode> entry : node.children.entrySet()) {
                queue.offer(entry.getValue());
                pathQueue.offer(path + "/" + entry.getKey());
            }
        }
        if (!found) System.out.println("find: " + name + ": Not found");
    }
}
