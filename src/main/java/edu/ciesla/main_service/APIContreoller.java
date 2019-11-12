package edu.ciesla.main_service;

import edu.ciesla.main_service.database.models.Song;
import edu.ciesla.main_service.database.models.User;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class APIContreoller {
    public APIContreoller(){
        System.out.println("testin:");
        ArrayList<User> sample = User.getUser(1,3);
        for(User u: sample){
            System.out.println(u);
        }
        ArrayList<Song> songs = Song.getSongs();
        for(Song s: songs){
            System.out.println(s);
        }
    }
}
