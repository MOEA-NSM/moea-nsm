//  SPEA2_main.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
package jmetal.metaheuristics.spea2;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.Kursawe;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import static jmetal.metaheuristics.nsgaII.NSGAII_mTSP_main.logger_;
import jmetal.problems.TSPM;

/**
 * Class for configuring and running the SPEA2 algorithm
 */
public class SPEA2_mainEstudosTSP {

    public static Logger logger_;      // Logger object
    public static FileHandler fileHandler_; // FileHandler object

    /**
     * @param args Command line arguments. The first (optional) argument
     * specifies the problem to solve.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException Usage: three options -
     * jmetal.metaheuristics.mocell.MOCell_main -
     * jmetal.metaheuristics.mocell.MOCell_main problemName -
     * jmetal.metaheuristics.mocell.MOCell_main problemName ParetoFrontFile
     */
    public static void main(String[] args) throws JMException, IOException, ClassNotFoundException {
        Problem problem;         // The problem to solve
        Algorithm algorithm;         // The algorithm to use
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator
        Operator selection;         // Selection operator

        QualityIndicator indicators; // Object to get quality indicators

        HashMap parameters; // Operator parameters

        // Logger object and file to store log messages
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler("SPEA2.log");
        logger_.addHandler(fileHandler_);

        indicators = null;
        if (args.length == 1) {
            Object[] params = {"Real"};
            problem = (new ProblemFactory()).getProblem(args[0], params);
        } // if
        else if (args.length == 2) {
            Object[] params = {"Real"};
            problem = (new ProblemFactory()).getProblem(args[0], params);
            indicators = new QualityIndicator(problem, args[1]);
        } // if
        else { // Default problem
//            problem = new mTSP("Permutation", "E://kroA150.tsp", "E://kroB150.tsp");
//      problem = new Kursawe("Real", 3); 
            //problem = new Water("Real");
            //problem = new ZDT1("ArrayReal", 1000);
            //problem = new ZDT4("BinaryReal");
            //problem = new WFG1("Real");
            //problem = new DTLZ1("Real");
            //problem = new OKA2("Real") ;
        } // else

        problem = new TSPM("Permutation", "E://TSPBiobjetivo//kroA100.tsp", "E://TSPBiobjetivo//kroB100.tsp");
        indicators = new QualityIndicator(problem, "E:\\FrontParetoHipervolume\\besteuclidAB100\\besteuclidAB100.pf");

        algorithm = new SPEA2(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", 50);
        algorithm.setInputParameter("archiveSize", 50);
        algorithm.setInputParameter("maxEvaluations", 1000000);

        // Mutation and Crossover for Real codification 
        parameters = new HashMap();
        parameters.put("probability", 0.9);
        parameters.put("distributionIndex", 20.0);
        crossover = CrossoverFactory.getCrossoverOperator("PMXCrossover", parameters);
//    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);                   

        parameters = new HashMap();
        parameters.put("probability", 0.2);
        parameters.put("distributionIndex", 20.0);
        mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);
//    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

        // Selection operator 
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        // Execute the algorithm
        long initTime = System.currentTimeMillis();
        SolutionSet population = algorithm.execute();
        long estimatedTime = System.currentTimeMillis() - initTime;

        // Result messages 
        logger_.info("Total execution time: " + estimatedTime + "ms");
        logger_.info("Objectives values have been writen to file FUN");
        population.printObjectivesToFile("FUN");
        logger_.info("Variables values have been writen to file VAR");
        population.printVariablesToFile("VAR");

        if (indicators != null) {
            logger_.info("Quality indicators");
            logger_.info("Hypervolume: " + indicators.getHypervolume(population));
            logger_.info("GD         : " + indicators.getGD(population));
            logger_.info("IGD        : " + indicators.getIGD(population));
            logger_.info("Spread     : " + indicators.getSpread(population));
            logger_.info("Epsilon    : " + indicators.getEpsilon(population));
            int evaluations = ((Integer) algorithm.getOutputParameter("requiredEvaluations")).intValue();
            logger_.info("Speed      : " + evaluations + " requiredEvaluations");
        } // if 
    }//main
} // SPEA2_main.java
