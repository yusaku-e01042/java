import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {
    private static final String FILE_PATH = "engineers.csv";
    private static final String HEADER = "社員ID,氏名,生年月日,入社年月,経歴,研修の受講歴,技術力,受講態度,コミュニケーション能力,リーダーシップ,言語,備考,最終更新";

    public static synchronized List<Engineer> readCSV() {
        return readExternalCSV(new File(FILE_PATH));
    }

    public static synchronized List<Engineer> readExternalCSV(File file) {
        List<Engineer> list = new ArrayList<>();
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // ヘッダー行を読み飛ばす
            while ((line = br.readLine()) != null) {
                String[] t = line.split(",", -1);
                if (t.length >= 13) {
                    String id = t[0]; 
                    String name = t[1]; 
                    String birth = t[2]; 
                    String join = t[3];
                    String career = t[4];
                    String training = t[5];
                    
                    double tech = 1.0, att = 1.0, comm = 1.0, lead = 1.0;
                    try { tech = Double.parseDouble(t[6]); } catch (Exception e) {}
                    try { att = Double.parseDouble(t[7]); } catch (Exception e) {}
                    try { comm = Double.parseDouble(t[8]); } catch (Exception e) {}
                    try { lead = Double.parseDouble(t[9]); } catch (Exception e) {}
                    
                    String lang = t[10]; 
                    String note = t[11];
                    String update = t[12];
                    
                    list.add(new Engineer(id, name, birth, join, career, training, tech, att, comm, lead, lang, note, update));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static synchronized void writeCSV(List<Engineer> list) {
        exportCSV(list, new File(FILE_PATH));
    }

    public static synchronized void exportCSV(List<Engineer> list, File file) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println(HEADER);
            for (Engineer eng : list) pw.println(eng.toCSVString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static synchronized void writeTemplate(File file) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println(HEADER);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
