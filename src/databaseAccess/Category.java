
package databaseAccess;

import java.awt.Color;

/**
 * Represents one record in the table "category".
 */

public class Category {
    private final int id;
    private final int subCatOf;
    private final String name;
    private final Color colorJava;
    private final String color;
    private final String note;
    
    public Category(int id, int subCatOf, String name, String color, String note) {
        this.id = id;
        this.subCatOf = subCatOf;
        this.name = name;
        this.colorJava = Color.getColor(color);
        this.color = color;
        this.note = note;
    }
    
    public int getId() {
        return id;
    }
    
    public int getSubCatOf() {
        return subCatOf;
    }
    
    public String getName() {
        return name;
    }
    
    public Color getColorJava() {
        return colorJava;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getNote() {
        return note;
    }

    public String toString() {return getName(); }
}
