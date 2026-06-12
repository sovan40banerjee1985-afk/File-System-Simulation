import org.example.FileSystem;
import org.example.TrieNode;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class FileSystemTest {

    FileSystem fs;

    @BeforeEach
    void setUp() {
        fs = new FileSystem();
    }

    @Test
    void testMkdir() {
        fs.mkdir("home");
        assertTrue(fs.current.children.containsKey("home"));
        assertFalse(fs.current.children.get("home").isFile);
    }

    @Test
    void testMkdirDuplicate() {
        fs.mkdir("home");
        fs.mkdir("home");
        assertEquals(1, fs.current.children.size());
    }

    @Test
    void testTouch() {
        fs.touch("notes.txt");
        assertTrue(fs.current.children.containsKey("notes.txt"));
        assertTrue(fs.current.children.get("notes.txt").isFile);
    }

    @Test
    void testTouchDuplicate() {
        fs.touch("notes.txt");
        fs.touch("notes.txt");
        assertEquals(1, fs.current.children.size());
    }

    @Test
    void testCdRelative() {
        fs.mkdir("home");
        fs.cd("home");
        assertEquals("home", fs.current.name);
    }

    @Test
    void testCdAbsolute() {
        fs.mkdir("home");
        fs.cd("home");
        fs.mkdir("user");
        fs.cd("/home/user");
        assertEquals("user", fs.current.name);
    }

    @Test
    void testCdBack() {
        fs.mkdir("home");
        fs.cd("home");
        fs.cd("..");
        assertEquals("/", fs.current.name);
    }

    @Test
    void testCdHome() {
        fs.mkdir("home");
        fs.cd("home");
        fs.cd("~");
        assertEquals("/", fs.current.name);
    }

    @Test
    void testCdIntoFile() {
        fs.touch("notes.txt");
        TrieNode before = fs.current;
        fs.cd("notes.txt");
        assertEquals(before, fs.current);
    }

    @Test
    void testCdNonExistent() {
        TrieNode before = fs.current;
        fs.cd("ghost");
        assertEquals(before, fs.current);
    }

    @Test
    void testWriteAndCat() {
        fs.touch("notes.txt");
        fs.write("notes.txt", "Hello World!");
        assertEquals("Hello World!",
                fs.current.children.get("notes.txt").content);
    }

    @Test
    void testCatNonExistent() {
        assertDoesNotThrow(() -> fs.cat("ghost.txt"));
    }

    @Test
    void testRmFile() {
        fs.touch("notes.txt");
        fs.rm("notes.txt");
        assertFalse(fs.current.children.containsKey("notes.txt"));
    }

    @Test
    void testRmEmptyDir() {
        fs.mkdir("empty");
        fs.rm("empty");
        assertFalse(fs.current.children.containsKey("empty"));
    }

    @Test
    void testRmNonEmptyDir() {
        fs.mkdir("home");
        fs.cd("home");
        fs.touch("notes.txt");
        fs.cd("..");
        fs.rm("home");
        assertTrue(fs.current.children.containsKey("home"));
    }

    @Test
    void testMv() {
        fs.touch("notes.txt");
        fs.mv("notes.txt", "renamed.txt");
        assertFalse(fs.current.children.containsKey("notes.txt"));
        assertTrue(fs.current.children.containsKey("renamed.txt"));
    }

    @Test
    void testCp() {
        fs.touch("notes.txt");
        fs.write("notes.txt", "Hello!");
        fs.cp("notes.txt", "copy.txt");

        assertTrue(fs.current.children.containsKey("copy.txt"));
        assertEquals("Hello!",
                fs.current.children.get("copy.txt").content);

        fs.write("notes.txt", "Changed!");
        assertEquals("Hello!",
                fs.current.children.get("copy.txt").content);
    }

    @Test
    void testSymlink() {
        fs.touch("notes.txt");
        fs.write("notes.txt", "Hello!");
        fs.symlink("link", "notes.txt");

        TrieNode link = fs.current.children.get("link");
        assertNotNull(link.symlinkTarget);
        assertEquals("Hello!", link.symlinkTarget.content);
    }

    @Test
    void testSymlinkCycleDetection() {
        fs.mkdir("dirA");
        fs.cd("dirA");
        fs.symlink("loop", "/dirA");
        assertDoesNotThrow(() -> fs.symlink("loop", "/dirA"));
    }

    @Test
    void testFind() {
        fs.mkdir("home");
        fs.cd("home");
        fs.touch("notes.txt");
        fs.cd("..");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        fs.find("notes.txt");
        assertTrue(out.toString().contains("Found"));
    }

    @Test
    void testFindNotExist() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        fs.find("ghost.txt");
        assertTrue(out.toString().contains("Not found"));
    }

    @Test
    void testTreeDoesNotThrow() {
        fs.mkdir("home");
        fs.cd("home");
        fs.touch("notes.txt");
        fs.cd("..");
        assertDoesNotThrow(() -> fs.tree());
    }

    @Test
    void testPwd() {
        fs.mkdir("home");
        fs.cd("home");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        fs.pwd();
        assertTrue(out.toString().contains("home"));
    }
}