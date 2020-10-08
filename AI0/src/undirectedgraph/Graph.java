package undirectedgraph;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import searchalgorithm.*;
import searchproblem.SearchProblem;
import searchproblem.State;

public class Graph {
	private HashMap<String,Vertex> vertices;
	private HashMap<Integer,Edge> edges;	
	private ArrayList<VertexSet> vSets;
	
	private long expansions;
	private long generated;
	private long repeated;
	private double time;
	
	public Graph() {
		this.vertices = new HashMap<String,Vertex>();
		this.edges = new HashMap<Integer,Edge>();
		this.vSets = new ArrayList<VertexSet>();
		this.expansions = 0;
		this.generated = 0;
		this.repeated = 0;
		this.time = 0;
	}
	
	public void addVertice(String label, double lat, double lng) {
		Vertex v =  new Vertex(label);
		this.vertices.put(label, v);
		v.setCoordinates(lat, lng);
	}
	
	public Vertex getVertice(String label) {
		return this.vertices.get(label);
	}
	
	public void addVerticeSet(String label) {
		VertexSet vSet =  new VertexSet(label);
		this.vSets.add(vSet);
	}
	
	public VertexSet getVerticeSet(String setLabel) {
		for (VertexSet vSet : vSets) {
			if (vSet.getLabel()==setLabel) 
				return vSet;
		}
		return null;
	}
	
	public void addVerticeToSet(String labelSet,String labelVertex) {
		Vertex v = this.vertices.get(labelVertex);
		for (VertexSet vSet : vSets) {
			if (vSet.getLabel()==labelSet) {
				vSet.addVertice(v);
				break;
			}
		}
	}
	
	public boolean addEdge(Vertex one, Vertex two, double weight) {
		if (one.equals(two)) return false;
		Edge e = new Edge(one,two,weight);
		if (edges.containsKey(e.hashcode())) return false;
		if (one.containsNeighbor(e) || two.containsNeighbor(e))  return false;	
		edges.put(e.hashcode(), e);
		one.addNeighbor(e);
		two.addNeighbor(e);
		return true;
	} 
	
	public boolean addEdge(String oneLabel, String twoLabel, double weight) {
		Vertex one = getVertice(oneLabel);
		Vertex two = getVertice(twoLabel);
		return addEdge(one,two,weight);
	}
	
	public boolean addEdge(String oneLabel, String twoLabel) {
		Vertex one = getVertice(oneLabel);
		Vertex two = getVertice(twoLabel);
		return addEdge(one,two,one.straightLineDistance(two));
	}

	public void showLinks() {
		System.out.println("********************* LINKS *********************");
		for (Vertex current: vertices.values()) {		
			System.out.print(current + ": ");
			for (Edge e: current.getNeighbors()) {
				System.out.print(e.getNeighbor(current) + " (" + e.getWeight() + "); ");
			}
			System.out.println();
		}
		System.out.println("*************************************************");
	}
	
	public void showSets() {
		System.out.println("********************* SETS *********************");
		for (VertexSet vSet: vSets) {
			System.out.println(vSet);
		}
		System.out.println("*************************************************");
	}
	
	public Node searchSolution(String initLabel, String goalLabel, @NotNull Algorithms algID) {
		State init = new State(this.getVertice(initLabel));
		State goal = new State(this.getVertice(goalLabel));
		SearchProblem prob = new SearchProblem(init,goal);
		SearchAlgorithm alg = null;
		switch(algID) {
			case BreadthFirstSearch :
				alg = new BreadthFirstSearch(prob);
				break;
			case DepthFirstSearch :
				alg = new DepthFirstSearch(prob);
				break; 
			case UniformCostSearch :
				alg = new UniformCostSearch(prob);
				break; 
			case GreedySearch :
				alg = new GreedySearch(prob);
				break; 
			case AStarSearch :
				alg = new AStarSearch(prob);
				break; 
		   default : 
			  System.out.println("ERROR: algorithm not implemented!");
		}
		Node n = alg.searchSolution();	
		Map<String,Number> m = alg.getMetrics();
		this.expansions += (long)m.get("Node Expansions");
		this.generated += (long)m.get("Nodes Generated");
		this.repeated += (long)m.get("State repetitions");
		this.time += (double)m.get("Runtime (ms)");
		return n;
	}

	public Node searchSolution(String initLabel, String goalLabel, String provinceLabel, @NotNull Algorithms algID){
		VertexSet set = this.getVerticeSet(provinceLabel);
		Vertex i = this.getVertice(initLabel);
		Vertex g = this.getVertice(goalLabel);
		Graph newGraph = new Graph();
		newGraph.addVertice(i.getLabel(),i.getLatitude(), i.getLongitude());
		newGraph.addVertice(g.getLabel(),g.getLatitude(),g.getLongitude());
		Iterator<Vertex> it = set.getVertices().iterator();
		while(it.hasNext()){
			Vertex v = it.next();
			newGraph.addVertice(v.getLabel(),v.getLatitude(),v.getLongitude());
			newGraph.addEdge(i.getLabel(), v.getLabel(),
					searchSolution(i.getLabel(), v.getLabel(), Algorithms.AStarSearch).getPathCost());
			newGraph.addEdge(g.getLabel(), v.getLabel(),
					searchSolution(g.getLabel(), v.getLabel(), Algorithms.AStarSearch).getPathCost());
		}
		Node n = newGraph.searchSolution(initLabel,goalLabel, algID);
		this.expansions += newGraph.expansions;
		this.generated += newGraph.generated;
		this.repeated += newGraph.repeated;
		this.time += newGraph.time;
		return n;
	}

	public Node searchSolution(String initLabel, String goalLabel, String [] provinceLabels, @NotNull Algorithms algID){
		if(provinceLabels.length < 1){
			return searchSolution(initLabel, goalLabel, algID);
		} else if (provinceLabels.length == 1){
			return searchSolution(initLabel, goalLabel, provinceLabels[0], algID);
		} else {
			Vertex i = this.getVertice(initLabel);
			Vertex g = this.getVertice(goalLabel);
			Graph newGraph = new Graph();
			newGraph.addVertice(i.getLabel(),i.getLatitude(), i.getLongitude());
			newGraph.addVertice(g.getLabel(),g.getLatitude(),g.getLongitude());
			VertexSet before = new VertexSet("before");
			before.addVertice(i);
			for(int j = 0; j < provinceLabels.length; j++){
				VertexSet set = this.getVerticeSet(provinceLabels[j]);
				Iterator<Vertex> it = set.getVertices().iterator();
				while(it.hasNext()){
					Vertex v = it.next();
					newGraph.addVertice(v.getLabel(),v.getLatitude(),v.getLongitude());
					Iterator<Vertex> beforeIt = before.getVertices().iterator();
					while(beforeIt.hasNext()){
						Vertex beforeV = beforeIt.next();
						newGraph.addEdge(beforeV.getLabel(), v.getLabel(),
								searchSolution(beforeV.getLabel(), v.getLabel(), Algorithms.AStarSearch).getPathCost());
					}
				}
				before = set;
			}
			//connecting with the goal
			Iterator<Vertex> it = before.getVertices().iterator();
			while(it.hasNext()){
				Vertex v = it.next();
				newGraph.addEdge(v.getLabel(), g.getLabel(),
						searchSolution(v.getLabel(), g.getLabel(), Algorithms.AStarSearch).getPathCost());
			}
			Node n = newGraph.searchSolution(initLabel,goalLabel, algID);
			this.expansions += newGraph.expansions;
			this.generated += newGraph.generated;
			this.repeated += newGraph.repeated;
			this.time += newGraph.time;
			return n;
		}
	}

	public void showSolution(Node n) {
		System.out.println("******************* SOLUTION ********************");
		System.out.println("Node Expansions: " + this.expansions);
		System.out.println("Nodes Generated: " + this.generated);
		System.out.println("State Repetitions: " + this.repeated);
		System.out.printf("Runtime (ms): %6.3f \n",this.time);
		Node ni = null;
		List<Object> solution = n.getPath();
		double dist = 0;
		for (int i = 0; i<solution.size()-1;i++) {
			System.out.printf("| %-9s | %4.0f | ",solution.get(i), dist);
			ni = searchSolution(solution.get(i).toString(), solution.get(i+1).toString(), Algorithms.AStarSearch);
			System.out.print(ni.getPath());	
			System.out.println(" -> " + (int)ni.getPathCost());
			dist += ni.getPathCost();
		}
		System.out.printf("| %-9s | %4.0f | \n",solution.get(solution.size()-1), dist);
		System.out.println("*************************************************");
	}
	
	
}
