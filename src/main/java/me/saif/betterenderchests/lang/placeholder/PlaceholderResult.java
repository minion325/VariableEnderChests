package me.saif.betterenderchests.lang.placeholder;

public class PlaceholderResult {

    private String placeholder,value;

    public PlaceholderResult(String placeholder, String value) {
        this.placeholder = placeholder;
        this.value = value;
    }

    public String replace(String message) {
        return message.replace(placeholder, value);
    }

    public static PlaceholderResult of(String placeholder, String value) {
        return new PlaceholderResult(placeholder, value);
    }

}
