/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.limes.core.ml.algorithm.wombat;


import java.util.List;
import java.util.Map;

import org.aksw.limes.core.datastrutures.Tree;
import org.aksw.limes.core.io.cache.Cache;
import org.aksw.limes.core.io.mapping.Mapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.aksw.limes.core.io.mapping.MappingFactory.MappingType;
import org.aksw.limes.core.io.mapping.MemoryMapping;
import org.aksw.limes.core.measures.mapper.MappingOperations;
import org.aksw.limes.core.ml.algorithm.euclid.LinearSelfConfigurator;
import org.apache.log4j.Logger;



/**
 *
 * @author ngonga
 */
public class SimplWombat extends Wombat {
	static Logger logger = Logger.getLogger(SimplWombat.class.getName());

	public double penaltyWeight = 0.5d;

	public static double MAX_FITNESS_THRESHOLD = 1;
	public static long MAX_TREE_SIZE = 2000;
	public static int MAX_ITER_NR = 10;
	public static long CHILDREN_PENALTY_WEIGHT = 1;
	public static long COMPLEXITY_PENALTY_WEIGHT = 1;
	public boolean STRICT = true;
	public double MIN_THRESHOLD = 0.4;
	public double learningRate = 0.9;
	public boolean verbose = false;
	Map<String, Double> sourcePropertiesCoverageMap; //coverage map for latter computations
	Map<String, Double> targetPropertiesCoverageMap;//coverage map for latter computations
	RefinementNode bestSolution = null; 

	double minCoverage;
	Cache source, target;

	Mapping reference;

	public Tree<RefinementNode> root = null;
	public List<ExtendedClassifier> classifiers = null;
	protected int iterationNr = 0;


	/**
	 * ** TODO 
	 * 1- Get relevant source and target resources from sample 
	 * 2- Sample source and target caches 
	 * 3- Run algorithm on samples of source and target 
	 * 4- Get mapping function 
	 * 5- Execute on the whole
	 */
	/**
	 * Constructor
	 *
	 * @param source
	 * @param target
	 * @param examples
	 * @param minCoverage
	 */
	public SimplWombat(Cache source, Cache target, Mapping examples, double minCoverage) {
		sourcePropertiesCoverageMap = LinearSelfConfigurator.getPropertyStats(source, minCoverage);
		targetPropertiesCoverageMap = LinearSelfConfigurator.getPropertyStats(target, minCoverage);
		this.minCoverage = minCoverage;
		this.source = source;
		this.target = target;
		reference = examples;
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.lgg.LGG#getMapping()
	 */
	public Mapping getMapping() {
		if(bestSolution == null){
			bestSolution =  getBestSolution();
		}
		return bestSolution.map;
	}

	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.lgg.LGG#getMetricExpression()
	 */
	@Override
	public String getMetricExpression() {
		if(bestSolution == null){
			bestSolution =  getBestSolution();
		}
		return bestSolution.metricExpression;
	}

	/**
	 * @return RefinementNode containing the best over all solution
	 * @author sherif
	 */
	public RefinementNode getBestSolution(){
		classifiers = getAllInitialClassifiers();
		createRefinementTreeRoot();
		Tree<RefinementNode> mostPromisingNode = getMostPromisingNode(root, penaltyWeight);
		logger.info("Most promising node: " + mostPromisingNode.getValue());
		iterationNr ++;
		while((mostPromisingNode.getValue().fMeasure) < MAX_FITNESS_THRESHOLD	 
				//				&& root.size() <= MAX_TREE_SIZE
				&& iterationNr <= MAX_ITER_NR)
		{
			iterationNr++;
			mostPromisingNode = expandNode(mostPromisingNode);
			mostPromisingNode = getMostPromisingNode(root, penaltyWeight);
			if(mostPromisingNode.getValue().fMeasure == -Double.MAX_VALUE){
				break; // no better solution can be found
			}
			logger.info("Most promising node: " + mostPromisingNode.getValue());
		}
		RefinementNode bestSolution = getMostPromisingNode(root, 0).getValue();
		logger.info("Overall Best Solution: " + bestSolution);
		return bestSolution;
	}

	/**
	 * initiate the refinement tree as a root node  with set of 
	 * children nodes containing all initial classifiers
	 * @return
	 * @author sherif
	 */
	protected void createRefinementTreeRoot(){
		RefinementNode initialNode = new RefinementNode(-Double.MAX_VALUE, MappingFactory.createMapping(MappingType.DEFAULT), "");
		root = new Tree<RefinementNode>(null,initialNode, null);
		for(ExtendedClassifier c : classifiers){
			RefinementNode n = new RefinementNode(c.fMeasure, c.mapping, c.getMetricExpression());
			root.addChild(new Tree<RefinementNode>(root,n, null));
		}
		if(verbose){
			root.print();
		}
	}

	/**
	 * Expand an input refinement node by applying 
	 * all available operators to the input refinement 
	 * node's mapping with all other classifiers' mappings
	 *   
	 * @param node Refinement node to be expanded
	 * @return The input tree node after expansion
	 * @author sherif
	 */
	private Tree<RefinementNode> expandNode(Tree<RefinementNode> node) {
		Mapping map = new MemoryMapping();
		for(ExtendedClassifier c : classifiers ){
			for(Operator op : Operator.values()){
				if(node.getValue().metricExpression != c.getMetricExpression()){ // do not create the same metricExpression again 
					if(op.equals(Operator.AND)){
						map = MappingOperations.intersection(node.getValue().map, c.mapping);
					}else if(op.equals(Operator.OR)){
						map = MappingOperations.union(node.getValue().map, c.mapping);
					}else if(op.equals(Operator.MINUS)){
						map = MappingOperations.difference(node.getValue().map, c.mapping);
					}
					String metricExpr = op + "(" + node.getValue().metricExpression + "," + c.getMetricExpression() +")|0";
					RefinementNode child = new RefinementNode(map, metricExpr,reference);
					node.addChild(new Tree<RefinementNode>(child));
				}
			}
		}
		if(verbose){
			root.print();
		}
		return node;
	}


	/**
	 * Get the most promising node as the node with the best F-score
	 *  
	 * @param r The whole refinement tree
	 * @param penaltyWeight 
	 * @return most promising node from the input tree r
	 * @author sherif
	 */
	protected Tree<RefinementNode> getMostPromisingNode(Tree<RefinementNode> r, double penaltyWeight){
		// trivial case
		if(r.getchildren() == null || r.getchildren().size() == 0){
			return r;
		}
		// get mostPromesyChild of children
		Tree<RefinementNode> mostPromesyChild = new Tree<RefinementNode>(new RefinementNode());
		for(Tree<RefinementNode> child : r.getchildren()){
			if(child.getValue().fMeasure >= 0){
				Tree<RefinementNode> promesyChild = getMostPromisingNode(child, penaltyWeight);
				double newFitness;
				newFitness = promesyChild.getValue().fMeasure - penaltyWeight * computePenality(promesyChild);
				if( newFitness > mostPromesyChild.getValue().fMeasure  ){
					mostPromesyChild = promesyChild;
				}
			}
		}
		// return the argmax{root, mostPromesyChild}
		if(penaltyWeight > 0){
			return mostPromesyChild;
		}else if(r.getValue().fMeasure >= mostPromesyChild.getValue().fMeasure){
			return r;
		}else{
			return mostPromesyChild;
		}
	}


	/**
	 * @return 
	 * @author sherif
	 */
	private double computePenality(Tree<RefinementNode> promesyChild) {
		long childrenCount = promesyChild.size() - 1;
		double childrenPenalty = (CHILDREN_PENALTY_WEIGHT * childrenCount) / root.size();
		long level = promesyChild.level();
		double complextyPenalty = (COMPLEXITY_PENALTY_WEIGHT * level) / root.depth();
		return  childrenPenalty + complextyPenalty;
	}


}