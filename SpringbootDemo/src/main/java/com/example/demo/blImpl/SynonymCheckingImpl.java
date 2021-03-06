package com.example.demo.blImpl;

import com.example.demo.bl.SynonymChecking;
import com.example.demo.util.aliyunOSS.PropertyReader;
import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

@Service
public class SynonymCheckingImpl implements SynonymChecking {

    //设置各层级参数
    private final double a = 0.65;
    private final double b = 0.8;
    private final double c = 0.9;
    private final double d = 0.96;
    private final double e = 0.5;
    private final double f = 0.1;

    private final double degrees = 180;

    //存放的是以词为key，以该词的编码为values的List集合，其中一个词可能会有多个编码
    private final Map<String, ArrayList<String>> wordsEncode = new HashMap<String, ArrayList<String>>();
    //存放的是以编码为key，以该编码多对应的词为values的List集合，其中一个编码可能会有多个词
    private final Map<String, ArrayList<String>> encodeWords = new HashMap<String, ArrayList<String>>();

    public SynonymCheckingImpl(){
        readCiLin();
    }

    /**
     * 读取同义词词林并将其注入wordsEncode和encodeWords
     */
    public void readCiLin() {
        InputStream input = SynonymCheckingImpl.class.getClassLoader().getResourceAsStream("cilin.txt");
        Preconditions.checkNotNull(input);
        List<String> contents = null;
        try {
            contents = Preconditions.checkNotNull(IOUtils.readLines(input));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Preconditions.checkNotNull(contents);
        for (String content : contents) {
            content = Preconditions.checkNotNull(content);
            String[] strArr = content.split(" ");
            String[] strs = Preconditions.checkNotNull(strArr);
            String encode = null;
            int length = strs.length;
            if (length > 1) {
                encode = strs[0];//获取编码
            }
            ArrayList<String> encodeWords_values = new ArrayList<>(Arrays.asList(strs).subList(1, length));
            encodeWords.put(encode, encodeWords_values);//以编码为key，其后所有值为value
            for (int i = 1; i < length; i++) {
                String key = strs[i];
                if (wordsEncode.containsKey(strs[i])) {
                    ArrayList<String> values = wordsEncode.get(key);
                    values.add(encode);
                    //重新放置回去
                    wordsEncode.put(key, values);//以某个value为key，其可能的所有编码为value
                } else {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(encode);
                    wordsEncode.put(key, temp);
                }
            }
        }
        System.out.println("SynonymCheckingImpl initialization success!");
    }

    /**
     * 对外暴露的接口，返回两个词的相似度的计算结果
     *
     * @param word1
     * @param word2
     * @return 相似度值
     */
    public double getSimilarity(String word1, String word2) {
        //如果比较词没有出现在同义词词林中，则相似度为0
        if (!wordsEncode.containsKey(word1) || !wordsEncode.containsKey(word2)) {
            return 0;
        }
        //获取第一个词的编码
        ArrayList<String> encode1 = getEncode(word1);
        //获取第二个词的编码
        ArrayList<String> encode2 = getEncode(word2);

        double maxValue = 0;//最终的计算结果值，取所有相似度里面结果最大的那个
        for (String e1 : encode1) {
            for (String e2 : encode2) {
                String commonStr = getCommonStr(e1, e2);
                int length = StringUtils.length(commonStr);
                double k = getK(e1, e2);
                double n = getN(commonStr);
                double res = 0;
                //如果有一个以“@”那么表示自我封闭，肯定不在一棵树上，直接返回f
                if (e1.endsWith("@") || e2.endsWith("@") || 0 == length) {
                    if (f > maxValue) {
                        maxValue = f;
                    }
                    continue;
                }
                if (1 == length) {
                    //说明在第二层上计算
                    res = a * cos(n * PI / degrees) * ((n - k + 1) / n);
                } else if (2 == length) {
                    //说明在第三层上计算
                    res = b * cos(n * PI / degrees) * ((n - k + 1) / n);
                } else if (4 == length) {
                    //说明在第四层上计算
                    res = c * cos(n * PI / degrees) * ((n - k + 1) / n);
                } else if (5 == length) {
                    //说明在第五层上计算
                    res = d * cos(n * PI / degrees) * ((n - k + 1) / n);
                } else {
                    //注意不存在前面七个字符相同，而结尾不同的情况，所以这个分支一定是8个字符都相同，那么只需比较结尾即可
                    if (e1.endsWith("=") && e2.endsWith("=")) {
                        //说明两个完全相同
                        res = 1;
                    } else if (e1.endsWith("#") && e2.endsWith("#")) {
                        //只有结尾不同，说明结尾是“#”
                        res = e;
                    }
                }
                if (res > maxValue) {
                    maxValue = res;
                }
            }
        }
        return maxValue;
    }

    /**
     * 根据word的内容，返回其对应的编码
     *
     * @param word
     * @return
     */
    public ArrayList<String> getEncode(String word) {
        return wordsEncode.get(word);
    }

    /**
     * 获取编码的公共部分字符串
     *
     * @param encode1
     * @param encode2
     * @return
     */
    public String getCommonStr(String encode1, String encode2) {
        int length = StringUtils.length(encode1);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            if (encode1.charAt(i) == encode2.charAt(i)) {
                sb.append(encode1.charAt(i));
            } else {
                break;
            }
        }
        int sbLen = StringUtils.length(sb);
        //注意第三层和第五层均有两个字符，所以长度不可能出现3和6的情况
        if (sbLen == 3 || sbLen == 6) {
            sb.deleteCharAt(sbLen - 1);
        }

        return String.valueOf(sb);
    }

    /**
     * @param encode1 第一个编码
     * @param encode2 第二个编码
     * @return 这两个编码对应的分支间的距离，用k表示
     */
    public int getK(String encode1, String encode2) {
        String temp1 = encode1.substring(0, 1);
        String temp2 = encode2.substring(0, 1);
        if (StringUtils.equalsIgnoreCase(temp1, temp2)) {
            temp1 = encode1.substring(1, 2);
            temp2 = encode2.substring(1, 2);
        } else {
            return Math.abs(temp1.charAt(0) - temp2.charAt(0));
        }
        if (StringUtils.equalsIgnoreCase(temp1, temp2)) {
            temp1 = encode1.substring(2, 4);
            temp2 = encode2.substring(2, 4);
        } else {
            return Math.abs(temp1.charAt(0) - temp2.charAt(0));
        }
        if (StringUtils.equalsIgnoreCase(temp1, temp2)) {
            temp1 = encode1.substring(4, 5);
            temp2 = encode2.substring(4, 5);
        } else {
            return Math.abs(Integer.parseInt(temp1) - Integer.parseInt(temp2));
        }
        if (StringUtils.equalsIgnoreCase(temp1, temp2)) {
            temp1 = encode1.substring(5, 7);
            temp2 = encode2.substring(5, 7);
        } else {
            return Math.abs(temp1.charAt(0) - temp2.charAt(0));
        }
        return Math.abs(Integer.parseInt(temp1) - Integer.parseInt(temp2));
    }

    /**
     * 计算N的值，N表示所在分支层分支数，如：人 Aa01A01= 和 少儿 Ab04B01=，以A开头的子分支只有14个
     *
     * @param encodeHead 输入两个字符串的公共开头
     * @return 经过计算之后得到N的值
     */
    public int getN(String encodeHead) {
        int length = StringUtils.length(encodeHead);
        switch (length) {
            case 1:
                return getCount(encodeHead, 2);
            case 2:
                return getCount(encodeHead, 4);
            case 4:
                return getCount(encodeHead, 5);
            case 5:
                return getCount(encodeHead, 7);
            default:
                return 0;
        }
    }

    public int getCount(String encodeHead, int end) {
        Set<String> res = new HashSet<String>();
        for (String curr : encodeWords.keySet()) {
            if (curr.startsWith(encodeHead)) {
                String temp = curr.substring(0, end);
                if (!res.contains(temp)) {
                    res.add(temp);
                }
            }
        }
        return res.size();
    }
}
