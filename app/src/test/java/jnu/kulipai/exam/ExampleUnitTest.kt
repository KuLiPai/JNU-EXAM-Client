package jnu.kulipai.exam

import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val testString1 = ""
        val testString2 = ""
        val testString3 = ""
        val testString4 = ""
        val testString5 = ""
        val testString6 = ""



        println("Level 1 (基础): ${encryptLevel1(testString1)}")
        //jlsjqqfmHwflzXhnyhfys~XsnqytPtYjrthqj\

        println("Level 2 (中等): ${encryptLevel2(testString2)}")
        //ipfma9j^la?oXBCx6
        //下面上面等效，我害怕复制到别的地方不一样了，下面是字符串的形式
        //"ipfm\u0082a9\u007Fj^la?oXBCx6\u0093"

        println("Level 3 (高级): ${encryptLevel3(testString3)}")
        //LFKMQ_uXOuy_ZOXu`dOXW
        //下面上面等效
        //"LFKMQ_u\u001EXOuy_ZOXu`d\u007FOXW"


        println("Level 4 (终极): ${encryptLevel4(testString4)}")
        //lgmjuD e~"zJ[#kmi=`
        //下面上面等效
        //"lgmjuD e~\"zJ[#km\u007Fi=`"

        println("Level 5 (地狱): ${encryptLevel5(testString5)}")
        //109-115|104-110|130-91|111-108|102-91|121-124|123-111|102-56|122-102|104-105|55-124|123-102|123-55|102-106|55-116|108-102|55-124|123-40|40-40|40-40|40-40|40-132


        println("Level 6 (噩梦): ${encryptLevel6(testString6)}")
        ///721,901,111,011,901,111,011,901,111,011,901,111,011,901,111,011,69,401,111,69,401,201,211,201,301,101,89,911,211,29,79,801,311,99,501,78,521,401,001,011,301

    }
}


fun encryptLevel1(input: String) = input
    .map { (it.code + 5).toChar() }  // 每个字符+5
    .joinToString("")
    .reversed()  // 反转字符串


fun encryptLevel2(input: String) = input
    .mapIndexed { index, char ->
        (char.code + index + 3).toChar()
    }.joinToString("")


fun encryptLevel3(input: String) = input
    .map { char ->
        (char.code xor 42).toChar()
    }.joinToString("")


fun encryptLevel4(input: String) = input
    .mapIndexed { index, char ->
        (char.code xor (index + 10)).toChar()
    }.joinToString("")


fun encryptLevel5(input: String) = input
    .chunked(2).joinToString("|") { chunk ->
        chunk.map { char -> char.code + 7 }  // 每个字符+7
            .joinToString("-")  // 用-连接ASCII值
    }

fun encryptLevel6(input: String): String {
    return input
        .mapIndexed { i, c -> c.code + (i % 3 + 1) }.joinToString(",") { it.toString() }  // 逗号分隔
        .reversed()  // 反转整个字符串
}

