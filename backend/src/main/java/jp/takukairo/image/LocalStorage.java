package jp.takukairo.image;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.io.*;
import java.util.*;
@Component
public class LocalStorage {
 private final Path root;
 public LocalStorage(@Value("${app.storage}")String p)throws IOException{root=Path.of(p).toAbsolutePath().normalize();Files.createDirectories(root);}
 private Path path(String key){if(!key.matches("[a-zA-Z0-9._/-]+"))throw new IllegalArgumentException();Path p=root.resolve(key).normalize();if(!p.startsWith(root))throw new IllegalArgumentException();return p;}
 public String save(byte[] bytes,String extension)throws IOException{String key=UUID.randomUUID()+"."+extension;Files.write(path(key),bytes,StandardOpenOption.CREATE_NEW);return key;}
 public byte[] read(String key)throws IOException{return Files.readAllBytes(path(key));}
 public void delete(String key)throws IOException{if(key!=null)Files.deleteIfExists(path(key));}
 public boolean exists(String key){return key!=null&&Files.isRegularFile(path(key));}
}
