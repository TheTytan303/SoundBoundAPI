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
    private String title;

    @Column(name = "token")
    private Integer token;

    @OneToMany(mappedBy = "playlist", fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    //@JoinTable(name="playlist_entry", joinColumns = @JoinColumn(name="Playlist"), inverseJoinColumns = @JoinColumn(name = "song"))
    private List<PlaylistEntry> entries= new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="playlist_owners", joinColumns = @JoinColumn(name = "Playlist"), inverseJoinColumns = @JoinColumn(name="user"))
    Set<User> owners= new HashSet<>();

    @OneToMany(mappedBy = "queue")
    Set<Room> roomsQueue = new HashSet<>();

    @OneToMany(mappedBy = "history")
    Set<Room> roomsHistory = new HashSet<>();

    //-------------------------------------------------------------------Constructors:
    public Playlist() {
    }
    //-------------------------------------------------------------------Public:
    public JSONObject getPlaylistShortJSON(){
        return new PlaylistShort(this.id,this.entries.size(),this.title).toJSON();
    }
    public JSONObject JSON(){
        JSONObject returnVale = new JSONObject();
        returnVale.put("id", this.id);
        returnVale.put("title", this.title);
        //TODO ograniczyć max liczbę piosenek
        JSONArray tmp = new JSONArray();
        for(PlaylistEntry pe: this.entries){
            tmp.put(pe.song.id);
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
    public void addSong(List<Song> songs){
        double position = this.entries.size();
        List<PlaylistEntry> entries = new ArrayList<>();
        PlaylistEntry.addPlaylistEntriesAtEnd(this, position, songs);
        this.entries.addAll(entries);
        //this.update();
    }
    public void addSong(List<Song> songs, int offset){
        if(offset<=0){
            if(this.entries.size() == 0){
                PlaylistEntry.addPlaylistEntriesAtEnd(this, this.entries.size(),songs);
            }else{
                PlaylistEntry.addPlaylistEntriesAtBeginning(this, this.entries.get(0).getPosition(),songs);
            }
            return;
        }
        if(offset >= this.entries.size()){
            PlaylistEntry.addPlaylistEntriesAtEnd(this, this.entries.size(),songs);
        }else{
            double position1 = this.entries.get(offset-1).position;
            double position2 = this.entries.get(offset).position;
            PlaylistEntry.addPlaylistEntries(this,position1,position2 ,songs);
        }

    }
    public void deleteSong(int offset, int count){
        //TODO delete offset song
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        for(int i = offset; i<count+offset && i<entries.size(); i++){
            session.remove(entries.get(i));
        }
        session.flush();
        tx.commit();
        session.close();
        //for(int i =0; i<count;i++){
        //    if(offset >= this.songs.size()) break;
        //    this.songs.remove(offset);
        //}
        //this.update();
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
    public boolean isOwner(User owner){
        for(User u: owners){
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
    public Song getAndRemove(int index){
        //TODO delete offset song
        Song returnVale = this.entries.remove(0).getSong();
        this.update();
        return returnVale;
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
    @Override
    public String toString() {
        return "["+id+"]"+title;
    }
    //-------------------------------------------------------------------Static:
    public static Playlist addPlaylist(String title, User owner, Song ...songs){
        Playlist returnVale = new Playlist();
        returnVale.title = title;
        returnVale.owners.add(owner);
        List<PlaylistEntry> entries = new ArrayList<>();
        double position = returnVale.entries.size();
        //returnVale.songs.addAll(Set.of(songs));
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
        List<Song> songList = new ArrayList<>();
        songList.addAll(Arrays.asList(songs));
        PlaylistEntry.addPlaylistEntriesAtEnd(returnVale, position, songList);
        returnVale.entries.addAll(entries);
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();
        for(PlaylistEntry pe: entries){
            songs.add(pe.getSong());
        }
        return songs;
    }
    public int getSongNumber(){
        return this.entries.size();
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
