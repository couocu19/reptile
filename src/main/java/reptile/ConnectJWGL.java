package reptile;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.cookie.Cookie;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConnectJWGL {

    private final String url = "http://www.zfjw.xupt.edu.cn";
    private Map<String,String> cookies = new HashMap<>();
    private String modulus;
    private String exponent;
    private String csrftoken;
    private Connection connection;
    private Connection.Response response;
    private Document document;
    private String stuNum;
    private String password;

    public ConnectJWGL(String stuNum,String password){
        this.stuNum = stuNum;
        this.password = password;
    }

    public void init() throws Exception{
        getCsrftoken();
        getRSApublickey();
    }
    ///jwglxt/xtgl/login_slogin.html?language=zh_CN&_t=1579964735360
    // 获取csrftoken和Cookies
    private void getCsrftoken(){
        try{
            connection = Jsoup.connect(url+ "/jwglxt/xtgl/login_slogin.html?language=zh_CN&_t="+new Date().getTime());
            connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
            response = connection.execute();
            cookies = response.cookies();
            for(Object o:cookies.keySet()){
                System.out.println(cookies.get(o));

            }
            //保存csrftoken
            document = Jsoup.parse(response.body());
            csrftoken = document.getElementById("csrftoken").val();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    ///jwglxt/cjcx/cjcx_cxDgXscj.html?gnmkdm=N305005&layout=default&su=04182098
    // 获取公钥并加密密码
    public void getRSApublickey() throws Exception{
        connection = Jsoup.connect(url+ "/jwglxt/xtgl/login_getPublicKey.html?" +
                "time="+ new Date().getTime());
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        response = connection.cookies(cookies).ignoreContentType(true).execute();
        JSONObject jsonObject = JSON.parseObject(response.body());
        modulus = jsonObject.getString("modulus");
        exponent = jsonObject.getString("exponent");
        password = RSAEncoder.RSAEncrypt(password, B64.b64tohex(modulus), B64.b64tohex(exponent));
        password = B64.hex2b64(password);
    }

    //登录
    public boolean beginLogin() throws Exception{

        connection = Jsoup.connect(url+ "/jwglxt/xtgl/login_slogin.html");
        connection.header("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");

        connection.data("csrftoken",csrftoken);
        connection.data("yhm",stuNum);
        connection.data("mm",password);
        connection.data("mm",password);
        connection.cookies(cookies).ignoreContentType(true)
                .method(Connection.Method.POST).execute();

        response = connection.execute();
        //System.out.println(response.body());
        document = Jsoup.parse(response.body());
        if(document.getElementById("tips") == null){
            System.out.println("欢迎登陆");
            return true;
        }else{
            System.out.println(document.getElementById("tips").text());
            return false;
        }
    }

    // 查询学生信息
    public void getStudentInformaction() throws Exception {
        connection = Jsoup.connect(url+ "/jwglxt/xsxxxggl/xsxxwh_cxCkDgxsxx.html?gnmkdm=N100801&su="+ stuNum);
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        response = connection.cookies(cookies).ignoreContentType(true).execute();
        JSONObject jsonObject = JSON.parseObject(response.body());
        System.out.println("--- 基本信息 ---");
        System.out.println("学号：" + jsonObject.getString("xh_id"));
        System.out.println("性别：" + jsonObject.getString("xbm"));
        System.out.println("民族：" + jsonObject.getString("mzm"));
        System.out.println("学院：" + jsonObject.getString("jg_id"));
        System.out.println("班级：" + jsonObject.getString("bh_id"));
        System.out.println("专业：" + jsonObject.getString("zszyh_id"));
        System.out.println("状态：" + jsonObject.getString("xjztdm"));
        System.out.println("入学年份：" + jsonObject.getString("njdm_id"));
        System.out.println("证件号码：" + jsonObject.getString("zjhm"));
        System.out.println("政治面貌：" + jsonObject.getString("zzmmm"));
    }

    // 获取课表信息
    public void getStudentTimetable(int year , int term) throws Exception {
        connection = Jsoup.connect(url+ "/jwglxt/kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151");
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        connection.data("xnm",String.valueOf(year));
        connection.data("xqm",String.valueOf(term * term * 3));
        response = connection.cookies(cookies).method(Connection.Method.POST).ignoreContentType(true).execute();
        JSONObject jsonObject = JSON.parseObject(response.body());
        if(jsonObject.get("kbList") == null){
            System.out.println("暂时没有安排课程");
            return;
        }
        JSONArray timeTable = JSON.parseArray(jsonObject.getString("kbList"));
        System.out.println(String.valueOf(year) + " -- " + String.valueOf(year + 1) + "学年 " + "第" + term + "学期");
        for (Iterator iterator = timeTable.iterator(); iterator.hasNext();) {
            JSONObject lesson = (JSONObject) iterator.next();
            System.out.println(lesson.getString("xqjmc") + " " +
                    lesson.getString("jc") + " " +
                    lesson.getString("kcmc") + " " +
                    lesson.getString("xm") + " " +
                    lesson.getString("xqmc") + " " +
                    lesson.getString("cdmc") + " " +
                    lesson.getString("zcd"));
        }
    }

    //jwglxt/cjcx/cjcx_cxDgXscj.html?gnmkdm=N305005&layout=default&su=04182098
    // 获取成绩信息
    public void getStudentGrade(int year , int term) throws Exception {
        Connection.Response response1 = null;
        Map<String,String> cookies1 = new HashMap<>();
        Map<String,String> datas = new HashMap<>();
        datas.put("xnm",String.valueOf(year));
        datas.put("xqm",String.valueOf(term * term * 3));
        datas.put("_search","false");
        datas.put("nd",String.valueOf(new Date().getTime()));
        datas.put("queryModel.showCount","15");
        datas.put("queryModel.currentPage","1");
        datas.put("queryModel.sortName","");
        datas.put("queryModel.sortOrder","asc");
        datas.put("time","1");
        //http://www.zfjw.xupt.edu.cn/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005
//        connection = Jsoup.connect(url+"/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005"+stuNum);
//        connection = Jsoup.connect(url+ "/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005");
//        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
//        response = connection.cookies(cookies).method(Connection.Method.POST)
//                .data(datas).ignoreContentType(true).execute();
        //connection = Jsoup.connect(url+ "/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005&layout=default&su=04182098");
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                  .timeout(10000).ignoreContentType(true).execute();


//        response1 = connection.cookies(cookies).method(Connection.Method.POST)
//                .data(datas).ignoreContentType(true).execute();


        response1 = Jsoup.connect(url+"/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005")
               // .header("Accept", "*/*")
                //.header("Accept-Encoding", "gzip, deflate")
                //.header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                //.header("Content-Type", "application/json;charset=UTF-8")
                .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
                .data(datas).timeout(10000).ignoreContentType(true).execute();

        cookies1 = response1.cookies();
        String cookie = null;
        for(Object c:cookies1.keySet()){
            cookie = cookies1.get(c);
        }
        System.out.println(cookie);
        response1.header("Cookie","JSESSIONID="+cookie);
        //  response1 = connection.method(Connection.Method.POST).execute();

        System.out.println(response1.body());
        System.out.println(1);
//        JSONObject jsonObject = JSON.parseObject(response.body());
//        JSONArray gradeTable = JSON.parseArray(jsonObject.getString("items"));
//        for (Iterator iterator = gradeTable.iterator(); iterator.hasNext();) {
//            JSONObject lesson = (JSONObject) iterator.next();
//            System.out.println(lesson.getString("kcmc") + " " +
//                    lesson.getString("jsxm") + " " +
//                    lesson.getString("bfzcj") + " " +
//                    lesson.getString("jd"));
//        }
    }

    public void logout() throws Exception {
        connection = Jsoup.connect(url+ "/jwglxt/logout");
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        response = connection.cookies(cookies).ignoreContentType(true).execute();
    }

}