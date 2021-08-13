public class Launcher {

    public static void main (String [] args){
        
        boolean custom = false;
        /*
        if(args[0].equals("custom")){
            custom = true;
            System.out.println("Custom parameters");
        }
        
        double [] params = new double [args.length-1];

        for(int i = 1; i < args.length; i++){
            params[i-1] = Double.parseDouble(args[i]);
        }*/

        Game game = new Game(custom, null);

    }

}