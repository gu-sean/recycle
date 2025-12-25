package db.DTO;

import java.time.LocalDate;

/**
 * 퀴즈 오답 정보를 담는 데이터 전송 객체 (DTO)
 * DB의 QUIZ_WRONG_NOTE 테이블과 매핑됩니다.
 */
public class QuizWrongNoteDTO {
    private int noteId;
    private String userId;
    private String questionText;
    private String selectedAnswer;
    private String correctAnswer;
    private LocalDate createdAt;

    public QuizWrongNoteDTO() {}

    public QuizWrongNoteDTO(String questionText, String selectedAnswer, String correctAnswer) {
        this.questionText = questionText;
        this.selectedAnswer = selectedAnswer;
        this.correctAnswer = correctAnswer;
        this.createdAt = LocalDate.now();
    }

    // Getter & Setter
    public String getQuestionText() { return questionText; }
    public String getSelectedAnswer() { return selectedAnswer; }
    public String getCorrectAnswer() { return correctAnswer; }
}