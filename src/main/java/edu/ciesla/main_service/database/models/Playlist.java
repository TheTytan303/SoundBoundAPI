package edu.ciesla.main_service.database.models;

import edu.ciesla.main_service.database.HibernateConfig;
import edu.ciesla.main_service.database.models.exceptions.AlreadyInDatabase;
import edu.ciesla.main_service.database.models.exceptions.Unidentified;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "playlist", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_Playlist"}))
public class Playlist {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_Playlist", nullable = false, unique = true)
    int id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "token", nullable = true)
    Integer token;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="playlist_entry", joinColumns = @JoinColumn(name="Playlist"), inverseJoinColumns = @JoinColumn(name = "song"))
    List<Song> songs = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="playlist_owners", joinColumns = @JoinColumn(name = "Playlist"), inverseJoinColumns = @JoinColumn(name="user"))
    Set<User> owners= new HashSet<>();

    @OneToMany(mappedBy = "queue")
    Set<Room> roomsQueue = new HashSet<>();

    @OneToMany(mappedBy = "history")
    Set<Room> roomsHistory = new HashSet<>();


    //-------------------------------------------------------------------Public:
    public JSONObject getPlaylistShortJSON(){
        return new PlaylistShort(this.id,this.songs.size(),this.title).toJSON();
    }
    public void addSong(List<Song> songs){
        this.songs.addAll(songs);
        this.update();
    }
    public void deleteSong(int offset, int count){
        for(int i =0; i<count;i++){
            if(offset >= this.songs.size()) break;
            this.songs.remove(offset);
        }
        this.update();
    }
    public JSONObject JSON(){
        JSONObject returnVale = new JSONObject();
        returnVale.put("id", this.id);
        returnVale.put("title", this.title);
        //TODO ograniczyć max liczbę piosenek
        JSONArray tmp = new JSONArray();
        for(Song s: this.songs){
            tmp.put(s.id);
        }
        returnVale.put("songs", tmp);
        //TODO ograniczyć max liczbę ownersow
        tmp = new JSONArray();
        for(User u: this.owners){
            tmp.put(u.getNickname());
        }
        returnVale.put("owners", tmp);
        return returnVale;
    }
    public Playlist() {
    }
    public boolean isOwner(User owner){
        for(User u: this.getOwners()){
            if(u.getId() == owner.getId()){
                return true;
            }
        }
        return false;
    }
    public int getOwnerToken(){
        this.generateToken();
        this.update();
        return this.token;
    }
    public void addOwner(int token, User user) throws Unidentified, AlreadyInDatabase {
        if(this. token == 0){
            throw new Unidentified("owner token not generated");
        }
        if(this.token != token){
            throw new Unidentified("invalid owner token");
        }
        this.token = 0;
        if(this.isOwner(user)){
            throw new AlreadyInDatabase("this User is already Owner of this Playlist");
        }
        this.owners.add(user);
        this.update();
    }
    //-------------------------------------------------------------------Private:
    private void generateToken(){
        Random random = new Random(System.currentTimeMillis());
        this.token = random.nextInt();
        if(this.token < 0) this.token *= (-1);
        this.token = this.token % 9000;
        this.token += 1000;
    }
    private void update(){
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tc = session.beginTransaction();
        session.update(this);
        session.flush();
        tc.commit();
        session.close();
    }
    //-------------------------------------------------------------------Overrides:

    //-------------------------------------------------------------------Static:
    public static Playlist addPlaylist(String title, User owner, Song ...songs){
        Playlist returnVale = new Playlist();
        returnVale.title = title;
        returnVale.owners.add(owner);
        returnVale.songs.addAll(Set.of(songs));
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tx;
        try{
            tx = session.beginTransaction();
            session.save(returnVale);
            tx.commit();
        }catch (PersistenceException pe){
            pe.printStackTrace();
        }finally {
            session.close();
        }
        return returnVale;
    }


    public static List<Playlist> getPlaylist(int ...ids){
        ArrayList<Playlist> returnVale = new ArrayList<>();
        String sql = "SELECT * FROM `playlist`";
        if(ids.length>0){
            sql=sql.concat(" WHERE");
            for (int value : ids) {
                sql = sql.concat(" `ID_Playlist` =" + value + " OR");
            }
            sql = sql.substring(0, sql.length() - 3);
        }
        try{
            returnVale = getCommand(sql);
        }catch (Exception e){
            return returnVale;
        }
        return returnVale;
    }

    private static ArrayList<Playlist> getCommand(String sqlCommand){
        ArrayList<Playlist> returnVale = new ArrayList<>();
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        NativeQuery query = session.createNativeQuery(sqlCommand);
        query.addEntity(Playlist.class);
        returnVale.addAll(query.list());
        transaction.commit();
        session.close();
        return returnVale;
    }
    //-------------------------------------------------------------------Getters/Setters:


    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Room> getRoomsQueue() {
        return roomsQueue;
    }

    public void setRoomsQueue(Set<Room> roomsQueue) {
        this.roomsQueue = roomsQueue;
    }

    public Set<Room> getRoomsHistory() {
        return roomsHistory;
    }

    public void setRoomsHistory(Set<Room> roomsHistory) {
        this.roomsHistory = roomsHistory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public void setOwners(Set<User> owners) {
        this.owners = owners;
    }
    public int getSongNumber(){
        return this.songs.size();
    }

    @Override
    public String toString() {
        return "["+id+"]"+title;
    }

    private class PlaylistShort{
        public int id, songCount;
        public String name;
        public PlaylistShort(int id, int soungCount, String name) {
            this.id = id;
            this.songCount = soungCount;
            this.name = name;
        }
        public int getId(){
            return id;
        }
        public JSONObject toJSON(){
            JSONObject returnVale = new JSONObject();
            returnVale.put("id" , this.id);
            returnVale.put("songNumber" , this.songCount);
            returnVale.put("title" , this.name);

            return returnVale;
        }
    }
}
