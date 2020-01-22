package base;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;



public class CdnRepoPageProcessor implements PageProcessor {

    // 抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
    private static int count =0;

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {


        page.putField("articleURL",page.getUrl().toString());
        page.putField("articleTitle",page.getHtml().xpath("//a[@id=\"cb_post_title_url\"]/text()").get());

        if(page.getResultItems().get("articleTitle")== null){
            //skip this page
            page.setSkip(true);
        }else {
            System.out.println("抓取的内容："+ page.getResultItems().get("articleTitle"));
            count ++;
        }

        //加入满足条件的链接
        page.addTargetRequests(
                //判断链接是否符合http://www.cnblogs.com/任意个数字字母-/p/任意字符.html格式
                page.getHtml().xpath("//div[@id=\"post_list\"]").links().regex("https://www.cnblogs.com/[a-z A-Z 0-9 -]+/p/.+.html").all()
        );

    }

//    public static void main(String[] args) {
//        long startTime, endTime;
//        System.out.println("开始爬取...");
//        startTime = System.currentTimeMillis();
//        Spider.create(new CdnRepoPageProcessor())
//                //从https://www.cnblogs.com开始抓
//                .addUrl("https://www.cnblogs.com/")
//                //开启5个线程抓取
//                .thread(5)
//                //运行爬虫
//                .run();
//        endTime = System.currentTimeMillis();
//        System.out.println("爬取结束，耗时约" + ((endTime - startTime) / 1000) + "秒，抓取了"+count+"条记录");
//    }

//    public static void main(String[] args) {
//        long startTime, endTime;
//        System.out.println("开始爬取...");
//        startTime = System.currentTimeMillis();
//        Spider.create(new CdnRepoPageProcessor())
//                //从https://www.cnblogs.com开始抓
//                .addUrl("https://www.cnblogs.com/")
//                //指定数据存储路径
//                .addPipeline(new JsonFilePipeline("data"))
//                //开启5个线程抓取
//                .thread(5)
//                //运行爬虫
//                .run();
//        endTime = System.currentTimeMillis();
//        System.out.println("爬取结束，耗时约" + ((endTime - startTime) / 1000) + "秒，抓取了"+count+"条记录");
//    }
public static void main(String[] args) throws Exception {
    long startTime, endTime;
    System.out.println("开始爬取...");
    startTime = System.currentTimeMillis();
    Spider cnblogSpider = Spider.create(new CdnRepoPageProcessor())
            //从https://www.cnblogs.com开始抓
            .addUrl("http://www.xiyou.edu.cn/xxfw/cyfw1.htm")
            //指定数据存储路径
            .addPipeline(new JsonFilePipeline("data"))
            //开启3个线程抓取
            .thread(3);
    //注册爬虫到spiderMonitor中,你可以在创建多个爬虫并且注册
    SpiderMonitor.instance().register(cnblogSpider);

    //开始爬虫,以创建的爬虫对象名开启,可以开启多个
    cnblogSpider.start();

    endTime = System.currentTimeMillis();
    System.out.println("爬取结束，耗时约" + ((endTime - startTime) / 1000) + "秒，抓取了"+count+"条记录");
}


}
