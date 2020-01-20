package edu.ciesla.main_service;

import edu.ciesla.main_service.database.models.Song;
import edu.ciesla.main_service.database.models.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RoomTests {
    static APIContreoller testUnit = MainServiceApplicationTests.testUnit;
    static String title = "TestRoom";
    static User host, owner1, owner2;
    static int history,queue, id, ownerToken;
    static Song s1,s2,s3;

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
        initSongs();
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
        id = ((BigInteger)map.get("id")).intValue();
        assertTrue(id>0);
        putOwner1();
    }
    static void initSongs(){
        s1= new Song();
        s1.setId(System.currentTimeMillis()+"T1");
        s1.setTitle("Test1");
        s1.setDuration(111);
        s1.setArtist("dunno");
        s2= new Song();
        s2.setId(System.currentTimeMillis()+"T2");
        s2.setTitle("Test2");
        s2.setDuration(222);
        s2.setArtist("dunno");
        s3= new Song();
        s3.setId(System.currentTimeMillis()+"T3");
        s3.setTitle("Test3");
        s3.setDuration(333);
        s3.setArtist("dunno");
        SongTests.createSong(s1);
        SongTests.createSong(s2);
        SongTests.createSong(s3);
    }
    static void putOwner1(){
        ResponseEntity responseEntity = testUnit.getRoomOwnerToken(id+"",host.getUser_token());
        assertEquals(200,responseEntity.getStatusCodeValue());
        Map<String, Object> map = MainServiceApplicationTests.deJSON(responseEntity);
        ownerToken = ((BigInteger)map.get("owner_token")).intValue();
        assertTrue(ownerToken>999);
        assertTrue(ownerToken<10000);
        String body = responseEntity.getBody().toString();
        responseEntity = testUnit.RoomAddOwner(id+"",owner1.getUser_token(),body);
        assertEquals(200,responseEntity.getStatusCodeValue());
        putOwner2(body);
    }
    static void putOwner2(String body){
        ResponseEntity responseEntity = testUnit.RoomAddOwner(id+"",owner2.getUser_token(),body);
        assertEquals(200,responseEntity.getStatusCodeValue());
    }


    @Test
    void vote() {
        String body = "{\n" +
                "    \"song_id\": \"" + s1.getId() + "\"\n" +
                "}";
        ResponseEntity responseEntity = testUnit.vote(id + "", owner1.getUser_token(), body);
        assertEquals(200, responseEntity.getStatusCodeValue());
        body = "{\n" +
                "    \"song_id\": \"" + s2.getId() + "\"\n" +
                "}";
        responseEntity = testUnit.vote(id + "", owner2.getUser_token(), body);
        assertEquals(200, responseEntity.getStatusCodeValue());

    }
    @Test
    void addToQueue(){
        String body = "{\n" +
                "\t\"song_id\":\""+s3.getId()+"\"\n" +
                "}";
        ResponseEntity responseEntity = testUnit.enqueue(id+"",host.getUser_token(),body);
        assertEquals(200, responseEntity.getStatusCodeValue());
        body = "{\n" +
                "\t\"song_id\":\""+s2.getId()+"\"\n" +
                "}";
        responseEntity = testUnit.enqueue(id+"",host.getUser_token(),body);
        assertEquals(200, responseEntity.getStatusCodeValue());
        body = "{\n" +
                "\t\"song_id\":\""+s1.getId()+"\"\n" +
                "}";
        responseEntity = testUnit.enqueue(id+"",host.getUser_token(),body);
        assertEquals(200, responseEntity.getStatusCodeValue());
        responseEntity = testUnit.getQueue(id+"",20,0);
        assertEquals(200, responseEntity.getStatusCodeValue());
        responseEntity = testUnit.getCurrentSong(id+"");
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.getBody().toString().length() < 2);
        responseEntity = testUnit.skip(id+"",host.getUser_token());
        assertEquals(200, responseEntity.getStatusCodeValue());
        responseEntity = testUnit.getCurrentSong(id+"");
        assertEquals(200, responseEntity.getStatusCodeValue());
        Map<String,Object> map = MainServiceApplicationTests.deJSON(responseEntity);
        String id = (String)map.get("id");
        assertTrue(id.equals(s3.getId()));
        responseEntity = testUnit.getHistory(RoomTests.id+"",20,0);
        assertEquals(200, responseEntity.getStatusCodeValue());
        /*JSONParser parser = new JSONParser((String)responseEntity.getBody());
        try {
            JSONArray array = (JSONArray) parser.parse();
            for(Object o: array){
                JSONObject json = (JSONObject)o;
                Map<String, Object> map;
                JSONParser parser2 = new JSONParser(json.toString());
                map = (Map<String, Object>) parser2.parse();

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }

    @Test
    void voteNoPermissions(){
        String body = "{\n" +
                "    \"song_id\": \"" + s3.getId() + "\"\n" +
                "}";
        ResponseEntity responseEntity = testUnit.vote(id + "", host.getUser_token(), body);
        assertEquals(401, responseEntity.getStatusCodeValue());
    }

    @AfterAll
    @Test
    static void collectVotes(){
        ResponseEntity responseEntity = testUnit.getVotes(id+"",host.getUser_token());
        assertEquals(200, responseEntity.getStatusCodeValue());
        Map<String, Object> map = MainServiceApplicationTests.deJSON(responseEntity);
        for(Map.Entry<String, Object> entry: map.entrySet()){
            if(entry.getKey().equals(owner1.getNickname())){
                assertTrue(((String)entry.getValue()).equals(s1.getId()));
            }else {
                assertTrue(((String)entry.getValue()).equals(s2.getId()));
            }
        }
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
