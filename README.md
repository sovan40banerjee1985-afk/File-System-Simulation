# File System Simulator

A terminal-style file system simulator built in Java,
using core data structures: Trie, Graph, Queue, and HashMap.

Built as a DSA project to simulate how real operating
systems manage files and directories internally.

---

## Data Structures Used

### Trie — Directory Paths
Every folder and file is a node in a Trie.
Each path like `/home/user/notes.txt` is a journey
from root → home → user → notes.txt through
the children map.

Why Trie?
- Natural fit for hierarchical paths
- Fast prefix-based lookups
- Each node knows exactly its children

### HashMap — Children Storage
Each TrieNode stores its children in a HashMap:
`Map<String, TrieNode> children`

Why HashMap?
- O(1) lookup by name
- Fast insert and delete

### Graph — Symlinks
A symlink is a node that holds a reference
(pointer) to another node anywhere in the tree.
This breaks the pure tree structure and creates
a graph edge between two nodes.

Why Graph?
- Files can point to other files across the tree
- One change reflects everywhere (not a copy)

### Queue — BFS Traversal (tree command)
The `tree` command visits every node level by level
using Breadth First Search with a Queue.

Why Queue?
- BFS processes level by level
- Gives the natural top-down directory view

---

## Commands Supported

| Command         | Description                    |
|-----------------|--------------------------------|
| mkdir name      | Create a directory             |
| touch name      | Create a file                  |
| cd path         | Change directory               |
| ls              | List current directory         |
| pwd             | Print working directory        |
| cat file        | Read file content              |
| write file text | Write content to file          |
| rm name         | Remove file or empty directory |
| mv src dst      | Move or rename                 |
| cp src dst      | Copy a file                    |
| symlink ln tgt  | Create a symlink               |
| tree            | Show full structure (BFS)      |
| find name       | Search for file/dir (BFS)      |

---

## Project Structure

FileSystemSimulator/
├── src/
│   ├── main/java/
│   │   ├── TrieNode.java
│   │   ├── FileSystem.java
│   │   └── Main.java
│   └── test/java/
│       └── FileSystemTest.java
├── pom.xml
└── README.md

---

## How to Run

### Prerequisites
- Java 17+
- Maven

### Run the simulator
```bash
mvn compile
mvn exec:java -Dexec.mainClass="Main"