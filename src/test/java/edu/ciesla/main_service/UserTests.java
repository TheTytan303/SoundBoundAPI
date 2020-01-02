package edu.ciesla.main_service;

import org.apache.coyote.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {
    APIContreoller testUnit = MainServiceApplicationTests.testUnit;
    String userName = "Thanos";
    String userPassword = "everything";
    String userToken;
    Integer userID;
    @Test
    void createLoginUserTest(){
        String body =
                "{\n" +
                        "  \"nickname\": \""+userName+"\",\n" +
                        "  \"password\": \""+userPassword+"\"\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.createUser(body);
        assertEquals(200,responseEntity.getStatusCodeValue());
        Map<String, Object> response = MainServiceApplicationTests.deJSON(responseEntity);
        assertNotNull(response);
        userID =((BigInteger) response.get("id")).intValue();
        assertNotNull(userID);
        login();
    }
    void login(){
        String body =
                "{\n" +
                        "  \"id\": \""+userID+"\",\n" +
                        "  \"password\": \""+userPassword+"\"\n" +
                        "}";
        userToken = testUnit.authorize(body).getBody();
        assertNotNull(userToken);
        assertTrue(userToken.contains("."));
    }

    @Test
    void loginNoPassword(){
        String body =
                "{\n" +
                        "  \"id\": \""+userID+"\",\n" +
                        "}";
        ResponseEntity responseEntity =  testUnit.authorize(body);
        assertEquals(400, responseEntity.getStatusCodeValue());
    }
    @Test
    void createUserNoPasswordTest(){
        String body =
                "{\n" +
                        "  \"nickname\": \""+userName+"\",\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.createUser(body);
        assertEquals(400,responseEntity.getStatusCodeValue());
    }
    @Test
    void loginWrongId(){
        String body =
                "{\n" +
                        "  \"id\": \" -2 \",\n" +
                        "  \"password\": \""+userPassword+"\"\n" +
                        "}";
        ResponseEntity responseEntity =  testUnit.authorize(body);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }
    @Test
    void createUserWrongDataTest(){
        String body =
                "{\n" +
                        "  \"nickname\": \""+userName+"\",\n" +
                        "  \"ghhh\": \""+userPassword+"\"\n" +
                        "}";
        ResponseEntity<String> responseEntity = testUnit.createUser(body);
        assertEquals(400,responseEntity.getStatusCodeValue());
    }
}
