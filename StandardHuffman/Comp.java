import java.util.Comparator;

public class Comp implements Comparator<node>{
	public int compare(node x,node y){
		if(x.prob>y.prob)return 1;
		return -1;
	}
}
