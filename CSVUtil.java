import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {
    private static final String FILE_PATH = "engineers.csv";
    // 💡 ヘッダーに新しい項目をすべて追加
    private static final String HEADER = "社員ID,氏名,生年月日,入社年月,技術力,言語,最終更新,写真パス,エンジニア歴,経歴,研修受講歴,受講態度,コミュニケーション能力,リーダーシップ,備考";

    public static synchronized List<Engineer> readCSV() {
        return readExternalCSV(new File(FILE_PATH));
    }

    public static synchronized List<Engineer> readExternalCSV(File file) {
        List<Engineer> list = new ArrayList<>();
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line = br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length >= 7) {
                    double score = 0.0;
                    try { score = Double.parseDouble(data[4]); } catch (Exception e) {}
                    
                    String photoPath = (data.length >= 8 && !data[7].isEmpty()) ? data[7] : "なし";
                    
                    // 新規項目の読み込み（古いデータ用になければ初期値をセット）
                    String engHist = data.length >= 9 ? data[8] : "0年";
                    String career = data.length >= 10 ? data[9] : "";
                    String training = data.length >= 11 ? data[10] : "";
                    
                    double attitude = 3.0;
                    if(data.length >= 12 && !data[11].isEmpty()) try { attitude = Double.parseDouble(data[11]); } catch(Exception e){}
                    
                    double comm = 3.0;
                    if(data.length >= 13 && !data[12].isEmpty()) try { comm = Double.parseDouble(data[12]); } catch(Exception e){}
                    
                    double lead = 3.0;
                    if(data.length >= 14 && !data[13].isEmpty()) try { lead = Double.parseDouble(data[13]); } catch(Exception e){}
                    
                    String remarks = data.length >= 15 ? data[14] : "";

                    list.add(new Engineer(data[0], data[1], data[2], data[3], score, data[5], data[6], photoPath,
                                          engHist, career, training, attitude, comm, lead, remarks));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static synchronized void writeCSV(List<Engineer> list) {
        exportCSV(list, new File(FILE_PATH));
    }

    public static synchronized void exportCSV(List<Engineer> list, File file) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.println(HEADER);
            for (Engineer emp : list) {
                pw.println(emp.toCSVString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void writeTemplate(File file) {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.println(HEADER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}