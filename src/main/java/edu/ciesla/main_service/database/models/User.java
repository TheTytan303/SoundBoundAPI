package edu.ciesla.main_service.database.models;

import edu.ciesla.main_service.database.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.ArrayList;

public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_user", unique = true, nullable = false)
    int id_user;

    @Column(name = "nickname", nullable = false)
    String nickname;

    @Column(name = "user_token", nullable = true)
    @GeneratedValue()
    String user_token;

    @Column(name = "password", nullable = true)
    String password;

    private User(){}
    public String generateToken(){
        return user_token;
    }
    public String getToken() {
        return user_token;
    }


    //-------------------------------------------------------------------STATIC:
    public static User getUser(int ID){
        User returnVale = new User();
        String sql = "";


        return returnVale;
    }

    private static ArrayList<User> getCommand(String sqlCommand){
        ArrayList<User> returnVale = new ArrayList<>();
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            NativeQuery query = session.createNativeQuery(sqlCommand);
            query.addEntity(User.class);
            returnVale.addAll(query.list());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnVale;
    }
}