package db.DTO; // 또는 db.DTO

/**
 * 오답 데이터를 담기 위한 객체
 */
public class WrongData {
    private String question;
    private String selected;
    private String correct;

    public WrongData(String question, String selected, String correct) {
        this.question = question;
        this.selected = selected;
        this.correct = correct;
    }

    // Getter들 (필요시 추가)
    public String getQuestion() { return question; }
    public String getSelected() { return selected; }
    public String getCorrect() { return correct; }
}