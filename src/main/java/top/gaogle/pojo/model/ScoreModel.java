package top.gaogle.pojo.model;

import java.util.List;

public class ScoreModel {

    private String 省份专业;

    public String 省份;

    private String 科类;

    private String 专业;

    private List<Score1Model> 录取分数线;


    public String get省份() {
        return 省份;
    }

    public void set省份(String 省份) {
        this.省份 = 省份;
    }

    public String get科类() {
        return 科类;
    }

    public void set科类(String 科类) {
        this.科类 = 科类;
    }

    public String get专业() {
        return 专业;
    }

    public void set专业(String 专业) {
        this.专业 = 专业;
    }

    public List<Score1Model> get录取分数线() {
        return 录取分数线;
    }

    public void set录取分数线(List<Score1Model> 录取分数线) {
        this.录取分数线 = 录取分数线;
    }

    public String get省份专业() {
        return 省份专业;
    }

    public void set省份专业(String 省份专业) {
        this.省份专业 = 省份专业;
    }
}
