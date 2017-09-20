package jmetal.operators.mutation;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import jmetal.core.Problem;
import jmetal.util.PseudoRandom;

/**
 * Esta classe implementa um operador de busca local 2-opt must be Permutation.
 */
public class DoisOpt extends Mutation {

    /**
     * Valid solution types to apply this operator
     */
    private Double probability = null;
    private static final List VALID_TYPES = Arrays.asList(PermutationSolutionType.class);
    private Problem problema = null;

    /**
     * Constructor
     */
    public DoisOpt(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("probability") != null) {
            probability = (Double) parameters.get("probability");
        }
        if (parameters.get("problem") != null) {
            problema = (Problem) parameters.get("problem");
        }
    }

    public Solution doisOpt(Solution solution) throws JMException {

        int permutation[];
        int permutationLength;
        Solution solutionClone = new Solution(solution);
        if (solution.getType().getClass() == PermutationSolutionType.class) {

            /*Obtem o tamanho do cromossomo*/
            permutationLength = ((Permutation) solution.getDecisionVariables()[0]).getLength();
            permutation = ((Permutation) solution.getDecisionVariables()[0]).vector_;

            int[] new_tour = ((Permutation) solutionClone.getDecisionVariables()[0]).vector_;
            // repetir até que nenhuma melhoria é feita 

//            for (int melhoria = 0; melhoria < 1; melhoria++) {
//
//                double best_distance = calcularFitness(solution);
//
//                for (int i = 0; i < permutationLength - 1; i++) {
//                    for (int k = i + 1; k < permutationLength; k++) {
//                        swap(i, k, permutation, new_tour);
//
//                        double new_distance = calcularFitness(solutionClone);
//
//                        if (new_distance < best_distance) {
//                            // Improvement found so reset
////                            improve = 0;
//                            permutation = new_tour.clone();
//                            best_distance = new_distance;
////                            System.out.println("Distancia: " + best_distance);
//                        }
//                    }
//                }
//            }
            /*Obtem o tamanho do cromossomo*/
            // repetir até que nenhuma melhoria é feita 
            double change = 0, minChange = 0, best_distance = calcularFitness(solution);
                int quantidadeTRocasPermitidas =0;
            do {
                minChange = 0;
                int mini = 0;
                int minj = 0;

                
                for (int i = 0; i < permutationLength - 2; i++) {
                    for (int j = i + 2; j < permutationLength - 2; j++) {
                        change = 0;
                        change = (obterDistancia(solution, permutation[i], permutation[j]) + obterDistancia(solution, permutation[i + 1], permutation[j + 1]))
                                - (obterDistancia(solution, permutation[i], permutation[i + 1]) + obterDistancia(solution, permutation[j], permutation[j + 1]));
                        if (minChange > change) {
                            minChange = change;
                            mini = i;
                            minj = j;
                        }
                    }
                }
                swap2(mini, minj, permutation, new_tour);
                double new_distance = calcularFitness(solutionClone);
                if (new_distance < best_distance) {
                    permutation = new_tour.clone();
                    best_distance = new_distance;
//                    System.out.println("Distancia: " + best_distance);
                }
//            } while (minChange < 0);
                quantidadeTRocasPermitidas ++;
            } while (quantidadeTRocasPermitidas > 30);

            // if
        } // if
        else {
            Configuration.logger_.severe("DoisOpt.doMutation: invalid type. "
                    + "" + solution.getDecisionVariables()[0].getVariableType());

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doMutation()");
        }
        return solutionClone;
    }

    public void swap2(int i, int j, int permutation[], int new_tour[]) {

        if (i > j) {
            int aux = i;
            i = j;
            j = aux;
        }

        int size = permutation.length;

        //Obtem os genes do inicio do pai para filho
        // 1. take route[0] to route[i-1] and add them in order to new_route
        for (int c = 0; c <= i; c++) {
            new_tour[c] = permutation[c];
        }

        //Realiza a troca de genes adicionando os genes do pai em ordem reversa para o filho
        // 2. take route[i] to route[k] and add them in reverse order to new_route
        int dec = 0;
        for (int c = i + 1; c <= j; c++) {
            new_tour[c] = permutation[j - dec];
            dec++;
        }

        //Obtem os genes do final do pai para filho
        // 3. take route[k+1] to end and add them in order to new_route
        for (int c = j + 1; c < size; c++) {
            new_tour[c] = permutation[c];
        }
    }

//    public void swap(int i, int k, int permutation[], int new_tour[]) {
//        int size = permutation.length;
//
//        //Obtem os genes do inicio do pai para filho
//        // 1. take route[0] to route[i-1] and add them in order to new_route
//        for (int c = 0; c <= i - 1; c++) {
//            new_tour[c] = permutation[c];
//        }
//
//        //Realiza a troca de genes adicionando os genes do pai em ordem reversa para o filho
//        // 2. take route[i] to route[k] and add them in reverse order to new_route
//        int dec = 0;
//        for (int c = i; c <= k; c++) {
//            new_tour[c] = permutation[k - dec];
//            dec++;
//        }
//
//        //Obtem os genes do final do pai para filho
//        // 3. take route[k+1] to end and add them in order to new_route
//        for (int c = k + 1; c < size; c++) {
//            new_tour[c] = permutation[c];
//        }
//    }
    public double calcularFitness(Solution solucao) {
        if (solucao.getObjetivoBuscaLocal() == null) {
            return calcularFitnessPonderado(solucao);
        } else {
            return calcularFitnessPorObjetivo(solucao, solucao.getObjetivoBuscaLocal());
        }
    }

    public double calcularFitnessPonderado(Solution solucao) {
        try {
            problema.evaluate(solucao);
        } catch (JMException ex) {
            ex.printStackTrace();
        }
        return solucao.getFitnessMeuPonderado();
    }

    public double calcularFitnessPorObjetivo(Solution solucao, int objetivo) {
        try {
            problema.evaluate(solucao);
        } catch (JMException ex) {
            ex.printStackTrace();
        }
        return solucao.getFitnessMeuPorObjetivo(objetivo);
    }

    public double obterDistancia(Solution solucao, int x, int y) {
        if (solucao.getObjetivoBuscaLocal() == null) {
            return obterDistanciaPonderada(x, y);
        } else {
            return obterDistanciaPorObjetivo(solucao.getObjetivoBuscaLocal(), x, y);
        }
    }

    public double obterDistanciaPonderada(int x, int y) {
        return problema.obterDistanciaPonderada(x, y);
    }

    public double obterDistanciaPorObjetivo(int objetivo, int x, int y) {
        return problema.obterDistanciaPorObjetivo(objetivo, x, y);
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
            Configuration.logger_.severe("DoisOpt.execute: the solution "
                    + "is not of the right type. The type should be 'Binary', "
                    + "'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if 
        if (PseudoRandom.randDouble() < this.probability) {
            solution = this.doisOpt(solution);
        }
        return solution;
    } // execute  

}
