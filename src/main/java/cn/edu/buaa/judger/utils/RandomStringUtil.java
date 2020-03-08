package cn.edu.buaa.judger.utils;

public class RandomStringUtil {
    /**
     * 字符串生成模式枚举类.
     */
    public static enum Mode { ALPHA, ALPHANUMERIC, NUMERIC }

    /**
     * 生成随机字符串.
     * @param length - 字符串长度
     * @param mode - 字符串生成模式
     * @return 随机字符串
     */
    public static String getRandomString(int length, Mode mode) {
        StringBuffer buffer = new StringBuffer();
        String characters = "";
        switch(mode) {
            case ALPHA:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;
            case ALPHANUMERIC:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                break;
            case NUMERIC:
                characters = "1234567890";
                break;
        }
        int charactersLength = characters.length();
        for ( int i = 0; i < length; ++ i ) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();
    }
}
