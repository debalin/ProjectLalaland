package com.lalaland.environment;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.lalaland.utility.*;

public class GraphSearch {

  private int visited[], numOfNodes;
  private double pathCost;
  private LinkedList<Integer> solutionPath;
  @SuppressWarnings("unused")
	private int expandedNodes;
  private Map<Integer, List<Utility.Neighbour>> adjacencyList;
  private Utility utility;
  
  public enum SearchType {
    ASTAR, DJIKSTRAS
  }
  
  public double getPathCost() {
    return pathCost;
  }

  public LinkedList<Integer> getSolutionPath() {
    return solutionPath;
  }

  public GraphSearch(Environment environment, int numOfNodes) {
    expandedNodes = 0;
    this.adjacencyList = environment.getAdjacencyList();
    this.utility = environment.getUtility();
    this.numOfNodes = numOfNodes;
    visited = new int[numOfNodes];
  }
  
  public void reset() {
    visited = new int[numOfNodes];
  }
  
  class PathComparator implements Comparator<SolutionPath> {
    public int compare(SolutionPath s1, SolutionPath s2) {  
      if (s1.totalCost < s2.totalCost) 
        return -1;
      else if (s1.totalCost > s2.totalCost)
        return 1;
      else 
        return 0;
    }
  }

  class SolutionPath {
    int lastNode;
    double pathCost;
    double totalCost;
    LinkedList<Integer> path;
    int depthLevel;

    SolutionPath(int lastNode, double pathCost, List<Integer> path, int depthLevel) {
      this.lastNode = lastNode;
      this.pathCost = pathCost;
      this.path = new LinkedList<>();
      this.path.addAll(path);
      this.depthLevel = depthLevel;
    }
    SolutionPath(SolutionPath otherPath) {
      this.lastNode = otherPath.lastNode;
      this.pathCost = otherPath.pathCost;
      this.path = new LinkedList<>();
      this.path.addAll(otherPath.path);
      this.depthLevel = otherPath.depthLevel + 2;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final SolutionPath otherPath = (SolutionPath)obj;
      if (lastNode == otherPath.lastNode) {
        return true;
      }
      else {
        return false;
      }
    }
  }
  
  public boolean search(int origin, int destination, SearchType searchType) {
    boolean searchStatus;
    
    switch (searchType) {
    case ASTAR:
      searchStatus = aStarSearch(origin, destination);
      break;
    case DJIKSTRAS:
      searchStatus = djikstrasSearch(origin, destination);
      break;
    default:
      searchStatus = aStarSearch(origin, destination);
      break;
    }
    
    return searchStatus;
  }

  public boolean djikstrasSearch(int origin, int destination) {
    int originIndex;
    PriorityQueue<SolutionPath> djikstraQueue = new PriorityQueue<>(10, new PathComparator());
    LinkedList<Integer> path = new LinkedList<>();
    path.add(origin);
    SolutionPath headPath = new SolutionPath(origin, 0, path, 0);
    headPath.totalCost = headPath.pathCost;
    
    djikstraQueue.offer(headPath);

    while ((headPath = djikstraQueue.poll()) != null) {
      originIndex = headPath.lastNode;
      visited[originIndex] = 1;
      if (headPath.lastNode == destination) {
        expandedNodes++;
        solutionPath = headPath.path;
        pathCost = headPath.pathCost;
        return true;
      }
      else {
        expandedNodes++;
      } 
      List<Utility.Neighbour> neighbours = adjacencyList.get(headPath.lastNode);
      if (neighbours != null) {
        for (Utility.Neighbour neighbour : neighbours) {
          int neighbourIndex = neighbour.node;
          double neighbourWeight = neighbour.weight;
          if (visited[neighbourIndex] != 1) {
            SolutionPath tempPath = new SolutionPath(headPath);
            tempPath.path.add(neighbourIndex);
            tempPath.pathCost += neighbourWeight;
            tempPath.totalCost = tempPath.pathCost;
            tempPath.lastNode = neighbourIndex;
            tempPath.depthLevel += 0;
            if (djikstraQueue.contains(tempPath)) {
              for (SolutionPath eachPath : djikstraQueue) {
                if (eachPath.equals(tempPath)) {
                  if (tempPath.totalCost < eachPath.totalCost) {
                    djikstraQueue.remove(eachPath);
                    djikstraQueue.offer(tempPath);
                  }
                  break;
                }
              }
            }
            else {
              djikstraQueue.offer(tempPath);
            }
          }
        }
      }
    }
    return false;
  }
  
  public boolean aStarSearch(int origin, int destination) {
    int originIndex;
    PriorityQueue<SolutionPath> aStarQueue = new PriorityQueue<>(10, new PathComparator());
    LinkedList<Integer> path = new LinkedList<>();
    path.add(origin);
    SolutionPath headPath = new SolutionPath(origin, 0, path, 0);
    headPath.totalCost = headPath.pathCost;
    
    aStarQueue.offer(headPath);

    while ((headPath = aStarQueue.poll()) != null) {
      originIndex = headPath.lastNode;
      visited[originIndex] = 1;
      if (headPath.lastNode == destination) {
        expandedNodes++;
        solutionPath = headPath.path;
        pathCost = headPath.pathCost;
        return true;
      }
      else {
        expandedNodes++;
      } 
      List<Utility.Neighbour> neighbours = adjacencyList.get(headPath.lastNode);
      if (neighbours != null) {
        for (Utility.Neighbour neighbour : neighbours) {
          int neighbourIndex = neighbour.node;
          double neighbourWeight = neighbour.weight;
          if (visited[neighbourIndex] != 1) {
            SolutionPath tempPath = new SolutionPath(headPath);
            tempPath.path.add(neighbourIndex);
            tempPath.pathCost += neighbourWeight;
            //tempPath.totalCost = tempPath.pathCost + utility.calculateHeuristic(neighbourIndex, destination);
            tempPath.totalCost = tempPath.pathCost + utility.calculateHeuristicManhattan(neighbourIndex, destination);
            //tempPath.totalCost = tempPath.pathCost + utility.calculateHeuristicManhattanRandom(neighbourIndex, destination);
            tempPath.lastNode = neighbourIndex;
            tempPath.depthLevel += 0;
            if (aStarQueue.contains(tempPath)) {
              for (SolutionPath eachPath : aStarQueue) {
                if (eachPath.equals(tempPath)) {
                  if (tempPath.totalCost < eachPath.totalCost) {
                    aStarQueue.remove(eachPath);
                    aStarQueue.offer(tempPath);
                  }
                  break;
                }
              }
            }
            else {
              aStarQueue.offer(tempPath);
            }
          }
        }
      }
    }
    return false;
  }

}