package recycle;

import Main.MainApp;
import db.DAO.UserDAO;
import db.DTO.UserDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class LoginPanel extends JFrame implements ActionListener {

    private JTextField idField;
    private JPasswordField passwordField;
    private JCheckBox rememberMe;
    private JButton loginButton, registerButton;
    private UserDAO userDAO;

    private boolean isLoading = false;
    private int angle = 0;
    private Timer loadingTimer;
    private JPanel loadingOverlay;

    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    private static final String PREF_ID = "remembered_id";

    protected static final Color BG_DARK = new Color(15, 12, 30);
    protected static final Color BG_LIGHT = new Color(35, 30, 70);
    protected static final Color POINT_PURPLE = new Color(138, 43, 226);
    protected static final Color POINT_CYAN = new Color(0, 255, 240);
    protected static final Color FIELD_BG = new Color(255, 255, 255, 15);
    protected static final Color TEXT_SOFT = new Color(200, 200, 220);
    protected static final Color COLOR_ERR = new Color(255, 100, 100);
    protected static final Color COLOR_OK = new Color(100, 255, 150);

    public LoginPanel() {
        initDAO();
        setupFrame();
        initComponents();
        setupLoadingOverlay();
        loadSavedId(); 
        setVisible(true);
    }

    private void initDAO() {
        UserDAO.initializeDatabase();
        this.userDAO = new UserDAO();
    }

    private void setupFrame() {
        setTitle("에코 리사이클 로그인");
        setSize(420, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
    }

    private void setupLoadingOverlay() {
        loadingOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(POINT_CYAN);
                int size = 60;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.drawArc(x, y, size, size, angle, 280);
                g2.dispose();
            }
        };
        loadingOverlay.setOpaque(false);
        loadingOverlay.setVisible(false);
        setGlassPane(loadingOverlay);

        loadingTimer = new Timer(50, e -> {
            angle = (angle + 20) % 360;
            loadingOverlay.repaint();
        });
    }

    private void initComponents() {
        JPanel mainPanel = createGradientPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 45, 40, 45));
        add(mainPanel, BorderLayout.CENTER);

        JLabel logoLabel = new JLabel("♻");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70));
        logoLabel.setForeground(POINT_CYAN);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(logoLabel);

        mainPanel.add(Box.createVerticalStrut(10));
        JLabel titleLabel = new JLabel("에코 리사이클 케어");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(30));

        mainPanel.add(createLabel("사용자 계정 (ID)"));
        idField = createStyledTextField();
        idField.addActionListener(e -> passwordField.requestFocusInWindow());
        mainPanel.add(idField);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(createLabel("비밀번호 (PASSWORD)"));
        JPanel pwWrapper = new JPanel(new BorderLayout());
        pwWrapper.setOpaque(false);
        pwWrapper.setMaximumSize(new Dimension(320, 50));
        
        passwordField = createStyledPasswordField();
        passwordField.addActionListener(e -> handleLogin());
        
        JButton showPwBtn = new JButton("👁");
        showPwBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        showPwBtn.setForeground(TEXT_SOFT);
        showPwBtn.setContentAreaFilled(false);
        showPwBtn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        showPwBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPwBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { passwordField.setEchoChar((char)0); }
            public void mouseReleased(MouseEvent e) { passwordField.setEchoChar('●'); }
        });
        
        pwWrapper.add(passwordField, BorderLayout.CENTER);
        pwWrapper.add(showPwBtn, BorderLayout.EAST);
        mainPanel.add(pwWrapper);

        rememberMe = new JCheckBox("아이디 저장");
        rememberMe.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        rememberMe.setForeground(TEXT_SOFT);
        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);
        rememberMe.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(rememberMe);

        mainPanel.add(Box.createVerticalStrut(30));

        loginButton = createStyledButton("로그인 하기", POINT_PURPLE, Color.WHITE);
        registerButton = createStyledButton("새 계정 만들기", new Color(255, 255, 255, 20), TEXT_SOFT);

        loginButton.addActionListener(this);
        registerButton.addActionListener(this);

        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(registerButton);
    }

    private void loadSavedId() {
        String savedId = prefs.get(PREF_ID, "");
        if (!savedId.isEmpty()) {
            idField.setText(savedId);
            rememberMe.setSelected(true);
            passwordField.requestFocusInWindow();
        }
    }

    private void saveIdPreference(String id) {
        if (rememberMe.isSelected()) prefs.put(PREF_ID, id);
        else prefs.remove(PREF_ID);
    }

    public void setRegisteredId(String id) {
        idField.setText(id);
        passwordField.requestFocusInWindow();
    }

    private void handleLogin() {
        if (isLoading) return;
        String id = idField.getText().trim();
        String pw = new String(passwordField.getPassword());
        if (id.isEmpty() || pw.isEmpty()) return;

        showLoading(true);
        new Timer(800, e -> {
            ((Timer)e.getSource()).stop();
            try {
                UserDTO user = userDAO.loginUser(id, pw);
                showLoading(false);
                if (user != null) {
                    saveIdPreference(id); 
                    new MainApp(user);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호를 확인해주세요.");
                }
            } catch (SQLException ex) {
                showLoading(false);
                ex.printStackTrace();
            }
        }).start();
    }

    private void showLoading(boolean show) {
        isLoading = show;
        loadingOverlay.setVisible(show);
        if (show) loadingTimer.start();
        else loadingTimer.stop();
    }

    protected JPanel createGradientPanel() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, BG_DARK, 0, getHeight(), BG_LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (isFocusOwner()) {
                    g2.setColor(POINT_CYAN);
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        applyFieldStyle(f);
        return f;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (isFocusOwner()) {
                    g2.setColor(POINT_CYAN);
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setEchoChar('●');
        applyFieldStyle(f);
        return f;
    }

    private void applyFieldStyle(JTextField f) {
        f.setMaximumSize(new Dimension(320, 50));
        f.setPreferredSize(new Dimension(320, 50));
        f.setOpaque(false);
        f.setForeground(Color.WHITE);
        f.setCaretColor(POINT_CYAN);
        f.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    protected JButton createStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float alpha = isEnabled() ? 1.0f : 0.4f; 
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setColor(getModel().isRollover() && isEnabled() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setMaximumSize(new Dimension(320, 55));
        b.setPreferredSize(new Dimension(320, 55));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    protected JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        l.setForeground(new Color(160, 160, 190));
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) handleLogin();
        else if (e.getSource() == registerButton) new RegisterWindow(userDAO, this);
    }

    class RegisterWindow extends JDialog {
        private JTextField nickF, idF;
        private JPasswordField pwF, pwConfirmF;
        private JLabel nickMsg, idMsg, pwMsg;
        private JButton joinB;
        private UserDAO dao;
        private LoginPanel parent;
        private boolean isNickOk = false, isIdOk = false, isPwOk = false;

        public RegisterWindow(UserDAO dao, LoginPanel parent) {
            super(parent, "회원가입", true);
            this.dao = dao;
            this.parent = parent;
            setupUI();
        }

        private void setupUI() {
            setSize(440, 780);
            setLocationRelativeTo(null);
            JPanel p = parent.createGradientPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(30, 45, 30, 45));
            setContentPane(p);

            JLabel t = new JLabel("회원가입");
            t.setFont(new Font("맑은 고딕", Font.BOLD, 28));
            t.setForeground(POINT_CYAN);
            t.setAlignmentX(CENTER_ALIGNMENT);
            p.add(t);
            p.add(Box.createVerticalStrut(25));

            p.add(parent.createLabel("사용할 닉네임 (2~8자)"));
            nickF = parent.createStyledTextField();
            nickF.addActionListener(e -> idF.requestFocusInWindow());
            p.add(nickF);
            nickMsg = createStatusLabel();
            p.add(nickMsg);
            p.add(Box.createVerticalStrut(10));

            p.add(parent.createLabel("아이디 (5~12자 영문/숫자)"));
            idF = parent.createStyledTextField();
            idF.addActionListener(e -> pwF.requestFocusInWindow());
            p.add(idF);
            idMsg = createStatusLabel();
            p.add(idMsg);
            p.add(Box.createVerticalStrut(10));

            p.add(parent.createLabel("비밀번호 (8자 이상, 특수문자 포함)"));
            pwF = parent.createStyledPasswordField();
            pwF.addActionListener(e -> pwConfirmF.requestFocusInWindow());
            p.add(pwF);
            p.add(Box.createVerticalStrut(10));

            p.add(parent.createLabel("비밀번호 확인"));
            pwConfirmF = parent.createStyledPasswordField();
            pwConfirmF.addActionListener(e -> { if(joinB.isEnabled()) handleJoin(); });
            p.add(pwConfirmF);
            pwMsg = createStatusLabel();
            p.add(pwMsg);
            p.add(Box.createVerticalStrut(30));

            joinB = parent.createStyledButton("가입 완료", POINT_CYAN, BG_DARK);
            joinB.setEnabled(false); 
            p.add(joinB);

            setupRealtimeCheck();
            joinB.addActionListener(e -> handleJoin());

            setVisible(true);
        }

        private JLabel createStatusLabel() {
            JLabel l = new JLabel(" ");
            l.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            l.setAlignmentX(CENTER_ALIGNMENT);
            l.setBorder(new EmptyBorder(3, 0, 0, 0));
            return l;
        }

        private void setStatus(JLabel label, String text, boolean isOk) {
            label.setText(text);
            label.setForeground(isOk ? COLOR_OK : COLOR_ERR);
            validateAll(); 
        }

        private void validateAll() {
            joinB.setEnabled(isNickOk && isIdOk && isPwOk);
        }

        private void setupRealtimeCheck() {
            nickF.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
                String val = nickF.getText().trim();
                if (val.length() < 2 || val.length() > 8) {
                    isNickOk = false;
                    setStatus(nickMsg, "2~8자 사이로 입력하세요.", false);
                } else {
                    try {
                        boolean dup = dao.isExists("NICKNAME", val);
                        isNickOk = !dup;
                        setStatus(nickMsg, dup ? "이미 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.", !dup);
                    } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }));

            idF.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
                String val = idF.getText().trim();
                if (!val.matches("^[a-zA-Z0-9]{5,12}$")) {
                    isIdOk = false;
                    setStatus(idMsg, "5~12자 영문/숫자만 가능합니다.", false);
                } else {
                    try {
                        boolean dup = dao.isExists("USER_ID", val);
                        isIdOk = !dup;
                        setStatus(idMsg, dup ? "이미 사용중인 아이디입니다." : "사용 가능한 아이디입니다.", !dup);
                    } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }));

            DocumentListener pwListener = new SimpleDocumentListener(() -> {
                String p1 = new String(pwF.getPassword());
                String p2 = new String(pwConfirmF.getPassword());
                boolean lengthOk = p1.length() >= 8;
                boolean regexOk = p1.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
                
                if (!lengthOk || !regexOk) {
                    isPwOk = false;
                    setStatus(pwMsg, "8자 이상, 특수문자를 포함해야 합니다.", false);
                } else if (!p1.equals(p2)) {
                    isPwOk = false;
                    setStatus(pwMsg, "비밀번호가 일치하지 않습니다.", false);
                } else {
                    isPwOk = true;
                    setStatus(pwMsg, "비밀번호가 확인되었습니다.", true);
                }
            });
            pwF.getDocument().addDocumentListener(pwListener);
            pwConfirmF.getDocument().addDocumentListener(pwListener);
        }

        private void handleJoin() {
            String registeredId = idF.getText().trim();
            try {
                if (dao.registerUser(registeredId, new String(pwF.getPassword()), nickF.getText())) {
                    JOptionPane.showMessageDialog(this, "환영합니다! 가입이 완료되었습니다.");
                    parent.setRegisteredId(registeredId);
                    dispose();
                }
            } catch (SQLException ex) { 
                JOptionPane.showMessageDialog(this, "오류가 발생했습니다.");
            }
        }
    }

    class SimpleDocumentListener implements DocumentListener {
        private Runnable action;
        public SimpleDocumentListener(Runnable action) { this.action = action; }
        public void insertUpdate(DocumentEvent e) { action.run(); }
        public void removeUpdate(DocumentEvent e) { action.run(); }
        public void changedUpdate(DocumentEvent e) { action.run(); }
    }
}