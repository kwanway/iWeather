package com.kwanway.iweather.db;

import org.litepal.crud.DataSupport;

public class Provice extends DataSupport {

    private int id;
    private String proviceName;
    private int proviceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProviceName() {
        return proviceName;
    }

    public void setProviceCode(int proviceCode) {
        this.proviceCode = proviceCode;
    }

    public int getProviceCode() {
        return proviceCode;
    }

    public void setProviceName(String proviceName) {
        this.proviceName = proviceName;
    }
}
