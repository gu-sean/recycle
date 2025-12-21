package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import db.DTO.UserDTO;
import db.DAO.RecycleLogDAO;

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

    private List<Quiz> quizList;
    private List<WrongAnswer> wrongQuizzes = new ArrayList<>();
    private int currentQuizIndex = 0;
    private int correctCount = 0;
    private boolean answerSubmitted = false;
    private final Random random = new Random();
    private boolean quizAlreadyTaken = false;

    // --- 네온 다크 퍼플 테마 색상 ---
    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color CARD_BG = new Color(35, 30, 70);

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
        WrongAnswer(Quiz quiz, String selectedAnswer) { this.quiz = quiz; this.selectedAnswer = selectedAnswer; }
    }

    public QuizPanel(UserDTO user, Runnable rankUpdateCallback) throws Exception {
        this.currentUser = user;
        this.rankUpdateCallback = rankUpdateCallback;
        this.logDAO = new RecycleLogDAO();

        try {
            this.quizAlreadyTaken = logDAO.hasTakenQuizToday(currentUser.getUserId());
            initializeQuizData();
        } catch (SQLException e) { throw new Exception("데이터 로드 실패", e); }

        setupLayout();
        
        if (this.quizAlreadyTaken) showQuizAlreadyTakenMessage();
        else loadNextQuiz();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setOpaque(false);

        questionLabel = new JLabel("분리수거 퀴즈", SwingConstants.LEFT);
        questionLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        questionLabel.setForeground(POINT_CYAN);
        northPanel.add(questionLabel);
        northPanel.add(Box.createVerticalStrut(20));

        questionImageLabel = new JLabel("", SwingConstants.CENTER);
        questionImageLabel.setPreferredSize(new Dimension(180, 180));
        JPanel imageWrapper = new JPanel(new GridBagLayout());
        imageWrapper.setOpaque(false);
        imageWrapper.add(questionImageLabel);
        northPanel.add(imageWrapper);

        guideDisplayArea = new JEditorPane("text/html", "");
        guideDisplayArea.setEditable(false);
        guideDisplayArea.setBackground(CARD_BG);
        guideScrollPane = new JScrollPane(guideDisplayArea);
        guideScrollPane.setPreferredSize(new Dimension(600, 90));
        guideScrollPane.setBorder(new LineBorder(POINT_PURPLE, 1));
        northPanel.add(guideScrollPane);

        add(northPanel, BorderLayout.NORTH);

        quizGrid = new JPanel(new GridLayout(2, 2, 25, 25));
        quizGrid.setOpaque(false);
        add(quizGrid, BorderLayout.CENTER);

        messageLabel = new JLabel("준비되셨나요?", SwingConstants.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        messageLabel.setForeground(Color.WHITE);
        add(messageLabel, BorderLayout.SOUTH);
    }

    private class QuizOptionItem extends JPanel {
        private String answer;
        public QuizOptionItem(String text, boolean isImage) {
            this.answer = text;
            setLayout(new BorderLayout(5, 5));
            setBackground(CARD_BG);
            setBorder(new LineBorder(new Color(80, 80, 130), 2));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (isImage) {
                JLabel img = new JLabel(loadImage(text, 100), SwingConstants.CENTER);
                add(img, BorderLayout.CENTER);
            }
            
            JLabel lbl = new JLabel("<html><center>" + text + "</center></html>", SwingConstants.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            lbl.setForeground(Color.WHITE);
            add(lbl, isImage ? BorderLayout.SOUTH : BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if(!answerSubmitted) setBorder(new LineBorder(POINT_CYAN, 2)); }
                public void mouseExited(MouseEvent e) { if(!answerSubmitted) setBorder(new LineBorder(new Color(80, 80, 130), 2)); }
            });
        }
        public String getAnswer() { return answer; }
        public void setSelected(boolean correct) {
            setBorder(new LineBorder(correct ? Color.GREEN : Color.RED, 3));
            setBackground(correct ? new Color(0, 50, 0) : new Color(50, 0, 0));
        }
    }

    // --- 핵심 수정 영역: Timer 관련 오류 해결 ---
    private void handleAnswer(QuizOptionItem selectedItem, Quiz current) {
        answerSubmitted = true;
        boolean isCorrect = selectedItem.getAnswer().equals(current.correctAnswer);
        selectedItem.setSelected(isCorrect);

        if (isCorrect) {
            correctCount++;
            messageLabel.setText("✅ 정답입니다!");
            messageLabel.setForeground(Color.GREEN);
        } else {
            wrongQuizzes.add(new WrongAnswer(current, selectedItem.getAnswer()));
            messageLabel.setText("❌ 오답! 정답은 [" + current.correctAnswer + "]");
            messageLabel.setForeground(Color.RED);
        }

        // javax.swing.Timer를 명시적으로 호출하여 모호성 제거
        javax.swing.Timer transitionTimer = new javax.swing.Timer(1500, e -> {
            currentQuizIndex++;
            loadNextQuiz();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        transitionTimer.setRepeats(false); // 한 번만 실행되도록 보장
        transitionTimer.start();
    }

    private void finishQuiz() {
        questionLabel.setText("퀴즈 결과");
        quizGrid.removeAll();
        quizGrid.setLayout(new GridBagLayout());
        
        int reward = (correctCount == 5) ? 50 : (correctCount >= 3) ? 20 : 0;
        try {
            if (reward > 0) {
                logDAO.insertQuizReward(currentUser.getUserId(), "퀴즈 보상", reward);
                if (rankUpdateCallback != null) rankUpdateCallback.run();
            }
        } catch (Exception e) {}

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setOpaque(false);
        
        JLabel score = new JLabel(correctCount + " / 5 문제 정답", SwingConstants.CENTER);
        score.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        score.setForeground(POINT_CYAN);
        
        JLabel point = new JLabel("획득 포인트: " + reward + " P", SwingConstants.CENTER);
        point.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        point.setForeground(Color.WHITE);
        
        JButton btn = createStyledButton("오답 노트 보기", POINT_PURPLE);
        btn.addActionListener(e -> showWrongAnswerNote());

        panel.add(score);
        panel.add(point);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);
        
        quizGrid.add(panel);
        quizGrid.revalidate(); quizGrid.repaint();
    }

    private void showWrongAnswerNote() {
        questionLabel.setText("📖 오답 노트");
        quizGrid.removeAll();
        quizGrid.setLayout(new BorderLayout());

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG_DARK);

        for (WrongAnswer wa : wrongQuizzes) {
            JPanel item = new JPanel(new BorderLayout(15, 10));
            item.setBackground(CARD_BG);
            item.setMaximumSize(new Dimension(800, 80));
            item.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 100), 1), new EmptyBorder(10, 15, 10, 15)));
            
            String text = String.format("<html><font color='#00fff0'>Q: %s</font><br>" +
                    "<font color='#ff5555'>나의 답: %s</font> | <font color='#55ff55'>정답: %s</font></html>",
                    wa.quiz.question, wa.selectedAnswer, wa.quiz.correctAnswer);
            
            JLabel lbl = new JLabel(text);
            lbl.setForeground(Color.WHITE);
            item.add(lbl, BorderLayout.CENTER);
            item.add(new JLabel(loadImage(wa.quiz.correctAnswer, 40)), BorderLayout.WEST);

            list.add(item);
            list.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        quizGrid.add(scroll, BorderLayout.CENTER);

        JButton back = createStyledButton("메인으로", new Color(80, 80, 80));
        back.setPreferredSize(new Dimension(0, 45));
        back.addActionListener(e -> showQuizAlreadyTakenMessage());
        quizGrid.add(back, BorderLayout.SOUTH);

        quizGrid.revalidate(); quizGrid.repaint();
    }

    private void showQuizAlreadyTakenMessage() {
        questionLabel.setText("🚫 오늘 참여 완료");
        quizGrid.removeAll();
        quizGrid.setLayout(new GridBagLayout());
        
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);
        JLabel m = new JLabel("퀴즈는 하루에 한 번만 가능합니다.", SwingConstants.CENTER);
        m.setForeground(Color.GRAY);
        m.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        
        JButton btn = createStyledButton("오답 노트 복습하기", POINT_PURPLE);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.addActionListener(e -> showWrongAnswerNote());
        
        p.add(m, BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        quizGrid.add(p);
        
        guideScrollPane.setVisible(false);
        questionImageLabel.setVisible(false);
        quizGrid.revalidate(); quizGrid.repaint();
    }

    private JButton createStyledButton(String t, Color bg) {
        JButton b = new JButton(t);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private ImageIcon loadImage(String cat, int size) {
        String f = CATEGORY_IMAGE_MAP.get(cat);
        if (f == null) return null;
        URL url = getClass().getResource("/images/" + f);
        if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        return null;
    }

    private void initializeQuizData() {
        List<String> cats = new ArrayList<>(CATEGORY_IMAGE_MAP.keySet());
        quizList = new ArrayList<>();
        Collections.shuffle(cats);
        for(int i=0; i<5; i++) {
            String cor = cats.get(i % cats.size());
            Quiz.QuizType type = (i < 2) ? Quiz.QuizType.IMAGE_TO_CATEGORY : Quiz.QuizType.GUIDE_TO_IMAGE;
            quizList.add(new Quiz(type, 
                type == Quiz.QuizType.IMAGE_TO_CATEGORY ? "이 아이콘의 분리수거 카테고리는?" : "지침에 맞는 카테고리 이미지를 고르세요.",
                cor, generateOptions(cats, cor), "이물질을 제거하고 깨끗이 씻어서 배출해야 합니다."));
        }
        Collections.shuffle(quizList);
    }

    private List<String> generateOptions(List<String> all, String cor) {
        List<String> opt = new ArrayList<>(); opt.add(cor);
        List<String> wrg = all.stream().filter(c -> !c.equals(cor)).collect(Collectors.toList());
        Collections.shuffle(wrg); opt.addAll(wrg.stream().limit(3).collect(Collectors.toList()));
        Collections.shuffle(opt); return opt;
    }

    private void loadNextQuiz() {
        quizGrid.removeAll(); answerSubmitted = false;
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setText("문제를 보고 정답을 클릭하세요!");

        if (currentQuizIndex < quizList.size()) {
            Quiz q = quizList.get(currentQuizIndex);
            questionLabel.setText((currentQuizIndex + 1) + ". " + q.question);
            
            if (q.type == Quiz.QuizType.IMAGE_TO_CATEGORY) {
                questionImageLabel.setIcon(loadImage(q.correctAnswer, 150));
                questionImageLabel.setVisible(true); guideScrollPane.setVisible(false);
            } else {
                guideDisplayArea.setText("<html><body style='color:white; font-family:맑은 고딕;'><b>[분리수거 지침]</b><br>" + q.guideSnippet + "</body></html>");
                guideScrollPane.setVisible(true); questionImageLabel.setVisible(false);
            }
            
            for (String o : q.options) {
                QuizOptionItem item = new QuizOptionItem(o, q.type == Quiz.QuizType.GUIDE_TO_IMAGE);
                item.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { if (!answerSubmitted) handleAnswer(item, q); }
                });
                quizGrid.add(item);
            }
            quizGrid.revalidate(); quizGrid.repaint();
        } else finishQuiz();
    }

    private static class Quiz {
        enum QuizType { IMAGE_TO_CATEGORY, GUIDE_TO_IMAGE }
        QuizType type; String question, correctAnswer, guideSnippet; List<String> options;
        Quiz(QuizType t, String q, String c, List<String> o, String g) {
            type = t; question = q; correctAnswer = c; options = o; guideSnippet = g;
        }
    }
}