package recycle;

import javax.swing.*;
import javax.swing.border.CompoundBorder; 
import javax.swing.border.EmptyBorder;    
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
import db.DAO.GuideDAO.ItemDetail;

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
    private ImageIcon loadImage(String categoryName, int size) {
        String fileName = CATEGORY_IMAGE_MAP.get(categoryName);
        if (fileName == null) {
            return null;
        }
        String imagePath = "/images/" + fileName; 
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image image = originalIcon.getImage();
            Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH); 
            return new ImageIcon(scaledImage);
        } else {
            return null;
        }
    }
    private class Quiz {
        enum QuizType {
            IMAGE_TO_CATEGORY, 
            GUIDE_TO_IMAGE    
        }
        QuizType type;
        String question;
        String correctAnswer; 
        List<String> options; 
        String questionImageName; 
        String guideSnippet; 
        String questionItemName; 

        public Quiz(QuizType type, String question, String correctAnswer, List<String> options, String image, String guide, String itemName) {
            this.type = type;
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.options = options;
            this.questionImageName = image;
            this.guideSnippet = guide;
            this.questionItemName = itemName; 
            Collections.shuffle(this.options, random);
        }
        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }
    private class QuizItem extends JPanel {
        private String answer;
        private Color defaultBorderColor = new Color(180, 200, 255); 
        private int defaultBorderThickness = 2;
        private boolean isImageOption; 
        public QuizItem(String answerText, boolean isImageOption) {
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
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER; 
            imageWrapper.add(imageLabel, gbc);
            JLabel textLabel = new JLabel("<html><center><b>" + answerText + "</b></center></html>", SwingConstants.CENTER);
            textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            textLabel.setForeground(Color.DARK_GRAY);
            textLabel.setPreferredSize(new Dimension(150, 20)); 
            textLabel.setBackground(new Color(240, 245, 255)); 
            textLabel.setOpaque(true);
            add(imageWrapper, BorderLayout.CENTER);
            add(textLabel, BorderLayout.SOUTH);
        }

        public QuizItem(String answerText) {
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
            textLabel.setForeground(new Color(40, 40, 40)); 
            textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 19)); 
            add(textLabel);
        }

        public String getAnswer() {
            return this.answer;
        }

        public void setSelected(boolean isCorrect) {
            Color borderColor = isCorrect ? new Color(0, 150, 0) : new Color(200, 0, 0);
            int thickness = 3; 
            int padding = this.isImageOption ? 3 : 10;
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, thickness), 
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
        } catch (SQLException e) {
             System.err.println("오늘 퀴즈 완료 여부 확인 DB 오류: " + e.getMessage());
             throw new Exception("퀴즈 제한 확인에 실패했습니다.", e);
        }
        try {
            if (!this.quizAlreadyTaken) {
                initializeQuizDataFromDB();
            }
        } catch (SQLException e) {
            System.err.println("퀴즈 데이터 로드 DB 오류: " + e.getMessage());
            throw new Exception("퀴즈 데이터 로드에 실패했습니다.", e);
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
        questionImageLabel.setMaximumSize(new Dimension(200, 200));
        
        JPanel imageWrapperPanel = new JPanel(new GridBagLayout());
        imageWrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200)); 
        imageWrapperPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
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
        guideScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        guideScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT); 
        northPanel.add(guideScrollPane);

        add(northPanel, BorderLayout.NORTH);

        quizGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        add(quizGrid, BorderLayout.CENTER);

        messageLabel = new JLabel("분리수거 퀴즈에 도전해 보세요!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        messageLabel.setForeground(Color.DARK_GRAY);
        add(messageLabel, BorderLayout.SOUTH);
        if (this.quizAlreadyTaken) {
            showQuizAlreadyTakenMessage();
        } else if (quizList != null && !quizList.isEmpty()) {
            loadNextQuiz();
        } else {
            questionLabel.setText("퀴즈 출제에 필요한 데이터가 부족합니다. DB를 확인해 주세요.");
            messageLabel.setText("");
        }
    }
    
    public QuizPanel(UserDTO user) throws Exception {
        this(user, null);
    }
    
    private void initializeQuizDataFromDB() throws SQLException {
        List<ItemDetail> allItems = GuideDAO.getAllItems();
        if (allItems.isEmpty()) {
            quizList = Collections.emptyList();
            return;
        }
        List<String> allCategoryNames = new ArrayList<>(CATEGORY_IMAGE_MAP.keySet());
        if (allCategoryNames.size() < 4) {
             System.err.println("경고: 퀴즈 출제에 필요한 최소 카테고리(4개)가 부족합니다.");
             quizList = Collections.emptyList(); 
             return;
        }
        Set<String> usedCategories = new HashSet<>();
        quizList = new ArrayList<>();
        Collections.shuffle(allItems, random);
        int type1Count = 2;
        int generatedType1 = 0;
        for (ItemDetail item : allItems) {
            if (generatedType1 >= type1Count) break;
            if (usedCategories.contains(item.categoryName)) continue;
            String correctCategoryName = item.categoryName;
            String imageFileName = CATEGORY_IMAGE_MAP.get(correctCategoryName);
            if (imageFileName == null) continue; 
            String questionText = "다음 분리수거 이미지에 해당하는 올바른 카테고리를 선택하세요.";
            List<String> options = generateOptions(allCategoryNames, correctCategoryName);
            quizList.add(new Quiz(
                Quiz.QuizType.IMAGE_TO_CATEGORY,
                questionText,
                correctCategoryName, 
                options,
                imageFileName, 
                null,
                null 
            ));
            usedCategories.add(correctCategoryName);
            generatedType1++;
        }
        int type2Count = QUIZ_COUNT - type1Count;
        int generatedType2 = 0;
        Collections.shuffle(allItems, random);
        Set<String> usedItemNames = new HashSet<>(); 
        for (ItemDetail item : allItems) {
            if (generatedType2 >= type2Count) break;
            String correctItemName = item.itemName;
            String correctCategoryName = item.categoryName;
            if (usedItemNames.contains(correctItemName) || !CATEGORY_IMAGE_MAP.containsKey(correctCategoryName)) continue;
            if (item.disposalGuide == null || item.disposalGuide.length() < 20) continue;
            String questionText = "다음은 [" + correctCategoryName + "] 카테고리의 공통 배출 지침입니다. 어떤 카테고리인지 이미지에서 골라주세요.";
            String commonGuideText = GuideDAO.getCommonDisposalGuide(correctCategoryName);
            String guideTextForQuiz = commonGuideText.replace("\n", "<br>");
            List<String> options = generateOptions(allCategoryNames, correctCategoryName);
            quizList.add(new Quiz(
                Quiz.QuizType.GUIDE_TO_IMAGE,
                questionText,
                correctCategoryName, 
                options,
                null, 
                guideTextForQuiz, 
                correctItemName 
            ));
            usedItemNames.add(correctItemName);
            generatedType2++;
        }

        Collections.shuffle(quizList, random);
    }
    
    private List<String> generateOptions(List<String> allCategories, String correctAnswer) {
        List<String> wrongOptions = allCategories.stream()
                                                .filter(name -> !name.equals(correctAnswer))
                                                .collect(Collectors.toList());
        Collections.shuffle(wrongOptions, random);
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        options.addAll(wrongOptions.stream().limit(3).collect(Collectors.toList()));
        Collections.shuffle(options, random);
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
            } else if (currentQuiz.type == Quiz.QuizType.GUIDE_TO_IMAGE) {
                String cssStyles = GuideDAO.getCSSStyles();
                String guideStyle = "background-color: #fff8e1; "   
                                  + "padding: 10px; "
                                  + "border: 1px solid #ddd; "      
                                  + "border-radius: 5px; "
                                  + "white-space: nowrap; "       
                                  + "overflow: hidden; "           
                                  + "text-overflow: ellipsis; "      
                                  + "display: block; "               
                                  + "margin-top: 10px;";          
                String guideForDisplay = String.format(
                    "<html><head>%s</head><body>"
                    + "<div style='padding:5px; font-size:1.1em;'>" 
                    + "<b>[공통 배출 지침]</b>"
                    + "<div style='%s'>%s</div>" 
                    + "</div></body></html>",
                    cssStyles, 
                    guideStyle,
                    currentQuiz.guideSnippet 
                );
                guideDisplayArea.setText(guideForDisplay);
                guideScrollPane.setVisible(true);
            }
            for (String option : currentQuiz.options) {
                QuizItem item;
                if (currentQuiz.type == Quiz.QuizType.GUIDE_TO_IMAGE) {
                    item = new QuizItem(option, true); 
                } else {
                    item = new QuizItem(option); 
                }
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
            messageLabel.setForeground(Color.DARK_GRAY);
            questionImageLabel.revalidate();
            guideScrollPane.revalidate();
            quizGrid.revalidate();
            quizGrid.repaint();
        } else {
            finishQuiz();
        }
    }
    
    private void handleAnswer(QuizItem selectedItem, Quiz currentQuiz) {
        String selectedAnswer = selectedItem.getAnswer();
        boolean isCorrect = selectedAnswer.equals(currentQuiz.getCorrectAnswer());
        for (Component comp : quizGrid.getComponents()) {
            if (comp instanceof QuizItem) {
                QuizItem item = (QuizItem) comp;
                if (item.getAnswer().equals(currentQuiz.getCorrectAnswer())) {
                    item.setSelected(true); 
                } else if (item == selectedItem && !isCorrect) {
                    item.setSelected(false); 
                } else {
                    item.resetBorder(); 
                }
            }
        }
        if (isCorrect) {
            correctCount++;
            messageLabel.setText("✅ 정답입니다! 다음 문제로 넘어갑니다.");
            messageLabel.setForeground(new Color(0, 150, 0));
        } else {
            String explanation = "오답입니다. 정답은 <b>'" + currentQuiz.getCorrectAnswer() + "'</b> 카테고리입니다.";
            messageLabel.setText("<html>❌ " + explanation + "</html>");
            messageLabel.setForeground(new Color(200, 0, 0));
        }
        Timer timer = new Timer(1500, e -> {
            currentQuizIndex++;
            loadNextQuiz();
            ((Timer)e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showQuizAlreadyTakenMessage() {
        questionLabel.setText("🚫 오늘 퀴즈는 이미 완료했습니다.");
        quizGrid.removeAll();
        quizGrid.setLayout(new GridBagLayout()); 
        guideScrollPane.setVisible(false);
        questionImageLabel.setIcon(null);
        questionImageLabel.setVisible(false);
        JLabel message = new JLabel("<html><center>분리수거 퀴즈는 하루에 한 번만 참여할 수 있습니다.<br>내일 다시 도전해 주세요!</center></html>", SwingConstants.CENTER);
        message.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        message.setForeground(new Color(20, 100, 150));
        quizGrid.add(message);
        messageLabel.setText("다음 퀴즈는 자정에 초기화됩니다.");
        messageLabel.setForeground(Color.GRAY);
        quizGrid.revalidate();
        quizGrid.repaint();
    }

    private void finishQuiz() {
        questionLabel.setText("퀴즈 완료! 당신의 점수는 " + correctCount + " / " + quizList.size() + "점 입니다.");
        quizGrid.removeAll();
        quizGrid.setLayout(new BorderLayout());
        guideScrollPane.setVisible(false);
        questionImageLabel.setIcon(null);
        questionImageLabel.setVisible(false);
        int reward;
        int totalQuizzes = quizList.size();
        if (correctCount == totalQuizzes) { 
            reward = QUIZ_REWARD_POINTS; 
        } else if (correctCount == totalQuizzes - 1) { 
            reward = 30; 
        } else if (correctCount >= totalQuizzes - 2) {
            reward = 10; 
        } else {
            reward = 0; 
        }
        String rewardGuideMessage = String.format(
            "<html>3개 이상 정답 시 포인트가 지급됩니다: (3개: 10P, 4개: 30P, 5개: %dP)</html>", 
            QUIZ_REWARD_POINTS
        );
        if (reward > 0) {
            try {
                String detail = String.format("분리수거 퀴즈 정답 (%d/%d) 보상", correctCount, quizList.size());
                logDAO.insertQuizReward(currentUser.getUserId(), detail, reward);
                if (rankUpdateCallback != null) {
                    rankUpdateCallback.run(); 
                }
                messageLabel.setText("🎉 축하합니다! 퀴즈 보상 " + reward + " P가 적립되었습니다.");
                messageLabel.setForeground(new Color(0, 100, 200));
            } catch (SQLException e) {
                System.err.println("퀴즈 포인트 적립 DB 오류: " + e.getMessage());
                messageLabel.setText("적립 오류! DB 연결을 확인하세요.");
                messageLabel.setForeground(Color.RED);
                reward = 0; 
            } catch (Exception e) {
                System.err.println("시스템 오류: " + e.getMessage());
                messageLabel.setText("시스템 오류로 적립에 실패했습니다.");
                messageLabel.setForeground(Color.RED);
                reward = 0; 
            }
        } else {
            messageLabel.setText(rewardGuideMessage);
            messageLabel.setForeground(new Color(0, 102, 204)); 
        }

        JLabel finalMessage = new JLabel("<html><center>퀴즈 종료</center></html>", SwingConstants.CENTER);
        finalMessage.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        JLabel resultDetail = new JLabel(
            String.format("<html><center>총 문제: %d 문제<br>정답: <font color='%s'>%d</font> 개<br>획득 포인트: %d P</center></html>",
                quizList.size(), (reward > 0 ? "blue" : "red"), correctCount, reward),
            SwingConstants.CENTER);
        resultDetail.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        JPanel resultPanel = new JPanel(new GridLayout(2, 1));
        resultPanel.add(finalMessage);
        resultPanel.add(resultDetail);
        quizGrid.add(resultPanel, BorderLayout.CENTER);
        quizGrid.revalidate();
        quizGrid.repaint();
    }
    
    private class DisabledCaret extends DefaultCaret {
        @Override
        public void setVisible(boolean v) {
            super.setVisible(false);
        }
        @Override
        public void paint(Graphics g) {
        
        }
        @Override
        protected void adjustVisibility(Rectangle r) {
 
        }
    }
}