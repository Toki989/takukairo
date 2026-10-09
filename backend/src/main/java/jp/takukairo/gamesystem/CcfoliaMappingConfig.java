package jp.takukairo.gamesystem;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import jp.takukairo.common.Json;
import java.util.*;
@Component
public class CcfoliaMappingConfig {
 private final List<Map<String,Object>> mappings;
 public CcfoliaMappingConfig(){try(var input=new ClassPathResource("config/ccfolia-mapping.json").getInputStream()){mappings=Json.list(Json.object(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).get("profiles"));}catch(Exception e){throw new IllegalStateException(e);}}
 public List<Map<String,Object>> mappings(){return mappings;}
 public Map<String,Object> forProfile(String key){return mappings.stream().filter(m->Objects.equals(m.get("profileKey"),key)).findFirst().orElse(Map.of());}
 public List<String> detectedProfiles(String memo){return mappings.stream().filter(m->Json.obj(m.get("memoMappings")).keySet().stream().anyMatch(key->memo.lines().map(Profiles::normalize).anyMatch(line->line.startsWith(key+":")))).map(m->Json.text(m.get("profileKey"))).distinct().toList();}
}
