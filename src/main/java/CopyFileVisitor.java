import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private Path sourcePath = null;

    public CopyFileVisitor(Path targetPath) {
        this.targetPath = targetPath;
    } //constructor.

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException { //Invoked for a directory before entries in the directory are visited.
        sourcePath = dir;
        Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir))); //creating directory with the same name as source directory.
        return FileVisitResult.CONTINUE; //returning CONTINUE. for future iterations.
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Files.copy(file, targetPath.resolve(sourcePath.relativize(file))); //copying all files in folder.
        return FileVisitResult.CONTINUE;
    }
}
