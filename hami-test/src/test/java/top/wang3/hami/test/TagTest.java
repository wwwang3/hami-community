package top.wang3.hami.test;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

class TagTest {


    @Test
    void testGenAllTagCombination() {
        // 生成所有标签组合
        int min = 1000;
        int max = 1060;
        File file = new File("./article_tag.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            for (int one = min; one <= max; one++) {
                for (int second = one + 1; second <= max; second++) {
                    for (int third = second + 1; third <= max ; third++) {
                        String articleTag = "[" +
                                            one +
                                            ',' +
                                            second +
                                            ',' +
                                            third +
                                            ']' +
                                            '\n';
                        writer.write(articleTag);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
