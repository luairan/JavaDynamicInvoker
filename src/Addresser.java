import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Addresser {

    public static void main(String[] args) {



        Pattern EMPID_PATTERN = Pattern.compile("^.*\\((.+)\\)$");

        try {
            Matcher matcher = EMPID_PATTERN.matcher("残墨(卢爱然)");
            String creatorEmpId = null;
            if(matcher.find()){
                creatorEmpId = matcher.group(1);
                System.out.println(creatorEmpId);
            }
        } catch (Exception e) {
        }













        //int a =10;
        //
        //
        //setAto200(a);
        //
        //System.out.println(a+"should equal 200");
        //
        //
        //
        //
        //Long ab = new Long("1235");
        //
        //
        //Long ac = new Long("1235");
        //
        //System.out.println(ab.equals(ac));
    }

    public static void setAto200(int ato200) {

    }
}
