package jp.takukairo.importsession;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@RestController @RequestMapping("/api/import/session")
public class ImportController {
 private final ImportService service;public ImportController(ImportService s){service=s;}
 @GetMapping("/current") ResponseEntity<?> current(){Object current=service.current();return current==null?ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("null"):ResponseEntity.ok(current);}
 @PostMapping ResponseEntity<?> create(){return ResponseEntity.status(201).body(service.create());}
 @GetMapping("/{id}") Object get(@PathVariable long id){return service.get(id);}
 @PatchMapping("/{id}") Object step(@PathVariable long id,@RequestBody Map<String,Object>b){return service.step(id,b);}
 @DeleteMapping("/{id}") ResponseEntity<?> delete(@PathVariable long id,@RequestParam long expectedSessionVersion){service.delete(id,expectedSessionVersion);return ResponseEntity.noContent().build();}
 @PostMapping("/{id}/sources/text") ResponseEntity<?> text(@PathVariable long id,@RequestBody Map<String,Object>b){return ResponseEntity.status(201).body(service.addText(id,b));}
 @PatchMapping("/{id}/sources/{sid}") Object patchText(@PathVariable long id,@PathVariable long sid,@RequestBody Map<String,Object>b){return service.patchText(id,sid,b);}
 @PostMapping("/{id}/sources/files") ResponseEntity<?> files(@PathVariable long id,@RequestParam long expectedSessionVersion,@RequestPart List<MultipartFile> files){return ResponseEntity.status(201).body(service.addFiles(id,expectedSessionVersion,files));}
 @PutMapping("/{id}/sources/{sid}/file") Object replace(@PathVariable long id,@PathVariable long sid,@RequestParam long expectedSessionVersion,@RequestPart("file")List<MultipartFile> file){return service.replace(id,sid,expectedSessionVersion,file);}
 @DeleteMapping("/{id}/sources/{sid}") Object deleteSource(@PathVariable long id,@PathVariable long sid,@RequestParam long expectedSessionVersion){return service.deleteSource(id,sid,expectedSessionVersion);}
 @PostMapping("/{id}/analysis/reset") Object reset(@PathVariable long id,@RequestBody Map<String,Object>b){return service.reset(id,b);}
 @PostMapping("/{id}/analyze") Object analyze(@PathVariable long id,@RequestBody Map<String,Object>b){return service.analyze(id,b);}
 @GetMapping("/{id}/review") Object review(@PathVariable long id,@RequestParam(required=false)String status,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return service.review(id,status,page,size);}
 @GetMapping("/{id}/candidates/{cid}") Object detail(@PathVariable long id,@PathVariable long cid){return service.detail(id,cid);}
 @PatchMapping("/{id}/candidates/{cid}") Object patch(@PathVariable long id,@PathVariable long cid,@RequestBody Map<String,Object>b){return service.patch(id,cid,b);}
 @GetMapping("/{id}/resolutions") Object resolutions(@PathVariable long id,@RequestParam(required=false)String entityType){return service.resolutions(id,entityType);}
 @PostMapping("/{id}/bulk-apply/preview") Object bulkPreview(@PathVariable long id,@RequestBody Map<String,Object>b){return service.preview(id,"BULK",null,b);}
 @PostMapping("/{id}/bulk-apply") Object bulk(@PathVariable long id,@RequestBody Map<String,Object>b){return service.apply(id,"BULK",null,b);}
 @PostMapping("/{id}/candidates/{cid}/split-preview") Object splitPreview(@PathVariable long id,@PathVariable long cid,@RequestBody Map<String,Object>b){return service.preview(id,"SPLIT",cid,b);}
 @PostMapping("/{id}/candidates/{cid}/split") Object split(@PathVariable long id,@PathVariable long cid,@RequestBody Map<String,Object>b){return service.apply(id,"SPLIT",cid,b);}
 @PostMapping("/{id}/candidates/merge-preview") Object mergePreview(@PathVariable long id,@RequestBody Map<String,Object>b){return service.preview(id,"MERGE",null,b);}
 @PostMapping("/{id}/candidates/merge") Object merge(@PathVariable long id,@RequestBody Map<String,Object>b){return service.apply(id,"MERGE",null,b);}
 @PostMapping("/{id}/register") Object register(@PathVariable long id,@RequestBody Map<String,Object>b){return service.register(id,b);}
 @PostMapping("/{id}/complete") Object complete(@PathVariable long id,@RequestBody Map<String,Object>b){return service.complete(id,b);}
}
