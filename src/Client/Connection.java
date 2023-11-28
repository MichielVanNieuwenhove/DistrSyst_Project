package Client;

import javax.crypto.SecretKey;
import java.util.LinkedList;
import java.util.List;

public class Connection {
    private int index_mine;
    private String tag_mine;
    private SecretKey key_mine;

    private int index_other;
    private String tag_other;
    private SecretKey key_other;

    private String name_other;

    private List<String> history = new LinkedList<>();

    public void setAttributes_mine(int currentIndex_mine, String currentTag_mine, SecretKey key_mine){
        this.index_mine = currentIndex_mine;
        this.tag_mine = currentTag_mine;
        this.key_mine = key_mine;
    }

    public void setAttributes_other(int currentIndex_other, String currentTag_other, SecretKey key_other){
        this.index_other = currentIndex_other;
        this.tag_other = currentTag_other;
        this.key_other = key_other;
    }

    public void addToHistory(String text){
        history.add(text);
    }

    //getters & setters
    public int getIndex_mine() {
        return index_mine;
    }

    public String getTag_mine() {
        return tag_mine;
    }

    public SecretKey getKey_mine() {
        return key_mine;
    }

    public int getIndex_other() {
        return index_other;
    }

    public String getTag_other() {
        return tag_other;
    }

    public SecretKey getKey_other() {
        return key_other;
    }

    public String getName_other() {
        return name_other;
    }

    public void setName_other(String name_other) {
        this.name_other = name_other;
    }

    public List<String> getHistory() {
        return history;
    }
}
