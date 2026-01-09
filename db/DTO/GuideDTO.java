package db.DTO;

public class GuideDTO {
    private final String category;   
    private final String itemName; 
    private final String content;    

    public GuideDTO(String category, String itemName, String content) {
        this.category = category;
        this.itemName = itemName;
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public String getItemName() {
        return itemName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "[" + category + "] " + itemName;
    }
}