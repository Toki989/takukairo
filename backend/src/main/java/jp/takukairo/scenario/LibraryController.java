package jp.takukairo.scenario;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class LibraryController {
 private final LibraryService service;public LibraryController(LibraryService s){service=s;}
 @GetMapping("/scenarios") Object scenarios(@RequestParam(required=false)String search,@RequestParam(required=false)List<String> participation,@RequestParam(required=false)Boolean favorite,@RequestParam(required=false)String sort){return service.scenarios(search,participation,favorite,sort);}
 @GetMapping("/scenarios/{id}") Object scenario(@PathVariable long id){return service.scenario(id);}
 @PostMapping("/scenarios") ResponseEntity<?> createScenario(@RequestBody Map<String,Object>b){return ResponseEntity.status(201).body(service.saveScenario(null,b));}
 @PatchMapping("/scenarios/{id}") Object updateScenario(@PathVariable long id,@RequestBody Map<String,Object>b){return service.saveScenario(id,b);}
 @DeleteMapping("/scenarios/{id}") ResponseEntity<?> deleteScenario(@PathVariable long id){service.deleteScenario(id);return ResponseEntity.noContent().build();}
 @PostMapping("/scenarios/{id}/favorite") ResponseEntity<?> favorite(@PathVariable long id){service.favorite(id,true);return ResponseEntity.noContent().build();}
 @DeleteMapping("/scenarios/{id}/favorite") ResponseEntity<?> unfavorite(@PathVariable long id){service.favorite(id,false);return ResponseEntity.noContent().build();}
 @PostMapping("/scenarios/duplicate-candidates") Object duplicate(@RequestBody Map<String,Object>b){return service.duplicate(b);}
 @GetMapping("/persons") Object persons(@RequestParam(required=false)String search,@RequestParam(defaultValue="false")boolean recent){return service.persons(search,recent);}
 @PostMapping("/persons") ResponseEntity<?> createPerson(@RequestBody Map<String,Object>b){return ResponseEntity.status(201).body(service.savePerson(null,b));}
 @PatchMapping("/persons/{id}") Object updatePerson(@PathVariable long id,@RequestBody Map<String,Object>b){return service.savePerson(id,b);}
 @DeleteMapping("/persons/{id}") ResponseEntity<?> deletePerson(@PathVariable long id){service.deletePerson(id);return ResponseEntity.noContent().build();}
 @GetMapping("/persons/{id}/impact") Object impact(@PathVariable long id){return service.impact(id);}
 @GetMapping("/pcs") Object pcs(@RequestParam(required=false)String search,@RequestParam(required=false)Long personId,@RequestParam(required=false)String sort){return service.pcs(search,personId,sort);}
 @GetMapping("/pcs/{id}") Object pc(@PathVariable long id){return service.pc(id);}
 @PostMapping("/pcs") ResponseEntity<?> createPc(@RequestBody Map<String,Object>b){return ResponseEntity.status(201).body(service.savePc(null,b));}
 @PatchMapping("/pcs/{id}") Object updatePc(@PathVariable long id,@RequestBody Map<String,Object>b){return service.savePc(id,b);}
 @DeleteMapping("/pcs/{id}") ResponseEntity<?> deletePc(@PathVariable long id){service.deletePc(id);return ResponseEntity.noContent().build();}
 @GetMapping("/pcs/{id}/change-person-context") Object context(@PathVariable long id){return service.changeContext(id);}
 @PostMapping("/pcs/{id}/change-person") Object change(@PathVariable long id,@RequestBody Map<String,Object>b){return service.changePerson(id,b);}
}
