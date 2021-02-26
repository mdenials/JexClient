package me.dustin.jex.option.types;


import me.dustin.jex.option.Option;

import java.lang.reflect.Field;

public class StringOption extends Option {
    public StringOption(String name) {
        this.name = name;
    }

    public String getValue() {
        try {
            for (Field f : this.getModule().getClass().getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(this.getFieldName())) {
                    return (String) f.get(this.getModule());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
