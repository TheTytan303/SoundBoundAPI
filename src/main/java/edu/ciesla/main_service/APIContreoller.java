package edu.ciesla.main_service;


import edu.ciesla.main_service.database.models.Playlist;
import edu.ciesla.main_service.database.models.Song;
import edu.ciesla.main_service.database.models.User;
import edu.ciesla.main_service.database.models.exceptions.AlreadyInDatabase;
import edu.ciesla.main_service.database.models.exceptions.NoIdFound;
import edu.ciesla.main_service.database.models.exceptions.Unidentified;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            //System.out.println(bodyContent.get("nickname"));
            User user = User.addUser(bodyContent.get("nickname"),bodyContent.get("password"));
            JSONObject response = new JSONObject();
            response.put("user_name", user.getNickname());

            response.put("id", user.getId());
            response.put("token", user.getToken());
            return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value ="/authorize", method =  RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authorize(@RequestBody String body){
        JSONParser parser = new JSONParser(body);
        try {
            Map<String, String>bodyContent = (Map<String, String>) parser.parse();
            String id = bodyContent.get("id");
            String password= bodyContent.get("Password");
            String token = User.authorize(Integer.parseInt(id), password);
            return new ResponseEntity<>(token, HttpStatus.OK);

        } catch (ParseException e) {
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
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
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
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
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>("could not read data - wrong JSON format", HttpStatus.BAD_REQUEST);
        }catch (NoIdFound e){
            System.err.println(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(400));

        } catch (AlreadyInDatabase alreadyInDatabase) {
            Song song = (Song)alreadyInDatabase.object;
            return new ResponseEntity<>("Song with specified ID exists in DataBase", HttpStatus.valueOf(409));
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
    public ResponseEntity<String> addPlaylist(@RequestBody String body, @RequestHeader String authorization){
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
        for(int n = offset; n<(offset+limit) && n<target.getSongs().size();n++){
            returnVale.put(target.getSongs().get(n).JSON());
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
        if(offset < 0){
            target.addSong(targetedSongs);
        }
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
        }catch (Unidentified e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(401));
        }
        if(!target.isOwner(owner)){
            return new ResponseEntity<>("User identified by this JWT cannot edit this playlist", HttpStatus.FORBIDDEN);
        }
        target.deleteSong(offset,count);
        return getSongs(id,20,offset);
    }

    @RequestMapping(value = "/playlist/{id}/owner/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOwnerToken(@PathVariable String id, @RequestHeader String authorization){
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
        } catch (Unidentified unidentified) {
            return new ResponseEntity<>(unidentified.getMessage(), HttpStatus.valueOf(401));
        }catch (NumberFormatException | NoIdFound e){
            return new ResponseEntity<>("Object targeted by ID has not been found", HttpStatus.NOT_FOUND);
        }
    }
    @RequestMapping(value = "/playlist/{id}/owner", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOwnerAdd(@PathVariable String id, @RequestHeader String authorization,@RequestBody String body){
        JSONParser parser = new JSONParser(body);
        try {
            User user = User.identifyUser(authorization);List<Playlist> list = Playlist.getPlaylist(Integer.parseInt(id));
            if(list.size()!= 1){
                throw new NoIdFound("Object targeted by ID has not been found");
            }
            Playlist target = list.get(0);
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
//----------------------------------------------------------------ROOM

}
