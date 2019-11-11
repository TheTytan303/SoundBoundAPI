package edu.ciesla.main_service.database.models;

public interface jsonable {
    String toJson();
    jsonable deJson(String json);
}
