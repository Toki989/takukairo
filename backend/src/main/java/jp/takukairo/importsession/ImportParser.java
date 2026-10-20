package jp.takukairo.importsession;
import org.springframework.stereotype.Component;
import java.util.*;
import java.nio.charset.*;
@Component
public class ImportParser {
 public record Parsed(Map<String,Object> data,int lineStart,int lineEnd,String excerpt,List<Map<String,Object>> traces,Map<String,Object> inference) {}
 public String decode(byte[] data){try{return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(data)).toString().replaceFirst("^\\uFEFF","");}catch(Exception e){return Charset.forName("windows-31j").decode(java.nio.ByteBuffer.wrap(data)).toString();}}
 public ImportStructure.Document structure(String name,String raw){return new ImportStructureParser().parse(name,raw);}
 public List<Parsed> parse(String name,String raw){return new ImportInference().infer(structure(name,raw));}
}
