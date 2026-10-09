package jp.takukairo.image;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class ImageController {
 private final ImageService service;public ImageController(ImageService s){service=s;}
 @GetMapping("/images/{kind}/{id}/derivative-url") ResponseEntity<?> url(@PathVariable String kind,@PathVariable long id){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.url(kind,id));}
 @GetMapping("/dev-images/{token}") ResponseEntity<?> read(@PathVariable String token){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType("image/webp")).body(service.read(token));}
 @PutMapping("/pcs/{id}/image") Object pc(@PathVariable long id,@RequestPart MultipartFile file,@RequestParam long expectedVersion,@RequestParam String positionX,@RequestParam String positionY,@RequestParam String zoom){return service.put("pcs",id,expectedVersion,file,jp.takukairo.common.Json.map("positionX",positionX,"positionY",positionY,"zoom",zoom));}
 @PutMapping("/scenarios/{id}/image") Object scenario(@PathVariable long id,@RequestPart MultipartFile file,@RequestParam long expectedVersion){return service.put("scenarios",id,expectedVersion,file,Map.of());}
 @DeleteMapping("/pcs/{id}/image") Object deletePc(@PathVariable long id,@RequestParam long expectedVersion){return service.remove("pcs",id,expectedVersion);}
 @DeleteMapping("/scenarios/{id}/image") Object deleteScenario(@PathVariable long id,@RequestParam long expectedVersion){return service.remove("scenarios",id,expectedVersion);}
 @PatchMapping("/pcs/{id}/image-transform") Object transform(@PathVariable long id,@RequestBody Map<String,Object>b){return service.transform(id,b);}
}
