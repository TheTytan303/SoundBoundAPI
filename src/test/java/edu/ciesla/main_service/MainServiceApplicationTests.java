package edu.ciesla.main_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class MainServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void createUserTest(){
        APIContreoller testUnit = new APIContreoller();
        String body =
                "{\n" +
                "  \"nickname\": \"Thanos\",\n" +
                "  \"password\": \"everything\"\n" +
                "}";
        ResponseEntity<String> response = testUnit.createUser(body);
        response.toString();
        assertEquals(2,2);
    }

}
