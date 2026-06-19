import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private List<Engineer> allEngineers;

    // ログイン画面部品
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;

    // 一覧画面部品
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearchId, txtSearchName;
    private JComboBox<String> comboSearchExp;
    private JCheckBox[] searchChkLanguages;

    // 詳細・編集画面部品
    private JTextField txtEngId, txtEngName;
    private DateSelectionPanel panelBirthDate;
    private JComboBox<String> comboJoinYear, comboJoinMonth; 
    private JComboBox<String> comboEngHistory; 
    private JTextField txtCareer, txtTraining; 
    private JComboBox<String> comboScore, comboAttitude, comboComm, comboLeadership; 
    private JTextArea txtRemarks; 
    private JCheckBox[] chkLanguages;
    private JLabel lblPhotoPreview; 
    private File selectedPhotoFile; 
    
    private final String[] LANGS = {"Java", "Python", "C言語", "JavaScript", "Ruby"};
    private final String[] SCORES = {"1.0","1.5","2.0","2.5","3.0","3.5","4.0","4.5","5.0"};
    private final String PHOTO_DIR = "photos"; 

    public MainApp() {
        setTitle("エンジニア情報管理システム");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        new File(PHOTO_DIR).mkdirs();

        allEngineers = new ArrayList<>();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createListPanel(), "LIST");
        mainPanel.add(createDetailPanel(), "DETAIL");

        add(mainPanel);
    }

    // ========== 1. ログイン画面 ==========
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JPanel innerPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        innerPanel.add(new JLabel("社員ID:"));
        txtLoginUser = new JTextField();
        innerPanel.add(txtLoginUser);

        innerPanel.add(new JLabel("パスワード:"));
        txtLoginPass = new JPasswordField();
        innerPanel.add(txtLoginPass);

        JButton btnLogin = new JButton("ログイン");
        innerPanel.add(new JLabel()); 
        innerPanel.add(btnLogin);

        txtLoginUser.addActionListener(e -> btnLogin.doClick());
        txtLoginPass.addActionListener(e -> btnLogin.doClick());

        btnLogin.addActionListener(e -> {
            String user = txtLoginUser.getText();
            String pass = new String(txtLoginPass.getPassword());

            if (PasswordUtil.authenticate(user, pass)) {
                allEngineers = CSVUtil.readCSV();
                refreshTable(allEngineers);
                cardLayout.show(mainPanel, "LIST");
            } else {
                JOptionPane.showMessageDialog(this, "IDまたはパスワードが正しくありません。", "エラー", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(innerPanel);
        return panel;
    }

    // ========== 2. 一覧画面 ==========
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel(new BorderLayout());
        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton btnLogout = new JButton("ログアウト");
        btnLogout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "ログアウトしますか？", "確認", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                txtLoginUser.setText(""); txtLoginPass.setText("");
                cardLayout.show(mainPanel, "LOGIN");
            }
        });
        logoutWrapper.add(btnLogout);
        topContainer.add(logoutWrapper, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("条件検索"));

        searchPanel.add(new JLabel("社員ID:"));
        txtSearchId = new JTextField(8);
        searchPanel.add(txtSearchId);

        searchPanel.add(new JLabel(" 氏名:"));
        txtSearchName = new JTextField(10);
        searchPanel.add(txtSearchName);

        searchPanel.add(new JLabel(" 歴:"));
        comboSearchExp = new JComboBox<>(new String[]{"選択なし", "1年以上", "3年以上", "5年以上", "10年以上"});
        searchPanel.add(comboSearchExp);

        searchPanel.add(new JLabel(" 言語:"));
        searchChkLanguages = new JCheckBox[LANGS.length];
        for (int i = 0; i < LANGS.length; i++) {
            searchChkLanguages[i] = new JCheckBox(LANGS[i]);
            searchPanel.add(searchChkLanguages[i]);
        }

        JButton btnSearch = new JButton("検索");
        JButton btnReset = new JButton("クリア");
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        txtSearchId.addActionListener(e -> executeSearch());
        txtSearchName.addActionListener(e -> executeSearch());
        btnSearch.addActionListener(e -> executeSearch());
        btnReset.addActionListener(e -> {
            txtSearchId.setText(""); txtSearchName.setText("");
            comboSearchExp.setSelectedIndex(0);
            for (JCheckBox chk : searchChkLanguages) chk.setSelected(false);
            refreshTable(allEngineers);
        });

        topContainer.add(searchPanel, BorderLayout.CENTER);

        String[] cols = {"社員ID", "氏名", "年齢", "エンジニア歴", "言語", "技術力", "最終更新"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        sorter.setComparator(2, (s1, s2) -> Integer.compare(extractNumber(s1), extractNumber(s2)));
        sorter.setComparator(3, (s1, s2) -> Integer.compare(extractNumber(s1), extractNumber(s2)));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int modelRow = table.convertRowIndexToModel(row);
                        Engineer emp = allEngineers.get(modelRow);
                        loadDetailData(emp);
                        cardLayout.show(mainPanel, "DETAIL");
                    }
                }
            }
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = new JButton("新規追加");
        JButton btnDelete = new JButton("選択削除");
        // 💡 ボタン名も仕様に合わせて分かりやすく変更
        JButton btnImport = new JButton("CSV一括読込"); 
        JButton btnExport = new JButton("CSV出力選択"); 
        JButton btnTemplate = new JButton("テンプレート出力");

        btnAdd.addActionListener(e -> {
            clearDetailFields();
            txtEngId.setText(generateRandomId());
            txtEngId.setEditable(false);
            cardLayout.show(mainPanel, "DETAIL");
        });
        btnDelete.addActionListener(e -> executeDelete());
        btnImport.addActionListener(e -> executeImport());
        btnExport.addActionListener(e -> executeExportSelected());
        btnTemplate.addActionListener(e -> executeTemplate());

        actionPanel.add(btnAdd); actionPanel.add(btnDelete);
        actionPanel.add(new JLabel("   |   ")); 
        actionPanel.add(btnImport); actionPanel.add(btnExport); actionPanel.add(btnTemplate);

        panel.add(topContainer, BorderLayout.NORTH); 
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ========== 3. 詳細・編集画面 ==========
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel photoWrapper = new JPanel(new BorderLayout(5, 5));
        lblPhotoPreview = new JLabel("NO IMAGE", SwingConstants.CENTER);
        lblPhotoPreview.setPreferredSize(new Dimension(120, 150)); 
        lblPhotoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JButton btnSelectPhoto = new JButton("写真を選択");
        btnSelectPhoto.addActionListener(e -> selectPhoto());

        photoWrapper.add(lblPhotoPreview, BorderLayout.CENTER);
        photoWrapper.add(btnSelectPhoto, BorderLayout.SOUTH);

        JPanel topPhotoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPhotoPanel.add(photoWrapper);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;

        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2; formPanel.add(new JLabel("社員ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8; txtEngId = new JTextField(); txtEngId.setBackground(Color.LIGHT_GRAY); formPanel.add(txtEngId, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("氏名:"), gbc);
        gbc.gridx = 1; txtEngName = new JTextField(); formPanel.add(txtEngName, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("生年月日:"), gbc);
        gbc.gridx = 1; panelBirthDate = new DateSelectionPanel(1960, LocalDate.now().getYear()); formPanel.add(panelBirthDate, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("入社年月:"), gbc);
        gbc.gridx = 1; 
        JPanel panelJoin = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboJoinYear = new JComboBox<>();
        for(int i = 1980; i <= LocalDate.now().getYear(); i++) comboJoinYear.addItem(String.valueOf(i));
        comboJoinMonth = new JComboBox<>();
        for(int i = 1; i <= 12; i++) comboJoinMonth.addItem(String.format("%02d", i));
        panelJoin.add(comboJoinYear); panelJoin.add(new JLabel("年 "));
        panelJoin.add(comboJoinMonth); panelJoin.add(new JLabel("月"));
        formPanel.add(panelJoin, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("エンジニア歴:"), gbc);
        gbc.gridx = 1; 
        comboEngHistory = new JComboBox<>();
        for(int i = 0; i <= 50; i++) comboEngHistory.addItem(i + "年");
        JPanel expWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        expWrap.add(comboEngHistory);
        formPanel.add(expWrap, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("経歴:"), gbc);
        gbc.gridx = 1; txtCareer = new JTextField(); formPanel.add(txtCareer, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("研修の受講歴:"), gbc);
        gbc.gridx = 1; txtTraining = new JTextField(); formPanel.add(txtTraining, gbc);
        row++;

        comboScore = new JComboBox<>(SCORES);
        comboAttitude = new JComboBox<>(SCORES);
        comboComm = new JComboBox<>(SCORES);
        comboLeadership = new JComboBox<>(SCORES);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("技術力スコア:"), gbc);
        gbc.gridx = 1; JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); p1.add(comboScore); formPanel.add(p1, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("受講態度:"), gbc);
        gbc.gridx = 1; JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); p2.add(comboAttitude); formPanel.add(p2, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("コミュニケーション:"), gbc);
        gbc.gridx = 1; JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); p3.add(comboComm); formPanel.add(p3, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("リーダーシップ:"), gbc);
        gbc.gridx = 1; JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); p4.add(comboLeadership); formPanel.add(p4, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("言語:"), gbc);
        gbc.gridx = 1; 
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        chkLanguages = new JCheckBox[LANGS.length];
        for (int i = 0; i < LANGS.length; i++) {
            chkLanguages[i] = new JCheckBox(LANGS[i]);
            langPanel.add(chkLanguages[i]);
        }
        formPanel.add(langPanel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("備考:"), gbc);
        gbc.gridx = 1; 
        txtRemarks = new JTextArea(3, 20);
        txtRemarks.setLineWrap(true);
        JScrollPane scrollRemarks = new JScrollPane(txtRemarks);
        formPanel.add(scrollRemarks, gbc);

        JPanel centerContainer = new JPanel(new BorderLayout(20, 20));
        centerContainer.add(topPhotoPanel, BorderLayout.NORTH); 
        centerContainer.add(formPanel, BorderLayout.CENTER);    

        JScrollPane mainScroll = new JScrollPane(centerContainer);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSave = new JButton("更新保存");
        JButton btnCancel = new JButton("戻る");

        btnSave.addActionListener(e -> executeSave());
        btnCancel.addActionListener(e -> cardLayout.show(mainPanel, "LIST"));

        btnPanel.add(btnSave); btnPanel.add(btnCancel);

        panel.add(new JLabel("■ エンジニア詳細情報", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(mainScroll, BorderLayout.CENTER); 
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ========== 各種ロジック ==========

    private String generateRandomId() {
        Random random = new Random();
        return String.format("%08d", random.nextInt(100000000));
    }

    private void selectPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("画像ファイル", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPhotoFile = chooser.getSelectedFile();
            displayPhoto(selectedPhotoFile.getAbsolutePath());
        }
    }

    private void displayPhoto(String path) {
        if (path == null || path.equals("なし") || !new File(path).exists()) {
            lblPhotoPreview.setIcon(null); lblPhotoPreview.setText("NO IMAGE");
            return;
        }
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(120, 150, Image.SCALE_SMOOTH);
        lblPhotoPreview.setIcon(new ImageIcon(scaled));
        lblPhotoPreview.setText("");
    }

    private String copyPhotoToDir(File source, String empId) {
        if (source == null || !source.exists()) return "なし";
        try {
            String ext = "";
            String name = source.getName();
            int i = name.lastIndexOf('.');
            if (i > 0) ext = name.substring(i);
            
            String destPath = PHOTO_DIR + "/" + empId + ext;
            Files.copy(source.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
            return destPath;
        } catch (IOException e) { return "なし"; }
    }

    private void executeSave() {
        String id = txtEngId.getText();
        String name = txtEngName.getText();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "氏名を入力してください。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder langs = new StringBuilder();
        for (JCheckBox chk : chkLanguages) {
            if (chk.isSelected()) {
                if (langs.length() > 0) langs.append("/");
                langs.append(chk.getText());
            }
        }

        String finalPhotoPath = "なし";
        if (selectedPhotoFile != null) {
            finalPhotoPath = copyPhotoToDir(selectedPhotoFile, id);
        } else {
            for (Engineer e : allEngineers) {
                if (e.getId().equals(id)) { finalPhotoPath = e.getPhotoPath(); break; }
            }
        }

        String joinYM = comboJoinYear.getSelectedItem() + "/" + comboJoinMonth.getSelectedItem();

        Engineer emp = new Engineer(
            id, name,
            panelBirthDate.getDateString(),
            joinYM, 
            Double.parseDouble((String)comboScore.getSelectedItem()),
            langs.toString(),
            LocalDate.now().toString(),
            finalPhotoPath,
            (String)comboEngHistory.getSelectedItem(), 
            txtCareer.getText(),                       
            txtTraining.getText(),                     
            Double.parseDouble((String)comboAttitude.getSelectedItem()), 
            Double.parseDouble((String)comboComm.getSelectedItem()),     
            Double.parseDouble((String)comboLeadership.getSelectedItem()),
            txtRemarks.getText()                       
        );

        boolean isNew = true;
        for (int i = 0; i < allEngineers.size(); i++) {
            if (allEngineers.get(i).getId().equals(id)) {
                allEngineers.set(i, emp);
                isNew = false;
                break;
            }
        }
        if (isNew) allEngineers.add(emp);

        CSVUtil.writeCSV(allEngineers);
        refreshTable(allEngineers);
        cardLayout.show(mainPanel, "LIST");
        JOptionPane.showMessageDialog(this, "データを保存しました。");
    }

    private void loadDetailData(Engineer emp) {
        txtEngId.setText(emp.getId()); txtEngId.setEditable(false);
        txtEngName.setText(emp.getName());
        panelBirthDate.setDateString(emp.getBirthDate());
        
        String join = emp.getJoinDate();
        if(join != null && join.contains("/")) {
            String[] parts = join.split("/");
            if(parts.length >= 2) {
                comboJoinYear.setSelectedItem(parts[0]);
                comboJoinMonth.setSelectedItem(parts[1]);
            }
        }

        comboEngHistory.setSelectedItem(emp.getEngineerHistory());
        txtCareer.setText(emp.getCareerHistory());
        txtTraining.setText(emp.getTrainingHistory());
        
        comboScore.setSelectedItem(String.valueOf(emp.getTechnicalScore()));
        comboAttitude.setSelectedItem(String.valueOf(emp.getAttitudeScore()));
        comboComm.setSelectedItem(String.valueOf(emp.getCommunicationScore()));
        comboLeadership.setSelectedItem(String.valueOf(emp.getLeadershipScore()));
        txtRemarks.setText(emp.getRemarks());

        String empLangs = emp.getLanguage();
        for (JCheckBox chk : chkLanguages) chk.setSelected(empLangs.contains(chk.getText()));

        selectedPhotoFile = null;
        displayPhoto(emp.getPhotoPath());
    }

    private void clearDetailFields() {
        txtEngId.setText(""); txtEngId.setEditable(false);
        txtEngName.setText("");
        panelBirthDate.setDateString("2000-01-01");
        
        comboJoinYear.setSelectedItem(String.valueOf(LocalDate.now().getYear()));
        comboJoinMonth.setSelectedItem("04");
        
        comboEngHistory.setSelectedIndex(0);
        txtCareer.setText("");
        txtTraining.setText("");
        
        comboScore.setSelectedItem("3.0");
        comboAttitude.setSelectedItem("3.0");
        comboComm.setSelectedItem("3.0");
        comboLeadership.setSelectedItem("3.0");
        txtRemarks.setText("");

        for (JCheckBox chk : chkLanguages) chk.setSelected(false);
        selectedPhotoFile = null;
        displayPhoto(null);
    }

    private void executeSearch() {
        String sId = txtSearchId.getText().trim();
        String sName = txtSearchName.getText().replaceAll("[\\s ]", "");
        int reqExp = extractNumber(comboSearchExp.getSelectedItem());
        
        List<String> reqLangs = new ArrayList<>();
        for (JCheckBox chk : searchChkLanguages) {
            if (chk.isSelected()) reqLangs.add(chk.getText().toLowerCase());
        }

        List<Engineer> result = new ArrayList<>();
        for (Engineer emp : allEngineers) {
            boolean match = true;
            if (!sId.isEmpty() && !emp.getId().contains(sId)) match = false;
            
            String targetName = emp.getName() != null ? emp.getName().replaceAll("[\\s ]", "") : "";
            if (!sName.isEmpty() && !targetName.contains(sName)) match = false;
            
            if (emp.getExperienceYearsInt() < reqExp) match = false;

            if (!reqLangs.isEmpty()) {
                boolean langMatch = false;
                String empLang = emp.getLanguage().toLowerCase();
                for (String lang : reqLangs) {
                    if (empLang.contains(lang)) { langMatch = true; break; }
                }
                if (!langMatch) match = false;
            }

            if (match) result.add(emp);
        }
        refreshTable(result);
    }

    private void executeDelete() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) return;
        
        if (JOptionPane.showConfirmDialog(this, "選択したエンジニアを削除しますか？", "確認", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            List<Engineer> toDelete = new ArrayList<>();
            for (int r : rows) {
                toDelete.add(allEngineers.get(table.convertRowIndexToModel(r)));
            }
            allEngineers.removeAll(toDelete);
            CSVUtil.writeCSV(allEngineers);
            refreshTable(allEngineers);
        }
    }

    // 💡 メッセージをご要望の仕様書通りに変更
    private void executeImport() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            List<Engineer> imported = CSVUtil.readExternalCSV(fc.getSelectedFile());
            allEngineers.addAll(imported);
            CSVUtil.writeCSV(allEngineers);
            refreshTable(allEngineers);
            JOptionPane.showMessageDialog(this, "エンジニア情報を一括で追加しました。");
        }
    }

    // 💡 メッセージをご要望の仕様書通りに変更
    private void executeExportSelected() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "出力するエンジニアを選択してください。");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("selected_engineers.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            List<Engineer> targets = new ArrayList<>();
            for (int r : rows) {
                targets.add(allEngineers.get(table.convertRowIndexToModel(r)));
            }
            CSVUtil.exportCSV(targets, fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "選択したエンジニアの情報をCSVに出力しました。");
        }
    }

    // 💡 メッセージをご要望の仕様書通りに変更
    private void executeTemplate() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("template.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            CSVUtil.writeTemplate(fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "テンプレートとなるCSVを出力しました。");
        }
    }

    private void refreshTable(List<Engineer> list) {
        tableModel.setRowCount(0);
        for (Engineer e : list) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getName(), e.getAge() + "歳", e.getEngineerHistory(),
                e.getLanguage(), e.getTechnicalScore(), e.getUpdatedAt()
            });
        }
    }

    private int extractNumber(Object obj) {
        if (obj == null) return 0;
        String str = obj.toString();
        if (str.isEmpty() || str.equals("選択なし")) return 0;
        String numStr = str.replaceAll("[^0-9]", "");
        return numStr.isEmpty() ? 0 : Integer.parseInt(numStr);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}