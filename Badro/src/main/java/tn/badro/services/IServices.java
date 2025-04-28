package tn.badro.services;

import tn.badro.entities.Preferences;
import java.sql.SQLException;


import java.util.List;

public interface IServices<T> {
    void ajouter (T t) throws SQLException;
    void supprimer (T t);
    void modifier (Preferences preferences) throws SQLException;
    List<T> recuperer() throws SQLException;
}
