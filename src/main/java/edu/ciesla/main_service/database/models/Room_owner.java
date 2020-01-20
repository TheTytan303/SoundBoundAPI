package edu.ciesla.main_service.database.models;

import edu.ciesla.main_service.database.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;

@Entity
@Table(name = "room_owners", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_room_owner"}))
public class Room_owner{
    @Id
    @Column(name = "ID_room_owner", nullable = false, unique = true)
    int id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user", nullable = true)
    User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room", nullable = true)
    Room room;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "song", nullable = true)
    Song song;

    void update(){
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tc = session.beginTransaction();
        session.update(this);
        session.flush();
        tc.commit();
        session.close();
    }

    public Room_owner(){};
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Song getSong() {
        return song;
    }
    public void setSong(Song song) {
        this.song = song;
    }
}