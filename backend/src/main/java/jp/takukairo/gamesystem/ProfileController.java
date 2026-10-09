package jp.takukairo.gamesystem;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class ProfileController {
 private final Profiles profiles;public ProfileController(Profiles p){profiles=p;}
 @GetMapping("/game-system-profiles") Object active(){return profiles.active();}
 @GetMapping("/game-system-profiles/{key}") Object get(@PathVariable String key){return profiles.get(key);}
 @PostMapping("/game-systems/resolve") Object resolve(@RequestBody Map<String,Object> body){return profiles.resolve(jp.takukairo.common.Json.text(body.get("gameSystem")));}
}
