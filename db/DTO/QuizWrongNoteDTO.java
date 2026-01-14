package db.DTO;

import java.time.LocalDate;


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