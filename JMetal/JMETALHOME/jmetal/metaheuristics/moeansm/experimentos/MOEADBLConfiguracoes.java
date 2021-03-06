package jmetal.metaheuristics.moeansm.experimentos;

import java.util.HashMap;
import jmetal.core.Algorithm;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.moeansm.util.UtilAP;
import jmetal.metaheuristics.moead.MOEADBuscaLocal;
import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

/**
 *
 * @author Deyvid
 */
public class MOEADBLConfiguracoes extends Settings {

    public int maxEvaluations_;

    public MOEADBLConfiguracoes(String problem, int maxGeracoes) {
        super(problem);
        this.maxEvaluations_ = maxGeracoes;
        try {
            Object[] problemParams = {"Permutation"};
            problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
        } catch (JMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Crossover crossover;
        Mutation mutation;
        Mutation buscaLocal = null;
        boolean aplicarBuscaLocal = true;

        algorithm = new MOEADBuscaLocal(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", 100);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);

        algorithm.setInputParameter("dataDirectory", UtilAP.obterDiretorioProjeto() + UtilAP.barraDupla() + "moead" + UtilAP.barraDupla() + "weight");
        algorithm.setInputParameter("finalSize", 300); // used by MOEAD_DRA

        algorithm.setInputParameter("T", 20);
        algorithm.setInputParameter("delta", 0.9);
        algorithm.setInputParameter("nr", 2);

        HashMap parameters = new HashMap(); // Operator parameters
        parameters.put("probability", 0.80);
        crossover = CrossoverFactory.getCrossoverOperator("OXCrossover", parameters);
        /*Mutacao*/
        parameters = new HashMap();
        parameters.put("probability", 0.1);
        mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);

        if (aplicarBuscaLocal) {
            /*Busca Local*/
            parameters = new HashMap();
            parameters.put("probability", 0.1);
            parameters.put("problem", problem_);
            buscaLocal = MutationFactory.getMutationOperator("DoisOpt", parameters);
        }

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        if (aplicarBuscaLocal) {
            algorithm.addOperator("buscaLocal", buscaLocal);
        }
        return algorithm;
    } // configure
} // NSGAII_Settings
