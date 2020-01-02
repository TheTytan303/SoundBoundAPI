package edu.ciesla.main_service;

import edu.ciesla.main_service.database.models.Song;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MainServiceApplicationTests {

    static APIContreoller testUnit =new APIContreoller();


    public static Map<String, Object> deJSON(ResponseEntity<String> responseEntity){
        try{
            Map<String, Object> returnVale;
            JSONParser parser = new JSONParser(responseEntity.getBody());
            returnVale = (Map<String, Object>) parser.parse();
            return returnVale;
        }
        catch (Exception e){
            return null;
        }
    }
}
