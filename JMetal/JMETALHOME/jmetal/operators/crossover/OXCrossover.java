/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.operators.crossover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 *
 * @author Deyvid
 */
public class OXCrossover extends Crossover {

    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(PermutationSolutionType.class);

    private Double crossoverProbability_ = null;

    /**
     * Constructor Creates a new intance of the two point crossover operator
     */
    public OXCrossover(HashMap<String, Object> parameters) {
        super(parameters);

        if (parameters.get("probability") != null) {
            crossoverProbability_ = (Double) parameters.get("probability");
        }
    } // TwoPointsCrossover

    public Solution[] doCrossover(double probability, Solution parent1, Solution parent2) throws JMException {

        Solution[] offspring = new Solution[2];

        offspring[0] = new Solution(parent1);
        offspring[1] = new Solution(parent2);

        if (parent1.getType().getClass() == PermutationSolutionType.class) {
            if (PseudoRandom.randDouble() < probability) {
                int a;
                int b;
                int permutationLength;
                int offspring1Vector[];
                int offspring2Vector[];

                permutationLength = ((Permutation) parent1.getDecisionVariables()[0]).getLength();
                offspring1Vector = ((Permutation) offspring[0].getDecisionVariables()[0]).vector_;
                offspring2Vector = ((Permutation) offspring[1].getDecisionVariables()[0]).vector_;

                // STEP 1: Get two cutting points
                a = PseudoRandom.randInt(0, permutationLength - 3);
                b = PseudoRandom.randInt(0, permutationLength - 1);

                while (b == a) {
                    b = PseudoRandom.randInt(0, permutationLength - 1);
                }

                if (a > b) {
                    int swap;
                    swap = a;
                    a = b;
                    b = swap;
                }

                /*pai1 offspring1Vector*/
                /*pai2  offspring2Vector;*/
                int[] y1 = new int[permutationLength - (b - a)];
                int[] y2 = new int[permutationLength - (b - a)];

                int j1 = 0, j2 = 0, k = 0;

                /*Preenche os valores de j1 e j2 com os valores que nao estao no intervalor a e b*/
                for (int i = b + 1; i < permutationLength; i++) {

                    boolean pertenceJ1 = false;
                    for (int m = a; m <= b; m++) {
                        if (offspring1Vector[i] == offspring2Vector[m]) {
                            pertenceJ1 = true;
                            break;
                        }
                    }

                    if (!pertenceJ1) {
                        y1[j1] = offspring1Vector[i];
                        j1++;
                    }

                    boolean pertenceJ2 = false;
                    for (int m = a; m <= b; m++) {
                        if (offspring2Vector[i] == offspring1Vector[m]) {
                            pertenceJ2 = true;
                            break;
                        }
                    }
                    if (!pertenceJ2) {
                        y2[j2] = offspring2Vector[i];
                        j2++;
                    }
                    k = k + 1;
                }
                for (int i = 0; i <= b; i++) {

                    boolean pertenceJ1 = false;
                    for (int m = a; m <= b; m++) {
                        if (offspring1Vector[i] == offspring2Vector[m]) {
                            pertenceJ1 = true;
                            break;
                        }
                    }

                    if (!pertenceJ1) {
                        y1[j1] = offspring1Vector[i];
                        j1++;
                    }

                    boolean pertenceJ2 = false;
                    for (int m = a; m <= b; m++) {
                        if (offspring2Vector[i] == offspring1Vector[m]) {
                            pertenceJ2 = true;
                            break;
                        }
                    }
                    if (!pertenceJ2) {
                        y2[j2] = offspring2Vector[i];
                        j2++;
                    }
                    k = k + 1;
                }

                /*Transfere os genes do pai1 para filho2 e vice-versa*/
                int[] aux = new int[1 + b - a];
                int g = 0;
                for (int i = a; i <= b; i++) {
                    aux[g] = offspring1Vector[i];
                    offspring1Vector[i] = offspring2Vector[i];
                    g++;
                }
                int g2 = 0;
                for (int i = a; i <= b; i++) {
                    offspring2Vector[i] = aux[g2];
                    g2++;
                }

                /*preenche a primeira parte ate ao ponto a*/
                int f = 0;
                for (int i = b + 1; i < permutationLength; i++) {
                    offspring1Vector[i] = y1[f];
                    offspring2Vector[i] = y2[f];
                    y1[f] = -1;
                    y2[f] = -1;
                    f++;
                }
                /*preenche a ultima parte ate o final*/
                for (int i = 0; i < a; i++) {
                    offspring1Vector[i] = y1[f];
                    offspring2Vector[i] = y2[f];
                    y1[f] = -1;
                    y2[f] = -1;
                    f++;
                }
            }
        } // if
        else {
            Configuration.logger_.log(Level.SEVERE, "OXCrossover.doCrossover: invalid type{0}", parent1.getDecisionVariables()[0].getVariableType());
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doCrossover()");
        }

        return offspring;
    } // makeCrossover

    /**
     * Executes the operation
     *
     * @param object An object containing an array of two solutions
     * @return An object containing an array with the offSprings
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution[] parents = (Solution[]) object;
        Double crossoverProbability;

        if (!(VALID_TYPES.contains(parents[0].getType().getClass())
                && VALID_TYPES.contains(parents[1].getType().getClass()))) {

            Configuration.logger_.log(Level.SEVERE,"TwoPointsCrossover.execute: the solutions " + "are not of the right type. The type should be ''Permutation'', but {0} and {1} are obtained", new Object[]{parents[0].getType(), parents[1].getType()});
        } // if 

        crossoverProbability = (Double) getParameter("probability");

        if (parents.length < 2) {
            Configuration.logger_.severe("TwoPointsCrossover.execute: operator needs two "
                    + "parents");
            Class cls = java.lang.String.class;
            String name = cls.getName();

            throw new JMException(
                    "Exception in " + name + ".execute()");
        }

        Solution[] offspring = doCrossover(crossoverProbability_,
                parents[0],
                parents[1]);

        return offspring;
    } // execute
}
