package edu.ciesla.main_service;

import edu.ciesla.main_service.database.models.Playlist;
import edu.ciesla.main_service.database.models.Song;
import edu.ciesla.main_service.database.models.User;
import javafx.beans.binding.ObjectBinding;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PlaylistTest{
    static APIContreoller testUnit = MainServiceApplicationTests.testUnit;
    static List<Song> testList;
    static Song testSong1, testSong2, testSong3;
    static String title;
    static int id;
    static User creator,owner1,owner2;

    @BeforeAll
    @Test
    static void init(){
        testList = new ArrayList<>();
        for(int i=0; i<5;i++){
            testList.add(new Song("testListedSong"+i+System.currentTimeMillis(),"Title"+i,"tester",i+100));
            SongTests.createSong(testList.get(i));
        }
        testSong1 = new Song("testSong"+1+System.currentTimeMillis(),"Title"+1,"tester",1);
        testSong2 = new Song("testSong"+2+System.currentTimeMillis(),"Title"+2,"tester",2);
        testSong3 = new Song("testSong"+3+System.currentTimeMillis(),"Title"+3,"tester",3);
        SongTests.createSong(testSong1);
        SongTests.createSong(testSong2);
        SongTests.createSong(testSong3);
        creator = new User("Creator", "zaq123");
        owner1 = new User("Owner1", "zaq123");
        owner2 = new User("Owner2", "zaq123");
        UserTests.createUser(creator);
        UserTests.createUser(owner1);
        UserTests.createUser(owner2);
        title = "TestList";
        createPlaylist();
        getPlaylist();
    }
    static void createPlaylist(){
        String body =  "{\n" +
                       "  \"title\": \""+title+"\",\n" +
                       "  \"songs\": [\n";
        for(Song s: testList){
            body = body.concat("{\nid: \"" + s.getId()+"\"\n} ,\n");
        }
        body = body.substring(0,body.length()-2);
        body = body.concat("\n" +
                            "  ]\n" +
                            "}");
        System.out.println(body);
        ResponseEntity responseEntity = testUnit.addPlaylist(body, creator.getUser_token());
        assertEquals(200,responseEntity.getStatusCodeValue());
        Map<String, Object> response = MainServiceApplicationTests.deJSON(responseEntity);
        id = ((BigInteger) response.get("id")).intValue();
    }
    static void getPlaylist(){
        ResponseEntity responseEntity = testUnit.getPlaylist(id+"");
        assertEquals(200,responseEntity.getStatusCodeValue());
        Map<String, Object> response = MainServiceApplicationTests.deJSON(responseEntity);
        BigInteger songNumber = (BigInteger) response.get("songNumber");
        BigInteger id = (BigInteger) response.get("id");
        String title = (String) response.get("title");
        assertEquals(PlaylistTest.title, title, "wrong title");
        assertEquals(PlaylistTest.id, id.intValue(), "wrong id");
        assertEquals(PlaylistTest.testList.size(), songNumber.intValue(), "wrong song number");
    }
    @Test
    void getPlaylistWrongId(){
        ResponseEntity responseEntity = testUnit.getPlaylist("-1");
        assertEquals(404,responseEntity.getStatusCodeValue());
        responseEntity = testUnit.getPlaylist("dsss");
        assertEquals(404,responseEntity.getStatusCodeValue());
    }
    @Test
    void getOwnerTokenUnauthorized(){
        ResponseEntity responseEntity = testUnit.getOwnerPlaylistToken(id+"", " ");
        assertEquals(401,responseEntity.getStatusCodeValue());
    }
    @Test
    void addOwner(){
        ResponseEntity responseEntity = testUnit.getOwnerPlaylistToken(id+"32", creator.getUser_token());
        assertEquals(404,responseEntity.getStatusCodeValue());
        responseEntity = testUnit.getOwnerPlaylistToken(id+"", creator.getUser_token());
        assertEquals(200,responseEntity.getStatusCodeValue());
        Map<String, Object> response = MainServiceApplicationTests.deJSON(responseEntity);
        BigInteger ownerToken = (BigInteger) response.get("owner_token");
        String body = "{\n" +
                "    \"owner_token\": "+ownerToken+"dsa\n" +
                "}";
        responseEntity = testUnit.PlaylistAddOwner(id+"",owner1.getUser_token(),body);
        assertEquals(400,responseEntity.getStatusCodeValue());
        body = "{\n" +
                "    \"owner_token\": "+ownerToken+"\n" +
                "}";
        responseEntity = testUnit.PlaylistAddOwner(id+"",owner1.getUser_token(),body);
        assertEquals(200,responseEntity.getStatusCodeValue());
        responseEntity = testUnit.PlaylistAddOwner(id+"",owner1.getUser_token(),body);
        assertEquals(401,responseEntity.getStatusCodeValue());
        responseEntity = testUnit.PlaylistAddOwner(id+"32",owner1.getUser_token(),body);
        assertEquals(404,responseEntity.getStatusCodeValue());
    }
    @Test
    void addOwnerWrongToken(){
        String body = "{\n" +
                "    \"owner_token\": "+41141+"\n" +
                "}";
        ResponseEntity responseEntity = testUnit.PlaylistAddOwner(id+"",owner2.getUser_token(),body);
        assertEquals(401,responseEntity.getStatusCodeValue());
    }
    @Test
    void addDeleteTest(){
        int code;
        String body = "{\n" +
                "  \"songs\": [\n" +
                "    \""+testSong1.getId()+"\"\n" +
                "  ]\n" +
                "}";
        code = testUnit.putSong(id+"",creator.getUser_token(),body,-1).getStatusCodeValue();
        assertEquals(200, code);
        body = "{\n" +
                "  \"songs\": [\n" +
                "    \""+testSong2.getId()+"\"\n" +
                "  ]\n" +
                "}";
        code = testUnit.putSong(id+"",creator.getUser_token(),body,2).getStatusCodeValue();
        assertEquals(200, code);
        body = "{\n" +
                "  \"songs\": [\n" +
                "    \""+testSong3.getId()+"\"\n" +
                "  ]\n" +
                "}";
        code = testUnit.putSong(id+"",creator.getUser_token(),body,2334).getStatusCodeValue();
        assertEquals(200, code);
        ArrayList<Song> testList1 = new ArrayList<>(PlaylistTest.testList);
        testList1.add(0,testSong1);
        testList1.add(2,testSong2);
        testList1.add(testList1.size(),testSong3);
        ResponseEntity responseEntity = testUnit.getSongs(id+"",20,0);
        assertEquals(200,responseEntity.getStatusCodeValue());
        ArrayList<Song> testList2 = deJSONSongs(responseEntity.getBody().toString());

        assertTrue(compareLists(testList1,testList2));
        code = testUnit.deleteSong(id+"",creator.getUser_token(),1,7).getStatusCodeValue();
        assertEquals(200, code);
        code = testUnit.deleteSong(id+"",creator.getUser_token(),1,3).getStatusCodeValue();
        assertEquals(200, code);
        code = testUnit.deleteSong(id+"",creator.getUser_token(),1,0).getStatusCodeValue();
        assertEquals(200, code);
        code = testUnit.deleteSong(id+""," ",1,0).getStatusCodeValue();
        assertEquals(401, code);
        code = testUnit.deleteSong(id+""," ",1,0).getStatusCodeValue();
        assertEquals(401, code);
        code = testUnit.deleteSong(id+"32",owner2.getUser_token(),1,0).getStatusCodeValue();
        assertEquals(404, code);
    }
    @Test
    void addNotExistingSong(){
        String body = "{\n" +
                "  \"songs\": [\n" +
                "    \""+testSong3.getId()+1+"\"\n" +
                "  ]\n" +
                "}";
        int code = testUnit.putSong(id+"",creator.getUser_token(),body,2334).getStatusCodeValue();
        assertEquals(200, code);
    }

    public static boolean compareLists(ArrayList<Song> list1, ArrayList<Song> list2){
        if(list1.size() != list2.size()) return false;
        for(int i =0; i<list1.size();i++){
            if(!SongTests.ifEqualsSong(list1.get(i),list2.get(i))){
                return false;
            }
        }
        return true;
    }
    public ArrayList<Song> deJSONSongs(String body){
        System.out.println(body);
        ArrayList<Song> returnVale = new ArrayList<>();
        JSONParser parser = new JSONParser(body);
        List<Object> tmp;
        try {
            tmp = parser.parseArray();
            for(Object o : tmp){
                LinkedHashMap<String, Object> jsonObject = (LinkedHashMap<String, Object>)o;
                returnVale.add(new Song((String)jsonObject.get("id"),(String)jsonObject.get("title"),
                        (String)jsonObject.get("artist"),((BigInteger)jsonObject.get("duration")).intValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(1,2);
            return null;
        }
        return returnVale;
    }
}
