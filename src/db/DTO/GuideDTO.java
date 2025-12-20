package db.DTO;


public class GuideDTO {
    
    private final String category;
    private final String content;

    public GuideDTO(String category, String content) {
        this.category = category;
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }
}