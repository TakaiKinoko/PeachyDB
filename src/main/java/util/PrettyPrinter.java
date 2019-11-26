package util;

public class PrettyPrinter {

    public static String getBox(String content, int len, String boundary){
        return boundary + " " + content + new String(new char[len - content.length() - boundary.length()]).replace("\0", " ");
    }

    public static String getBorder(int len, String boundary, String filler){
        return boundary + getBorderPart(len, filler);
    }

    private static String getBorderPart(int len, String filler){
        // generate border without boundary
        return new String(new char[len]).replace("\0", filler);
    }

    public static void printWelcome(){
        System.out.println("+-----------------------------------------+");
        System.out.println("|           Welcome to PeachyDB           |");
        System.out.println("|                                         |");
        System.out.println("|           @author: Fang Han             |");
        System.out.println("+-----------------------------------------+\n");
    }
}

