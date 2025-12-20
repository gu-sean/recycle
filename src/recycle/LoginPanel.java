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
            JOptionPane.showMessageDialog(null, "DB 연결 또는 초기화 오류: " + e.getMessage() + "\n프로그램을 종료합니다.", "오류", JOptionPane.ERROR_MESSAGE);
            System.err.println("DB 연결 오류: " + e.getMessage());
            dispose();
            return;
        }
        
        setTitle("분리수거 안내 서비스");
        setSize(350, 300); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        // 제목 레이블
        JLabel titleLabel = new JLabel("로그인", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // 입력 패널
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID 입력 필드
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("ID:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1; gbc.gridy = 0; idField = new JTextField(15); inputPanel.add(idField, gbc);

        // PW 입력 필드
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("PW:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1; gbc.gridy = 1; passwordField = new JPasswordField(15); inputPanel.add(passwordField, gbc);

        add(inputPanel, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginButton = new JButton("로그인");
        registerButton = new JButton("회원가입");
        

        loginButton.setBackground(BUTTON_BACKGROUND);
        registerButton.setBackground(BUTTON_BACKGROUND);
        
        loginButton.addActionListener(this);
        registerButton.addActionListener(this);
        
        // PW 필드에서 Enter 키 입력 시 로그인 처리
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == loginButton) {
            handleLogin();
        } else if (source == registerButton) {
            if (userDAO != null) {
                new RegisterPanel(userDAO); 
            } else {
                 JOptionPane.showMessageDialog(this, "DB 연결 오류로 회원가입을 시작할 수 없습니다.");
            }
        }
    }

    private void handleLogin() {
        if (userDAO == null) {
            JOptionPane.showMessageDialog(this, "DB 연결 오류로 로그인을 할 수 없습니다.");
            return;
        }
        
        String id = idField.getText();
        String password = new String(passwordField.getPassword());
        
        if (id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID와 비밀번호를 모두 입력하세요.");
            return;
        }

        try {
            UserDTO user = userDAO.loginUser(id, password);

            if (user != null) { 
                JOptionPane.showMessageDialog(this, user.getNickname() + "님, 로그인 성공!");
                
                SwingUtilities.invokeLater(() -> {
                     new MainApp(user); 
                });
                
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "ID 또는 비밀번호가 일치하지 않습니다.");
            }
        } catch (SQLException ex) { 
            System.err.println("로그인 DB 오류: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "로그인 처리 중 DB 오류가 발생했습니다.");
        }
    }
}


//회원가입 화면
class RegisterPanel extends JFrame implements ActionListener { 
    
    private JTextField nicknameField, idField;
    private JPasswordField passwordField;
    private JButton checkNicknameButton, checkIdButton, registerButton; 
    private boolean isIdChecked = false; 
    private boolean isNicknameChecked = false; 
    
    private UserDAO userDAO; 
    
    private static final int FIELD_COLUMNS = 15; 
    
    private static final Color BUTTON_BACKGROUND = new Color(220, 240, 255);
    
    public RegisterPanel(UserDAO userDAO) { 
        this.userDAO = userDAO;
        
        setTitle("회원가입");
        setSize(400, 300);
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

    
        // 닉네임 입력 필드 및 중복 확인 버튼
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; inputPanel.add(new JLabel("닉네임:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1; gbc.gridy = 0; nicknameField = new JTextField(FIELD_COLUMNS); inputPanel.add(nicknameField, gbc); 
        
        gbc.gridx = 2; gbc.gridy = 0; checkNicknameButton = new JButton("중복확인"); 
        checkNicknameButton.setBackground(BUTTON_BACKGROUND);
        inputPanel.add(checkNicknameButton, gbc);
        
      
        // ID 입력 필드 및 중복 확인 버튼
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; inputPanel.add(new JLabel("ID:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1; gbc.gridy = 1; idField = new JTextField(FIELD_COLUMNS); inputPanel.add(idField, gbc); 
        
        gbc.gridx = 2; gbc.gridy = 1; checkIdButton = new JButton("중복확인"); 
        checkIdButton.setBackground(BUTTON_BACKGROUND);
        inputPanel.add(checkIdButton, gbc);

      
        // PW 입력 필드
        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("PW:", SwingConstants.RIGHT), gbc);
        

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1; 
        passwordField = new JPasswordField(FIELD_COLUMNS); 
        
        inputPanel.add(passwordField, gbc); 

        add(inputPanel, BorderLayout.CENTER);

  
        registerButton = new JButton("회원가입 하기");
        registerButton.setBackground(BUTTON_BACKGROUND);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        checkNicknameButton.addActionListener(this); 
        checkIdButton.addActionListener(this);
        registerButton.addActionListener(this);
        
        // 닉네임 필드에 키 입력 시 중복 확인 상태 초기화
        nicknameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                isNicknameChecked = false;
            }
        });

        // ID 필드에 키 입력 시 중복 확인 상태 초기화
        idField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                isIdChecked = false;
            }
        });

        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == checkNicknameButton) {
            handleNicknameCheck();
        } else if (source == checkIdButton) {
            handleIdCheck();
        } else if (source == registerButton) {
            handleRegister();
        }
    }
    
    //닉네임 중복 확인 처리
     
    private void handleNicknameCheck() {
        String nickname = nicknameField.getText();
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "닉네임을 입력하세요.");
            return;
        }

        try {
            if (userDAO.isNicknameDuplicate(nickname)) { 
                JOptionPane.showMessageDialog(this, "이미 사용 중인 닉네임입니다.");
                isNicknameChecked = false;
            } else {
                JOptionPane.showMessageDialog(this, "사용 가능한 닉네임입니다.");
                isNicknameChecked = true;
            }
        } catch (SQLException ex) { 
             System.err.println("닉네임 중복 확인 DB 오류: " + ex.getMessage());
             JOptionPane.showMessageDialog(this, "중복 확인 중 DB 오류 발생.");
        }
    }
    
    private void handleIdCheck() {
        String id = idField.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디를 입력하세요.");
            return;
        }

        try {
            if (userDAO.isIdDuplicate(id)) { 
                JOptionPane.showMessageDialog(this, "이미 사용 중인 아이디입니다.");
                isIdChecked = false;
            } else {
                JOptionPane.showMessageDialog(this, "사용 가능한 아이디입니다.");
                isIdChecked = true;
            }
        } catch (SQLException ex) { 
             System.err.println("ID 중복 확인 DB 오류: " + ex.getMessage());
             JOptionPane.showMessageDialog(this, "중복 확인 중 DB 오류 발생.");
        }
    }
    
    private void handleRegister() {
        String nickname = nicknameField.getText();
        String id = idField.getText();
        String password = new String(passwordField.getPassword());

        if (nickname.isEmpty() || id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 항목을 입력하세요.");
            return;
        }

        // 닉네임 중복 확인 필수
        if (!isNicknameChecked) {
            JOptionPane.showMessageDialog(this, "닉네임 중복 확인을 먼저 해주세요.");
            return;
        }

        // ID 중복 확인 필수
        if (!isIdChecked) {
            JOptionPane.showMessageDialog(this, "아이디 중복 확인을 먼저 해주세요.");
            return;
        }
        
        try {
            boolean success = userDAO.registerUser(id, password, nickname);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인 창으로 돌아가 로그인해주세요.");
                

                nicknameField.setText("");
                idField.setText("");
                passwordField.setText("");
                isIdChecked = false; 
                isNicknameChecked = false; 
                
                this.dispose(); 
            } else {
                 JOptionPane.showMessageDialog(this, "회원가입 중 알 수 없는 오류가 발생했습니다.");
            }
        } catch (SQLException ex) { 
            System.err.println("회원가입 DB 오류: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "회원가입 중 DB 오류가 발생했습니다.");
        }
    }
}