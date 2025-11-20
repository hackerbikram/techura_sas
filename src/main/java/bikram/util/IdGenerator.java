package bikram.util;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;

public class IdGenerator {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String randomString(int length){
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i< length;i++){
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
    private static String shortTime(){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        return LocalDateTime.now().format(fmt);
    }
    public static String idGenerate(String prefix,int randomLength){
        String rand = randomString(randomLength);
        String time = shortTime().substring(4);
        return prefix.toUpperCase()+ "-" + rand + "-" +time;
    }

}
