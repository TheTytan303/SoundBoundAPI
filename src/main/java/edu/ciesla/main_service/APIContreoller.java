package edu.ciesla.main_service;


import com.auth0.jwt.exceptions.JWTDecodeException;
import edu.ciesla.main_service.database.models.Playlist;
import edu.ciesla.main_service.database.models.Room;
import edu.ciesla.main_service.database.models.Song;
import edu.ciesla.main_service.database.models.User;
import edu.ciesla.main_service.database.models.exceptions.*;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;

@RestController
public class APIContreoller {
    public APIContreoller(){}

//----------------------------------------------------------------USER
    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createUser(@RequestBody String body){
        JSONParser parser = new JSONParser(body);
        try {
            Object obj = parser.parse();
            Map<String, String> bodyContent = (Map<String, String>) obj;
            if(bodyContent.size() != 2){
                return new ResponseEntity<>("Incorrect number of arguments", HttpStatus.BAD_REQUEST);
            }
            User user = User.addUser(bodyContent.get("nickname"),bodyContent.get("password"));
            JSONObject response = new JSONObject();
            response.put("user_name", user.getNickname());
            response.put("id", user.getId());
            response.put("token", user.getToken());
            return new ResponseEntity<>(response.toString(), HttpStatus.valueOf(200));
        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (WrongInputData wrongInputData) {
            return new ResponseEntity<>(wrongInputData.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value ="/authorize", method =  RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authorize(@RequestBody String body){
        JSONParser parser = new JSONParser(body);
        try {
            Map<String, String>bodyContent = (Map<String, String>) parser.parse();
            String id = bodyContent.get("id");
            String password= bodyContent.get("password");
            String token = User.authorize(Integer.parseInt(id), password);
            return new ResponseEntity<>(token, HttpStatus.OK);

        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (Unauthorized unauthorized) {
            return new ResponseEntity<>(unauthorized.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (NoIdFound | NumberFormatException noIdFound) {
            return new ResponseEntity<>(noIdFound.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value ="/me/lists", method =  RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserLists(@RequestHeader String authorization){
        try {
            User user = User.identifyUser(authorization);
            JSONArray array = new JSONArray();
            for(Playlist pl: user.getPlaylists()){
                JSONObject tmp = new JSONObject();
                tmp.put("id", pl.getId());
                tmp.put("title",pl.getTitle());
                tmp.put("songNumber",pl.getSongNumber());
                array.put(tmp);
            }
            //response.put("playlists: ",array);
            //response.put("Playlists: ",array.toList());
            return new ResponseEntity<>(array.toString(), HttpStatus.OK);
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

//----------------------------------------------------------------SONG
    @RequestMapping(value = "/song", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postSong(@RequestBody String body){
        JSONParser parser = new JSONParser(body);
        Song posted;
        try {
            Object obj = parser.parse();
            Map<String, String> bodyContent = (Map<String, String>) obj;
            String id = bodyContent.get("id");
            String title = bodyContent.get("title");
            String artist = bodyContent.get("artist");
            int duration = Integer.parseInt(bodyContent.get("duration"));
            posted = Song.addSong(id,title,artist,duration);
        } catch (ParseException | NumberFormatException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        }catch (AlreadyInDatabase alreadyInDatabase) {
            return new ResponseEntity<>("Song with specified ID exists in DataBase", HttpStatus.valueOf(409));
        } catch (WrongInputData wrongInputData) {
            return new ResponseEntity<>(wrongInputData.getMessage(), HttpStatus.valueOf(400));
        }
        JSONObject retrunVale = new JSONObject();
        retrunVale.put("id", posted.getId());
        retrunVale.put("title", posted.getTitle());
        retrunVale.put("artist", posted.getArtist());
        retrunVale.put("duration", posted.getDuration());
        return new ResponseEntity<>(retrunVale.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/song/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSong(@PathVariable String id){
        List<Song> songs = Song.getSong(id);
        if(songs.size() == 0){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(songs.get(0).JSON().toString(), HttpStatus.OK);
    }


//----------------------------------------------------------------PLAYLIST
    @RequestMapping(value = "/playlist", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addPlaylist(@RequestBody String body,
                                              @RequestHeader String authorization){
        JSONParser parser = new JSONParser(body);
        Playlist returnVale;
        try{
            Object obj = parser.parse();
            Map<String, Object> bodyContent = (Map<String, Object>) obj;
            User owner = User.identifyUser(authorization);
            List<LinkedHashMap<String, String>> song_maps =( List<LinkedHashMap<String, String>>) bodyContent.get("songs");
            List<String> tmp = new ArrayList<>();
            for(LinkedHashMap<String, String> m : song_maps){
                tmp.add(m.get("id"));
            }
            //tmp = song_ids.values().toArray(tmp);
            String[] ids = new String[tmp.size()];
            int i = 0;
            for(String s: tmp){
                ids[i] = s;
                i++;
            }
            List<Song> songList = Song.getSong(ids);
            Song[] songs = new Song[songList.size()];
            i = 0;
            for(Song s: songList){
                songs[i] = s;
                i++;
            }
            returnVale = Playlist.addPlaylist((String) bodyContent.get("title"), owner, songs);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(returnVale.JSON().toString(), HttpStatus.valueOf(200));
}

    @RequestMapping(value = "/playlist/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPlaylist(@PathVariable String id){
        try{
            List<Playlist> list = Playlist.getPlaylist(Integer.parseInt(id));
            if(list.size()!= 1){
                throw new NoIdFound(" ");
            }
            Playlist target = list.get(0);
            return new ResponseEntity<>(target.getPlaylistShortJSON().toString(), HttpStatus.valueOf(200));
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/playlist/{id}/songs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSongs(@PathVariable String id,
                                           @RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
                                           @RequestParam(value = "offset", required = false, defaultValue = "0") int offset){
        Playlist target;
        try{
            List<Playlist> list = Playlist.getPlaylist(Integer.parseInt(id));
            if(list.size()!= 1){
                throw new NoIdFound(" ");
            }
            target = list.get(0);
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
        JSONArray returnVale = new JSONArray();
        List<Song> list = target.getSongs();
        for(int n = offset; n<(offset+limit) && n<target.getSongs().size();n++){
            returnVale.put(list.get(n).JSON());
        }
        return new ResponseEntity<>(returnVale.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/playlist/{id}/songs", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putSong(@PathVariable String id,
                                          @RequestHeader String authorization,
                                          @RequestBody String body,
                                          @RequestParam(value = "offset", required = false, defaultValue = "-1") int offset){
        Playlist target;
        User owner;
        try{
            List<Playlist> list = Playlist.getPlaylist(Integer.parseInt(id));
            if(list.size()!= 1){
                throw new NoIdFound(" ");
            }
            target = list.get(0);
            owner = User.identifyUser(authorization);
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }catch (Unidentified e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(401));
        }
        if(!target.isOwner(owner)){
            return new ResponseEntity<>("User identified by this JWT cannot edit this playlist", HttpStatus.FORBIDDEN);
        }
        ArrayList<String> songArray ;
        JSONParser parser = new JSONParser(body);
        try{
            Object obj = parser.parse();
            Map<String, Object> bodyContent = (Map<String, Object>) obj;
            songArray =(ArrayList<String>) bodyContent.get("songs");
        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        }
        String[] array = songArray.toArray(new String[songArray.size()]);
        List<Song> targetedSongs = Song.getSong(array);
        target.addSong(targetedSongs, offset);
        return getSongs(id,20,0);
    }

    @RequestMapping(value = "/playlist/{id}/songs", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteSong(@PathVariable String id,
                                          @RequestHeader String authorization,
                                          @RequestParam(value = "count", required = false, defaultValue = "1") int count,
                                          @RequestParam(value = "offset") int offset){
        Playlist target;
        User owner;
        try{
            List<Playlist> list = Playlist.getPlaylist(Integer.parseInt(id));
            if(list.size()!= 1){
                throw new NoIdFound(" ");
            }
            target = list.get(0);
            owner = User.identifyUser(authorization);
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }catch (Unidentified | JWTDecodeException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(401));
        }
        if(!target.isOwner(owner)){
            return new ResponseEntity<>("User identified by this JWT cannot edit this playlist", HttpStatus.FORBIDDEN);
        }
        target.deleteSong(offset,count);
        return getSongs(id,20,offset);
    }

    @RequestMapping(value = "/playlist/{id}/owner/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOwnerPlaylistToken(@PathVariable String id, @RequestHeader String authorization){
        try {
            User user = User.identifyUser(authorization);
            List<Playlist> list = Playlist.getPlaylist(Integer.parseInt(id));
            if(list.size()!= 1){
                throw new NoIdFound("");
            }
            Playlist target = list.get(0);
            if(!target.isOwner(user)) return new ResponseEntity<>("User identified by this JWT cannot edit this playlist", HttpStatus.FORBIDDEN);
            JSONObject returnVale = new JSONObject();
            returnVale.put("owner_token", target.getOwnerToken());
            return new ResponseEntity<>(returnVale.toString(),HttpStatus.valueOf(200));
        } catch (Unidentified | JWTDecodeException unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }
    @RequestMapping(value = "/playlist/{id}/owner", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> PlaylistAddOwner(@PathVariable String id,
                                              @RequestHeader String authorization,
                                              @RequestBody String body){
        JSONParser parser = new JSONParser(body);
        try {
            User user = User.identifyUser(authorization);
            List<Playlist> list = Playlist.getPlaylist(Integer.parseInt(id));
            if(list.size()!= 1){
                throw new NoIdFound("Object targeted by ID has not been found");
            }
            Playlist target = list.get(0);
            Map<String, Object>bodyContent = (Map<String, Object>) parser.parse();
            int token;
            try{
                BigInteger s =(BigInteger) bodyContent.get("owner_token");
                token = s.intValue();
            }catch (ClassCastException e){
                return new ResponseEntity<>("could not Owner Token - wrong JSON format, or passed Owner Token is not a number", HttpStatus.BAD_REQUEST);
            }
            target.addOwner(token, user);
            return new ResponseEntity<>(HttpStatus.valueOf(200));
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NoIdFound e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch(NumberFormatException e){
            return new ResponseEntity<>("Playlist ID must be a number", HttpStatus.NOT_FOUND);
        }catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (AlreadyInDatabase e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
//----------------------------------------------------------------ROOM
    @RequestMapping(value = "/room", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addRoom(@RequestBody String body,
                                        @RequestHeader String authorization){
        try {
            User host = User.identifyUser(authorization);
            JSONParser parser = new JSONParser(body);
            Map<String, Object>bodyContent = (Map<String, Object>) parser.parse();
            String name =(String) bodyContent.get("name");
            Room returnVale = Room.addRoom(host,name);
            return new ResponseEntity<>(returnVale.JSON().toString(), HttpStatus.valueOf(200));
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        }
    }
    @RequestMapping(value = "/room/{id}", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRoom(@PathVariable String id){
        try {
            Room returnVale = Room.getRoom(Integer.parseInt(id));
            return new ResponseEntity<>(returnVale.JSON().toString(), HttpStatus.valueOf(200));
        }catch(NumberFormatException e){
            return new ResponseEntity<>("Playlist ID must not a number", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/room/{id}/owner/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRoomOwnerToken(@PathVariable String id,
                                                @RequestHeader String authorization){
        try {
            User user = User.identifyUser(authorization);
            Room target = Room.getRoom(Integer.parseInt(id));
            if(target == null){
                throw new NoIdFound("Object targeted by ID has not been found");
            }
            if(!target.isHost(user)) return new ResponseEntity<>("User identified by this JWT cannot get token of that Room", HttpStatus.FORBIDDEN);
            JSONObject returnVale = new JSONObject();
            returnVale.put("owner_token", target.getOwnerToken());
            return new ResponseEntity<>(returnVale.toString(),HttpStatus.valueOf(200));
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/room/{id}/owner", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> RoomAddOwner(@PathVariable String id,
                                              @RequestHeader String authorization,
                                              @RequestBody String body){
        JSONParser parser = new JSONParser(body);
        try {
            User user = User.identifyUser(authorization);
            Room target = Room.getRoom(Integer.parseInt(id));
            if(target == null){
                throw new NoIdFound("Object targeted by ID has not been found");
            }
            Map<String, Object>bodyContent = (Map<String, Object>) parser.parse();
            int token;
            try{
                BigInteger s =(BigInteger) bodyContent.get("owner_token");
                token = s.intValue();
            }catch (Exception e){
                return new ResponseEntity<>("could not Owner Token - wrong JSON format, or passed Owner Token is not a number", HttpStatus.BAD_REQUEST);
            }
            target.addOwner(token, user);
            return new ResponseEntity<>(HttpStatus.valueOf(200));
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NoIdFound e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch(NumberFormatException e){
            return new ResponseEntity<>("Playlist ID must not a number", HttpStatus.NOT_FOUND);
        }catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (AlreadyInDatabase e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/room/{id}/vote", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> vote(@PathVariable String id,
                                       @RequestHeader String authorization,
                                       @RequestBody String body){
        try {
            User owner = User.identifyUser(authorization);
            JSONParser parser = new JSONParser(body);
            Map<String, Object>bodyContent = (Map<String, Object>) parser.parse();
            Song song = Song.getSong((String)bodyContent.get("song_id")).get(0);
            Room room = Room.getRoom(Integer.parseInt(id));
            room.vote(owner,song);
            return new ResponseEntity<>(HttpStatus.valueOf(200));
        } catch (Unidentified | Unauthorized unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "room/{id}/vote", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getVotes(@PathVariable String id,
                                           @RequestHeader String authorization){
        try {
            User user = User.identifyUser(authorization);
            Room target = Room.getRoom(Integer.parseInt(id));
            if(target == null){
                throw new NoIdFound("Object targeted by ID has not been found");
            }
            if(!target.isHost(user)) return new ResponseEntity<>("User identified by this JWT cannot get Votes for this Room", HttpStatus.FORBIDDEN);
            Map<User, Song> votes = target.getVotes();
            JSONArray returnVale = new JSONArray();
            for(Map.Entry<User, Song> entry: votes.entrySet()){
                JSONObject tmp = new JSONObject();
                tmp.put("user",entry.getKey().getNickname());
                tmp.put("song",entry.getValue().getId());
                returnVale.put(tmp);
            }
            return new ResponseEntity<>(returnVale.toString(), HttpStatus.valueOf(200));
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }

    //TODO optimize:
    @RequestMapping(value = "room/{id}/queue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getQueue(@PathVariable String id,
                                           @RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
                                           @RequestParam(value = "offset", required = false, defaultValue = "0") int offset){
        try {
            Room target = Room.getRoom(Integer.parseInt(id));
            return getSongs(target.getQueue().getId()+"", limit, offset);
        }catch (NumberFormatException e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }
    @RequestMapping(value = "room/{id}/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHistory(@PathVariable String id,
                                           @RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
                                           @RequestParam(value = "offset", required = false, defaultValue = "0") int offset){
        try {
            Room target = Room.getRoom(Integer.parseInt(id));
            return getSongs(target.getHistory().getId()+"", limit, offset);
        }catch (NumberFormatException e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "room/{id}/currentSong", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCurrentSong(@PathVariable String id){
        try {
            Room target = Room.getRoom(Integer.parseInt(id));
            if(target.getPlaying() == null){
                return new ResponseEntity<>("", HttpStatus.valueOf(200));
            }else{
                return new ResponseEntity<>(target.getPlaying().JSON().toString(), HttpStatus.valueOf(200));
            }
        }catch (NumberFormatException e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "room/{id}/queue/song", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> enqueue(@PathVariable String id,
                                          @RequestHeader String authorization,
                                          @RequestBody String body){
        try {
            User user = User.identifyUser(authorization);
            Room target = Room.getRoom(Integer.parseInt(id));
            if(target == null){
                throw new NoIdFound("Object targeted by ID has not been found");
            }
            if(!target.isHost(user)) return new ResponseEntity<>("User identified by this JWT edit this Room", HttpStatus.FORBIDDEN);
            JSONParser parser = new JSONParser(body);
            Map<String, Object> bodyContent =(Map<String, Object>) parser.parse();
            String songId = (String) bodyContent.get("song_id");
            if(Song.getSong(songId).size() == 0){
                throw new NoIdFound("");
            }
            Song song = Song.getSong(songId).get(0);
            target.getQueue().addSong(List.of(song), target.getQueue().getSongs().size());
            return new ResponseEntity<>( HttpStatus.valueOf(200));
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "room/{id}/skip", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> skip(@PathVariable String id,
                                       @RequestHeader String authorization){
        try {
            User user = User.identifyUser(authorization);
            Room target = Room.getRoom(Integer.parseInt(id));
            if(target == null){
                throw new NoIdFound("Object targeted by ID has not been found");
            }
            if(!target.isHost(user)) return new ResponseEntity<>("User identified by this JWT edit this Room", HttpStatus.FORBIDDEN);
            target.skip();
            return new ResponseEntity<>(target.JSON().toString(), HttpStatus.valueOf(200));
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }
}
