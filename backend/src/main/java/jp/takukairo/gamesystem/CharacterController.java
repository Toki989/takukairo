package jp.takukairo.gamesystem;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
public class CharacterController {
 private final CharacterPreviewService service;public CharacterController(CharacterPreviewService s){service=s;}
 @PostMapping("/api/ccfolia/character-preview") Object preview(@RequestBody Map<String,Object>b){return service.preview(b);}
}
