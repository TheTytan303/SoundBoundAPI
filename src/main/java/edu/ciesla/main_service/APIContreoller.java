package edu.ciesla.main_service;


import edu.ciesla.main_service.database.models.Playlist;
import edu.ciesla.main_service.database.models.Song;
import edu.ciesla.main_service.database.models.User;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@RestController
public class APIContreoller {
    public APIContreoller(){}

    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createUser(@RequestBody String body){
        JSONParser parser = new JSONParser(body);
        try {
            Object obj = parser.parse();
            Map<String, String> bodyContent = (Map<String, String>) obj;
            if(bodyContent.size() != 2){
                return new ResponseEntity<>("Incorrect number of arguments", HttpStatus.BAD_REQUEST);
            }
            //System.out.println(bodyContent.get("nickname"));
            User user = User.addUser(bodyContent.get("nickname"),bodyContent.get("password"));
            JSONObject response = new JSONObject();
            response.put("user_name", user.getNickname());

            response.put("id", user.getId());
            response.put("token", user.getToken());
            return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
        } catch (ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value ="/authorize", method =  RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authorize(@RequestHeader String header){
        JSONParser parser = new JSONParser(header);
        try {
            Map<String, String> headerContent = (Map<String, String>) parser.parse();
            String id = headerContent.get("Username");
            String password= headerContent.get("Password");
            String token = User.authorize(Integer.parseInt(id), password);
            return new ResponseEntity<>(token, HttpStatus.OK);

        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value ="/me/lists", method =  RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserLists(@RequestHeader String header){
        JSONParser parser = new JSONParser(header);
        try {
            Map<String, String> headerContent = (Map<String, String>) parser.parse();
            String jwt = headerContent.get("authorization");
            User user = User.identifyUser(jwt);
            JSONObject response = new JSONObject();
            JSONArray array = new JSONArray();
            for(Playlist pl: user.getPlaylists()){
                array.put(pl.getPlaylistShort());
            }
            response.put("",array);
        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return null;
    }
}
