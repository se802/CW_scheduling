import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class test {
    static double getSum(double[] arr){
        double sum=0;
        for(double x:arr) sum+=x;
        return sum;
    }

    static boolean completeSolution(MyNode root, int N){
        for(int i=1;i<=N;i++)
            if(!root.visited.contains(i))
                return false;
        return true;
    }

    static boolean dependencyOkay(MyNode root, HashMap<Integer,HashSet<Integer>> dependencies,int id){
        if(dependencies.get(id)==null)
            return true;
        while (root!=null){
            if(dependencies.get(id).contains(root.job_number))
                return false;
            root=root.parent;
        }
        return true;
    }

    static HashMap<Integer,HashSet<Integer>> cloneDependencies( HashMap<Integer,HashSet<Integer>> dependencies){
        HashMap<Integer,HashSet<Integer>> newMap=new HashMap<>();
        for (int x:dependencies.keySet()){
            newMap.put(x,new HashSet<>());
            for (int y:dependencies.get(x))
                newMap.get(x).add(y);
        }
        return newMap;
    }

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        /**
         * Read the due dates from json file.
         * idToName : Maps from node_id to nodes name: e.g 5 -> emboss_8
         * nameToID : Maps from name to node_id: e.g emboss_8 -> 5
         * dueTimeMap : Mapping from node_id to corresponding due date.
         * procTime: Mapping from job type to processing time.
         */
        HashMap<String,Double> dueTimeMap=new HashMap<>();
        HashMap<String,Double> procTime=new HashMap<>();
        HashMap<Integer,String> idToName=new HashMap<>();
        HashMap<String,Integer> nameToID=new HashMap<>();

        //procTime.put("vii",2.0);procTime.put("blur",2.0  );procTime.put("night",24.8989 );
        //procTime.put("onnx",4.0);procTime.put("emboss",7.0 );procTime.put("muse",3.0 );procTime.put("wave",13.1546 );

        procTime.put("wave",12.2572 );
        procTime.put("vii",2.0);procTime.put("blur",2.0 );
        procTime.put("night",24.04 );procTime.put("onnx",4.0 );
        procTime.put("emboss",7.0  );procTime.put("muse",3.0  );

        int id=1;
        JSONObject jsonObject=null;
        try (FileReader reader = new FileReader("input1.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            jsonObject =(JSONObject) ((JSONObject) obj).get("workflow_0");
            JSONObject json_duate_dates= (JSONObject) jsonObject.get("due_dates");

            /**
             * Read the due dates from json file.
             * idToName : Maps from node_id to nodes name: e.g 5 -> emboss_8
             * nameToID : Maps from name to node_id: e.g emboss_8 -> 5
             * dueTimeMap : Mapping from node_id to corresponding due date.
             */
            for (Iterator i = json_duate_dates.keySet().iterator(); i.hasNext ();) {
                String key = (String) i.next ();
                double val = (Long) json_duate_dates.get(key);
                idToName.put(id,key);
                nameToID.put(key,id);
                dueTimeMap.put(key,val);
                id++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int N=idToName.size();

        double [] processing_time=new double[N+1];
        double [] due_dates=new double[N+1];
        for (int i=1;i<=N;i++){
            /**
             * Splits a job name emboss_8 -> emboss to get job type.
             * And using the job type gets the ith job processing time.
             */
            String name=idToName.get(i).split("_")[0];
            processing_time[i]=procTime.get(name);
            due_dates[i]=dueTimeMap.get(idToName.get(i));
        }

        /**
         * Create dependencies graph using the edges given in the input.json.
         * So for an edge a->b reversed_dependencies contains b->a and has indegree[a]=1 .
         */
        HashMap<Integer,HashSet<Integer>> reversed_dependencies=new HashMap<>();
        JSONArray edges= (JSONArray) jsonObject.get("edge_set");
        int []indegree=new int[N+1];
        for (int i=0;i<edges.size();i++)
        {
            String fromStr= (String) ((JSONArray) edges.get(i)).get(0);
            String toStr=(String) ((JSONArray) edges.get(i)).get(1);
            int from=nameToID.get(fromStr);
            int to=nameToID.get(toStr);

            /**
             * For an edge from -> to. creates a reverse edge to -> from.
             */
            reversed_dependencies.putIfAbsent(to,new HashSet<>());
            reversed_dependencies.get(to).add(from);
            indegree[from]++;

        }

        Comparator<MyNode> comparator=(MyNode a,MyNode b)->{
            int x=Double.compare(a.tardiness,b.tardiness);
            return x;
        };

        /**
         * PQ that sorts based on tardiness.
         */
        PriorityQueue<MyNode> pq=new PriorityQueue<>(comparator);


        double makespan=getSum(processing_time);

        /**
         * We start with the job that has no dependencies on the reversed graph.
         * So the "emboss_8" node or node 31 on the spec.
         * We adjust it's tardiness and it's makespan and save it on the node and then add it on Queue to explore it next.
         */
        for (int i=1;i<=N;i++){
            if (indegree[i]==0){
                double jobTardiness=Math.max( makespan-due_dates[i],0);
                MyNode root=new MyNode(i,null,new HashSet<>(),makespan-processing_time[i],jobTardiness,indegree);
                pq.add(root);
            }
        }



        int best_partial_solSize=0;
        double best_partial_Tardiness=Integer.MAX_VALUE;
        MyNode best_partial_SolNode=null;


        int iterations=0;
        while (!pq.isEmpty() && iterations<=30*1000 ){
            iterations++;
            MyNode root=pq.poll();
            /**
             * Just take the solution that has the biggest number of nodes in it.
             * Not sure if it's the best approach but couldn't think of anything else.
             * If 2 partial solutions got the same size we pick the one with the smallest tardiness.
             */
            if (best_partial_solSize<=root.visited.size()){

                if (root.visited.size()==best_partial_solSize && root.tardiness<best_partial_Tardiness){
                    best_partial_Tardiness=root.tardiness;
                    best_partial_solSize=root.visited.size();
                    best_partial_SolNode=root;
                }else {
                    best_partial_solSize=root.visited.size();
                    best_partial_SolNode=root;
                }
            }

            if(completeSolution(root,N) ){
                System.out.println(root);
                /**
                 * Change logic here.
                 */

            }

            /**
             * Update indegree of neighbour nodes.
             */
            for(int i: reversed_dependencies.getOrDefault(root.job_number,new HashSet<>()))
                root.indegree[i]--;

            for(int i=1;i<=N;i++){
                /**
                 * if node i not already contained current partial solution(saved in root)
                 * and has no dependencies then add it to the queue to explore it.
                 */
                if(  !root.visited.contains(i) && root.indegree[i]==0 ){
                    /**
                     * Adjust the totalTardiness of the current node and find it's makespan.
                     */
                    double jobTardiness=Math.max( root.makespan-due_dates[i],0);
                    double totalTardiness= root.tardiness+ jobTardiness;
                    makespan=root.makespan-processing_time[i];

                    /**
                     * Copy the nodes contained in the partial's solution path ( saved in root )
                     * and add node i in the new the path.
                     */
                    HashSet<Integer> temp=new HashSet<>();
                    temp.addAll(root.visited);
                    temp.add(i);
                    MyNode kid=new MyNode(i,root,temp,makespan,totalTardiness,root.indegree);
                    pq.add(kid);
                }
            }
        }
        System.exit(1);
        List<Integer> res=new LinkedList<>();
        Queue<Integer> queue=new LinkedList<>();
        /**
         * Here I add to the queue all the jobs that are not already placed in the partial solution,
         * and then I just apply topological sort.
         */
        for (int i=1;i<=N;i++){
            if (!best_partial_SolNode.visited.contains(i) && best_partial_SolNode.indegree[i]==0)
                if (best_partial_SolNode.indegree[i]==0){
                    queue.add(i);
                }
        }

        /**
         * Topological sort algorithm to find a complete solution from a partial solution.
         */
        while (!queue.isEmpty()){
            int v=queue.poll();
            res.add(v);


            double jobTardiness=Math.max( best_partial_SolNode.makespan-due_dates[v],0);
            best_partial_SolNode.tardiness= best_partial_SolNode.tardiness+ jobTardiness;
            best_partial_SolNode.makespan=best_partial_SolNode.makespan-processing_time[v];

            if (reversed_dependencies.containsKey(v)){
                for (int neigh:reversed_dependencies.get(v)){
                    if (!best_partial_SolNode.visited.contains(neigh))
                        best_partial_SolNode.indegree[neigh]--;
                    if (best_partial_SolNode.indegree[neigh]==0)
                        queue.add(neigh);

                }


            }

        }
        Collections.reverse(res);
        System.out.print(res+" ");
        System.out.println(best_partial_SolNode);
        ArrayList<Integer> finalSol=new ArrayList<>();
        for (int x:res) finalSol.add(x);
        for (String str:best_partial_SolNode.toString().split(" "))
            finalSol.add(Integer.parseInt(str));


        //Creating a JSONObject object
        jsonObject = new JSONObject();

        //Creating a json array
        JSONArray array = new JSONArray();
        for (int x:finalSol)
            array.add(idToName.get(x));

        //Adding array to the json object
        jsonObject.put("workflow_0",array);
        try {
            FileWriter file = new FileWriter("output.json");
            file.write(jsonObject.toJSONString());
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("JSON file created: "+jsonObject);
    }


}




