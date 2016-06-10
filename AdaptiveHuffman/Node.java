public class Node {
	int weight;
	String symbol;
	Node left, right, parent;

	Node(int nweight, String nsymbol, Node nleft, Node nright, Node nparent) {
		weight = nweight;
		symbol = nsymbol;
		left = nleft;
		right = nright;
		parent = nparent;
	}
}
