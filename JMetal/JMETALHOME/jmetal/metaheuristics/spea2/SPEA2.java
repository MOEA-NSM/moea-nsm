//  SPEA2.java
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

import jmetal.core.*;
import jmetal.metaheuristics.abordagem.util.TempoExecucao;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.Spea2Fitness;

/**
 * This class representing the SPEA2 algorithm
 */
public class SPEA2 extends Algorithm {

    /**
     * Defines the number of tournaments for creating the mating pool
     */
    public static final int TOURNAMENTS_ROUNDS = 1;

    /**
     * Constructor. Create a new SPEA2 instance
     *
     * @param problem Problem to solve
     */
    public SPEA2(Problem problem) {
        super(problem);
    } // Spea2

    /**
     * Runs of the Spea2 algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize, archiveSize, maxEvaluations, evaluations;
        Operator crossoverOperator, mutationOperator, selectionOperator;
        SolutionSet solutionSet, archive, offSpringSolutionSet;
        QualityIndicator indicators; // QualityIndicator object

        TempoExecucao tempoExecucao = new TempoExecucao();
        /*ESTAGNACAO*/
        int estagnacao = 0;
        int MAX_ESTAGNACAO = 200;
        double HV = 0.0;
        int requiredEvaluations = 0;

        //Read the params
        populationSize = ((Integer) getInputParameter("populationSize"));
        archiveSize = ((Integer) getInputParameter("archiveSize"));
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations"));
        indicators = (QualityIndicator) getInputParameter("indicators");

        //Read the operators
        crossoverOperator = operators_.get("crossover");
        mutationOperator = operators_.get("mutation");
        selectionOperator = operators_.get("selection");

        //Initialize the variables
        solutionSet = new SolutionSet(populationSize);
        archive = new SolutionSet(archiveSize);
        evaluations = 0;

        //-> Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            solutionSet.add(newSolution);
        }

        while (evaluations < maxEvaluations) {
            SolutionSet union = ((SolutionSet) solutionSet).union(archive);
            Spea2Fitness spea = new Spea2Fitness(union);
            spea.fitnessAssign();
            archive = spea.environmentalSelection(archiveSize);
            // Create a new offspringPopulation
            offSpringSolutionSet = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            while (offSpringSolutionSet.size() < populationSize) {
                int j = 0;
                do {
                    j++;
                    parents[0] = (Solution) selectionOperator.execute(archive);
                } while (j < SPEA2.TOURNAMENTS_ROUNDS); // do-while                    
                int k = 0;
                do {
                    k++;
                    parents[1] = (Solution) selectionOperator.execute(archive);
                } while (k < SPEA2.TOURNAMENTS_ROUNDS); // do-while

                //make the crossover 
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);
                mutationOperator.execute(offSpring[1]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[0]);
                problem_.evaluateConstraints(offSpring[1]);
                offSpringSolutionSet.add(offSpring[0]);
                offSpringSolutionSet.add(offSpring[1]);
                evaluations++;
            } // while
            // End Create a offSpring solutionSet
            solutionSet = offSpringSolutionSet;

            // This piece of code shows how to use the indicator object into the code
            // of NSGA-II. In particular, it finds the number of evaluations required
            // by the algorithm to obtain a Pareto front with a hypervolume higher
            // than the hypervolume of the true Pareto front.
            if ((indicators != null) && (requiredEvaluations == 0)) {
                double HVAtual = indicators.getHypervolume(solutionSet);
                if (HV >= HVAtual && HV != 0.0) {
                    estagnacao++;
                } else {
                    HV = HVAtual;
                    estagnacao = 0;
                }
                if (estagnacao >= MAX_ESTAGNACAO) {
//                    System.out.println("MOEAD Estagnou na geracao: " + avaliacoes_);
                    break;
                }
            } // if

        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("requiredEvaluations", requiredEvaluations);
        Ranking ranking = new Ranking(archive);
//        ranking.getSubfront(0).printFeasibleFUNCSV("FUN_SPEA2");
        ranking.getSubfront(0).setTempoExecucao(tempoExecucao.getTempo());
        ranking.getSubfront(0).setGeracaoParou(evaluations);

        return ranking.getSubfront(0);
    } // execute    
} // SPEA2
