public class Engineer {
    private String id;
    private String name;
    private String birthDate;
    private String joinDate; // 日にちなし（YYYY/MM）
    private double technicalScore;
    private String language;
    private String updatedAt;
    private String photoPath;
    
    // ▼ 新規追加項目
    private String engineerHistory; // エンジニア歴
    private String careerHistory;   // 経歴
    private String trainingHistory; // 研修の受講歴
    private double attitudeScore;   // 受講態度
    private double communicationScore; // コミュニケーション能力
    private double leadershipScore; // リーダーシップ
    private String remarks;         // 備考

    public Engineer(String id, String name, String birthDate, String joinDate, double technicalScore, 
                    String language, String updatedAt, String photoPath, String engineerHistory, 
                    String careerHistory, String trainingHistory, double attitudeScore, 
                    double communicationScore, double leadershipScore, String remarks) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.joinDate = joinDate;
        this.technicalScore = technicalScore;
        this.language = language;
        this.updatedAt = updatedAt;
        this.photoPath = photoPath;
        this.engineerHistory = engineerHistory;
        this.careerHistory = careerHistory;
        this.trainingHistory = trainingHistory;
        this.attitudeScore = attitudeScore;
        this.communicationScore = communicationScore;
        this.leadershipScore = leadershipScore;
        this.remarks = remarks;
    }

    // 各種データの取得用メソッド
    public String getId() { return id; }
    public String getName() { return name; }
    public String getBirthDate() { return birthDate; }
    public String getJoinDate() { return joinDate; }
    public double getTechnicalScore() { return technicalScore; }
    public String getLanguage() { return language; }
    public String getUpdatedAt() { return updatedAt; }
    public String getPhotoPath() { return photoPath; }
    public String getEngineerHistory() { return engineerHistory; }
    public String getCareerHistory() { return careerHistory; }
    public String getTrainingHistory() { return trainingHistory; }
    public double getAttitudeScore() { return attitudeScore; }
    public double getCommunicationScore() { return communicationScore; }
    public double getLeadershipScore() { return leadershipScore; }
    public String getRemarks() { return remarks; }

    // 年齢計算
    public int getAge() {
        if (birthDate == null || birthDate.isEmpty()) return 0;
        try {
            String d = birthDate.replace("-", "/");
            if (d.length() == 7) d += "/01";
            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd");
            java.time.LocalDate birth = java.time.LocalDate.parse(d, f);
            return java.time.Period.between(birth, java.time.LocalDate.now()).getYears();
        } catch (Exception e) { return 0; }
    }

    // 検索・ソート用に「エンジニア歴」から数字だけを抽出する安全な処理
    public int getExperienceYearsInt() {
        if (engineerHistory == null) return 0;
        String numStr = engineerHistory.replaceAll("[^0-9]", "");
        try {
            return numStr.isEmpty() ? 0 : Integer.parseInt(numStr);
        } catch(Exception e) { return 0; }
    }

    // CSV保存時の文字列化（カンマや改行によるデータ破損を防止）
    public String toCSVString() {
        String safeCareer = careerHistory != null ? careerHistory.replace(",", "，") : "";
        String safeTraining = trainingHistory != null ? trainingHistory.replace(",", "，") : "";
        String safeRemarks = remarks != null ? remarks.replace(",", "，").replace("\n", " ") : "";

        return id + "," + name + "," + birthDate + "," + joinDate + "," + technicalScore + "," + 
               language + "," + updatedAt + "," + photoPath + "," + engineerHistory + "," + 
               safeCareer + "," + safeTraining + "," + attitudeScore + "," + 
               communicationScore + "," + leadershipScore + "," + safeRemarks;
    }
}
