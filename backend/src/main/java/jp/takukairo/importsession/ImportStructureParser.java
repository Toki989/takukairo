package jp.takukairo.importsession;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import org.apache.commons.csv.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import jp.takukairo.common.ApiException;
import static jp.takukairo.importsession.ImportStructure.*;
/** Format adapters preserve hierarchy, cell boundaries and original locations. */
public final class ImportStructureParser {
 private Index index;
 public Document parse(String name,String raw) {
  index=new Index(raw);String ext=name==null?"":name.toLowerCase(Locale.ROOT);
  if(ext.endsWith(".pdf"))throw ApiException.invalid("/sources","UNSUPPORTED_FORMAT","PDFは対象外です。");
  if(ext.endsWith(".csv"))return csv(raw);
  if(ext.endsWith(".html")||ext.endsWith(".htm"))return html(raw);
  return lines(raw,ext.endsWith(".md")||ext.endsWith(".markdown"),name==null?"PASTED_TEXT":"TXT");
 }
 private Document csv(String raw) {
  var blocks=new ArrayList<Block>();var records=csvRanges(raw);if(records.isEmpty())return new Document("CSV",raw,blocks);
  try(var csv=CSVFormat.DEFAULT.builder().setIgnoreEmptyLines(false).get().parse(new StringReader(raw))) {
   var rows=csv.getRecords();if(rows.isEmpty())return new Document("CSV",raw,blocks);var header=rows.getFirst();if(rows.size()==1){blocks.add(new Block("csv-header",Kind.PARAGRAPH,0,null,raw,List.of(),index.range(0,raw.length()),1));}
   for(int r=1;r<rows.size();r++) {
    var row=rows.get(r);var spans=records.get(r);var cells=new ArrayList<Cell>();
    for(int c=0;c<row.size();c++)cells.add(new Cell(c<header.size()?header.get(c):"",row.get(c),spans.get(c),c<records.getFirst().size()?records.getFirst().get(c):null));
    blocks.add(new Block("r-"+r,Kind.RECORD,0,null,"",cells,index.range(spans.getFirst().startOffset(),spans.getLast().endOffset()),r+1));
   }
  }catch(Exception e){throw ApiException.invalid("/sources","INVALID_CSV","CSVの内容を確認してください。");}
  return new Document("CSV",raw,blocks);
 }
 private List<List<Range>> csvRanges(String raw) {
  var records=new ArrayList<List<Range>>();var cells=new ArrayList<Range>();boolean quoted=false;int start=0;
  for(int i=0;i<raw.length();i++) {
   char c=raw.charAt(i);if(c=='"'){if(quoted&&i+1<raw.length()&&raw.charAt(i+1)=='"')i++;else if(quoted||i==start)quoted=!quoted;}
   if(!quoted&&(c==','||c=='\r'||c=='\n')) {
    cells.add(index.range(start,i));if(c!=','){records.add(cells);cells=new ArrayList<>();if(c=='\r'&&i+1<raw.length()&&raw.charAt(i+1)=='\n')i++;}start=i+1;
   }
  }
  if(start<raw.length()||!cells.isEmpty()){cells.add(index.range(start,raw.length()));records.add(cells);}return records;
 }
 private Document lines(String raw,boolean markdown,String fallback) {
  var blocks=new ArrayList<Block>();var headings=new TreeMap<Integer,String>();var headers=new ArrayList<String>();var headerRanges=new ArrayList<Range>();
  String parent=null;int offset=0;String[] lines=raw.split("\\r\\n|\\n|\\r",-1);var codeLines=new BitSet();var quoteLines=new BitSet();var headingLevels=new HashMap<Integer,Integer>();var headingEnds=new HashMap<Integer,Integer>();
  if(markdown){var tree=org.commonmark.parser.Parser.builder().includeSourceSpans(org.commonmark.parser.IncludeSourceSpans.BLOCKS).build().parse(raw);tree.accept(new org.commonmark.node.AbstractVisitor(){
   private void code(org.commonmark.node.Node node){var spans=node.getSourceSpans();if(!spans.isEmpty())codeLines.set(spans.getFirst().getLineIndex(),spans.getLast().getLineIndex()+1);}
   @Override public void visit(org.commonmark.node.FencedCodeBlock node){code(node);}
   @Override public void visit(org.commonmark.node.IndentedCodeBlock node){code(node);}
   @Override public void visit(org.commonmark.node.BlockQuote node){var spans=node.getSourceSpans();if(!spans.isEmpty())quoteLines.set(spans.getFirst().getLineIndex(),spans.getLast().getLineIndex()+1);visitChildren(node);}
   @Override public void visit(org.commonmark.node.Heading node){var spans=node.getSourceSpans();if(!spans.isEmpty()){headingLevels.put(spans.getFirst().getLineIndex(),node.getLevel());headingEnds.put(spans.getFirst().getLineIndex(),spans.getLast().getLineIndex());}}
  });}
  for(int i=0;i<lines.length;i++) {
   String original=lines[i],text=original;int start=offset,end=start+original.length();
   offset=end+(end<raw.length()&&raw.charAt(end)=='\r'&&end+1<raw.length()&&raw.charAt(end+1)=='\n'?2:1);
   String id="b-"+(i+1);Kind kind=text.isBlank()?Kind.BLANK:Kind.PARAGRAPH;int level=0;var cells=new ArrayList<Cell>();
   boolean fence=codeLines.get(i);
   // CommonMark includes lazy continuation lines even when they have no '>' marker.
   if(markdown&&(fence||quoteLines.get(i))){headers.clear();headerRanges.clear();blocks.add(new Block(id,kind,0,parent,original,List.of(),index.range(start,end),0,true));continue;}
   boolean setext=headingLevels.containsKey(i)&&headingEnds.get(i)>i;
   int headingEnd=setext?headingEnds.get(i):i;
   if(setext)text=String.join("\n",Arrays.copyOfRange(lines,i,headingEnd)).strip();
   if(markdown&&!fence&&headingLevels.containsKey(i)){kind=Kind.HEADING;level=headingLevels.get(i);text=setext?text.strip():text.strip().replaceFirst("^#{1,6}(?:\\s+|$)","").replaceFirst("\\s+#+\\s*$","");headings.tailMap(level).clear();parent=headings.isEmpty()?null:headings.lastEntry().getValue();headings.put(level,id);headers.clear();headerRanges.clear();}
   else if(!fence&&(text.stripLeading().startsWith("|")||original.contains("\t")||markdown&&text.contains("|")&&(!headers.isEmpty()||i+1<lines.length&&tableSeparator(lines[i+1])))) {
    char delimiter=original.contains("\t")?'\t':'|';var spans=splitCells(raw,start,end,delimiter);var values=spans.stream().map(s->s.excerpt().strip()).toList();
    if(!values.isEmpty()&&values.stream().allMatch(s->s.matches(":?-{3,}:?")))continue;
    if(headers.isEmpty()){
     boolean separator=i+1<lines.length&&tableSeparator(lines[i+1]);
     boolean labelled=values.stream().anyMatch(s->!ImportInference.field(s).equals("unknown"));
     if(labelled||separator){headers.addAll(values);headerRanges.addAll(spans);
      // Unknown header-only content is still reviewable; a following data row consumes it as a label.
      boolean dataFollows=i+(separator?2:1)<lines.length&&(lines[i+(separator?2:1)].contains("|")||lines[i+(separator?2:1)].contains("\t"));
      if(dataFollows||values.stream().allMatch(s->!ImportInference.field(s).equals("unknown")))continue;
     }
     blocks.add(new Block(id,Kind.PARAGRAPH,0,parent,original,List.of(),index.range(start,end),0));continue;
    }
    if(values.size()!=headers.size()){kind=Kind.UNPARSED;headers.clear();headerRanges.clear();}
    else {kind=Kind.RECORD;for(int c=0;c<values.size();c++){String label=headers.get(c);cells.add(new Cell(label,ImportInference.field(label).equals("displayQuote")?spans.get(c).excerpt():values.get(c),spans.get(c),headerRanges.get(c)));}}
   }else {headers.clear();headerRanges.clear();if(!fence&&markdown)text=text.replaceFirst("^(?:[-*+]|[0-9]+[.)])\\s+","");}
   boolean quoted=markdown&&text.stripLeading().startsWith(">");
   if(setext){for(int line=i+1;line<=headingEnd;line++){end=offset+lines[line].length();offset=end+(end<raw.length()&&raw.charAt(end)=='\r'&&end+1<raw.length()&&raw.charAt(end+1)=='\n'?2:1);}i=headingEnd;}
   blocks.add(new Block(id,kind,level,parent,text,cells,index.range(start,end),0,quoted));if(kind==Kind.HEADING)parent=id;
  }
  return new Document(markdown?"MARKDOWN":fallback,raw,blocks);
 }
 private boolean tableSeparator(String line){return line.strip().matches("\\|?\\s*:?-{3,}:?\\s*(?:\\|\\s*:?-{3,}:?\\s*)*\\|?");}
 private List<Range> splitCells(String raw,int start,int end,char delimiter) {
  var out=new ArrayList<Range>();int from=start;boolean first=true;
  for(int i=start;i<end;i++)if(raw.charAt(i)==delimiter&&(i==start||raw.charAt(i-1)!='\\')){
   boolean border=delimiter=='|'&&first&&raw.substring(from,i).isBlank();
   if(!border)out.add(index.range(from,i));from=i+1;first=false;
  }
  // Internal empty cells carry a column; only a trailing Markdown border is omitted.
  if(from<end&&!raw.substring(from,end).isBlank()||delimiter=='\t'||from<end&&out.isEmpty())out.add(index.range(from,end));return out;
 }
 private Document html(String raw) {
  var doc=Jsoup.parse(raw,"",org.jsoup.parser.Parser.htmlParser().setTrackPosition(true));
  var adapter=new HtmlAdapter();adapter.visit(doc.body());return new Document("HTML",raw,adapter.blocks);
 }
 /** Walk every visible text node once, including own text of mixed/unknown containers. */
 private final class HtmlAdapter {
  final List<Block> blocks=new ArrayList<>();final TreeMap<Integer,String> headings=new TreeMap<>();int sequence;String parent;
  Range span(Node n){int start=n.sourceRange().startPos(),end=n.sourceRange().endPos();if(n instanceof Element e&&e.endSourceRange().isTracked())end=e.endSourceRange().endPos();return index.range(start,end);}
  boolean quoted(Element e){return e.closest("blockquote,pre")!=null;}
  boolean structural(Element e){return e.normalName().equals("br")||e.tag().isBlock()||!e.select("br").isEmpty()||e.getAllElements().stream().anyMatch(x->x!=e&&x.tag().isBlock());}
  void add(Element owner,String text,Range range,Kind kind,List<Cell> cells){if(text.isBlank()&&cells.isEmpty())return;int level=0;String id="h-"+(++sequence);if(kind==Kind.HEADING){level=Integer.parseInt(owner.normalName().substring(1));headings.tailMap(level).clear();parent=headings.isEmpty()?null:headings.lastEntry().getValue();headings.put(level,id);}blocks.add(new Block(id,kind,level,parent,text,cells,range,0,quoted(owner)));if(kind==Kind.HEADING)parent=id;}
  void visit(Element e){
   if(Set.of("script","style","template").contains(e.normalName()))return;
   if(e.normalName().equals("table")){table(e);return;}
   if(e.normalName().equals("tr")){row(e);return;}
   boolean heading=e.normalName().matches("h[1-6]");
   if(e.sourceRange().isTracked()&&e.children().stream().noneMatch(this::structural)){
    add(e,e.wholeText(),span(e),heading?Kind.HEADING:Kind.PARAGRAPH,List.of());return;
   }
   var text=new StringBuilder();int start=-1,end=-1;
   for(Node node:e.childNodes()){
    if(node instanceof Element child&&structural(child)){
     if(start>=0)add(e,text.toString(),index.range(start,end),Kind.PARAGRAPH,List.of());text.setLength(0);start=-1;
     if(!child.normalName().equals("br"))visit(child);
    }else if(node instanceof TextNode t&&t.sourceRange().isTracked()){
     if(start<0)start=span(t).startOffset();end=span(t).endOffset();text.append(t.getWholeText());
    }else if(node instanceof Element child&&child.sourceRange().isTracked()&&!Set.of("script","style","template").contains(child.normalName())){
     if(start<0)start=span(child).startOffset();end=span(child).endOffset();text.append(child.wholeText());
    }
   }
   if(start>=0)add(e,text.toString(),index.range(start,end),Kind.PARAGRAPH,List.of());
  }
  List<Element> columns(Element row){return row.children().stream().filter(x->Set.of("th","td").contains(x.normalName())).toList();}
  void table(Element table){
   var rows=table.select("tr").stream().filter(x->x.closest("table")==table).toList();
   int width=rows.isEmpty()?0:columns(rows.getFirst()).size();
   // A physical column index is only valid for a rectangular, single-header table.
   // Preserve unsupported grids as one reviewable source range, including nested tables.
   boolean unsupported=table.select("table").stream().anyMatch(x->x!=table)||table.select("th,td").stream().anyMatch(x->!x.attr("rowspan").matches("|1")||!x.attr("colspan").matches("|1"))||rows.stream().anyMatch(r->columns(r).size()!=width)||rows.stream().skip(1).anyMatch(r->columns(r).stream().allMatch(c->c.normalName().equals("th")));
   if(unsupported){add(table,span(table).excerpt(),span(table),Kind.UNPARSED,List.of());}
   else {for(var caption:table.children().stream().filter(x->x.normalName().equals("caption")).toList())visit(caption);for(var row:rows)row(row);}
   blocks.add(new Block("h-"+(++sequence),Kind.BLANK,0,parent,"",List.of(),index.range(span(table).endOffset(),span(table).endOffset()),0));
  }
  void row(Element row){
   var table=row.closest("table");var first=table==null?row:table.select("tr").stream().filter(x->x.closest("table")==table).findFirst().orElse(row);var header=columns(first);
   boolean hasHeader=!header.isEmpty()&&(header.stream().allMatch(x->x.normalName().equals("th"))||header.stream().filter(x->!ImportInference.field(x.wholeText()).equals("unknown")).count()>=2);
   if(row==first&&hasHeader){if(table.select("tr").size()==1&&header.stream().anyMatch(x->ImportInference.field(x.wholeText()).equals("unknown")))add(row,row.wholeText(),span(row),Kind.PARAGRAPH,List.of());return;}
   var values=columns(row);var cells=new ArrayList<Cell>();for(int i=0;i<values.size();i++){
    var cell=values.get(i);String label=hasHeader&&i<header.size()?header.get(i).wholeText().strip():"";
    cells.add(new Cell(label,ImportInference.field(label).equals("displayQuote")?cell.wholeText():cell.wholeText().strip(),span(cell),hasHeader&&i<header.size()?span(header.get(i)):null));
   }
   add(row,row.wholeText(),span(row),Kind.RECORD,cells);
  }
 }
}
