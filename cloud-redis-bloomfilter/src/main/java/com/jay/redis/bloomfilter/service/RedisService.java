package com.jay.redis.bloomfilter.service;

import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    private final static int numHashFunctions;

    private final static int bitSize;

    private final static Funnel funnel;

    //bimap的maxsize
    private final static int expect_size = 10000000;

    //最大容错率
    private final static double max_Error_rate = 0.01;

    private final static String bloom_filter = "testBloomFilter";

    static {
        //expectedInsertions集合大小，fpp最大错误率
        bitSize = optimalNumOfBits(10000000, 0.01);
        numHashFunctions = optimalNumOfHashFunctions(10000000, bitSize);
        funnel = (Funnel<String>) (from,
                                   into) -> into.putString(from, Charsets.UTF_8).putString(from, Charsets.UTF_8);
    }


//    public BloomFilterHelper(Funnel<T> funnel, int expectedInsertions, double fpp) {
//        Preconditions.checkArgument(funnel != null, "funnel不能为空");
//        this.funnel = funnel;
//        bitSize = optimalNumOfBits(expectedInsertions, fpp);
//        numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, bitSize);
//    }

    /**
     * 计算出当前key通过hash函数后散落在数组的哪些位置上
     *
     * @param key
     * @return
     */
    long[] murmurHashOffset(Object key) {
        long[] offset = new long[numHashFunctions];
        Funnel funnel = (Funnel<String>) (from,
                                          into) -> into.putString(from, Charsets.UTF_8).putString(from, Charsets.UTF_8);

        long hash64 = Hashing.murmur3_128().hashObject(key, funnel).asLong();
        long hash1 = hash64;
        long hash2 = (hash64 >>> 32);
        for (int i = 1; i <= numHashFunctions; i++) {
            long nextHash = hash1 + i * hash2;
            if (nextHash < 0) {
                nextHash = ~nextHash;
            }
            offset[i - 1] = nextHash % bitSize;
        }

        return offset;
    }

    /**
     * 计算bit数组的长度
     */
    private static int optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 计算hash方法执行次数
     */
    private static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }


    public void multiSetBit(boolean value, long... offsets) {
        redisTemplate.executePipelined((RedisCallback) connection -> {

            for (long offset : offsets) {
                connection.setBit(bloom_filter.getBytes(), offset, value);
            }
            return null;
        });

    }

    public List multiGetBit(String name, long... offsets) {

        List results = redisTemplate.executePipelined((RedisCallback) connection -> {

            for (long offset : offsets) {
                connection.getBit(name.getBytes(), offset);
            }
            return null;
        });


        if (CollectionUtils.isEmpty(results)) {
            return null;
        }

        List list = new ArrayList<>();

        results.forEach(obj -> {
            list.add((Boolean) obj);
        });
        return list;
    }

    public boolean exsistsKey(String key) {
        List results = multiGetBit(bloom_filter,murmurHashOffset(key));

        if(CollectionUtils.isEmpty(results)){
            return false;
        }

        for(Object obj:results){
            if((Boolean)obj == false){
                return false;
            }
        }
        return true;
    }

    public void putKey(String key) {
        long[] keyIndex = murmurHashOffset(key);
        multiSetBit(true, keyIndex);
    }
}
