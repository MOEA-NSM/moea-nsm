package jmetal.operators.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import org.logisticPlanning.tsp.solving.algorithms.localSearch.permutation.chainedMNSLocalOpt.ChainedMNSLocalNOptMEU;

/**
 *
 * @author Deyvid Esta Classe faz a chamada do metodo ChainedLK da lib TSP Suite
 */
public class ChainedLK extends Mutation {

    /**
     * Valid solution types to apply this operator
     */
    private Double probability = null;
    private static final List VALID_TYPES = Arrays.asList(PermutationSolutionType.class);
    private Problem problema = null;

    /**
     * Constructor
     */
    public ChainedLK(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("probability") != null) {
            probability = (Double) parameters.get("probability");
        }
        if (parameters.get("problem") != null) {
            problema = (Problem) parameters.get("problem");
        }
    }

    public Solution chainedLK(Solution solution) throws JMException {

        Solution solutionClone = new Solution(solution);
        if (solution.getType().getClass() == PermutationSolutionType.class) {

            /*Obtem o tamanho do cromossomo*/
            int[] new_tour = ((Permutation) solutionClone.getDecisionVariables()[0]).vector_;

//            calcularFitnessPorObjetivo(solution, solution.getObjetivoBuscaLocal());
//            System.out.println(solution.toString());

            int[] solucaoMelhorada = ChainedMNSLocalNOptMEU.aplicarChainedMNSLocalNOpt(solution.getInstancia(), new_tour);
            try {
                solutionClone = new Solution(problema, solucaoMelhorada);

//                calcularFitnessPorObjetivo(solutionClone, solution.getObjetivoBuscaLocal());
//                System.out.println(solutionClone.toString());

            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

        } else {
            Configuration.logger_.severe("ChainedLK.doMutation: invalid type. "
                    + "" + solution.getDecisionVariables()[0].getVariableType());

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doMutation()");
        }
        return solutionClone;
    }

    public double calcularFitnessPorObjetivo(Solution solucao, int objetivo) {
        try {
            problema.evaluate(solucao);
        } catch (JMException ex) {
            ex.printStackTrace();
        }
        return solucao.getFitnessMeuPorObjetivo(objetivo);
    }

    /**
     * Executes the operation
     *
     * @param object An object containing the solution to mutate
     * @return an object containing the mutated solution
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("ChainedLK.execute: the solution "
                    + "is not of the right type. The type should be 'Binary', "
                    + "'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if 
        if (PseudoRandom.randDouble() < this.probability) {
            solution = this.chainedLK(solution);
        }
        return solution;
    } // execute  
}
