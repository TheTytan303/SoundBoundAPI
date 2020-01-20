package edu.ciesla.main_service.database.models;

import edu.ciesla.main_service.database.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist_entry", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_Playlist_entry"}))
public class PlaylistEntry {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_Playlist_entry", nullable = false, unique = true)
    int id;

    @ManyToOne
    @JoinColumn(name = "Playlist", nullable = false)
    Playlist playlist;

    @ManyToOne
    @JoinColumn(name = "song", nullable = false)
    Song song;

    @Column(name = "position")
    Double position;

    @Column(name = "addDate", nullable = false)
    Timestamp addDate;



    static List<PlaylistEntry> addPlaylistEntriesAtEnd(Playlist playlist, double position, List<Song> songs){
        List<PlaylistEntry> returnVale = new ArrayList<>();
        int i=1;
        Session session = HibernateConfig.getSessionFactory().openSession();
        session.setJdbcBatchSize(20);
        Transaction tx = session.beginTransaction();
        for(Song song: songs){
            PlaylistEntry tmp = new PlaylistEntry();
            tmp.song =song;
            tmp.playlist=playlist;
            tmp.position = position+i-1;
            tmp.addDate = new Timestamp(System.currentTimeMillis());
            session.save(tmp);
            if(i % 20==0){
                session.flush();
                session.clear();
            }
            i++;
        }
        tx.commit();
        session.close();
        return returnVale;
    }
    static List<PlaylistEntry> addPlaylistEntries(Playlist playlist, double position1, double position2, List<Song> songs){
        List<PlaylistEntry> returnVale = new ArrayList<>();
        double dx = Math.abs(position2-position1)/((double)songs.size()+1);
        int i=1;
        Session session = HibernateConfig.getSessionFactory().openSession();
        session.setJdbcBatchSize(20);
        Transaction tx = session.beginTransaction();
        for(Song song: songs){
            PlaylistEntry tmp = new PlaylistEntry();
            tmp.song =song;
            tmp.playlist=playlist;
            tmp.position = position1+((i)*dx);
            tmp.addDate = new Timestamp(System.currentTimeMillis());
            session.save(tmp);
            if(i % 20==0){
                session.flush();
                session.clear();
            }
            i++;
        }
        tx.commit();
        session.close();
        return returnVale;
    }
    static List<PlaylistEntry> addPlaylistEntriesAtBeginning(Playlist playlist, double position, List<Song> songs){
        List<PlaylistEntry> returnVale = new ArrayList<>();
        int i=-1;
        Session session = HibernateConfig.getSessionFactory().openSession();
        session.setJdbcBatchSize(20);
        Transaction tx = session.beginTransaction();
        for(Song song: songs){
            PlaylistEntry tmp = new PlaylistEntry();
            tmp.song =song;
            tmp.playlist=playlist;
            tmp.position = position+i-1;
            tmp.addDate = new Timestamp(System.currentTimeMillis());
            session.save(tmp);
            if(i % 20==0){
                session.flush();
                session.clear();
            }
            i--;
        }
        tx.commit();
        session.close();
        return returnVale;
    }




    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Song getSong() {
        return song;
    }
    public void setSong(Song song) {
        this.song = song;
    }
    public double getPosition() {
        return position;
    }
}
