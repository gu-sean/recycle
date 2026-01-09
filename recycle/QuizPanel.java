package recycle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set; 
import java.util.HashSet; 
import java.util.Random; 
import java.util.stream.Collectors;
import java.sql.SQLException;

import javax.swing.text.DefaultCaret; 

import db.DTO.UserDTO;
import db.DAO.RecycleLogDAO;
import db.DAO.GuideDAO;

public class QuizPanel extends JPanel {

    private final UserDTO currentUser;
    private final RecycleLogDAO logDAO;
    private final int QUIZ_REWARD_POINTS = 50; 
    private static final int QUIZ_COUNT = 5; 
    
    private final Runnable rankUpdateCallback;

    private JLabel questionLabel;
    private JPanel quizGrid;
    private JLabel messageLabel;
    
    private JEditorPane guideDisplayArea; 
    private JScrollPane guideScrollPane; 
    private JLabel questionImageLabel; 

    private List<Quiz> quizList;
    private List<WrongAnswer> wrongQuizzes = new ArrayList<>(); 
    private int currentQuizIndex = 0;
    private int correctCount = 0;
    private boolean answerSubmitted = false;
    private final Random random = new Random(); 
    
    private boolean quizAlreadyTaken = false; 

    private static final Map<String, String> CATEGORY_IMAGE_MAP = new HashMap<>();
    static {
        CATEGORY_IMAGE_MAP.put("종이", "paper.png");      
        CATEGORY_IMAGE_MAP.put("비닐", "vinyl.png");
        CATEGORY_IMAGE_MAP.put("유리병", "glass.png");
        CATEGORY_IMAGE_MAP.put("캔ㆍ고철", "can_metal.png");
        CATEGORY_IMAGE_MAP.put("스티로폼", "styrofoam.png");
        CATEGORY_IMAGE_MAP.put("플라스틱", "plastic.png");
        CATEGORY_IMAGE_MAP.put("종이팩", "paper_pack.png");
        CATEGORY_IMAGE_MAP.put("기타", "clothes.png"); 
    }

    private static class WrongAnswer {
        Quiz quiz;
        String selectedAnswer;

        WrongAnswer(Quiz quiz, String selectedAnswer) {
            this.quiz = quiz;
            this.selectedAnswer = selectedAnswer;
        }
    }

    private ImageIcon loadImage(String categoryName, int size) {
        String fileName = CATEGORY_IMAGE_MAP.get(categoryName);
        if (fileName == null) return null;
        String imagePath = "/images/" + fileName; 
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH); 
            return new ImageIcon(scaledImage);
        }
        return null;
    }

    private class Quiz {
        enum QuizType { IMAGE_TO_CATEGORY, GUIDE_TO_IMAGE }
        QuizType type;
        String question;
        String correctAnswer; 
        List<String> options; 
        String guideSnippet; 

        public Quiz(QuizType type, String question, String correctAnswer, List<String> options, String guide) {
            this.type = type; this.question = question; this.correctAnswer = correctAnswer;
            this.options = options; this.guideSnippet = guide;
            Collections.shuffle(this.options, random);
        }
        public String getCorrectAnswer() { return correctAnswer; }
    }
    
    private class QuizOptionItem extends JPanel {
        private String answer;
        private Color defaultBorderColor = new Color(180, 200, 255); 
        private boolean isImageOption; 

        public QuizOptionItem(String answerText, boolean isImageOption) {
            this.answer = answerText; this.isImageOption = isImageOption; 
            setLayout(new BorderLayout(5, 5)); setPreferredSize(new Dimension(150, 150)); 
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setBackground(new Color(240, 245, 255)); 
            setBorder(BorderFactory.createLineBorder(defaultBorderColor, 2));

            ImageIcon itemIcon = loadImage(answerText, 120); 
            JLabel imageLabel = new JLabel(itemIcon, SwingConstants.CENTER); 
            JLabel textLabel = new JLabel("<html><center><b>" + answerText + "</b></center></html>", SwingConstants.CENTER);
            add(imageLabel, BorderLayout.CENTER); add(textLabel, BorderLayout.SOUTH);
        }
        
        public QuizOptionItem(String answerText) {
            this.answer = answerText; this.isImageOption = false; 
            setLayout(new GridBagLayout()); setPreferredSize(new Dimension(150, 150));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setBackground(new Color(240, 245, 255)); 
            setBorder(BorderFactory.createLineBorder(defaultBorderColor, 2));
            JLabel textLabel = new JLabel("<html><center><b>" + answerText + "</b></center></html>", SwingConstants.CENTER);
            textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18)); add(textLabel);
        }

        public String getAnswer() { return this.answer; }
        public void setSelected(boolean isCorrect) {
            setBorder(BorderFactory.createLineBorder(isCorrect ? new Color(0, 150, 0) : new Color(200, 0, 0), 3));
        }
    }

    public QuizPanel(UserDTO user, Runnable rankUpdateCallback) throws Exception {
        this.currentUser = user;
        this.rankUpdateCallback = rankUpdateCallback; 
        this.logDAO = new RecycleLogDAO(); 
        
        try {
            this.quizAlreadyTaken = logDAO.hasTakenQuizToday(currentUser.getUserId());
            initializeQuizData();
        } catch (SQLException e) { throw new Exception("데이터 로드 실패", e); }

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(15, 15));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        
        questionLabel = new JLabel("분리수거 퀴즈", SwingConstants.LEFT);
        questionLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        northPanel.add(questionLabel);
        northPanel.add(Box.createVerticalStrut(10)); 
        
        questionImageLabel = new JLabel("", SwingConstants.CENTER);
        questionImageLabel.setPreferredSize(new Dimension(180, 180)); 
        JPanel imageWrapper = new JPanel(new GridBagLayout());
        imageWrapper.add(questionImageLabel); 
        northPanel.add(imageWrapper);
        
        guideDisplayArea = new JEditorPane("text/html", "");
        guideDisplayArea.setEditable(false);
        guideScrollPane = new JScrollPane(guideDisplayArea); 
        guideScrollPane.setPreferredSize(new Dimension(600, 80)); 
        northPanel.add(guideScrollPane);

        add(northPanel, BorderLayout.NORTH);

        quizGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        add(quizGrid, BorderLayout.CENTER);

        messageLabel = new JLabel("준비되셨나요?", SwingConstants.CENTER);
        add(messageLabel, BorderLayout.SOUTH);

        if (this.quizAlreadyTaken) {
            showQuizAlreadyTakenMessage();
        } else {
            loadNextQuiz();
        }
    }

    private void initializeQuizData() {
        List<String> allCategoryNames = new ArrayList<>(CATEGORY_IMAGE_MAP.keySet());
        quizList = new ArrayList<>();
        Collections.shuffle(allCategoryNames);

        for (int i = 0; i < 2; i++) {
            String correct = allCategoryNames.get(i);
            quizList.add(new Quiz(Quiz.QuizType.IMAGE_TO_CATEGORY, "다음 이미지의 분리수거 카테고리는?", correct, generateOptions(allCategoryNames, correct), null));
        }
        for (int i = 2; i < 5; i++) {
            String correct = allCategoryNames.get(i % allCategoryNames.size());
            quizList.add(new Quiz(Quiz.QuizType.GUIDE_TO_IMAGE, "다음 지침에 맞는 카테고리 이미지를 고르세요.", correct, generateOptions(allCategoryNames, correct), "이물질을 제거하고 깨끗이 씻어서 배출해야 합니다."));
        }
        Collections.shuffle(quizList);
    }

    private List<String> generateOptions(List<String> all, String correct) {
        List<String> options = new ArrayList<>(); options.add(correct);
        List<String> wrongs = all.stream().filter(c -> !c.equals(correct)).collect(Collectors.toList());
        Collections.shuffle(wrongs); options.addAll(wrongs.stream().limit(3).collect(Collectors.toList()));
        Collections.shuffle(options); return options;
    }

    private void loadNextQuiz() {
        quizGrid.removeAll(); answerSubmitted = false;
        guideScrollPane.setVisible(false); questionImageLabel.setVisible(false);

        if (currentQuizIndex < quizList.size()) {
            Quiz current = quizList.get(currentQuizIndex);
            questionLabel.setText((currentQuizIndex + 1) + ". " + current.question);
            
            if (current.type == Quiz.QuizType.IMAGE_TO_CATEGORY) {
                questionImageLabel.setIcon(loadImage(current.correctAnswer, 150));
                questionImageLabel.setVisible(true);
            } else {
                guideDisplayArea.setText("<html><body style='padding:5px;'><b>[분리수거 지침]</b><br>" + current.guideSnippet + "</body></html>");
                guideScrollPane.setVisible(true);
            }
            
            for (String option : current.options) {
                QuizOptionItem item = (current.type == Quiz.QuizType.GUIDE_TO_IMAGE) ? new QuizOptionItem(option, true) : new QuizOptionItem(option);
                item.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (!answerSubmitted) { handleAnswer(item, current); answerSubmitted = true; }
                    }
                });
                quizGrid.add(item);
            }
            quizGrid.revalidate(); quizGrid.repaint();
        } else { finishQuiz(); }
    }

    private void handleAnswer(QuizOptionItem selectedItem, Quiz current) {
        boolean isCorrect = selectedItem.getAnswer().equals(current.getCorrectAnswer());
        if (isCorrect) {
            correctCount++;
            messageLabel.setText("✅ 정답입니다!");
        } else {
          
            wrongQuizzes.add(new WrongAnswer(current, selectedItem.getAnswer()));
            messageLabel.setText("❌ 오답! 정답은 '" + current.getCorrectAnswer() + "'입니다.");
        }
        selectedItem.setSelected(isCorrect);

        new Timer(1500, e -> { currentQuizIndex++; loadNextQuiz(); ((Timer)e.getSource()).stop(); }).start();
    }

    private void showWrongAnswerNote() {
        questionLabel.setText("📖 오답 노트 (틀린 문제 복습)");
        quizGrid.removeAll();
        quizGrid.setLayout(new BorderLayout());
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        if (wrongQuizzes.isEmpty()) {
            listPanel.add(new JLabel("🎉 모든 문제를 맞히셨습니다! 오답이 없습니다."));
        } else {
            for (WrongAnswer wa : wrongQuizzes) {
                JPanel item = new JPanel(new BorderLayout(10, 5));
                item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
                item.setBackground(Color.WHITE);
                
                String text = String.format("<html><b>질문:</b> %s<br><font color='red'>내가 쓴 답: %s</font> | <font color='blue'>정답: %s</font></html>",
                        wa.quiz.question, wa.selectedAnswer, wa.quiz.correctAnswer);
                item.add(new JLabel(text), BorderLayout.CENTER);
                item.add(new JLabel(loadImage(wa.quiz.correctAnswer, 50)), BorderLayout.WEST);
                
                listPanel.add(item);
                listPanel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        quizGrid.add(scroll, BorderLayout.CENTER);
        
        JButton backBtn = new JButton("처음으로");
        backBtn.addActionListener(e -> showQuizAlreadyTakenMessage());
        quizGrid.add(backBtn, BorderLayout.SOUTH);

        quizGrid.revalidate(); quizGrid.repaint();
    }

    private void showQuizAlreadyTakenMessage() {
        questionLabel.setText("🚫 오늘 퀴즈 완료");
        quizGrid.removeAll();
        quizGrid.setLayout(new GridBagLayout());
        
        JPanel container = new JPanel(new GridLayout(2, 1, 10, 10));
        JLabel msg = new JLabel("<html><center>내일 다시 도전해 주세요!</center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        
        JButton noteBtn = new JButton("오답 노트 확인하기");
        noteBtn.addActionListener(e -> showWrongAnswerNote());
        
        container.add(msg); container.add(noteBtn);
        quizGrid.add(container);
        
        guideScrollPane.setVisible(false); questionImageLabel.setVisible(false);
        quizGrid.revalidate(); quizGrid.repaint();
    }

    private void finishQuiz() {
        questionLabel.setText("퀴즈 결과");
        quizGrid.removeAll();
        quizGrid.setLayout(new BorderLayout());
        
        int reward = (correctCount == 5) ? 50 : (correctCount == 4) ? 30 : (correctCount == 3) ? 10 : 0;
        try { if (reward > 0) { logDAO.insertQuizReward(currentUser.getUserId(), "퀴즈 보상", reward); if (rankUpdateCallback != null) rankUpdateCallback.run(); }
        } catch (Exception e) { e.printStackTrace(); }

        JPanel resPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        JLabel res = new JLabel("정답 개수: " + correctCount + " / 5", SwingConstants.CENTER);
        res.setFont(new Font("맑은 고딕", Font.BOLD, 25));
        
        JButton noteBtn = new JButton("오답 노트 보기");
        noteBtn.addActionListener(e -> showWrongAnswerNote());
        
        resPanel.add(res);
        resPanel.add(new JLabel("획득 포인트: " + reward + " P", SwingConstants.CENTER));
        resPanel.add(noteBtn);

        quizGrid.add(resPanel, BorderLayout.CENTER);
        quizGrid.revalidate(); quizGrid.repaint();
    }

    private class DisabledCaret extends DefaultCaret {
        @Override public void setVisible(boolean v) { super.setVisible(false); }
        @Override public void paint(Graphics g) {}
    }
}