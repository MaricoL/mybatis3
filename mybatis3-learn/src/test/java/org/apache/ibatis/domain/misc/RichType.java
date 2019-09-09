package org.apache.ibatis.domain.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichType {

    private RichType richType;

    private String richField;

    private String richProperty;

    private Map richMap = new HashMap();

    private List<RichType> richList = new ArrayList<RichType>() {
        {
            add(new RichType(){
                {
                    setRichProperty("qqq");
                }
            });
        }
    };

    public RichType getRichType() {
        return richType;
    }

    public void setRichType(RichType richType) {
        this.richType = richType;
    }

    public String getRichProperty() {
        return richProperty;
    }

    public void setRichProperty(String richProperty) {
        this.richProperty = richProperty;
    }

    public List<RichType> getRichList() {
        return richList;
    }

    public void setRichList(List<RichType> richList) {
        this.richList = richList;
    }

    public Map getRichMap() {
        return richMap;
    }

    public void setRichMap(Map richMap) {
        this.richMap = richMap;
    }
}
