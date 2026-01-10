package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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

    private JLabel questionLabel;
    private JPanel quizGrid;
    private JLabel messageLabel;
    private JEditorPane guideDisplayArea;
    private JScrollPane guideScrollPane;
    private JLabel questionImageLabel;
    private JButton nextButton;
    private JProgressBar progressBar;

    private List<Quiz> quizList;
    private List<WrongData> wrongAnswerList = new ArrayList<>();
    private int currentQuizIndex = 0;
    private int correctCount = 0;
    private boolean answerSubmitted = false;
    private final Random random = new Random();
    private boolean quizAlreadyTaken = false;

    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color POINT_RED = new Color(255, 80, 120);
    private static final Color CARD_BG = new Color(35, 30, 70);
    private static final Color TEXT_LIGHT = new Color(240, 240, 240);

    public QuizPanel(UserDTO user, Runnable rankUpdateCallback) {
        this.currentUser = user;
        this.rankUpdateCallback = rankUpdateCallback;
        this.logDAO = new RecycleLogDAO();

        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        loadInitialData();
        initUI();
    }

    private void loadInitialData() {
        if (currentUser == null) return;
        try {
            quizAlreadyTaken = logDAO.hasTakenQuizToday(currentUser.getUserId());
            if (quizAlreadyTaken) {
                this.wrongAnswerList = logDAO.getWrongAnswersToday(currentUser.getUserId());
            }
        } catch (SQLException e) { 
            System.err.println("데이터 로딩 중 오류: " + e.getMessage());
        }
    }

    private void initUI() {
        removeAll();
        if (quizAlreadyTaken) showAlreadyTakenScreen();
        else showStartScreen();
        revalidate(); repaint();
    }

    private void showAlreadyTakenScreen() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10, 0, 10, 0);

        JLabel msg = new JLabel("오늘의 퀴즈 참여 완료!", SwingConstants.CENTER);
        msg.setFont(new Font("맑은 고딕", Font.BOLD, 35));
        msg.setForeground(POINT_CYAN);

        JButton noteBtn = new JButton("오늘의 오답 노트 복습");
        styleButton(noteBtn, 20);
        noteBtn.addActionListener(e -> {
            if (wrongAnswerList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "오늘 틀린 문제가 없습니다. 완벽합니다! ✨");
            } else {
                showWrongNotePopup();
            }
        });

        gbc.gridy = 0; container.add(msg, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(50, 0, 0, 0);
        container.add(noteBtn, gbc);
        add(container, BorderLayout.CENTER);
    }

    private void showStartScreen() {
        JPanel startPanel = new JPanel(new GridBagLayout());
        startPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        JLabel title = new JLabel("오늘의 분리수거 퀴즈");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 42));
        title.setForeground(POINT_CYAN);

        JLabel notice = new JLabel("※ 중간 종료 시 진행 상황이 저장되지 않습니다.");
        notice.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        notice.setForeground(new Color(180, 180, 200));

        JButton startBtn = new JButton("퀴즈 시작하기");
        styleButton(startBtn, 22);
        startBtn.addActionListener(e -> startQuiz());

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);
        startPanel.add(title, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 40, 0);
        startPanel.add(notice, gbc);
        gbc.gridy = 2;
        startPanel.add(startBtn, gbc);
        add(startPanel, BorderLayout.CENTER);
    }

    private void startQuiz() {
        showLoadingScreen();
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                currentQuizIndex = 0;
                correctCount = 0;
                prepareQuizFromGuide();
                Thread.sleep(500); 
                return null;
            }

            @Override
            protected void done() {
                if (quizList == null || quizList.isEmpty()) {
                    JOptionPane.showMessageDialog(QuizPanel.this, "퀴즈 데이터를 불러올 수 없습니다.");
                    initUI();
                    return;
                }
                setupQuizUI();
                showNextQuiz();
            }
        };
        worker.execute();
    }

    private void showLoadingScreen() {
        removeAll();
        JPanel lp = new JPanel(new GridBagLayout());
        lp.setOpaque(false);
        JLabel l = new JLabel("분리수거 지침을 분석 중입니다...");
        l.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        l.setForeground(POINT_CYAN);
        
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        pb.setPreferredSize(new Dimension(300, 5));
        pb.setForeground(POINT_CYAN);
        pb.setBackground(CARD_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; lp.add(l, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(20,0,0,0); lp.add(pb, gbc);
        
        add(lp, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    @SuppressWarnings("unchecked")
    private void prepareQuizFromGuide() {
        quizList = new ArrayList<>();
        wrongAnswerList.clear(); 
        try {
            List<ItemDetail> allItems = GuideDAO.getAllItems();
            Map<String, String> categoryIds = GuideDAO.getAllCategoryNamesAndIds();
            
            java.lang.reflect.Field field = GuideDAO.class.getDeclaredField("CATEGORY_RESOURCES");
            field.setAccessible(true);
            Map<String, String[]> resources = (Map<String, String[]>) field.get(null);

            Collections.shuffle(allItems);
            int count = Math.min(allItems.size(), 5);
            for (int i = 0; i < count; i++) {
                ItemDetail target = allItems.get(i);
                Quiz.QuizType qType = random.nextBoolean() ? Quiz.QuizType.IMAGE_TO_CATEGORY : Quiz.QuizType.GUIDE_TO_IMAGE;
                
                String cid = categoryIds.get(target.categoryName);
                String markImg = (resources.get(cid) != null) ? resources.get(cid)[2] : "";

                String clean = target.disposalGuide.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
                String summary = summarizeGuide(clean);

                List<QuizOption> options = new ArrayList<>();
                options.add(new QuizOption((qType == Quiz.QuizType.IMAGE_TO_CATEGORY) ? target.categoryName : target.itemName, markImg, true));

                if (qType == Quiz.QuizType.IMAGE_TO_CATEGORY) {
                    categoryIds.keySet().stream().filter(c -> !c.equals(target.categoryName)).distinct().limit(3)
                        .forEach(c -> options.add(new QuizOption(c, resources.get(categoryIds.get(c))[2], false)));
                } else {
                    allItems.stream().filter(it -> !it.itemName.equals(target.itemName)).distinct().limit(3)
                        .forEach(it -> options.add(new QuizOption(it.itemName, resources.get(categoryIds.get(it.categoryName))[2], false)));
                }

                Collections.shuffle(options);
                String qTitle = (qType == Quiz.QuizType.IMAGE_TO_CATEGORY) ? "다음 지침이 설명하는 카테고리는?" : "다음 품목이 속하는 카테고리는?";
                quizList.add(new Quiz(qType, qTitle, summary, markImg, options, (qType == Quiz.QuizType.IMAGE_TO_CATEGORY ? 1 : 0)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String summarizeGuide(String fullText) {
        if (fullText.length() <= 80) return fullText;
        String[] sentences = fullText.split("(?<=[.!?])\\s+");
        StringBuilder sb = new StringBuilder();
        for (String s : sentences) {
            if (sb.length() + s.length() < 100) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(s);
            } else {
                if (sb.length() == 0) sb.append(s.substring(0, Math.min(s.length(), 90)));
                break;
            }
        }
        return sb.toString();
    }

    private void setupQuizUI() {
        removeAll();
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        progressBar = new JProgressBar(0, 5);
        progressBar.setForeground(POINT_CYAN);
        progressBar.setBackground(new Color(50, 50, 80));
        progressBar.setPreferredSize(new Dimension(0, 8));
        progressBar.setBorderPainted(false);
        topContainer.add(progressBar, BorderLayout.NORTH);

        JPanel questionPanel = new JPanel(new BorderLayout(0, 15));
        questionPanel.setOpaque(false);
        questionPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        questionLabel = new JLabel(" ", SwingConstants.CENTER);
        questionLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        questionLabel.setForeground(TEXT_LIGHT);
        
        questionImageLabel = new JLabel();
        questionImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        guideDisplayArea = new JEditorPane("text/html", "");
        guideDisplayArea.setEditable(false);
        guideDisplayArea.setBackground(CARD_BG);
        guideScrollPane = new JScrollPane(guideDisplayArea);
        guideScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        guideScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        guideScrollPane.setBorder(new LineBorder(POINT_PURPLE, 2));
        guideScrollPane.setPreferredSize(new Dimension(0, 110));

        questionPanel.add(questionLabel, BorderLayout.NORTH);
        questionPanel.add(questionImageLabel, BorderLayout.CENTER);
        questionPanel.add(guideScrollPane, BorderLayout.SOUTH);
        topContainer.add(questionPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        quizGrid = new JPanel(new GridLayout(2, 2, 20, 20)); 
        quizGrid.setOpaque(false);
        centerPanel.add(quizGrid);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(0, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        
        nextButton = new JButton("다음 문제 >");
        styleButton(nextButton, 16);
        nextButton.setVisible(false);
        nextButton.addActionListener(e -> {
            currentQuizIndex++;
            showNextQuiz();
        });

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        bottomPanel.add(messageLabel, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 20);
        bottomPanel.add(nextButton, gbc);
        gbc.gridx = 0; gbc.weightx = 0;
        bottomPanel.add(Box.createHorizontalStrut(100), gbc);

        add(bottomPanel, BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    private void showNextQuiz() {
        if (currentQuizIndex < quizList.size()) {
            answerSubmitted = false;
            nextButton.setVisible(false);
            messageLabel.setText(" ");
            progressBar.setValue(currentQuizIndex + 1);
            quizGrid.removeAll();

            Quiz q = quizList.get(currentQuizIndex);
            questionLabel.setText("Q" + (currentQuizIndex + 1) + ". " + q.title);
            
            if (q.type == Quiz.QuizType.GUIDE_TO_IMAGE) {
                questionImageLabel.setIcon(loadImage(q.mainImg, 140));
                questionImageLabel.setVisible(true);
                guideScrollPane.setVisible(false);
            } else {
                guideScrollPane.setVisible(true);
                questionImageLabel.setVisible(false);
            }

            guideDisplayArea.setText("<html><body style='background-color:#231E46; color:#00FFF0; font-family:맑은 고딕; padding:10px; margin:0;'>" +
                                     "<div style='text-align:center; font-size:16px; line-height:1.4;'>"+ q.guideSnippet + "</div></body></html>");

            for (QuizOption opt : q.options) {
                QuizOptionCard card = new QuizOptionCard(opt, q.optionUIStyle);
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) { if (!answerSubmitted) handleAnswer(card, opt, q); }
                    @Override
                    public void mouseEntered(MouseEvent e) { if (!answerSubmitted) card.setHover(true); }
                    @Override
                    public void mouseExited(MouseEvent e) { if (!answerSubmitted) card.setHover(false); }
                });
                quizGrid.add(card);
            }
            quizGrid.revalidate(); 
            quizGrid.repaint();
        } else {
            finishQuiz();
        }
    }

    private void handleAnswer(QuizOptionCard card, QuizOption opt, Quiz currentQuiz) {
        answerSubmitted = true;
        if (opt.isCorrect) {
            correctCount++;
            messageLabel.setForeground(POINT_CYAN);
            messageLabel.setText("✔ 정답입니다!");
            card.setResult(true);
        } else {
            messageLabel.setForeground(POINT_RED);
            messageLabel.setText("✘ 오답입니다.");
            card.setResult(false);
          
            for (Component c : quizGrid.getComponents()) {
                if (c instanceof QuizOptionCard && ((QuizOptionCard) c).option.isCorrect) ((QuizOptionCard) c).setResult(true);
            }
            String correctAnswer = currentQuiz.options.stream().filter(o -> o.isCorrect).findFirst().get().text;
            wrongAnswerList.add(new WrongData("Q" + (currentQuizIndex+1) + " " + currentQuiz.title, opt.text, correctAnswer));
        }
        nextButton.setVisible(true);
    }

    private void finishQuiz() {
        removeAll();
        showLoadingScreen(); 

        SwingWorker<Integer, Void> saver = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int score = correctCount * 20;
                if (currentUser != null) {
                    logDAO.insertQuizReward(currentUser.getUserId(), "퀴즈 보상", score);
                    for (WrongData data : wrongAnswerList) {
                        logDAO.insertWrongAnswer(currentUser.getUserId(), data.question, data.selected, data.correct);
                    }
                }
                return score;
            }

            @Override
            protected void done() {
                try {
                    int score = get();
                    displayResult(score);
                    if (rankUpdateCallback != null) rankUpdateCallback.run();
                    quizAlreadyTaken = true;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(QuizPanel.this, "결과 저장 중 오류 발생");
                    initUI();
                }
            }
        };
        saver.execute();
    }

    private void displayResult(int score) {
        removeAll();
        JPanel resPanel = new JPanel();
        resPanel.setLayout(new BoxLayout(resPanel, BoxLayout.Y_AXIS));
        resPanel.setOpaque(false);

        JLabel scoreLbl = new JLabel("최종 점수: " + score + " P", SwingConstants.CENTER);
        scoreLbl.setFont(new Font("맑은 고딕", Font.BOLD, 48));
        scoreLbl.setForeground(POINT_CYAN);
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        resPanel.add(Box.createVerticalStrut(80));
        resPanel.add(scoreLbl);
        resPanel.add(Box.createVerticalStrut(40));

        if (!wrongAnswerList.isEmpty()) {
            JButton noteBtn = new JButton("오답 노트 확인하기");
            styleButton(noteBtn, 18);
            noteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            noteBtn.addActionListener(e -> showWrongNotePopup());
            resPanel.add(noteBtn);
        }

        add(resPanel, BorderLayout.CENTER);
        JButton homeBtn = new JButton("완료");
        styleButton(homeBtn, 18);
        homeBtn.addActionListener(e -> initUI());
        add(homeBtn, BorderLayout.SOUTH);
        
        revalidate(); repaint();
    }

    private void showWrongNotePopup() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "오늘의 오답 노트", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_DARK);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG_DARK);
        list.setBorder(new EmptyBorder(20, 20, 20, 20));

        for (WrongData d : wrongAnswerList) {
            JPanel item = new JPanel(new GridLayout(3, 1, 5, 5));
            item.setBackground(CARD_BG);
            item.setBorder(BorderFactory.createCompoundBorder(new LineBorder(POINT_PURPLE), new EmptyBorder(10, 10, 10, 10)));
            JLabel q = new JLabel("문제: " + d.question); q.setForeground(POINT_CYAN);
            JLabel s = new JLabel("선택: " + d.selected); s.setForeground(POINT_RED);
            JLabel c = new JLabel("정답: " + d.correct); c.setForeground(Color.GREEN);
            item.add(q); item.add(s); item.add(c);
            list.add(item);
            list.add(Box.createVerticalStrut(15));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);
        
        JButton close = new JButton("닫기");
        styleButton(close, 14);
        close.addActionListener(e -> dialog.dispose());
        dialog.add(close, BorderLayout.SOUTH);

        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private ImageIcon loadImage(String path, int size) {
        try {
            String fullPath = System.getProperty("user.dir") + "/src/main/webapp/" + path;
            ImageIcon icon = new ImageIcon(fullPath);
            if (icon.getImageLoadStatus() != MediaTracker.COMPLETE || icon.getIconWidth() <= 0) {
                return createDefaultIcon(size);
            }
            Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) { 
            return createDefaultIcon(size); 
        }
    }

    private ImageIcon createDefaultIcon(int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CARD_BG); g2.fillRoundRect(0, 0, size, size, 20, 20);
        g2.setColor(POINT_PURPLE); g2.drawRoundRect(2, 2, size-4, size-4, 20, 20);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        g2.drawString("No Image", size/4, size/2);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void styleButton(JButton btn, int fontSize) {
        btn.setBackground(POINT_PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, fontSize));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 35, 12, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(POINT_PURPLE.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(POINT_PURPLE); }
        });
    }

    private static class Quiz {
        enum QuizType { IMAGE_TO_CATEGORY, GUIDE_TO_IMAGE }
        QuizType type; String title, guideSnippet, mainImg; List<QuizOption> options; int optionUIStyle;
        Quiz(QuizType t, String title, String g, String i, List<QuizOption> o, int s) {
            this.type = t; this.title = title; this.guideSnippet = g; this.mainImg = i; this.options = o; this.optionUIStyle = s;
        }
    }

    private static class QuizOption {
        String text, imgPath; boolean isCorrect;
        QuizOption(String t, String p, boolean c) { this.text = t; this.imgPath = p; this.isCorrect = c; }
    }

    public static class WrongData {
        public String question, selected, correct;
        public WrongData(String q, String s, String c) { this.question = q; this.selected = s; this.correct = c; }
    }

    private class QuizOptionCard extends JPanel {
        QuizOption option;
        QuizOptionCard(QuizOption opt, int style) {
            this.option = opt;
            setLayout(new BorderLayout());
            setBackground(CARD_BG);
            setBorder(new LineBorder(POINT_PURPLE, 2));
            setPreferredSize(new Dimension(300, 130));
            if (style == 1) {
                JLabel imgLabel = new JLabel(loadImage(opt.imgPath, 90));
                imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                add(imgLabel, BorderLayout.CENTER);
            } else {
                JLabel txtLabel = new JLabel("<html><center>" + opt.text + "</center></html>", SwingConstants.CENTER);
                txtLabel.setForeground(TEXT_LIGHT);
                txtLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
                add(txtLabel, BorderLayout.CENTER);
            }
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        void setHover(boolean hover) {
            setBackground(hover ? new Color(50, 45, 90) : CARD_BG);
            setBorder(new LineBorder(hover ? POINT_CYAN : POINT_PURPLE, 2));
        }
        void setResult(boolean correct) {
            setBackground(correct ? new Color(0, 120, 80) : new Color(150, 0, 0));
            setBorder(new LineBorder(correct ? Color.GREEN : Color.RED, 3));
        }
    }
}