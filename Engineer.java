import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Engineer {
    private String id;
    private String name;
    private String birthDate;
    private String joinDate;
    
    // --- 追加項目 ---
    private String career;          // 経歴
    private String training;        // 研修の受講歴
    private double technicalScore;  // 技術力
    private double attitude;        // 受講態度
    private double communication;   // コミュニケーション能力
    private double leadership;      // リーダーシップ
    private String language;
    private String note;            // 備考
    // ---------------
    
    private String updatedAt;

    public Engineer(String id, String name, String birthDate, String joinDate, 
                    String career, String training, 
                    double technicalScore, double attitude, double communication, double leadership, 
                    String language, String note, String updatedAt) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.joinDate = joinDate;
        this.career = career;
        this.training = training;
        this.technicalScore = technicalScore;
        this.attitude = attitude;
        this.communication = communication;
        this.leadership = leadership;
        this.language = language;
        this.note = note;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBirthDate() { return birthDate; }
    public String getJoinDate() { return joinDate; }
    public String getCareer() { return career; }
    public String getTraining() { return training; }
    public double getTechnicalScore() { return technicalScore; }
    public double getAttitude() { return attitude; }
    public double getCommunication() { return communication; }
    public double getLeadership() { return leadership; }
    public String getLanguage() { return language; }
    public String getNote() { return note; }
    public String getUpdatedAt() { return updatedAt; }

    public int getAge() {
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            return Period.between(LocalDate.parse(this.birthDate, f), LocalDate.now()).getYears();
        } catch (Exception e) { return 0; }
    }

    public int getExperienceYears() {
        try {
            String dateStr = this.joinDate.length() == 7 ? this.joinDate + "/01" : this.joinDate;
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            return Period.between(LocalDate.parse(dateStr, f), LocalDate.now()).getYears();
        } catch (Exception e) { return 0; }
    }

    public String toCSVString() {
        // 入力されたテキストにカンマが含まれるとCSVが壊れるため、全角カンマに置換して出力します
        return String.join(",", 
                id, 
                name, 
                birthDate, 
                joinDate, 
                career.replace(",", "，"), 
                training.replace(",", "，"), 
                String.valueOf(technicalScore), 
                String.valueOf(attitude), 
                String.valueOf(communication), 
                String.valueOf(leadership), 
                language, 
                note.replace(",", "，"), 
                updatedAt);
    }
}
