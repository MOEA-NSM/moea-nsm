package jmetal.metaheuristics.moead.tspMain;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.problems.TSPM;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 *
 * @author Deyvid
 */
public class MOEAD_mainMTSPBuscaLocal {

    public static Logger logger_;      // Logger object
    public static FileHandler fileHandler_; // FileHandler object

    public static void main(String[] args) throws JMException, SecurityException, IOException, ClassNotFoundException {
        Problem problem;         // The problem to solve
        Algorithm algorithm;         // The algorithm to use
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator

        QualityIndicator indicators; // Object to get quality indicators

        HashMap parameters; // Operator parameters

        // Logger object and file to store log messages
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler("MOEADTSP.log");
        logger_.addHandler(fileHandler_);

        problem = new TSPM("Permutation", "E://TSPBiobjetivo//euclidA100.tsp", "E://TSPBiobjetivo//euclidB100.tsp");
        indicators = new QualityIndicator(problem, "E:\\FrontParetoHipervolume\\besteuclidAB100\\besteuclidAB100.pf");
        algorithm = new MOEAD(problem);
    //algorithm = new MOEAD_DRA(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", 100);
        algorithm.setInputParameter("maxEvaluations", 300000);

        // Directory with the files containing the weight vectors used in 
        // Q. Zhang,  W. Liu,  and H Li, The Performance of a New Version of MOEA/D 
        // on CEC09 Unconstrained MOP Test Instances Working Report CES-491, School 
        // of CS & EE, University of Essex, 02/2009.
        // http://dces.essex.ac.uk/staff/qzhang/MOEAcompetition/CEC09final/code/ZhangMOEADcode/moead0305.rar
//        algorithm.setInputParameter("dataDirectory", "/Users/antelverde/Softw/pruebas/data/MOEAD_parameters/Weight");
        algorithm.setInputParameter("dataDirectory", "E:\\jmetal\\moead\\weight");

        algorithm.setInputParameter("finalSize", 300); // used by MOEAD_DRA

        algorithm.setInputParameter("T", 20);//quantidade de vizinhos de cada solucao  - Vizinhanca
        algorithm.setInputParameter("delta", 0.9);//probabilidade de solucoes pais serem selecionadas de solucoes vizinhas
        algorithm.setInputParameter("nr", 2);//maximo numero de solucoes substituidas por cada solucao filha

        // Crossover operator 
        parameters = new HashMap();
//        parameters.put("CR", 1.0);
//        parameters.put("F", 0.5);
//        crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover", parameters);
        parameters.put("probability", 0.95);
        crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);

        // Mutation operator
//        parameters = new HashMap();
//        parameters.put("probability", 1.0 / problem.getNumberOfVariables());
//        parameters.put("distributionIndex", 20.0);
//        mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);
        parameters = new HashMap();
        parameters.put("probability", 0.2);
        mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);

        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);

        // Execute the Algorithm
        long initTime = System.currentTimeMillis();
        SolutionSet population = algorithm.execute();
        long estimatedTime = System.currentTimeMillis() - initTime;

        // Result messages 
        logger_.info("Total execution time: " + estimatedTime + "ms");
        logger_.info("Objectives values have been writen to file FUN");
        population.printObjectivesToFileCSV("FUN_MOEAD_TSP");
        logger_.info("Variables values have been writen to file VAR");
        population.printVariablesToFile("VAR");

        if (indicators != null) {
            logger_.info("Quality indicators");
            logger_.info("Hypervolume: " + indicators.getHypervolume(population));
            logger_.info("EPSILON    : " + indicators.getEpsilon(population));
            logger_.info("GD         : " + indicators.getGD(population));
            logger_.info("IGD        : " + indicators.getIGD(population));
            logger_.info("Spread     : " + indicators.getSpread(population));
        } // if          
    } //main
} // MOEAD_main

