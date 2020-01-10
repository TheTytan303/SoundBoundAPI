package edu.ciesla.main_service;

import edu.ciesla.main_service.database.models.Room;
import edu.ciesla.main_service.database.models.Song;
import edu.ciesla.main_service.database.models.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RoomTests {
    static APIContreoller testUnit = MainServiceApplicationTests.testUnit;
    static String title = "TestRoom";
    static User host, owner1, owner2;
    static int history,queue;

    @BeforeAll
    @Test
    static void init(){
        host = new User("Host", "zaq123");
        owner1 = new User("Owner1", "zaq123");
        owner2 = new User("Owner2", "zaq123");
        UserTests.createUser(host);
        UserTests.createUser(owner1);
        UserTests.createUser(owner2);
        createRoom();

    }
    static void createRoom(){
        String body ="{\n" +
                "  \"name\": \""+title+"\"\n" +
                "}";
        ResponseEntity responseEntity = testUnit.addRoom(body, host.getUser_token());
        assertEquals(200,responseEntity.getStatusCodeValue());
        Map<String, Object> map = MainServiceApplicationTests.deJSON(responseEntity);
        String hostName =(String) map.get("host");
        assertEquals(hostName,host.getNickname());
        assertEquals(title, (String)map.get("name"));
    }

    @Test
    static void addToQueue(){

    }
    //static Room deJSONRoom(ResponseEntity responseEntity){
    //    Room returnVale = new Room();
    //    Map<String, Object> response = MainServiceApplicationTests.deJSON(responseEntity);
    //    returnVale.setId(((BigInteger)response.get("id")).intValue());
    //    returnVale.setName((String) response.get("name"));
    //    User host = new User();
    //    host.setId();
    //    returnVale.setHost();
    //    return returnVale;
    //}
}
