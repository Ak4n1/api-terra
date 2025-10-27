package com.ak4n1.terra.api.terra_api.game.dto;


import java.util.List;

public class OfflineStoreDTO {
    private String char_name;
    private String title;
    private byte type;
    private long time;
    private List<OfflineStoreItemDTO> items;

    public OfflineStoreDTO(String title, byte type, long time, List<OfflineStoreItemDTO> items) {
        this.title = title;
        this.type = type;
        this.time = time;
        this.items = items;
    }

    public OfflineStoreDTO() {
    }

    public String getChar_name() {
        return char_name;
    }

    public void setChar_name(String char_name) {
        this.char_name = char_name;
    }

    // Getters
    public String getTitle() { return title; }
    public byte getType() { return type; }
    public long getTime() { return time; }
    public List<OfflineStoreItemDTO> getItems() { return items; }



    public void setTime(long time) {
        this.time = time;
    }

    public void setItems(List<OfflineStoreItemDTO> items) {
        this.items = items;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
