package edu.ciesla.main_service.database.models.exceptions;

public class AlreadyInDatabase  extends Exception{

    public Object object;

    public AlreadyInDatabase(String message){
        super(message);
    }
}
