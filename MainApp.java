import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // ログイン画面用
    private JTextField txtLoginUser = new JTextField(15);
    private JPasswordField txtLoginPass = new JPasswordField(15);

    // 一覧画面用
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearchId = new JTextField(8);
    private JTextField txtSearchName = new JTextField(10);
    private JTextField txtSearchMinAge = new JTextField(3);
    private JTextField txtSearchMaxAge = new JTextField(3);
    private JComboBox<String> comboSearchExp = new JComboBox<>(new String[]{"選択なし", "1年以上", "3年以上", "5年以上", "10年以上"});
    private JCheckBox[] searchChkLanguages = { new JCheckBox("Java"), new JCheckBox("Python"), new JCheckBox("C言語"), new JCheckBox("JavaScript"), new JCheckBox("Ruby") };
    
    private JButton btnSearch = new JButton("検索");
    private JButton btnCancel = new JButton("× キャンセル");
    private JButton btnAdd = new JButton("新規追加");
    private JButton btnDelete = new JButton("選択削除");
    private JButton btnImport = new JButton("CSV読み込み");
    private JButton btnExport = new JButton("CSV出力");
    private JButton btnTemplate = new JButton("テンプレート出力");
    private SearchWorker currentWorker;

    // 詳細画面用（追加項目含む）
    private JTextField txtEngId = new JTextField(20);
    private JTextField txtEngName = new JTextField(20);
    private DateSelectionPanel panelBirthDate = new DateSelectionPanel(1950, 2010);
    private DateSelectionPanel panelJoinDate = new DateSelectionPanel(1980, 2030);
    private JTextField txtCareer = new JTextField(30);   // 経歴
    private JTextField txtTraining = new JTextField(30); // 研修の受講歴
    private JCheckBox[] detailChkLanguages = { new JCheckBox("Java"), new JCheckBox("Python"), new JCheckBox("C言語"), new JCheckBox("JavaScript"), new JCheckBox("Ruby") };
    
    private JComboBox<String> comboScore;
    private JComboBox<String> comboAttitude;
    private JComboBox<String> comboCommunication;
    private JComboBox<String> comboLeadership;
    
    private JTextField txtNote = new JTextField(30);     // 備考
    private JLabel lblCurrentUpdate = new JLabel("-");
    private JButton btnSave = new JButton("更新保存");

    private boolean isEditMode = false;

    public MainApp() {
        setTitle("エンジニア情報管理システム");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createListPanel(), "LIST");
        mainPanel.add(createDetailPanel(), "DETAIL");
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("ユーザー:"), gbc);
        gbc.gridx = 1; panel.add(txtLoginUser, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("パスワード:"), gbc);
        gbc.gridx = 1; panel.add(txtLoginPass, gbc);
        JButton btnLogin = new JButton("ログイン");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(btnLogin, gbc);

        txtLoginUser.addActionListener(e -> btnLogin.doClick());
        txtLoginPass.addActionListener(e -> btnLogin.doClick());

        btnLogin.addActionListener(e -> {
            String user = txtLoginUser.getText();
            String pass = new String(txtLoginPass.getPassword());

            // 修正：ハッシュ化されたパスワードを保持・比較する
            java.util.Map<String, String> validUsers = new java.util.HashMap<>();
            validUsers.put("0001", PasswordUtil.hash("pass1"));   
            validUsers.put("0002", PasswordUtil.hash("pass2"));   
            validUsers.put("0003", PasswordUtil.hash("pass3"));   
            validUsers.put("0004", PasswordUtil.hash("pass4"));   

            if (validUsers.containsKey(user) && validUsers.get(user).equals(PasswordUtil.hash(pass))) {
                refreshTable(CSVUtil.readCSV());
                cardLayout.show(mainPanel, "LIST");
            } else {
                JOptionPane.showMessageDialog(this, "IDまたはパスワードが正しくありません", "認証エラー", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("社員 検索（項目別）"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; searchPanel.add(new JLabel("社員ID:"), gbc);
        gbc.gridx = 1; searchPanel.add(txtSearchId, gbc);
        gbc.gridx = 2; searchPanel.add(new JLabel("氏名:"), gbc);
        gbc.gridx = 3; searchPanel.add(txtSearchName, gbc);
        gbc.gridx = 0; gbc.gridy = 1; searchPanel.add(new JLabel("年齢:"), gbc);
        JPanel ageP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ageP.add(txtSearchMinAge); ageP.add(new JLabel(" 〜 ")); ageP.add(txtSearchMaxAge);
        gbc.gridx = 1; searchPanel.add(ageP, gbc);
        gbc.gridx = 2; searchPanel.add(new JLabel("歴:"), gbc);
        gbc.gridx = 3; searchPanel.add(comboSearchExp, gbc);
        gbc.gridx = 0; gbc.gridy = 2; searchPanel.add(new JLabel("言語:"), gbc);
        JPanel lp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        for (JCheckBox c : searchChkLanguages) lp.add(c);
        gbc.gridx = 1; gbc.gridwidth = 3; searchPanel.add(lp, gbc);
        
        JPanel bp = new JPanel(); bp.add(btnSearch); bp.add(btnCancel); btnCancel.setEnabled(false);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; searchPanel.add(bp, gbc);
        panel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"社員ID", "氏名", "年齢", "エンジニア歴", "言語", "技術力", "最終更新"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(2, (String s1, String s2) -> Integer.compare(Integer.parseInt(s1.replace("歳", "")), Integer.parseInt(s2.replace("歳", ""))));
        sorter.setComparator(3, (String s1, String s2) -> Integer.compare(Integer.parseInt(s1.replace("年", "")), Integer.parseInt(s2.replace("年", ""))));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(); 
        bottom.add(btnAdd); bottom.add(btnDelete); 
        bottom.add(btnImport); bottom.add(btnExport); bottom.add(btnTemplate);
        panel.add(bottom, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> executeSearch());
        btnCancel.addActionListener(e -> { if(currentWorker != null) currentWorker.cancel(true); });
        btnAdd.addActionListener(e -> showDetailForm(null)); 
        btnDelete.addActionListener(e -> executeDelete());   
        btnImport.addActionListener(e -> executeImport());   
        btnExport.addActionListener(e -> executeExportSelected()); 
        btnTemplate.addActionListener(e -> executeTemplate()); 

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    String id = (String) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
                    showDetailForm(CSVUtil.readCSV().stream().filter(en -> en.getId().equals(id)).findFirst().orElse(null));
                }
            }
        });
        return panel;
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15); gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.1; panel.add(new JLabel("社員ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 0.9; panel.add(txtEngId, gbc);
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("氏名:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(txtEngName, gbc);
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("生年月日:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(panelBirthDate, gbc);
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("入社年月:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(panelJoinDate, gbc);
        
        // 追加項目
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("経歴:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(txtCareer, gbc);
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("研修の受講歴:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(txtTraining, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("扱える言語:"), gbc);
        JPanel ldp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        for (JCheckBox c : detailChkLanguages) ldp.add(c);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(ldp, gbc);
        
        String[] scores = {"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"};
        comboScore = new JComboBox<>(scores);
        comboAttitude = new JComboBox<>(scores);
        comboCommunication = new JComboBox<>(scores);
        comboLeadership = new JComboBox<>(scores);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        scorePanel.add(new JLabel("技術力: ")); scorePanel.add(comboScore);
        scorePanel.add(new JLabel(" 受講態度: ")); scorePanel.add(comboAttitude);
        scorePanel.add(new JLabel(" コミュ力: ")); scorePanel.add(comboCommunication);
        scorePanel.add(new JLabel(" リーダーシップ: ")); scorePanel.add(comboLeadership);
        
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("各種評価:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(scorePanel, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("備考:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(txtNote, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("最終更新:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(lblCurrentUpdate, gbc);
        
        JPanel bbp = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        bbp.add(btnSave); JButton btnB = new JButton("戻る"); bbp.add(btnB);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; panel.add(bbp, gbc);
        
        btnSave.addActionListener(e -> executeSave()); 
        btnB.addActionListener(e -> cardLayout.show(mainPanel, "LIST"));
        return panel;
    }

    private void executeSearch() {
        setUILocked(true);
        List<String> langs = new ArrayList<>();
        for (JCheckBox c : searchChkLanguages) if (c.isSelected()) langs.add(c.getText());
        currentWorker = new SearchWorker(txtSearchId.getText().trim(), txtSearchName.getText().trim(), txtSearchMinAge.getText().trim(), txtSearchMaxAge.getText().trim(), (String) comboSearchExp.getSelectedItem(), langs);
        currentWorker.execute();
    }

    private void setUILocked(boolean l) {
        txtSearchId.setEnabled(!l); txtSearchName.setEnabled(!l); txtSearchMinAge.setEnabled(!l); txtSearchMaxAge.setEnabled(!l);
        comboSearchExp.setEnabled(!l); for (JCheckBox c : searchChkLanguages) c.setEnabled(!l);
        btnSearch.setEnabled(!l); btnAdd.setEnabled(!l); btnDelete.setEnabled(!l); btnImport.setEnabled(!l); btnExport.setEnabled(!l); btnTemplate.setEnabled(!l);
        btnCancel.setEnabled(l); 
    }

    class SearchWorker extends SwingWorker<List<Engineer>, Void> {
        String i, n, mi, ma, ex; List<String> ls;
        public SearchWorker(String id, String nm, String min, String max, String exp, List<String> lgs) { i = id; n = nm; mi = min; ma = max; ex = exp; ls = lgs; }
        @Override protected List<Engineer> doInBackground() throws Exception {
            Thread.sleep(800);
            List<Engineer> all = CSVUtil.readCSV(); List<Engineer> res = new ArrayList<>();
            for (Engineer e : all) {
                if (!i.isEmpty() && !e.getId().contains(i)) continue;
                if (!n.isEmpty() && !e.getName().contains(n)) continue;
                int age = e.getAge();
                if (!mi.isEmpty() && age < Integer.parseInt(mi)) continue;
                if (!ma.isEmpty() && age > Integer.parseInt(ma)) continue;
                if (!ex.equals("選択なし")) { if (e.getExperienceYears() < Integer.parseInt(ex.replaceAll("[^0-9]", ""))) continue; }
                if (!ls.isEmpty()) { boolean hit = false; for (String l : ls) if (e.getLanguage().toLowerCase().contains(l.toLowerCase())) { hit = true; break; } if (!hit) continue; }
                res.add(e);
            }
            return res;
        }
        @Override protected void done() { try { if (!isCancelled()) refreshTable(get()); } catch (Exception e) {} finally { setUILocked(false); } }
    }

    private void executeImport() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            List<Engineer> current = CSVUtil.readCSV();
            List<Engineer> imported = CSVUtil.readExternalCSV(fc.getSelectedFile());
            current.addAll(imported);
            CSVUtil.writeCSV(current);
            refreshTable(current);
            JOptionPane.showMessageDialog(this, imported.size() + "件を一括インポートしました。");
        }
    }

    private void executeExportSelected() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) { JOptionPane.showMessageDialog(this, "書き出す行を一覧で選択してください。"); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("selected_engineers.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            List<Engineer> list = new ArrayList<>();
            for (int r : rows) {
                String id = (String) tableModel.getValueAt(table.convertRowIndexToModel(r), 0);
                CSVUtil.readCSV().stream().filter(e -> e.getId().equals(id)).findFirst().ifPresent(list::add);
            }
            CSVUtil.exportCSV(list, fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, rows.length + "件をファイルに出力しました。");
        }
    }

    private void executeTemplate() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("template.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            CSVUtil.writeTemplate(fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "テンプレートCSVを出力しました。");
        }
    }

    private void showDetailForm(Engineer en) {
        isEditMode = (en != null);
        txtEngId.setEnabled(!isEditMode); 
        txtEngId.setText(isEditMode ? en.getId() : "");
        txtEngName.setText(isEditMode ? en.getName() : "");
        panelBirthDate.setDateString(isEditMode ? en.getBirthDate() : "1990/01/01");
        panelJoinDate.setDateString(isEditMode ? en.getJoinDate() : "2024/01/01");
        
        // 追加項目の反映
        txtCareer.setText(isEditMode ? en.getCareer() : "");
        txtTraining.setText(isEditMode ? en.getTraining() : "");
        txtNote.setText(isEditMode ? en.getNote() : "");
        
        String el = (isEditMode && en.getLanguage() != null) ? en.getLanguage().toLowerCase() : "";
        for (JCheckBox c : detailChkLanguages) c.setSelected(el.contains(c.getText().toLowerCase()));
        
        if (isEditMode) {
            comboScore.setSelectedItem(String.valueOf(en.getTechnicalScore()));
            comboAttitude.setSelectedItem(String.valueOf(en.getAttitude()));
            comboCommunication.setSelectedItem(String.valueOf(en.getCommunication()));
            comboLeadership.setSelectedItem(String.valueOf(en.getLeadership()));
        } else {
            comboScore.setSelectedIndex(4);
            comboAttitude.setSelectedIndex(4);
            comboCommunication.setSelectedIndex(4);
            comboLeadership.setSelectedIndex(4);
        }
        lblCurrentUpdate.setText(isEditMode ? en.getUpdatedAt() : "-");
        
        cardLayout.show(mainPanel, "DETAIL");
    }

    private void executeSave() {
        String id = txtEngId.getText().trim(); String nm = txtEngName.getText().trim();
        if (id.isEmpty() || nm.isEmpty() || !id.matches("\\d{8}")) { JOptionPane.showMessageDialog(this, "ID(半角数字8桁)と氏名を入力してください。"); return; }
        List<String> sl = new ArrayList<>();
        for (JCheckBox c : detailChkLanguages) if (c.isSelected()) sl.add(c.getText());
        
        List<Engineer> list = CSVUtil.readCSV();
        
        // 新しいコンストラクタに合わせて引数を追加
        Engineer newE = new Engineer(
            id, nm, 
            panelBirthDate.getDateString(), 
            panelJoinDate.getDateString(), 
            txtCareer.getText().trim(), 
            txtTraining.getText().trim(),
            Double.parseDouble((String)comboScore.getSelectedItem()), 
            Double.parseDouble((String)comboAttitude.getSelectedItem()), 
            Double.parseDouble((String)comboCommunication.getSelectedItem()), 
            Double.parseDouble((String)comboLeadership.getSelectedItem()), 
            String.join("/", sl), 
            txtNote.getText().trim(),
            LocalDate.now().toString()
        );
        
        if (!isEditMode) {
            if (list.stream().anyMatch(e -> e.getId().equals(id))) { JOptionPane.showMessageDialog(this, "この社員IDは既に登録されています。"); return; }
            list.add(newE);
        } else { for (int j = 0; j < list.size(); j++) if (list.get(j).getId().equals(id)) { list.set(j, newE); break; } }
        
        CSVUtil.writeCSV(list); refreshTable(list); cardLayout.show(mainPanel, "LIST");
    }

    private void executeDelete() {
        int[] rs = table.getSelectedRows();
        if (rs.length > 0 && JOptionPane.showConfirmDialog(this, "選択したエンジニアを削除しますか？") == JOptionPane.YES_OPTION) {
            List<Engineer> list = CSVUtil.readCSV();
            for (int r : rs) { String id = (String) tableModel.getValueAt(table.convertRowIndexToModel(r), 0); list.removeIf(e -> e.getId().equals(id)); }
            CSVUtil.writeCSV(list); refreshTable(list);
        }
    }

    private void refreshTable(List<Engineer> list) {
        tableModel.setRowCount(0);
        for (Engineer e : list) tableModel.addRow(new Object[]{e.getId(), e.getName(), e.getAge() + "歳", e.getExperienceYears() + "年", e.getLanguage(), e.getTechnicalScore(), e.getUpdatedAt()});
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new MainApp().setVisible(true)); }
}
