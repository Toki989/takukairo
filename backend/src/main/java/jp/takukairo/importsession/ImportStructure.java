package jp.takukairo.importsession;
import java.util.*;
import static jp.takukairo.common.Json.*;
/** Ephemeral structure; offsets address the untouched source in UTF-16. */
public final class ImportStructure {
 private ImportStructure() {}
 public enum Kind { HEADING, PARAGRAPH, RECORD, BLANK }
 public record Range(int startOffset,int endOffset,int lineStart,int lineEnd,String excerpt) {
  public Map<String,Object> trace(String field,long record,String evidence,String derivation,List<String> dependencies) {
   return map("fieldPath",field,"startOffset",startOffset,"endOffset",endOffset,"lineStart",lineStart,"lineEnd",lineEnd,"excerpt",excerpt,"logicalRecord",record==0?null:record,"evidenceId",evidence,"derivation",derivation,"dependsOn",dependencies);
  }
 }
 public record Cell(String label,String value,Range range,Range labelRange) {
  public Cell(String label,String value,Range range){this(label,value,range,null);}
 }
 public record Block(String id,Kind kind,int level,String parentId,String text,List<Cell> cells,Range range,long logicalRecord,boolean quoted) {
  public Block(String id,Kind kind,int level,String parentId,String text,List<Cell> cells,Range range,long logicalRecord){this(id,kind,level,parentId,text,cells,range,logicalRecord,false);}
 }
 public record Document(String format,String raw,List<Block> blocks) {}
 public static Range range(String raw,int start,int end) {
  start=Math.clamp(start,0,raw.length());end=Math.clamp(end,start,raw.length());
  return new Range(start,end,line(raw,start),line(raw,end>start?end-1:end),raw.substring(start,end));
 }
 private static int line(String raw,int offset) {
  int line=1;for(int i=0;i<offset;i++)if(raw.charAt(i)=='\n'||raw.charAt(i)=='\r'&&(i+1>=raw.length()||raw.charAt(i+1)!='\n'))line++;return line;
 }

 /** One line index per source avoids rescanning a large archive for each cell. */
 public static final class Index {
  private final String raw;private final int[] starts;
  public Index(String raw){this.raw=raw;var lines=new ArrayList<Integer>();lines.add(0);for(int i=0;i<raw.length();i++){char c=raw.charAt(i);if(c=='\r'){if(i+1<raw.length()&&raw.charAt(i+1)=='\n')i++;lines.add(i+1);}else if(c=='\n')lines.add(i+1);}starts=lines.stream().mapToInt(Integer::intValue).toArray();}
  private int lineAt(int offset){int found=Arrays.binarySearch(starts,offset);return found>=0?found+1:-found-1;}
  public Range range(int start,int end){start=Math.clamp(start,0,raw.length());end=Math.clamp(end,start,raw.length());return new Range(start,end,lineAt(start),lineAt(end>start?end-1:end),raw.substring(start,end));}
 }
}
