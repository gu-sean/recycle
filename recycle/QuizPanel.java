package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import db.DTO.UserDTO;
import db.DAO.RecycleLogDAO;
import db.DAO.GuideDAO;
import db.DAO.GuideDAO.ItemDetail;


public class QuizPanel extends JPanel {

    private final UserDTO currentUser;
    private final RecycleLogDAO logDAO;
    private final Runnable rankUpdateCallback;

    private JLabel questionLabel, messageLabel, imageQuestionLabel, itemNameLabel;
    private JTextArea guideTextArea;
    private JScrollPane guideScrollPane;
    private JPanel quizGrid, displayPanel;
    private JButton nextButton;
    private JProgressBar progressBar;

    private List<Quiz> quizList;
    private int currentQuizIndex = 0;
    private int correctCount = 0;
    private boolean answerSubmitted = false;
    private boolean quizAlreadyTaken = false;

    private static final Color BG_DARK = new Color(13, 13, 25);
    private static final Color BG_CARD = new Color(30, 30, 55);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color POINT_PINK = new Color(255, 100, 200);
    private static final Color POINT_RED = new Color(255, 60, 100);
    private static final Font FONT_BOLD_LARGE = new Font("맑은 고딕", Font.BOLD, 32);
    private static final Font FONT_BOLD_MEDIUM = new Font("맑은 고딕", Font.BOLD, 22);

    public QuizPanel(UserDTO user, Runnable rankUpdateCallback) {
        this.currentUser = user;
        this.rankUpdateCallback = rankUpdateCallback;
        this.logDAO = new RecycleLogDAO();

        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        
        checkQuizStatusFromDB();
        initUI();
    }

    private void checkQuizStatusFromDB() {
        if (currentUser == null) return;
        try {
            this.quizAlreadyTaken = logDAO.hasTakenQuizToday(currentUser.getUserId());
        } catch (SQLException e) {
            this.quizAlreadyTaken = false;
        }
    }

    public void initUI() {
        removeAll();
        if (quizAlreadyTaken) {
            showRestrictedScreen();
        } else {
            showStartScreen();
        }
        revalidate();
        repaint();
    }

    private void showRestrictedScreen() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        
        JLabel t = new JLabel("오늘의 미션 완료! ✨");
        t.setFont(FONT_BOLD_LARGE);
        t.setForeground(POINT_CYAN);
        
        JLabel s = new JLabel("퀴즈는 하루에 한 번만 참여할 수 있습니다. 내일 다시 도전하세요!");
        s.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        s.setForeground(Color.LIGHT_GRAY);
        
        JButton b = new JButton("오늘의 오답 노트 확인");
        styleButton(b, 20, POINT_PINK);
        b.addActionListener(e -> showWrongNotePopup());
        
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; p.add(t, g);
        g.gridy = 1; g.insets = new Insets(15, 0, 40, 0); p.add(s, g);
        g.gridy = 2; p.add(b, g);
        
        add(p, BorderLayout.CENTER);
    }

    private void showStartScreen() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        
        JLabel t = new JLabel("분리수거 데일리 퀴즈");
        t.setFont(new Font("맑은 고딕", Font.BOLD, 50));
        t.setForeground(POINT_CYAN);
        
        JButton b = new JButton("미션 시작하기");
        styleButton(b, 28, POINT_CYAN);
        b.addActionListener(e -> {
            checkQuizStatusFromDB();
            if(quizAlreadyTaken) {
                initUI();
            } else {
                startQuiz();
            }
        });
        
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.insets = new Insets(0, 0, 40, 0); p.add(t, g);
        g.gridy = 1; p.add(b, g);
        
        add(p, BorderLayout.CENTER);
    }

    private void startQuiz() {
        currentQuizIndex = 0;
        correctCount = 0;
        try {
            prepareMixedQuiz();
            setupQuizUI();
            showNextQuiz();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "퀴즈 데이터를 불러오는 중 오류가 발생했습니다.");
        }
    }

    private void prepareMixedQuiz() throws Exception {
        quizList = new ArrayList<>();
        List<ItemDetail> allItems = GuideDAO.getAllItems();
        if (allItems == null || allItems.isEmpty()) return;
        Collections.shuffle(allItems);
        
        for (int i = 0; i < 5; i++) {
            ItemDetail target = allItems.get(i % allItems.size());
            Quiz.QuizType type = (i % 2 == 0) ? Quiz.QuizType.IMAGE_TO_CATEGORY : Quiz.QuizType.GUIDE_TO_IMAGE;
            
            String clean = target.disposalGuide.replaceAll("<[^>]*>", " ").replaceAll("&nbsp;", " ").replaceAll("\\s+", " ").trim();
            String summary = clean;
            int firstDot = clean.indexOf(".");
            if (firstDot != -1) {
                int secondDot = clean.indexOf(".", firstDot + 1);
                summary = (secondDot != -1) ? clean.substring(0, secondDot + 1) : clean.substring(0, firstDot + 1);
            }
            
            List<QuizOption> options = new ArrayList<>();
            options.add(new QuizOption(target.categoryName, target.itemName, target.itemImagePath, true));
            
            List<ItemDetail> others = new ArrayList<>(allItems);
            others.remove(target);
            Collections.shuffle(others);
            for (int j = 0; j < 3; j++) {
                ItemDetail o = others.get(j % others.size());
                options.add(new QuizOption(o.categoryName, o.itemName, o.itemImagePath, false));
            }
            Collections.shuffle(options);
            quizList.add(new Quiz(type, summary, target.itemImagePath, target.itemName, options));
        }
    }

    private void setupQuizUI() {
        removeAll();
        progressBar = new JProgressBar(0, 5);
        progressBar.setPreferredSize(new Dimension(getWidth(), 12));
        progressBar.setForeground(POINT_CYAN);
        progressBar.setBackground(BG_CARD);
        progressBar.setUI(new BasicProgressBarUI());
        add(progressBar, BorderLayout.NORTH);
        
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.gridx = 0;
        
        questionLabel = new JLabel(" ", SwingConstants.CENTER);
        questionLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        questionLabel.setForeground(Color.WHITE);
        gbc.gridy = 0; gbc.insets = new Insets(10, 0, 10, 0);
        mainContent.add(questionLabel, gbc);
        
        displayPanel = new JPanel(new CardLayout());
        displayPanel.setOpaque(false);
        displayPanel.setPreferredSize(new Dimension(700, 200));
        
        guideTextArea = new JTextArea();
        guideTextArea.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        guideTextArea.setForeground(Color.WHITE);
        guideTextArea.setBackground(new Color(40, 40, 70));
        guideTextArea.setLineWrap(true);
        guideTextArea.setWrapStyleWord(true);
        guideTextArea.setEditable(false);
        guideTextArea.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        guideScrollPane = new JScrollPane(guideTextArea);
        guideScrollPane.setBorder(new LineBorder(POINT_PINK, 2, true));
        displayPanel.add(guideScrollPane, "TEXT");
        
        JPanel imgContainer = new JPanel(new GridBagLayout());
        imgContainer.setOpaque(false);
        imageQuestionLabel = new JLabel();
        itemNameLabel = new JLabel();
        itemNameLabel.setFont(FONT_BOLD_MEDIUM);
        itemNameLabel.setForeground(POINT_PINK);
        GridBagConstraints imgGbc = new GridBagConstraints();
        imgGbc.gridy=0; imgContainer.add(imageQuestionLabel, imgGbc);
        imgGbc.gridy=1; imgGbc.insets=new Insets(15,0,0,0); imgContainer.add(itemNameLabel, imgGbc);
        displayPanel.add(imgContainer, "IMAGE");

        gbc.gridy = 1; gbc.weighty = 0.3;
        mainContent.add(displayPanel, gbc);
        
        quizGrid = new JPanel(new GridLayout(2, 2, 25, 25));
        quizGrid.setOpaque(false);
        gbc.gridy = 2; gbc.weighty = 0.6; gbc.insets = new Insets(25, 0, 25, 0);
        mainContent.add(quizGrid, gbc);
        
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(FONT_BOLD_MEDIUM);
        gbc.gridy = 3; gbc.weighty = 0.1;
        mainContent.add(messageLabel, gbc);
        
        add(mainContent, BorderLayout.CENTER);
        
        nextButton = new JButton("다음 문제로 ➔");
        styleButton(nextButton, 18, POINT_PINK);
        nextButton.setVisible(false);
        nextButton.addActionListener(e -> { currentQuizIndex++; showNextQuiz(); });
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setOpaque(false); southPanel.setBorder(new EmptyBorder(0,0,20,30));
        southPanel.add(nextButton);
        add(southPanel, BorderLayout.SOUTH);
        
        revalidate(); repaint();
    }

    private void showNextQuiz() {
        if (currentQuizIndex < 5) {
            answerSubmitted = false; nextButton.setVisible(false); messageLabel.setText(" ");
            progressBar.setValue(currentQuizIndex + 1);
            quizGrid.removeAll();
            Quiz q = quizList.get(currentQuizIndex);
            questionLabel.setText("Q " + (currentQuizIndex + 1) + ". 아래 정보에 해당하는 정답을 고르세요.");
            
            CardLayout cl = (CardLayout) displayPanel.getLayout();
            if (q.type == Quiz.QuizType.GUIDE_TO_IMAGE) {
                guideTextArea.setText(q.guideContent);
                guideTextArea.setCaretPosition(0);
                cl.show(displayPanel, "TEXT");
            } else {
                imageQuestionLabel.setIcon(loadImage(q.itemImgPath, 130));
                itemNameLabel.setText(q.itemName);
                cl.show(displayPanel, "IMAGE");
            }
            
            for (QuizOption opt : q.options) {
                QuizOptionCard card = new QuizOptionCard(opt, q.type);
                card.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) { if(!answerSubmitted) handleAnswer(card, opt, q); }
                });
                quizGrid.add(card);
            }
            revalidate(); repaint();
        } else {
            finishQuiz();
        }
    }

    private void handleAnswer(QuizOptionCard card, QuizOption opt, Quiz q) {
        answerSubmitted = true;
        QuizOption correctOption = q.options.stream().filter(o -> o.isCorrect).findFirst().orElse(null);
        String correctText = (q.type == Quiz.QuizType.IMAGE_TO_CATEGORY) ? correctOption.category : correctOption.itemName;
        
        if (opt.isCorrect) {
            correctCount++;
            messageLabel.setForeground(POINT_CYAN); messageLabel.setText("정답입니다! 참 잘했어요. 👍");
            card.setResult(true);
        } else {
            messageLabel.setForeground(POINT_RED); messageLabel.setText("아쉬워요! 정답은 '" + correctText + "'입니다.");
            card.setResult(false);
            try {
                logDAO.insertWrongAnswer(currentUser.getUserId(), 
                                        (q.type == Quiz.QuizType.GUIDE_TO_IMAGE ? q.guideContent : q.itemName), 
                                        (q.type == Quiz.QuizType.GUIDE_TO_IMAGE ? opt.itemName : opt.category), 
                                        correctText);
            } catch (SQLException e) { e.printStackTrace(); }
        }
        nextButton.setVisible(true);
    }

    private void finishQuiz() {
        try {
            if (currentUser != null) {
                int earnedPoints = 0;
                String rank = "";
                switch (correctCount) {
                    case 5: earnedPoints = 50; rank = "완벽해요! (SSS)"; break;
                    case 4: earnedPoints = 30; rank = "훌륭해요! (A)"; break;
                    case 3: earnedPoints = 20; rank = "좋아요! (B)"; break;
                    default: earnedPoints = 0; rank = "좀 더 노력해봐요! (C)"; break;
                }

                logDAO.insertQuizReward(currentUser.getUserId(), earnedPoints);
                if (rankUpdateCallback != null) rankUpdateCallback.run();
                
                String resultMsg = String.format("퀴즈 결과: %s\n\n맞힌 개수: %d / 5\n획득 포인트: %d P", rank, correctCount, earnedPoints);
                JOptionPane.showMessageDialog(this, resultMsg, "미션 완료", JOptionPane.INFORMATION_MESSAGE);

                this.quizAlreadyTaken = true;
                initUI();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            initUI();
        }
    }

    private void showWrongNotePopup() {
        try {
            List<WrongData> list = logDAO.getWrongAnswersToday(currentUser.getUserId());
            if (list == null || list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "오늘 틀린 문제가 없습니다. 완벽해요! ✨");
                return;
            }
            JDialog dialog = new JDialog((Frame)null, "오늘의 오답 노트", true);
            dialog.getContentPane().setBackground(BG_DARK);
            
            JPanel content = new JPanel(); 
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(BG_DARK); 
            content.setBorder(new EmptyBorder(20,20,20,20));
            
            for (WrongData d : list) {
                JPanel card = new JPanel(new GridLayout(0, 1, 5, 5));
                card.setBackground(BG_CARD);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(POINT_PINK, 1, true), 
                    new EmptyBorder(15,15,15,15))
                );
                
                String qText = d.question.length() > 60 ? d.question.substring(0, 57) + "..." : d.question;
                JLabel q = new JLabel("질문: " + qText); q.setForeground(Color.WHITE);
                JLabel s = new JLabel("내가 고른 답: " + d.selected); s.setForeground(POINT_RED);
                JLabel c = new JLabel("올바른 정답: " + d.correct); c.setForeground(POINT_CYAN);
                c.setFont(new Font("맑은 고딕", Font.BOLD, 14));
                
                card.add(q); card.add(s); card.add(c);
                content.add(card); content.add(Box.createVerticalStrut(15));
            }
            
            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getViewport().setBackground(BG_DARK);
            
            dialog.add(scroll);
            dialog.setSize(550, 600);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ImageIcon loadImage(String path, int size) {
        try {
            String fullPath = System.getProperty("user.dir") + "/src/main/webapp/" + path;
            ImageIcon icon = new ImageIcon(fullPath);
            if (icon.getIconWidth() <= 0) return null;
            return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    private void styleButton(JButton btn, int size, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, size));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.brighter(), 1),
            new EmptyBorder(10, 25, 10, 25)
        ));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.brighter()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });
    }

    public static class Quiz {
        enum QuizType { IMAGE_TO_CATEGORY, GUIDE_TO_IMAGE }
        QuizType type; String guideContent, itemImgPath, itemName; List<QuizOption> options;
        Quiz(QuizType t, String g, String i, String n, List<QuizOption> o) {
            this.type=t; this.guideContent=g; this.itemImgPath=i; this.itemName=n; this.options=o;
        }
    }

    public static class QuizOption {
        String category, itemName, itemImgPath; boolean isCorrect;
        QuizOption(String c, String n, String i, boolean cor) {
            this.category=c; this.itemName=n; this.itemImgPath=i; this.isCorrect=cor;
        }
    }

    private class QuizOptionCard extends JPanel {
        QuizOptionCard(QuizOption opt, Quiz.QuizType type) {
            setLayout(new BorderLayout());
            setBackground(BG_CARD);
            setBorder(new LineBorder(new Color(100, 100, 150), 2, true));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (type == Quiz.QuizType.IMAGE_TO_CATEGORY) {
                JLabel l = new JLabel(opt.category, SwingConstants.CENTER);
                l.setFont(new Font("맑은 고딕", Font.BOLD, 22));
                l.setForeground(Color.WHITE);
                add(l, BorderLayout.CENTER);
            } else {
                ImageIcon icon = loadImage(opt.itemImgPath, 100);
                JLabel img = (icon != null) ? new JLabel(icon) : new JLabel("이미지 없음", SwingConstants.CENTER);
                if(icon == null) img.setForeground(Color.GRAY);
                
                JLabel name = new JLabel(opt.itemName, SwingConstants.CENTER);
                name.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                name.setForeground(Color.WHITE);
                name.setBorder(new EmptyBorder(5,0,10,0));
                
                add(img, BorderLayout.CENTER);
                add(name, BorderLayout.SOUTH);
            }

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if(!answerSubmitted) setBorder(new LineBorder(POINT_CYAN, 2, true)); }
                @Override public void mouseExited(MouseEvent e) { if(!answerSubmitted) setBorder(new LineBorder(new Color(100, 100, 150), 2, true)); }
            });
        }

        void setResult(boolean isCorrect) {
            setBackground(isCorrect ? new Color(0, 80, 80) : new Color(80, 0, 0));
            setBorder(new LineBorder(isCorrect ? POINT_CYAN : POINT_RED, 4, true));
        }
    }

    public static class WrongData {
        public String question, selected, correct;
        public WrongData(String q, String s, String c) {
            this.question = q; this.selected = s; this.correct = c;
        }
    }
}