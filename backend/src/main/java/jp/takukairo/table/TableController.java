package jp.takukairo.table;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
import jp.takukairo.common.Json;
@RestController @RequestMapping("/api")
public class TableController {
 private final TableService service;public TableController(TableService s){service=s;}
 @GetMapping({"/tables/{id}","/tables/{id}/detail"}) Object get(@PathVariable long id){return service.get(id);}
 @GetMapping("/scenarios/{id}/tables") Object list(@PathVariable long id){return service.forScenario(id);}
 @PostMapping("/scenarios/{id}/tables") ResponseEntity<?> create(@PathVariable long id,@RequestBody Map<String,Object>b){return ResponseEntity.status(201).body(service.save(null,id,b));}
 @PutMapping("/tables/{id}") Object update(@PathVariable long id,@RequestBody Map<String,Object>b){return service.save(id,Json.requiredLong(b,"scenarioId"),b);}
 @DeleteMapping("/tables/{id}") ResponseEntity<?> delete(@PathVariable long id){service.delete(id);return ResponseEntity.noContent().build();}
 @GetMapping("/pcs/{id}/appearances") Object appearances(@PathVariable long id){return service.appearances(id);}
 @PostMapping("/pcs/{id}/previous-end-state-candidate") Object previous(@PathVariable long id,@RequestBody Map<String,Object>b){return service.previous(id,b);}
 @GetMapping("/activity/summary") Object summary(){return service.summary();}
 @GetMapping("/activity/by-month") Object month(@RequestParam int year){return service.byMonth(year);}
 @GetMapping("/activity/by-year") Object year(){return service.byYear();}
}
