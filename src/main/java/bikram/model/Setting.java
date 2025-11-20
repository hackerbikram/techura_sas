package bikram.model;

public class Setting {
    private String name = "Techura";
    private String version = "0.1";
    private Language language;
    private Theme theme;
    private int textSize;
    public Setting(){}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getTextSize() {
        return textSize;
    }

    public Language getLanguage() {
        return language;
    }

    public String getVersion() {
        return version;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private enum Language{
        English,
        Japanise,
        Nepali,
        Chainese,
        Korean,
        Hindi,
        Others
    }
    private enum Theme{
        Dark,
        Light
    }

}
