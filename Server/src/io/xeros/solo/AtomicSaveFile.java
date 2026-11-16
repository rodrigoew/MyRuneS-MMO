package io.xeros.solo;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.charset.Charset;

/** Never truncate the last complete save while producing its replacement. */
public final class AtomicSaveFile {
    private AtomicSaveFile() {}
    public static Path begin(Path target) throws IOException {
        Files.createDirectories(target.toAbsolutePath().getParent());
        return Files.createTempFile(target.toAbsolutePath().getParent(), target.getFileName()+".", ".pending");
    }
    public static void commit(Path pending, Path target) throws IOException {
        try (FileChannel file = FileChannel.open(pending, StandardOpenOption.WRITE)) { file.force(true); }
        for(int attempt=0;;attempt++){
            try{replace(pending,target);return;}
            catch(FileSystemException locked){
                // Windows can briefly deny replacement while an external reader
                // holds the previous save. Keep that complete file and the same
                // fully-written replacement; never fall back to truncating it.
                if(attempt==5||Files.notExists(pending))throw locked;
                try{Thread.sleep(10L<<attempt);}
                catch(InterruptedException interrupted){
                    Thread.currentThread().interrupt();
                    InterruptedIOException failure=new InterruptedIOException("Interrupted while replacing a saved game");
                    failure.initCause(interrupted);failure.addSuppressed(locked);throw failure;
                }
            }
        }
    }
    private static void replace(Path pending,Path target)throws IOException{
        try { Files.move(pending, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException unsupported) { Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING); }
    }
    public static void write(Path target, String text) throws IOException {
        Path pending = begin(target);
        IOException failure=null;
        try { Files.writeString(pending, text, Charset.defaultCharset()); commit(pending, target); }
        catch(IOException problem){failure=problem;throw problem;}
        finally {try{Files.deleteIfExists(pending);}catch(IOException cleanup){if(failure!=null)failure.addSuppressed(cleanup);else throw cleanup;}}
    }
}
