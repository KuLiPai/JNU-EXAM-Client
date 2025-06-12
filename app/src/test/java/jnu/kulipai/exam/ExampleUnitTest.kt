package jnu.kulipai.exam

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


fun decryptLevel1(encrypted: String): String {
    return encrypted
        .reversed()  // 先反转回来
        .map { (it.code - 5).toChar() }  // 每个字符-5
        .joinToString("")
}


fun decryptLevel2(encrypted: String): String {
    return encrypted
        .mapIndexed { index, char ->
            (char.code - index - 3).toChar()  // 减去索引和3
        }.joinToString("")
}




fun decryptLevel3(encrypted: String): String {
    return encrypted
        .map { char ->
            (char.code xor 42).toChar()  // 再次XOR 42即可还原
        }.joinToString("")
}

fun decryptLevel4(encrypted: String): String {
    return encrypted
        .mapIndexed { index, char ->
            (char.code xor (index + 10)).toChar()  // 相同的XOR操作即可还原
        }.joinToString("")
}

fun decryptLevel5(encrypted: String): String {
    return encrypted
        .split("|")  // 按|分割得到各组
        .flatMap { group ->
            group.split("-")  // 按-分割得到各ASCII值
                .map { ascii ->
                    (ascii.toInt() - 7).toChar()  // 转数字，-7，转字符
                }
        }.joinToString("")
}

fun decryptLevel6(encrypted: String): String {
    return encrypted
        .reversed()  // 先反转
        .split(",")  // 按逗号分割
        .mapIndexed { i, numStr ->
            val num = numStr.toInt()
            val originalCode = num - (i % 3 + 1)  // 减去对应偏移
            originalCode.toChar()
        }.joinToString("")
}


class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val testString1 = "WelcomeToKotlinSyntacticSugarChallenge"
        val testString2 = "flag{Y0u_R_S0_G00d!}"
        val testString3 = "flag{u_4re_Super_JNUer}"
        val testString4 = "flag{K0tl1n_M4ster!}"
        val testString5 = "flag{The_Truth_1s_ab0ut_t0_c0me_0ut!!!!!!!!}"
        val testString6 = "flag{}"

        println("Level 1 (基础): ${encryptLevel1(testString1)}")
        println(decryptLevel1(encryptLevel1(testString1)))

        println("Level 2 (中等): ${encryptLevel2(testString2)}")
        println(decryptLevel2(encryptLevel2(testString2)))



        println("Level 3 (高级): ${encryptLevel3(testString3)}")
        println(decryptLevel3(encryptLevel3(testString3)))


        println("Level 4 (终极): ${encryptLevel4(testString4)}")
        println(decryptLevel4(encryptLevel4(testString4)))

        println("Level 5 (地狱): ${encryptLevel5(testString5)}")
        println(decryptLevel5(encryptLevel5(testString5)))

        println("Level 6 (噩梦): ${encryptLevel6(testString6)}")
        println(decryptLevel6(encryptLevel6(testString6)))



        println("\n=== 挑战任务 ===")
        println("1. 分析每个级别的加密逻辑")
        println("2. 编写对应的解密函数")
        println("3. 验证解密结果是否正确")


    }
}



// CTF Challenge: Kotlin语法糖加密题目 (大学生友好版)
// 任务：理解加密逻辑并写出解密代码

// 题目1: 入门级 - 简单Caesar + 反转
fun encryptLevel1(input: String) = input
    .map { (it.code + 5).toChar() }  // 每个字符+5
    .joinToString("")
    .reversed()  // 反转字符串

// 题目2: 初级 - 带索引的Caesar
fun encryptLevel2(input: String) = input
    .mapIndexed { index, char ->
        (char.code + index + 3).toChar()
    }.joinToString("")

// 题目3: 中级 - 简单XOR
fun encryptLevel3(input: String) = input
    .map { char ->
        (char.code xor 42).toChar()
    }.joinToString("")

// 题目4: 中高级 - XOR + 索引
fun encryptLevel4(input: String) = input
    .mapIndexed { index, char ->
        (char.code xor (index + 10)).toChar()
    }.joinToString("")

// 题目5: 高级 - 分块处理
fun encryptLevel5(input: String) = input
    .chunked(2)  // 每2个字符一组
    .map { chunk ->
        chunk.map { char -> char.code + 7 }  // 每个字符+7
            .joinToString("-")  // 用-连接ASCII值
    }.joinToString("|")  // 用|连接各组

// 题目6: 挑战级 - 综合运用
fun encryptLevel6(input: String): String {
    return input
        .mapIndexed { i, c -> c.code + (i % 3 + 1) }  // 循环加1,2,3
        .map { it.toString() }  // 转为字符串数字
        .joinToString(",")  // 逗号分隔
        .reversed()  // 反转整个字符串
}

