package edu.ciesla.main_service;

import edu.ciesla.main_service.database.models.Song;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SongTests {
    APIContreoller testUnit = MainServiceApplicationTests.testUnit;
    static Song testSong;
    @BeforeAll
    static void initialize(){
        testSong = new Song();
        testSong.setArtist("Frank Sinatra");
        testSong.setDuration(162);
        testSong.setTitle("I'll never smile again");
        testSong.setId("test "+ System.currentTimeMillis());
    }
    @Test
    void addSong(){
        String body =
                "{\n" +
                        "  \"id\": \""+testSong.getId()+"\",\n" +
                        "  \"title\": \""+testSong.getTitle()+"\",\n" +
                        "  \"artist\": \""+testSong.getArtist()+"\",\n" +
                        "  \"duration\": \""+testSong.getDuration()+"\"\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.postSong(body);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(409,testUnit.postSong(body).getStatusCodeValue());
        Map<String, Object> response = MainServiceApplicationTests.deJSON(responseEntity);
        assertNotNull(response);
        Song responseSong = new Song();
        responseSong.setDuration(((BigInteger)response.get("duration")).intValue());
        responseSong.setTitle((String)response.get("title"));
        responseSong.setArtist((String)response.get("artist"));
        responseSong.setId((String)response.get("id"));
        ifEqualsSong(testSong, responseSong);
        getSong();
    }
    void getSong(){
        Map<String, Object> response = MainServiceApplicationTests.deJSON(testUnit.getSong(testSong.getId()));
        assertNotNull(response);
        Song responseSong = new Song();
        responseSong.setDuration(((BigInteger)response.get("duration")).intValue());
        responseSong.setTitle((String)response.get("title"));
        responseSong.setArtist((String)response.get("artist"));
        responseSong.setId((String)response.get("id"));
       ifEqualsSong(testSong, responseSong);
    }

    @Test
    void addSongEmptyId(){
        String body =
                "{\n" +
                        "  \"id\": \""+""+"\",\n" +
                        "  \"title\": \""+testSong.getTitle()+"\",\n" +
                        "  \"artist\": \""+testSong.getArtist()+"\",\n" +
                        "  \"duration\": \""+testSong.getDuration()+"\"\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.postSong(body);
        assertEquals(400, responseEntity.getStatusCodeValue());
    }

    @Test
    void addSongNoId(){
        String body =
                "{\n" +
                        "  \"title\": \""+testSong.getTitle()+3+"\",\n" +
                        "  \"artist\": \""+testSong.getArtist()+"\",\n" +
                        "  \"duration\": \""+testSong.getDuration()+"\"\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.postSong(body);
        assertEquals(400, responseEntity.getStatusCodeValue());
    }

    @Test
    void addSongNoTitle(){
        String body =
                "{\n" +
                        "  \"id\": \""+testSong.getId()+1+"\",\n" +
                        "  \"artist\": \""+testSong.getArtist()+"\",\n" +
                        "  \"duration\": \""+testSong.getDuration()+"\"\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.postSong(body);
        assertEquals(400, responseEntity.getStatusCodeValue());
    }
    @Test
    void addSongNoArtist(){
        String body =
                "{\n" +
                        "  \"id\": \""+testSong.getId()+2+"\",\n" +
                        "  \"title\": \""+testSong.getTitle()+"\",\n" +
                        "  \"duration\": \""+testSong.getDuration()+"\"\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.postSong(body);
        assertEquals(200, responseEntity.getStatusCodeValue());
    }
    @Test
    void addSongNoDuration(){
        String body =
                "{\n" +
                        "  \"id\": \""+testSong.getId()+3+"\",\n" +
                        "  \"title\": \""+testSong.getTitle()+"\",\n" +
                        "  \"artist\": \""+testSong.getArtist()+"\",\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.postSong(body);
        assertEquals(400, responseEntity.getStatusCodeValue());
    }

    public static void ifEqualsSong(Song s1, Song s2){
        assertEquals(s1.getId(),s2.getId());
        assertEquals(s1.getArtist(),s2.getArtist());
        assertEquals(s1.getDuration(),s2.getDuration());
        assertEquals(s1.getTitle(),s2.getTitle());
    }
}
