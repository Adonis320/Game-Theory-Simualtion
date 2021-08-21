import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.*;
import java.io.*;

public class Game {

    public double p = 0.4; // proportion of IDN
    public double phi = 0.2; // proportion of Malicious nodes
    public double c_a = 1; // cost of action attack
    public double c_m = 1; // cost of action monitor
    public double g_a = 5; // gain on successfull attack, also loss on failed defense
    public double alpha = 0.9; // detection rate
    public double beta = 0.005; // false alarm rate
    public int nodes_number = 200; //total number of nodes
    public int plays_per_turn = 50;
    public int max_turns = 200; // number of turns played
    public Node [] nodes; // the nodes in the network
    public Node [][] game_history; // history of the game
    public List<String[]> [] history_of_stats;
    public List<String[]> final_stats = new ArrayList<>();

    public Game (boolean custom_param, double[] params){
        
        if(custom_param){
            setup_parameters(params);
        }


        history_of_stats = new List [100];
        
        final_stats.add(new String[] {"Turns","Number of detected malicious nodes", "Number of false detections", "Total monitoring costs"});

       // for(int i = 0; i < 100; i++){
            nodes = initialize_game();
            setup_neighbours();
            System.out.println(get_number_without_idn());

            game_history = new Node [max_turns][nodes_number];

            play_game();

         //   history_of_stats[i] = get_Stats();
    //    }

        /*
        for(int i = 0; i < history_of_stats[0].size(); i++){
            int number_detected = 0;
            int number_false_detected = 0;
            int total_monitoring_costs = 0;
            for(int j = 0; j < history_of_stats.length; j++){
                number_detected += Integer.parseInt(history_of_stats[j].get(i)[1]);
                number_false_detected += Integer.parseInt(history_of_stats[j].get(i)[2]);
                total_monitoring_costs += Integer.parseInt(history_of_stats[j].get(i)[3]);
            }
            final_stats.add(new String [] {String.valueOf(i+1),String.valueOf(number_detected/100),String.valueOf(number_false_detected/100), String.valueOf(total_monitoring_costs/100)});
        }
        
        try {
            givenDataArray_whenConvertToCSV_thenOutputCreated(final_stats);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
       /* try {
            givenDataArray_whenConvertToCSV_thenOutputCreated(final_stats);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

    }

    public int get_number_without_idn(){
        int number = 0;
        for(int i = 0; i < nodes.length; i++){
            if(nodes[i].type.equals("Malicious")){
                int number_idn = 0;
                for(int j = 0; j < nodes[i].neighbours.length; j++){
                    if(nodes[nodes[i].neighbours[j]].type.equals("IDN")){
                        number_idn++;
                    }
                }
                if(number_idn == 0){
                    number ++;
                }
            }
        }
        return number;
    }

    public void play_game(){

        for(int i = 0; i < max_turns; i++){
            //System.out.println("Turn "+i);
            play_turn();
            game_history[i] = new Node [nodes_number];

            for(int j = 0; j < nodes_number; j++){
                game_history[i][j] = new Node(nodes[j]);
            }
            //System.arraycopy(nodes, 0, game_history[i], 0, nodes_number);

        }

        get_Stats();
    }

    public boolean has_valid_neighbours(int id){
        boolean valid = false;

        Node node = nodes[id];

        for(int i = 0; i < node.neighbours.length; i++){
            if(!nodes[node.neighbours[i]].ran_out_of_power & !nodes[node.neighbours[i]].type.equals("Malicious") ){//& !nodes[node.neighbours[i]].detected){
                valid = true;
            }
        }

        return valid;
    }

    public Node [] initialize_game(){
        Node [] nodes = new Node [(int)nodes_number];

        int number_malicious = (int)(nodes_number*phi);

        for(int i = 0; i < number_malicious; i++){
            Node malicious = new Node (i, p ,phi, c_a, c_m, g_a, alpha, beta,"Malicious");
            nodes[i] = malicious;
        }

        int number_normal = (int)nodes_number - number_malicious;
        int number_IDN = (int)(number_normal*p);

        for(int i = number_malicious; i < number_malicious+number_IDN; i++){
            Node idn = new Node (i, p ,phi, c_a, c_m, g_a, alpha, beta,"IDN");
            nodes[i] = idn;
        }

        for(int i = number_malicious+number_IDN; i < nodes_number; i++){
            Node normal = new Node (i, p ,phi, c_a, c_m, g_a, alpha, beta,"Normal");
            nodes[i] = normal;
        }
        /*
        Random rand = new Random();
		
		for (int i = 0; i < nodes.length; i++) {
			int randomIndexToSwap = rand.nextInt(nodes.length);
			Node temp = nodes[randomIndexToSwap];
			nodes[randomIndexToSwap] = nodes[i];
			nodes[i] = temp;
		}*/

       // System.out.println("Number malicious:"+number_malicious);
       // System.out.println("Number IDN:"+number_IDN);

        return nodes;
    }

    public void setup_neighbours(){
        Random rand = new Random();
        for(int i = 0; i < nodes.length; i++){
            int [] neighbours = new int [0];
            for(int j = 0; j < nodes[i].neighbours.length; j++){
                boolean added = false;
                while(!added){
                    int neighbour = rand.nextInt(nodes.length);
                    if(neighbour != nodes[i].id & !id_exists_int(neighbour, neighbours)){
                        neighbours = add_node_ids(neighbours, neighbour);
                        added = true;
                    }
                }
            }
            nodes[i].neighbours = neighbours;
        }
    }

    public void setup_parameters(double [] params){
        //this.p = params[0];
    }

    public void play_turn(){

        Node [] sender_nodes = new Node [0];
        Node [] receiver_nodes = new Node [0];

        Random rand = new Random();

        for(int i = 0; i < plays_per_turn; i++){
            boolean added = false;
            while(!added){
                int int_random= rand.nextInt(nodes_number);
                Node selected_node = nodes[int_random];
                if(!selected_node.detected & !selected_node.ran_out_of_power & has_valid_neighbours(selected_node.id)){
                    sender_nodes = add_node(sender_nodes, selected_node);
                    selected_node.play("Sender");
                    added = true;
                }
            }
            boolean added2 = false;
            while(!added2){
                int int_random = rand.nextInt(sender_nodes[i].neighbours.length);
                Node selected_node = nodes[sender_nodes[i].neighbours[int_random]];
                if(!selected_node.type.equals("Malicious") &  !selected_node.ran_out_of_power){
                    receiver_nodes = add_node(receiver_nodes, selected_node);
                    selected_node.play("Receiver");
                    added2 = true;
                }
            }
            
            if(sender_nodes[i].type.equals("Malicious")){
                if(sender_nodes[i].current_action.equals("Attack")){
                    if(receiver_nodes[i].current_action.equals("Monitor")){
                        double double_random= rand.nextDouble();
                        if(double_random <= alpha){
                            sender_nodes[i].detected = true;
                        }
                    }
                }if(sender_nodes[i].current_action.equals("Not")){
                    if(receiver_nodes[i].current_action.equals("Monitor")){
                        double double_random= rand.nextDouble();
                        if(double_random <= beta){
                            sender_nodes[i].detected = true;
                        }
                    }
                }
            }else{
                if(receiver_nodes[i].current_action.equals("Monitor")){
                    double double_random= rand.nextDouble();
                    if(double_random <= beta){
                        sender_nodes[i].detected = true;
                    }
                }
            }

        }

        /*
        for(int i = 0; i < plays_per_turn; i++){
            boolean added = false;
            while(!added){
                int int_random= rand.nextInt(nodes_number);
                Node selected_node = nodes[int_random];
                if(!id_exists(selected_node.id, sender_nodes) & !selected_node.detected & !selected_node.ran_out_of_power){
                    sender_nodes = add_node(sender_nodes, selected_node);
                    selected_node.play("Sender");
                    added = true;
                }
            }
        }

        for(int i = 0; i < plays_per_turn; i++){
            boolean added = false;
            while(!added){
                int int_random= rand.nextInt(nodes_number);
                Node selected_node = nodes[int_random];
                if(!id_exists(selected_node.id, sender_nodes) & !id_exists(selected_node.id, receiver_nodes) & !selected_node.type.equals("Malicious") &  !selected_node.ran_out_of_power){
                    receiver_nodes = add_node(receiver_nodes, selected_node);
                    selected_node.play("Receiver");
                    added = true;
                }
            }
        }*/
        /*
        for(int i = 0; i < plays_per_turn; i++){
            //String action_sender = sender_nodes[i].current_action;
            //String action_receiver = receiver_nodes[i].current_action;
            
            if(sender_nodes[i].type.equals("Malicious")){
                if(sender_nodes[i].current_action.equals("Attack")){
                    if(receiver_nodes[i].current_action.equals("Monitor")){
                        double double_random= rand.nextDouble();
                        if(double_random <= alpha){
                            sender_nodes[i].detected = true;
                        }
                    }
                }if(sender_nodes[i].current_action.equals("Not")){
                    if(receiver_nodes[i].current_action.equals("Monitor")){
                        double double_random= rand.nextDouble();
                        if(double_random <= beta){
                            sender_nodes[i].detected = true;
                        }
                    }
                }
            }else{
                if(receiver_nodes[i].current_action.equals("Monitor")){
                    double double_random= rand.nextDouble();
                    if(double_random <= beta){
                        sender_nodes[i].detected = true;
                    }
                }
            }
        }*/

    }

    public Node [] add_node (Node [] table, Node node){
        Node [] result = new Node [table.length+1];

        for(int i = 0; i < table.length; i++){
            result[i] = table[i];
        }
        result[table.length] = node;

        return result;
    }

    public int [] add_node_ids (int [] table, int id){
        int [] result = new int [table.length+1];

        for(int i = 0; i < table.length; i++){
            result[i] = table[i];
        }
        result[table.length] = id;

        return result;
    }

    public boolean id_exists(int id, Node [] nodes){
        boolean exists = false;

        for(int i = 0; i < nodes.length; i++){
            if(nodes[i].id == id){
                exists = true;
            }
        }

        return exists;
    }

    public boolean id_exists_int(int id, int [] nodes){
        boolean exists = false;

        for(int i = 0; i < nodes.length; i++){
            if(nodes[i] == id){
                exists = true;
            }
        }

        return exists;
    }

    public List<String[]> get_Stats(){

        /*int number_detected = 0;

        for(int i = 0; i < nodes.length; i++){
            if(nodes[i].detected){
                number_detected++;
            }
        }
        System.out.println("Detected: "+number_detected);
        System.out.println("Percentage of malicious nodes detected: "+(number_detected)/(phi*nodes_number));
        */
        List<String[]> stats = new ArrayList<>();
        stats.add(new String[] {"Turns","Number of detected malicious nodes", "Number of false detections"});
        for(int i = 0; i < max_turns; i++){
            double percentage_nodes_detected;
            //double number_nodes_false_detected = 0;
            //double percentage_energy_left = 1;

            double number_detected = 0;
            double number_false_detected = 0;
            double total_monitoring_costs = 0;
            for(int j = 0; j < nodes_number; j++){
                if(game_history[i][j].detected & game_history[i][j].type.equals("Malicious")){
                    number_detected++;
                }else if(game_history[i][j].detected & !game_history[i][j].type.equals("Malicious")){
                    number_false_detected++;
                }else if(game_history[i][j].type.equals("IDN")){
                    total_monitoring_costs += game_history[i][j].max_energy_level - game_history[i][j].energy_level;
                }
            }

            percentage_nodes_detected = number_detected/(phi*nodes_number);
            
            stats.add(new String[] {String.valueOf(i+1), String.valueOf((int)number_detected), String.valueOf((int)number_false_detected), String.valueOf((int) total_monitoring_costs) }); 
        }

        try {
            givenDataArray_whenConvertToCSV_thenOutputCreated(stats);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return stats;
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
          .map(this::escapeSpecialCharacters)
          .collect(Collectors.joining(";"));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public void givenDataArray_whenConvertToCSV_thenOutputCreated(List<String[]> stats) throws IOException {
        File csvOutputFile = new File("results.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            stats.stream()
              .map(this::convertToCSV)
              .forEach(pw::println);
        }
        //assertTrue(csvOutputFile.exists());
    }
    

}