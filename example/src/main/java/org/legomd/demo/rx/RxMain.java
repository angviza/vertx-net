package org.legomd.demo.rx;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import rx.Observable;
import rx.Single;

import java.util.Map;
import java.util.stream.IntStream;

/**
 * @Author: Quinn
 * @Email angviza@gmail.com
 * @Date: 2021/5/9 16:51
 * @Description:
 */
public class RxMain {
    static final int batch = 5;

    public static void main(String[] args) {
        test1();
    }

    private static int page(int count) {
        if (count <= 0) return 0;
        return (count - 1) / batch + 1;
    }

    private static void test1() {
        Single.just(new Conn()).map(conn -> {
            conn.count("select count")
                    .flatMapObservable(count -> {
                        int page=page(count);
                       return Observable.from(IntStream.range(1,page).mapToObj(i->{return new Page().currPage(i);}).toArray(Page[]::new));
                       // return Observable.just(null);
                    }).map(page->{
                       return conn.query("select page "+page.currPage);
                    })
                    .map(r->{
                        r.map(rr->{System.out.println(rr);return null;}).subscribe();
                        System.out.println();
                        return null;
                    })
                    .subscribe(r->{});
            return null;
        }).map(m -> {

            return null;
        }).subscribe(res -> {

        });
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class Page {
        Map<String, Object> param;
        int count;//总数
        int offset;//
        int currPage;//当前页

        public Page currPage(int page) {
            currPage=page;
            offset = page * batch;
            return this;
        }


    }

    public static class Conn {
        public Single<Object> query(String sql) {
            return Single.just(sql + " return");
        }

        public Single<Integer> count(String sql) {
            return Single.just(39);
        }
    }
}
