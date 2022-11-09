import java.util.HashMap;
import java.util.HashSet;

public class MyNode
{
    int indegree[];
    int job_number;
    MyNode parent;
    double makespan;
    double tardiness=0;
    HashSet<Integer> visited;
    HashMap<Integer,Double> map=new HashMap<>();

    public MyNode(int job_number, MyNode parent, HashSet<Integer> visited,double makespan,double tardiness,int []indegree) {
        this.job_number = job_number;
        this.parent = parent;
        this.tardiness = tardiness;
        map.put(job_number,tardiness);
        this.makespan=makespan;
        this.visited=visited;
        visited.add(job_number);
        this.indegree=indegree.clone();
    }

    public String toString(){
        StringBuilder str= new StringBuilder();
        MyNode node=this;
        while (node!=null)
        {

            str.append(node.job_number);
            str.append(" ");
            node=node.parent;
        }



        return str.toString()+" "+tardiness;
    }
}