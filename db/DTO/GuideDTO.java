package db.DTO;


public class GuideDTO {
    
    private int itemId;           
    private String categoryId;    
    private String categoryName;  
    private String itemName;        
    private String content;      
    private String itemImagePath;  
    private String markImagePath;  

 
    public GuideDTO(int itemId, String itemName, String categoryId, String categoryName, 
            String content, String itemImagePath, String markImagePath) {
    	
		this.itemId = itemId;
		this.itemName = itemName;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.content = content;
		this.itemImagePath = itemImagePath;
		this.markImagePath = markImagePath;
		}

   
    public String getDisplayId() {
        String prefix = "I"; 
        
        if (categoryId != null) {
            switch (categoryId) {
                case "C01": prefix = "P"; break; // Paper (종이)
                case "C02": prefix = "V"; break; // Vinyl (비닐)
                case "C03": prefix = "G"; break; // Glass (유리)
                case "C04": prefix = "K"; break; // PacK (종이팩)
                case "C05": prefix = "M"; break; // Metal (캔/고철)
                case "C06": prefix = "S"; break; // Styrofoam (스티로폼)
                case "C07": prefix = "L"; break; // pLastic (플라스틱)
                case "C08": prefix = "E"; break; // Etc (기타)
            }
        }

        return String.format("%s%03d", prefix, this.itemId);
    }


    public int getItemId() { return itemId; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getItemName() { return itemName; }
    public String getContent() { return content; }
    public String getItemImagePath() { return itemImagePath; }
    public String getMarkImagePath() { return markImagePath; }

  
    @Override
    public String toString() {
        return "[" + getDisplayId() + "] " + itemName;
    }
}