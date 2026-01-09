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

    private static class QuizItemData {
        String itemName;
        String categoryName;
        String disposalGuide;

        QuizItemData(String itemName, String categoryName, String disposalGuide) {
            this.itemName = itemName;
            this.categoryName = categoryName;
            this.disposalGuide = disposalGuide;
        }
    }

    private ImageIcon loadImage(String categoryName, int size) {
        String fileName = CATEGORY_IMAGE_MAP.get(categoryName);
        if (fileName == null) return null;
     
        String imagePath = "/images/" + fileName; 
        URL imageUrl = getClass().getResource(imagePath);

        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image image = originalIcon.getImage();
            Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH); 
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
            this.type = type;
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.options = options;
            this.guideSnippet = guide;
            Collections.shuffle(this.options, random);
        }
        public String getCorrectAnswer() { return correctAnswer; }
    }
    
    private class QuizOptionItem extends JPanel {
        private String answer;
        private Color defaultBorderColor = new Color(180, 200, 255); 
        private int defaultBorderThickness = 2;
        private boolean isImageOption; 

        public QuizOptionItem(String answerText, boolean isImageOption) {
            this.answer = answerText;
            this.isImageOption = isImageOption; 
            setLayout(new BorderLayout(5, 5));
            setPreferredSize(new Dimension(150, 150)); 
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBackground(new Color(240, 245, 255)); 
            
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(defaultBorderColor, defaultBorderThickness), 
                BorderFactory.createEmptyBorder(3, 3, 3, 3) 
            ));

            ImageIcon itemIcon = loadImage(answerText, 140); 
            JPanel imageWrapper = new JPanel(new GridBagLayout());
            imageWrapper.setBackground(new Color(240, 245, 255)); 
            JLabel imageLabel = new JLabel(itemIcon, SwingConstants.CENTER); 
            imageWrapper.add(imageLabel);

            JLabel textLabel = new JLabel("<html><center><b>" + answerText + "</b></center></html>", SwingConstants.CENTER);
            textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            textLabel.setBackground(new Color(240, 245, 255)); 
            textLabel.setOpaque(true);

            add(imageWrapper, BorderLayout.CENTER);
            add(textLabel, BorderLayout.SOUTH);
        }
        
        public QuizOptionItem(String answerText) {
            this.answer = answerText;
            this.isImageOption = false; 
            setLayout(new GridBagLayout()); 
            setPreferredSize(new Dimension(150, 150));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBackground(new Color(240, 245, 255)); 
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(defaultBorderColor, defaultBorderThickness), 
                BorderFactory.createEmptyBorder(10, 10, 10, 10) 
            ));

            JLabel textLabel = new JLabel("<html><center><b>" + answerText + "</b></center></html>", SwingConstants.CENTER);
            textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 19)); 
            add(textLabel);
        }

        public String getAnswer() { return this.answer; }

        public void setSelected(boolean isCorrect) {
            Color borderColor = isCorrect ? new Color(0, 150, 0) : new Color(200, 0, 0);
            int padding = this.isImageOption ? 3 : 10;
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 3), 
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)
            ));
        }

        public void resetBorder() {
            int padding = this.isImageOption ? 3 : 10;
             setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(defaultBorderColor, defaultBorderThickness), 
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)
            ));
        }
    }

    public QuizPanel(UserDTO user, Runnable rankUpdateCallback) throws Exception {
        this.currentUser = user;
        this.rankUpdateCallback = rankUpdateCallback; 
        this.logDAO = new RecycleLogDAO(); 
        
        try {
            this.quizAlreadyTaken = logDAO.hasTakenQuizToday(currentUser.getUserId());
            if (!this.quizAlreadyTaken) {
                initializeQuizData();
            }
        } catch (SQLException e) {
             throw new Exception("데이터 로드 실패", e);
        }

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(15, 15));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        
        questionLabel = new JLabel("", SwingConstants.LEFT);
        questionLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        northPanel.add(questionLabel);

        northPanel.add(Box.createVerticalStrut(15)); 
        
        questionImageLabel = new JLabel("", SwingConstants.CENTER);
        questionImageLabel.setPreferredSize(new Dimension(200, 200)); 
        JPanel imageWrapperPanel = new JPanel(new GridBagLayout());
        imageWrapperPanel.add(questionImageLabel); 
        northPanel.add(imageWrapperPanel);
        
        guideDisplayArea = new JEditorPane();
        guideDisplayArea.setContentType("text/html");
        guideDisplayArea.setEditable(false);
        guideDisplayArea.setBackground(new Color(250, 250, 240));
        guideDisplayArea.setCaret(new DisabledCaret());
        
        guideScrollPane = new JScrollPane(guideDisplayArea); 
        guideScrollPane.setPreferredSize(new Dimension(600, 100)); 
        guideScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        northPanel.add(guideScrollPane);

        add(northPanel, BorderLayout.NORTH);

        quizGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        add(quizGrid, BorderLayout.CENTER);

        messageLabel = new JLabel("분리수거 퀴즈에 도전해 보세요!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        add(messageLabel, BorderLayout.SOUTH);

        if (this.quizAlreadyTaken) {
            showQuizAlreadyTakenMessage();
        } else if (quizList != null && !quizList.isEmpty()) {
            loadNextQuiz();
        }
    }

    private void initializeQuizData() {
        List<QuizItemData> allItems = new ArrayList<>();

        List<String> allCategoryNames = new ArrayList<>(CATEGORY_IMAGE_MAP.keySet());
        
        quizList = new ArrayList<>();
        Collections.shuffle(allCategoryNames, random);

        for (int i = 0; i < 2; i++) {
            String correctCategory = allCategoryNames.get(i);
            quizList.add(new Quiz(
                Quiz.QuizType.IMAGE_TO_CATEGORY,
                "다음 분리수거 이미지에 해당하는 올바른 카테고리를 선택하세요.",
                correctCategory,
                generateOptions(allCategoryNames, correctCategory),
                null
            ));
        }

        for (int i = 2; i < 5; i++) {
            String correctCategory = allCategoryNames.get(i % allCategoryNames.size());
            String guide = "분리수거 지침을 확인하고 카테고리를 맞춰보세요."; 
            
            quizList.add(new Quiz(
                Quiz.QuizType.GUIDE_TO_IMAGE,
                "다음 지침에 해당하는 카테고리를 이미지에서 골라주세요.",
                correctCategory,
                generateOptions(allCategoryNames, correctCategory),
                guide
            ));
        }
        Collections.shuffle(quizList);
    }

    private List<String> generateOptions(List<String> allCategories, String correctAnswer) {
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        List<String> wrongs = allCategories.stream()
                .filter(c -> !c.equals(correctAnswer))
                .collect(Collectors.toList());
        Collections.shuffle(wrongs);
        options.addAll(wrongs.stream().limit(3).collect(Collectors.toList()));
        Collections.shuffle(options);
        return options;
    }

    private void loadNextQuiz() {
        quizGrid.removeAll();
        answerSubmitted = false;
        guideScrollPane.setVisible(false);
        questionImageLabel.setIcon(null);
        questionImageLabel.setVisible(false);

        if (currentQuizIndex < quizList.size()) {
            Quiz currentQuiz = quizList.get(currentQuizIndex);
            questionLabel.setText("<html>" + (currentQuizIndex + 1) + "/" + quizList.size() + ". " + currentQuiz.question + "</html>");
            
            if (currentQuiz.type == Quiz.QuizType.IMAGE_TO_CATEGORY) {
                ImageIcon imageIcon = loadImage(currentQuiz.correctAnswer, 150);
                if (imageIcon != null) {
                    questionImageLabel.setIcon(imageIcon);
                    questionImageLabel.setVisible(true);
                }
            } else {
                guideDisplayArea.setText("<html><body><div style='padding:10px;'><b>[지침]</b><br>" + currentQuiz.guideSnippet + "</div></body></html>");
                guideScrollPane.setVisible(true);
            }
            
            for (String option : currentQuiz.options) {
                QuizOptionItem item = (currentQuiz.type == Quiz.QuizType.GUIDE_TO_IMAGE) 
                        ? new QuizOptionItem(option, true) : new QuizOptionItem(option);
                
                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!answerSubmitted) {
                            handleAnswer(item, currentQuiz);
                            answerSubmitted = true;
                        }
                    }
                });
                quizGrid.add(item);
            }
            messageLabel.setText("답변을 선택해 주세요.");
            quizGrid.revalidate(); quizGrid.repaint();
        } else {
            finishQuiz();
        }
    }

    private void handleAnswer(QuizOptionItem selectedItem, Quiz currentQuiz) {
        boolean isCorrect = selectedItem.getAnswer().equals(currentQuiz.getCorrectAnswer());
        for (Component comp : quizGrid.getComponents()) {
            if (comp instanceof QuizOptionItem) {
                QuizOptionItem item = (QuizOptionItem) comp;
                if (item.getAnswer().equals(currentQuiz.getCorrectAnswer())) item.setSelected(true);
                else if (item == selectedItem && !isCorrect) item.setSelected(false);
            }
        }

        if (isCorrect) {
            correctCount++;
            messageLabel.setText("✅ 정답입니다!");
            messageLabel.setForeground(new Color(0, 150, 0));
        } else {
            messageLabel.setText("<html>❌ 오답! 정답은 <b>'" + currentQuiz.getCorrectAnswer() + "'</b>입니다.</html>");
            messageLabel.setForeground(new Color(200, 0, 0));
        }

        new Timer(1500, e -> {
            currentQuizIndex++;
            loadNextQuiz();
            ((Timer)e.getSource()).stop();
        }).start();
    }

    private void showQuizAlreadyTakenMessage() {
        questionLabel.setText("🚫 오늘 퀴즈 완료");
        quizGrid.removeAll();
        quizGrid.setLayout(new GridBagLayout());
        JLabel msg = new JLabel("<html><center>내일 다시 도전해 주세요!</center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        quizGrid.add(msg);
        quizGrid.revalidate(); quizGrid.repaint();
    }

    private void finishQuiz() {
        questionLabel.setText("퀴즈 종료! 점수: " + correctCount + " / " + quizList.size());
        quizGrid.removeAll();
        quizGrid.setLayout(new BorderLayout());
        
        int reward = (correctCount == 5) ? 50 : (correctCount == 4) ? 30 : (correctCount == 3) ? 10 : 0;
        
        if (reward > 0) {
            try {
                logDAO.insertQuizReward(currentUser.getUserId(), "퀴즈 보상", reward);
                if (rankUpdateCallback != null) rankUpdateCallback.run();
                messageLabel.setText("🎉 " + reward + " P가 적립되었습니다.");
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            messageLabel.setText("3개 이상 정답 시 포인트가 지급됩니다.");
        }

        JLabel res = new JLabel(String.format("<html><center>정답: %d개<br>획득 포인트: %d P</center></html>", correctCount, reward), SwingConstants.CENTER);
        res.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        quizGrid.add(res, BorderLayout.CENTER);
        quizGrid.revalidate(); quizGrid.repaint();
    }

    private class DisabledCaret extends DefaultCaret {
        @Override public void setVisible(boolean v) { super.setVisible(false); }
        @Override public void paint(Graphics g) {}
    }
}