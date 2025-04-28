package tn.badro.tests;

import tn.badro.entities.Preferences;
import tn.badro.services.PreferencesService;
import tn.badro.tools.MyDataBase;


import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        //MyDataBase md = MyDataBase.getInstance();
        PreferencesService ps = new PreferencesService();
        Preferences pref = new Preferences("Temperate", "Austria", "Philosophy", "English", "Online", "Private university", "Visiting a museum", "B1");
        try {
            //ps.ajouter(pref);
            //ps.modifier(1,"Winter");
            System.out.println(ps.recuperer());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
