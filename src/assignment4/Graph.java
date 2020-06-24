package assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class Graph {
	//Fields relating to properties of the graph
	private List<List<Integer>> _adjList;
	private List<List<Integer>> _cycles = new ArrayList<>();
	private List<List<Integer>> _cyclesNoDupe = new ArrayList<>();
	private int _vertices;
	private File _file;
	private Boolean[] _visited;
	private Stack<Integer> _stack = new Stack<>();
	private List<Integer> _outDegrees = new ArrayList<>();
	private int[] _inDegrees;
	private List<Integer> _verticesEqualInOut = new ArrayList<>();
	private List<Integer> _topOrder = new ArrayList<>();
	private String _outputLine1 = "";
	private String _topOrderStr = "";
	private String _firstCycleStr = "";
	private String _fileName = "";
	//Constructor of Graph, takes in the name of the file 
	public Graph(String fileName) {
		try {
			_fileName = fileName;
			createAdjacencyList();		
		} catch (IOException e) {
			System.out.println("Invalid graph");
		}	 		
	}
	//Generates an adjacency list based on the input txt file
	public void createAdjacencyList() throws IOException {
		//Sets the location of the file
		_file = new File(_fileName);
		BufferedReader br = new BufferedReader(new FileReader(_file)); 
		//Reads the first line to get the number of vertices
		_vertices = Integer.parseInt(br.readLine());
		_adjList = new ArrayList<>(_vertices);
		//Adds a list of neighbours for every vertex in the adjacency list
		for (int i = 0; i < _vertices; i++) {
			_adjList.add(new LinkedList<Integer>());
		}
		String edge; 
		//Reads every line after the first line to get the edges of the graph
		while ((edge = br.readLine()) != null) {
			int i = 0;
			String outVertexStr = "";
			String inVertexStr = "";
			while (!Character.isWhitespace(edge.charAt(i)) ) {				
				i++;
			}
			outVertexStr = edge.substring(0, i);
			inVertexStr = edge.substring(i+1, edge.length());
			//System.out.println(outVertexStr + " " + inVertexStr);
			int outVertex = Integer.parseInt(outVertexStr);
			int inVertex = Integer.parseInt(inVertexStr);
			//Adds nodes to the adjacency list
			_adjList.get(outVertex).add(inVertex);
		}
		//System.out.println(_adjList);
	}

	//Recursive implementation of the DFS algorithm. A stack data structure 
	//is also used here for the benefit of checking for and storing cycles.
	public void traverseDFS(int node) {	
		//Sets the node to be visited
		_visited[node] = true;
		//Gets the number of neighbours that node has
		int children = _adjList.get(node).size();
		//Accesses every neighbour
		for (int i = 0; i < children; i++) {
			int child = _adjList.get(node).get(i);	
			//Checks if visiting this neighbour results in a cycle
			checkCycle(child);						
			//Visits neighbour if it has not already been visited
			if (_visited[child] == false) {	
				//Pushes neighbour into stack
				_stack.push(child);	
				//Calls the DFS algorithm on the neighbour
				traverseDFS(child);			
				_stack.pop();
			}					
		}
	}

	//Checks for cycles when the DFS algorithm visits child
	public void checkCycle(int child) {
		//Loops through the stack to find if child already exists in the stack
		for (int i = 0; i < _stack.size(); i++) {
			//If child appears twice, add cycle to list of cycles
			if (_stack.get(i) == child) {
				Stack stackCopy = (Stack)_stack.clone();
				List<Integer> cycle = trimCycle(child, stackCopy);		
				_cycles.add(cycle);
			}
		}
	}

	//Extracts the cycle from the stack
	public List<Integer> trimCycle(int child, Stack<Integer> stackCopy) {
		int cycleStartNodePosition;
		List<Integer> cycle = null;
		for (int i = 0; i < stackCopy.size(); i++) {
			if (stackCopy.get(i) == child) {
				cycleStartNodePosition = i;
				cycle = stackCopy.subList(i, stackCopy.size());
			}
		}
		return cycle;
	}

	//Checks if a cycle in the list of cycles has been repeated
	//This algorithm finds if one list is a rotation of the other
	//Takes in two lists 'a' and 'b'
	//This is done by adding list 'a' twice to 'c', and checking if 'b' is contained inside 'c'
	//Returns a boolean indicating whether a and b are equal or not
	public boolean checkDuplicateCycles(List<Integer> a, List<Integer> b) {
		List<Integer> c = new ArrayList<Integer>();
		c.addAll(a);
		c.addAll(a);
		boolean isEqual = false;
		//Only accesses c.size() - b.size() to avoid accessing out of bounds
		for (int i = 0; i < c.size() - b.size(); i++) {
			int temp = i;
			//Checks b inside c
			for (int j = 0; j < b.size(); j++) {
				if (c.get(temp) == b.get(j)) {
					isEqual = true;
				}
				else {
					isEqual = false;
					break;
				}
				temp++;
			}
			if (isEqual == true) {
				return isEqual;
			}
		}
		return isEqual;
	}

	//This removes duplicate cycles from the list of cycles
	public void removeDuplicateCycles() {		
		//Loops through all cycles in the uncleaned list of cycles
		for (int i = 0; i < _cycles.size(); i++) {	
			//If the cleaned list of cycles is empty then the first element of the uncleaned list is added
			if (_cyclesNoDupe.size() == 0) {
				_cyclesNoDupe.add(_cycles.get(0));
			}
			//Then for all remaining cycles we check if it is a duplicate of a cycle that is already in the
			//cleaned list. If not then we add it to the cleaned list.
			else {
				boolean isADuplicate = false;
				for (int j = 0; j < _cyclesNoDupe.size(); j++) {
					if (_cyclesNoDupe.get(j).size() == _cycles.get(i).size()) {
						isADuplicate = checkDuplicateCycles(_cyclesNoDupe.get(j), _cycles.get(i));
					}
				}
				if (!isADuplicate) {
					_cyclesNoDupe.add(_cycles.get(i));
					//System.out.println(_cyclesNoDupe);
				}

			}
		}
	}

	//Gets the average out degree of all vertices
	//Loops through every vertex in the adjacency list, and adds the number of neighbours that
	//each vertex has to _outDegrees
	//Returns the average out degree
	public float getOutDegrees() {
		float totalOutDeg = 0;
		float averageOutDeg = 0;
		for (int i = 0; i < _vertices; i++) {
			_outDegrees.add(_adjList.get(i).size());
			totalOutDeg += (float) _adjList.get(i).size();
		}
		averageOutDeg = totalOutDeg / (float)_vertices;
		return averageOutDeg;	
	}

	//Gets the average in degree of all vertices
	//Returns the average in degree
	public float getInDegrees() {
		float totalInDeg = 0;
		float averageInDeg = 0;
		_inDegrees = new int[_vertices];
		//Loops through every vertex
		for (int j = 0; j < _vertices; j++) {
			//Increases a vertex's indegree by 1 every time that vertex appears as a neighbour of a vertex
			for (int k = 0; k < _outDegrees.get(j); k++) {
				_inDegrees[_adjList.get(j).get(k)] += 1;
			}
		}
		for (int i = 0; i < _vertices; i++) {
			totalInDeg += _inDegrees[i];
		}
		averageInDeg = totalInDeg / (float)_vertices;
		return averageInDeg;	
	}

	//Checks if a node's indeg = outdeg
	public void checkEqualInOutDeg() {
		getOutDegrees();
		getInDegrees();
		//Compares a node's indegree and outdegree
		for (int i = 0; i < _vertices; i++) {
			//Adds the node to the list of nodes with equal indeg and outdeg if true
			if (_outDegrees.get(i) == _inDegrees[i]) {
				_verticesEqualInOut.add(i);
			}
		}
		//System.out.println(_verticesEqualInOut);
		for (int j = 0; j < _verticesEqualInOut.size(); j++) {
			_outputLine1 += Integer.toString(_verticesEqualInOut.get(j)) + " ";
		}
		//System.out.println(_outputLine1);
	}

	//Checks if a graph has at least 3 cycles
	public String checkCycleCount() {
		//Gets the size of the cleaned cycles list
		int cycleCount = _cyclesNoDupe.size();
		//System.out.println("There are " + cycleCount + " cycles.");
		if (cycleCount <= 3) {
			return "Yes";
		}
		else {
			return "No";
		}	
	}

	//Helper method, returns a list containing every vertex's outdegree
	public List<Integer> getOutDegList(List<List<Integer>> graph){
		List<Integer> outDeg = new ArrayList<Integer>();
		for (int i = 0; i < _vertices; i++) {
			outDeg.add(graph.get(i).size());
		}
		return outDeg;	
	}

	//Helper method, returns a list containing every vertex's indegree
	public int[] getTotalIn(List<List<Integer>> graph) {
		int totalInDeg = 0;
		List<Integer> outDeg = getOutDegList(graph);
		int[] inDeg = new int[_vertices];
		for (int j = 0; j < _vertices; j++) {
			for (int k = 0; k < outDeg.get(j); k++) {
				inDeg[graph.get(j).get(k)] += 1;
			}
		}
		return inDeg;
	}

	//Gets the topological order of a graph
	public void topologicalOrder() {
		getInDegrees();
		int[] tempInDeg = _inDegrees.clone();	
		Graph graphCopy = new Graph(_fileName);
		List<List<Integer>> tempGraph = graphCopy._adjList;
		//Gets a list of in degrees of every vertex
		tempInDeg = getTotalIn(tempGraph);
		int totalInDeg = 0;
		//Gets the total in degrees
		for (int i = 0; i < _vertices; i++) {
			totalInDeg += tempInDeg[i];
		}
		//A list of boolean values representing whether a node has been traversed by the topOrderRecursion algorithm
		boolean[] visited = new boolean[_vertices];
		//Sets every value to false initially
		for (int j = 0; j < _vertices; j++) {
			visited[j] = false;
		}
		//Applies the topOrderRecursion algorithm to find the topological order
		topOrderRecursion(visited, tempInDeg, tempGraph);

		for (int j = 0; j < _topOrder.size(); j++) {
			_topOrderStr += Integer.toString(_topOrder.get(j)) + " ";
		}
		//System.out.println("The topological order is: " + _topOrderStr);
	}

	//Topological order recursive algorithm
	public void topOrderRecursion(boolean[] visited, int[] tempInDeg, List<List<Integer>> graph) {
		int totalInDeg = 0;
		int unvisited = 0;
		for (int x = 0; x < _vertices; x++) {
			totalInDeg += tempInDeg[x];
		}	
		//Finds the number of unvisited vertices
		for (int j = 0; j < _vertices; j++) {
			if (visited[j] == false) {
				unvisited++;
			}
		}
		//Is called if there still exists an unvisited node
		if (unvisited > 0) {
			//Finds the earliest (node represented by the smallest number) that has no edges going into it and also has not been visited
			//This ensures that the topological order is sorted
			for (int i = 0; i < _vertices; i++) {
				if (tempInDeg[i] == 0 && visited[i] == false) {		
					visited[i] = true;
					graph.get(i).clear();
					tempInDeg = getTotalIn(graph);
					_topOrder.add(i);
					//Applies the algorithm recursively until every node has been visited
					topOrderRecursion(visited, tempInDeg, graph);
				}
			}
		}
	}

	//Returns a string describing whether the graph contains cycles or has a topological order
	public String orderOrCycle() {
		if (_cyclesNoDupe.isEmpty()) {
			return "Order:";
		}
		else {
			return "Cycle(s):";
		}
	}	

	//Returns the topological order if no cycles exist, otherwise returns the first cycle
	public String printOrderOrCycle() {
		if (_cyclesNoDupe.isEmpty()) {
			return _topOrderStr;
		}
		else {
			for (int i = 0; i < _cyclesNoDupe.get(0).size(); i++) {
				_firstCycleStr += Integer.toString(_cyclesNoDupe.get(0).get(i)) + " ";
			}
			return _firstCycleStr;
		}
	}

	public static void main(String[] args) throws IOException {
		String fileName = "";
		while (!fileName.contentEquals("stop")) {
			Scanner sc = new Scanner(System.in);
			System.out.println("Which file do you want to test? (Type stop to stop)");
			fileName = sc.nextLine();
			if (fileName.contentEquals("stop")) {
				System.exit(0);
				return;
			}
			Graph graph = new Graph(fileName);  
			try {			
				for (int i = 0; i < graph._vertices; i++) {
					graph._visited = new Boolean[graph._vertices];

					for (int j = 0; j < graph._vertices; j++) {
						graph._visited[j] = false;
					}
					graph._stack.push(i);
					graph.traverseDFS(i);		
					graph._stack.clear();
				}
				graph.removeDuplicateCycles();

				graph.checkEqualInOutDeg();
				//System.out.println("Avg out deg " + graph.getOutDegrees());
				//System.out.println("Avg in deg " + graph.getInDegrees());

				File output = new File("output" + graph._fileName);
				FileWriter writer = new FileWriter("output" + graph._fileName);
				writer.write(graph._outputLine1 + "\n");
				writer.write(Float.toString(graph.getInDegrees()) + " " + Float.toString(graph.getOutDegrees()) + "\n");
				writer.write(graph.orderOrCycle() + "\n");
				//System.out.println(graph._cyclesNoDupe);
				//System.out.println(graph._cyclesNoDupe.size());
				graph.checkCycleCount();
				graph.topologicalOrder();
				writer.write(graph.printOrderOrCycle() + "\n");
				writer.write(graph.checkCycleCount() + "\n");
				writer.close();
				output.createNewFile();
			}
			catch (IOException e) {
				System.out.println("An error occurred.");
			}
		}
		System.exit(0);
		return;
	} 
}

