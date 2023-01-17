package me.saif.betterenderchests.utils;

public class CaselessString {

    private String original;
    private String lowerCase;

    public CaselessString(String name) {
        this.original = name;
        this.lowerCase = name.toLowerCase();
    }

    public String getOriginal() {
        return original;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaselessString)) return false;
        CaselessString mcName = (CaselessString) o;
        return mcName.lowerCase.equals(this.lowerCase);
    }

    @Override
    public int hashCode() {
        return this.lowerCase.hashCode();
    }
}
