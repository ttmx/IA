import undirectedgraph.*;
import searchalgorithm.*;
public class Main {
    public static void main(String[] args) {
        Graph graph = Romenia.defineGraph();
        graph.showLinks();
        graph.showSets();
        Node n;
        String[] province = {"Dobrogea","Banat"};
        n = graph.searchSolution("Arad", "Bucharest",  province, Algorithms.AStarSearch);
        graph.showSolution(n);
    }
}
