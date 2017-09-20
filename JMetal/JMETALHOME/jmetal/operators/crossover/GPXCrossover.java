package jmetal.operators.crossover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
public class GPXCrossover extends Crossover {

    /**
     * Esta classe aplica o operador GPX da tese GENERALIZED PARTITION CROSSOVER
     * FOR THE TRAVELING SALESMAN PROBLEM Para o cruzamento é necessario dois
     * pais
     */
    private static final List VALID_TYPES = Arrays.asList(PermutationSolutionType.class);

    private Double crossoverProbability_ = null;

    public GPXCrossover(HashMap<String, Object> parameters) {
        super(parameters);

        if (parameters.get("probability") != null) {
            crossoverProbability_ = (Double) parameters.get("probability");
        }
    }

    /**
     * Perform the crossover operation
     *
     * @param probability Crossover probability
     * @param parent1 The first parent
     * @param parent2 The second parent
     * @return Two offspring solutions
     * @throws JMException
     */
    public Solution[] doCrossover(double probability,
            Solution parent1,
            Solution parent2) throws JMException {

        Solution[] offspring = new Solution[2];

        offspring[0] = new Solution(parent1);
        offspring[1] = new Solution(parent2);

        if (parent1.getType().getClass() == PermutationSolutionType.class) {
            if (PseudoRandom.randDouble() < probability) {
                int permutationLength;
                int parent1Vector[];
                int parent2Vector[];
                int offspring1Vector[];
                int offspring2Vector[];
                int offspringTesteVector[];

                /*Obtem o tamanho do cromossomo*/
                permutationLength = ((Permutation) parent1.getDecisionVariables()[0]).getLength();
                /*Obtem o cromossomo do pai1 e pai2*/
                parent1Vector = ((Permutation) parent1.getDecisionVariables()[0]).vector_;
                parent2Vector = ((Permutation) parent2.getDecisionVariables()[0]).vector_;
                /*Obtem o cromossomo do filho1 e filho2*/
                offspring1Vector = ((Permutation) offspring[0].getDecisionVariables()[0]).vector_;
                offspring2Vector = ((Permutation) offspring[1].getDecisionVariables()[0]).vector_;
                offspringTesteVector = new int[permutationLength];

                // STEP 1: Obtem dois pontos de corte
                int crosspoint1 = PseudoRandom.randInt(0, permutationLength - 1);
                int crosspoint2 = PseudoRandom.randInt(0, permutationLength - 1);

                int indice = 0;
                for (int i = 0; i < parent1Vector.length - 2; i++) {
                    for (int j = 0; j < parent2Vector.length - 2; j++) {

                        /*Compara se o pai1 tem arestas iguais em pai2 i: 64 27 - 92 j 27 - 92 independe do indice*/
                        if ((parent1Vector[i] == parent2Vector[j]) && (parent1Vector[i + 1] == parent2Vector[j + 1])) {
                            System.out.print(" i: " + i + " " + parent1Vector[i] + " - " + parent1Vector[i + 1]);
                            System.out.println(" j " + parent2Vector[j] + " - " + parent2Vector[j + 1]);
                         
                            /*Adiciona as arestas encontradas em offspringTesteVector/*/
                            if(indice == 0){
                                offspringTesteVector[indice] = parent1Vector[i];
                                indice++;
                            }else if (offspringTesteVector[indice - 1] != parent1Vector[i]) {
                                offspringTesteVector[indice] = parent1Vector[i];
                                indice++;
                            }
                            offspringTesteVector[indice] = parent1Vector[i + 1];
                            indice++;

                            break;
                        }
                    }
                }

            }
        } // if
        else {
            Configuration.logger_.severe("GPXCrossover.doCrossover: invalid "
                    + "type"
                    + parent1.getDecisionVariables()[0].getVariableType());
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

            Configuration.logger_.severe("GPXCrossover.execute: the solutions "
                    + "are not of the right type. The type should be 'Permutation', but "
                    + parents[0].getType() + " and "
                    + parents[1].getType() + " are obtained");
        } // if 

        crossoverProbability = (Double) getParameter("probability");

        if (parents.length < 2) {
            Configuration.logger_.severe("GPXCrossover.execute: operator needs two "
                    + "parents");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }

        Solution[] offspring = doCrossover(crossoverProbability_,
                parents[0],
                parents[1]);

        return offspring;
    } // execute

} // GPXCrossover
