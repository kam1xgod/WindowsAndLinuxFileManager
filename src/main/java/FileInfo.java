import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo {
    public enum FileType {
        FILE("F"), DIRECTORY("D"); //declaring file types. is it folder, file or trash?

        private String name;

        public String getName() {
            return name;
        } //getter for name.

        FileType(String name) {
            this.name = name;
        } //constructor.
    }

    private String fileName;
    private FileType type;
    private long size;
    private LocalDateTime lastModified;

    public String getFileName() {
        return fileName;
    }

    public FileType getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString(); //setting file name.
            this.size = Files.size(path); //setting file size.
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE; //setting type. and size of folders is equal -1. for declaring them as DIR type.
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            if (this.fileName.equals("Trash")) { //Trash folder have size of -2.
                this.size = -2L;
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(0)); //last modified time.
        } catch (IOException exception) {
            throw new RuntimeException("File can't be accessed.");
        }
    }
}
