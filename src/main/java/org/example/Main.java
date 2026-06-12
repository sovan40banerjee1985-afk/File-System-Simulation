package org.example;

public class Main {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();

        fs.mkdir("home");
        fs.cd("home");
        fs.mkdir("user");
        fs.cd("user");
        fs.touch("notes.txt");
        fs.write("notes.txt", "Hello World!");
        fs.mkdir("Documents");
        fs.cd("Documents");
        fs.touch("resume.pdf");
        fs.cd("..");

        fs.symlink("shortcut", "Documents/resume.pdf");

        fs.pwd();
        fs.ls("");
        fs.cat("notes.txt");
        fs.cp("notes.txt", "notes_backup.txt");
        fs.mv("notes_backup.txt", "backup.txt");
        fs.rm("backup.txt");
        fs.find("resume.pdf");

        fs.cd("/");
        fs.tree();
    }
}