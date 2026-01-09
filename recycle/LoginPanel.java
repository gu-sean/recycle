package recycle;

import Main.MainApp; 
import db.DAO.UserDAO; 
import db.DTO.UserDTO; 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

public class LoginPanel extends JFrame implements ActionListener {

    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private UserDAO userDAO;

    private static final Color BUTTON_BACKGROUND = new Color(220, 240, 255);
    
    public LoginPanel() {
        try {
           
            this.userDAO = new UserDAO();
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(null, "DB 연결 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }
        
        setTitle("분리수거 안내 서비스 - 로그인");
        setSize(350, 300); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("로그인", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("ID:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1; gbc.gridy = 0; idField = new JTextField(15); inputPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("PW:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1; gbc.gridy = 1; passwordField = new JPasswordField(15); inputPanel.add(passwordField, gbc);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginButton = new JButton("로그인");
        registerButton = new JButton("회원가입");

        loginButton.setBackground(BUTTON_BACKGROUND);
        registerButton.setBackground(BUTTON_BACKGROUND);
        
        loginButton.addActionListener(this);
        registerButton.addActionListener(this);
        
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        } else if (e.getSource() == registerButton) {
            new RegisterPanel(userDAO); 
        }
    }

    private void handleLogin() {
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID와 비밀번호를 입력하세요.");
            return;
        }

        try {
          
            UserDTO user = userDAO.loginUser(id, password);

            if (user != null) { 
                String welcomeMsg = user.isAdmin() ? 
                                    "[관리자] " + user.getNickname() + "님 환영합니다." : 
                                    user.getNickname() + "님 로그인 성공!";
                
                JOptionPane.showMessageDialog(this, welcomeMsg);
                
                SwingUtilities.invokeLater(() -> {
                   
                    new MainApp(user); 
                });
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "ID 또는 비밀번호가 틀렸습니다.");
            }
        } catch (SQLException ex) { 
            JOptionPane.showMessageDialog(this, "로그인 처리 중 DB 오류 발생.");
        }
    }
}

class RegisterPanel extends JFrame implements ActionListener { 
    
    private JTextField nicknameField, idField;
    private JPasswordField passwordField;
    private JButton checkNicknameButton, checkIdButton, registerButton; 
    private boolean isIdChecked = false; 
    private boolean isNicknameChecked = false; 
    
    private UserDAO userDAO; 
    private static final Color BUTTON_BACKGROUND = new Color(220, 240, 255);
    
    public RegisterPanel(UserDAO userDAO) { 
        this.userDAO = userDAO;
        
        setTitle("회원가입");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("회원가입", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("닉네임:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; nicknameField = new JTextField(12); inputPanel.add(nicknameField, gbc); 
        gbc.gridx = 2; gbc.gridy = 0; checkNicknameButton = new JButton("확인"); 
        checkNicknameButton.setBackground(BUTTON_BACKGROUND);
        inputPanel.add(checkNicknameButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; idField = new JTextField(12); inputPanel.add(idField, gbc); 
        gbc.gridx = 2; gbc.gridy = 1; checkIdButton = new JButton("확인"); 
        checkIdButton.setBackground(BUTTON_BACKGROUND);
        inputPanel.add(checkIdButton, gbc);

        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("PW:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        passwordField = new JPasswordField(12); inputPanel.add(passwordField, gbc); 

        add(inputPanel, BorderLayout.CENTER);

        registerButton = new JButton("회원가입 하기");
        registerButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        registerButton.setBackground(new Color(180, 220, 255));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        checkNicknameButton.addActionListener(this); 
        checkIdButton.addActionListener(this);
        registerButton.addActionListener(this);
        
        nicknameField.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) { isNicknameChecked = false; }
        });
        idField.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) { isIdChecked = false; }
        });

        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == checkNicknameButton) handleNicknameCheck();
        else if (e.getSource() == checkIdButton) handleIdCheck();
        else if (e.getSource() == registerButton) handleRegister();
    }
    
    private void handleNicknameCheck() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) return;
        try {
            if (userDAO.isNicknameDuplicate(nickname)) { 
                JOptionPane.showMessageDialog(this, "이미 사용 중인 닉네임입니다.");
            } else {
                JOptionPane.showMessageDialog(this, "사용 가능한 닉네임입니다.");
                isNicknameChecked = true;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
    
    private void handleIdCheck() {
        String id = idField.getText().trim();
        if (id.isEmpty()) return;
        try {
            if (userDAO.isIdDuplicate(id)) { 
                JOptionPane.showMessageDialog(this, "이미 사용 중인 아이디입니다.");
            } else {
                JOptionPane.showMessageDialog(this, "사용 가능한 아이디입니다.");
                isIdChecked = true;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
    
    private void handleRegister() {
        String nickname = nicknameField.getText().trim();
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (nickname.isEmpty() || id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "항목을 모두 입력하세요.");
            return;
        }

        if (!isNicknameChecked || !isIdChecked) {
            JOptionPane.showMessageDialog(this, "중복 확인을 완료해주세요.");
            return;
        }
        
        try {
        
            boolean success = userDAO.registerUser(id, password, nickname);
            if (success) {
                JOptionPane.showMessageDialog(this, "가입 성공! 이제 로그인하세요.");
                this.dispose(); 
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}