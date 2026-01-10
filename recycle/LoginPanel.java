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

public class LoginPanel extends JFrame implements ActionListener {

    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private UserDAO userDAO;

    protected static final Color BG_DARK = new Color(20, 15, 40);   
    protected static final Color BG_LIGHT = new Color(40, 45, 90);     
    protected static final Color POINT_PURPLE = new Color(150, 100, 255); 
    protected static final Color POINT_CYAN = new Color(0, 255, 240);     
    protected static final Color FIELD_BG = new Color(255, 255, 255, 25); 
    protected static final Color BTN_SUB = new Color(70, 70, 120);       

    public LoginPanel() {
        initDAO();
        setupFrame();
        initComponents();
        setVisible(true);
    }

    private void initDAO() {
        try { this.userDAO = new UserDAO(); } 
        catch (Exception e) { JOptionPane.showMessageDialog(null, "DB 연결 실패"); System.exit(0); }
    }

    private void setupFrame() {
        setTitle("분리수거 안내 서비스");
        setSize(420, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = createGradientPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(60, 50, 60, 50));
        setContentPane(mainPanel);

        JLabel titleLabel = new JLabel("분리수거 서비스");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        titleLabel.setForeground(POINT_PURPLE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(50));

        mainPanel.add(createLabel("아이디"));
        idField = new JTextField();
        styleField(idField);
        mainPanel.add(idField);
        mainPanel.add(Box.createVerticalStrut(25));

        mainPanel.add(createLabel("비밀번호"));
        passwordField = new JPasswordField();
        styleField(passwordField);
        passwordField.addActionListener(e -> handleLogin()); 
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(50));

        loginButton = createStyledButton("로그인", POINT_PURPLE, Color.WHITE);
        registerButton = createStyledButton("회원가입", BTN_SUB, new Color(220, 220, 220));

        loginButton.addActionListener(this);
        registerButton.addActionListener(this);
        
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(registerButton);
    }

    protected JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BG_DARK, 0, getHeight(), BG_LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    protected void styleField(JTextField f) {
        f.setMaximumSize(new Dimension(320, 45));
        f.setBackground(FIELD_BG);
        f.setForeground(Color.WHITE);
        f.setCaretColor(POINT_CYAN);
        f.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        f.setOpaque(false);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 180), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    protected JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        l.setForeground(POINT_CYAN);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    protected JButton createStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(320, 50));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) handleLogin();
        else if (e.getSource() == registerButton) new RegisterWindow(userDAO, this);
    }

    private void handleLogin() {
        String id = idField.getText().trim();
        String pw = new String(passwordField.getPassword());
        try {
            UserDTO user = userDAO.loginUser(id, pw);
            if (user != null) { new MainApp(user); this.dispose(); }
            else { JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호를 확인하세요."); }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    class RegisterWindow extends JDialog implements ActionListener {
        private JTextField nickF, idF;
        private JPasswordField pwF;
        private JButton checkNickB, checkIdB, joinB;
        private UserDAO dao;
        private boolean isNickOk = false, isIdOk = false;

        public RegisterWindow(UserDAO dao, JFrame parent) {
            super(parent, "회원 가입 신청", true);
            this.dao = dao;
            setupUI();
        }

        private void setupUI() {
            setSize(440, 620);
            setLocationRelativeTo(null);
            
            JPanel p = createGradientPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(40, 45, 40, 45));
            setContentPane(p);

            JLabel t = new JLabel("회원가입");
            t.setFont(new Font("맑은 고딕", Font.BOLD, 28));
            t.setForeground(POINT_PURPLE);
            t.setAlignmentX(CENTER_ALIGNMENT);
            p.add(t); 
            p.add(Box.createVerticalStrut(40));

            p.add(createLabel("사용할 닉네임"));
            p.add(Box.createVerticalStrut(8));
            JPanel nickRow = new JPanel(new BorderLayout(10, 0));
            nickRow.setOpaque(false);
            nickRow.setMaximumSize(new Dimension(350, 45));
            nickF = new JTextField(); styleField(nickF);
            
            nickF.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { isNickOk = false; }
                public void removeUpdate(DocumentEvent e) { isNickOk = false; }
                public void changedUpdate(DocumentEvent e) { isNickOk = false; }
            });

            checkNickB = createColoredSmallBtn("중복확인", POINT_PURPLE);
            nickRow.add(nickF, BorderLayout.CENTER);
            nickRow.add(checkNickB, BorderLayout.EAST);
            p.add(nickRow);
            p.add(Box.createVerticalStrut(20));

            p.add(createLabel("아이디 설정"));
            p.add(Box.createVerticalStrut(8));
            JPanel idRow = new JPanel(new BorderLayout(10, 0));
            idRow.setOpaque(false);
            idRow.setMaximumSize(new Dimension(350, 45));
            idF = new JTextField(); styleField(idF);

            idF.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { isIdOk = false; }
                public void removeUpdate(DocumentEvent e) { isIdOk = false; }
                public void changedUpdate(DocumentEvent e) { isIdOk = false; }
            });

            checkIdB = createColoredSmallBtn("중복확인", POINT_PURPLE);
            idRow.add(idF, BorderLayout.CENTER);
            idRow.add(checkIdB, BorderLayout.EAST);
            p.add(idRow);
            p.add(Box.createVerticalStrut(20));

            p.add(createLabel("비밀번호"));
            p.add(Box.createVerticalStrut(8));
            pwF = new JPasswordField(); styleField(pwF);
            pwF.setMaximumSize(new Dimension(350, 45));
            p.add(pwF);
            p.add(Box.createVerticalStrut(45));

            joinB = createStyledButton("가입 완료", POINT_PURPLE, Color.WHITE);
            p.add(joinB);

            checkNickB.addActionListener(this);
            checkIdB.addActionListener(this);
            joinB.addActionListener(this);

            setVisible(true);
        }

        private JButton createColoredSmallBtn(String txt, Color bgColor) {
            JButton b = new JButton(txt);
            b.setPreferredSize(new Dimension(95, 45));
            b.setBackground(bgColor);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("맑은 고딕", Font.BOLD, 11));
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == checkNickB) {
                handleCheck("nickname", nickF.getText().trim());
            } else if (e.getSource() == checkIdB) {
                handleCheck("id", idF.getText().trim());
            } else if (e.getSource() == joinB) {
                handleJoin();
            }
        }

        private void handleCheck(String type, String value) {
            if(value.isEmpty()) return;
            try {
                boolean isDup = type.equals("id") ? dao.isIdDuplicate(value) : dao.isNicknameDuplicate(value);
                if (type.equals("id")) isIdOk = !isDup; else isNickOk = !isDup;
                JOptionPane.showMessageDialog(this, isDup ? "이미 사용 중입니다." : "사용 가능합니다.");
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        private void handleJoin() {
            if (!isNickOk || !isIdOk) {
                JOptionPane.showMessageDialog(this, "중복 확인이 필요합니다."); return;
            }
            try {
                String id = idF.getText().trim();
                String pw = new String(pwF.getPassword());
                String nick = nickF.getText().trim();
                if (dao.registerUser(id, pw, nick)) {
                    JOptionPane.showMessageDialog(this, "가입이 완료되었습니다!");
                    this.dispose();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}