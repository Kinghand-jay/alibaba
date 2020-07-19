package redistest;

import com.jay.redis.bloomfilter.RedisBloomFilterMain;
import com.jay.redis.bloomfilter.service.RedisService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisBloomFilterMain.class)
public class BloomFilterTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void test(){
        for (int i = 0; i <10000 ; i++) {
            redisService.putKey("justtest"+i);
        }
    }

    @Test
    public void test2(){
        int correct = 0;
        int fail = 0;

        List<String> test = new ArrayList<>();
        for (int i = 0; i <10000 ; i++) {
           if((i&15 )== 0){
               test.add("justtest"+i);
           }
        }

        for (String str: test) {
            boolean resutl = redisService.exsistsKey(str);
            if(resutl){
                correct++;
            }else {
                fail++;
            }

        }

        System.out.println("正确的数:"+correct);
        System.out.println("错误数统计："+fail);
    }

    @Test
    public void test3(){

        boolean resutl = redisService.exsistsKey("justtest16");
        System.out.println(resutl);
    }

    public static void main(String[] args) {
            int count = 0;

        for (int i = 0; i <10 ; i++) {
            count++;
        }

        System.out.println(count);
    }
}
